package io.cloudmock.standalone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandaloneIntegrationTest {

    private static final int PORT = 14566;
    private static Process process;

    @BeforeAll
    static void startServer() throws Exception {
        String jarPath = System.getProperty("cloudmock.standalone.jar");
        assertNotNull(jarPath, "cloudmock.standalone.jar system property must be set");

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", jarPath, "--port=" + PORT);
        pb.redirectErrorStream(true);
        process = pb.start();

        // Drain process output in background to prevent pipe blocking
        new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                r.lines().forEach(line -> System.out.println("[standalone] " + line));
            } catch (IOException ignored) {
            }
        }, "standalone-output-drainer").start();

        // Poll until the port accepts connections or 30 s elapse
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                throw new IllegalStateException("Standalone process exited unexpectedly (exit=" + process.exitValue() + ")");
            }
            try (Socket s = new Socket("localhost", PORT)) {
                return; // connected — server is ready
            } catch (IOException e) {
                Thread.sleep(200);
            }
        }
        throw new IllegalStateException("Standalone server did not start on port " + PORT + " within 30 s");
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (process != null) {
            process.destroy();
            process.waitFor(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void sqsListQueuesIsServedByStandaloneProcess() {
        try (SqsClient sqs = SqsClient.builder()
                .endpointOverride(URI.create("http://localhost:" + PORT))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build()) {

            ListQueuesResponse response = sqs.listQueues();
            assertNotNull(response);
            assertTrue(response.sdkHttpResponse().isSuccessful());
        }
    }
}
