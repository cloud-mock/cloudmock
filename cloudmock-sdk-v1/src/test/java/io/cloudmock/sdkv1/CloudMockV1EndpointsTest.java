package io.cloudmock.sdkv1;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import io.cloudmock.core.CloudMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CloudMockV1EndpointsTest {

    static CloudMock cloudMock;
    static AmazonSQS sqsClient;

    @BeforeAll
    static void start() {
        cloudMock = new CloudMock();
        cloudMock.start();

        sqsClient = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(CloudMockV1Endpoints.forPort(cloudMock.port()))
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();
    }

    @AfterAll
    static void stop() {
        if (sqsClient != null) sqsClient.shutdown();
        if (cloudMock != null) cloudMock.stop();
    }

    @Test
    void forPortReturnsEndpointWithCorrectServiceEndpoint() {
        var cfg = CloudMockV1Endpoints.forPort(9999);
        assertEquals("http://localhost:9999", cfg.getServiceEndpoint());
    }

    @Test
    void forPortReturnsEndpointWithDummySigningRegion() {
        var cfg = CloudMockV1Endpoints.forPort(9999);
        assertEquals("us-east-1", cfg.getSigningRegion());
    }

    @Test
    void requestReachesCloudMockWithoutConnectionError() {
        // SDK v1 SQS uses QUERY protocol; CloudMock has no QUERY stubs registered, so WireMock
        // returns 404. AmazonServiceException (an HTTP response) proves the connection succeeded.
        assertThrows(AmazonServiceException.class, () -> sqsClient.listQueues());
    }
}
