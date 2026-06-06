package io.cloudmock.standalone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudmock.core.CloudMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiServerIntegrationTest {

    private CloudMock cloudMock;
    private ApiServer apiServer;
    private int apiPort;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() throws Exception {
        apiPort = freePort();
        cloudMock = new CloudMock();
        cloudMock.start();
        apiServer = new ApiServer(cloudMock, apiPort, List.of());
        apiServer.start();
    }

    @AfterEach
    void tearDown() {
        apiServer.stop();
        cloudMock.stop();
    }

    @Test
    void statusReturnsPortAndUptime() throws Exception {
        assertEquals(200, get("/api/status").statusCode());
        JsonNode body = getJson("/api/status");
        assertEquals(cloudMock.port(), body.get("port").asInt());
        assertEquals(apiPort, body.get("apiPort").asInt());
        assertNotNull(body.get("startedAt"));
        assertNotNull(body.get("uptime"));
    }

    @Test
    void statusListsRoutes() throws Exception {
        JsonNode routes = getJson("/api/status").get("routes");

        assertTrue(routes.isArray());
        assertFalse(routes.isEmpty());

        boolean hasStatus = false;
        for (JsonNode route : routes) {
            if ("/api/status".equals(route.get("path").asText())) {
                hasStatus = true;
                assertEquals("GET", route.get("method").asText());
            }
        }
        assertTrue(hasStatus, "Expected /api/status in routes list");
    }

    @Test
    void statusListsModules() throws Exception {
        JsonNode modules = getJson("/api/status").get("modules");
        assertTrue(modules.isArray());
        for (JsonNode module : modules) {
            assertNotNull(module.get("id"));
            assertTrue(module.get("stubs").isArray());
        }
    }

    @Test
    void resetClearsAllState() throws Exception {
        cloudMock.stateStore().put("sqs/queues/my-queue", "data");
        assertEquals(200, post("/api/reset").statusCode());
        assertFalse(cloudMock.stateStore().list("sqs/").contains("sqs/queues/my-queue"));
    }

    @Test
    void resetClearsStateForOneService() throws Exception {
        cloudMock.stateStore().put("sqs/queues/my-queue", "data");
        cloudMock.stateStore().put("s3/buckets/my-bucket", "data");

        assertEquals(200, post("/api/reset?service=sqs").statusCode());

        assertTrue(cloudMock.stateStore().list("sqs/").isEmpty());
        assertFalse(cloudMock.stateStore().list("s3/").isEmpty());
    }

    @Test
    void historyIsEmptyBeforeAnyRequests() throws Exception {
        assertTrue(getJson("/api/history").get("requests").isArray());
    }

    @Test
    void openApiSpecContainsCoreRoutes() throws Exception {
        JsonNode spec = getJson("/api/openapi.json");
        assertEquals("3.0.3", spec.get("openapi").asText());
        JsonNode paths = spec.get("paths");
        assertNotNull(paths.get("/api/status"));
        assertNotNull(paths.get("/api/reset"));
        assertNotNull(paths.get("/api/history"));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        assertEquals(404, get("/api/no-such-route").statusCode());
    }

    @Test
    void wrongMethodReturns405() throws Exception {
        HttpResponse<String> resp = http.send(
                HttpRequest.newBuilder()
                        .uri(uri("/api/status"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(405, resp.statusCode());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return http.send(
                HttpRequest.newBuilder().uri(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String pathAndQuery) throws IOException, InterruptedException {
        return http.send(
                HttpRequest.newBuilder()
                        .uri(uri(pathAndQuery))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode getJson(String path) throws Exception {
        return mapper.readTree(get(path).body());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + apiPort + path);
    }

    private static int freePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}
