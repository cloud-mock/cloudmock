package io.cloudmock.junit;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Socket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Verifies that {@link CloudMockExtension} starts and stops CloudMock around the test class and
 * that the port is accessible via {@code @RegisterExtension}.
 */
class CloudMockExtensionLifecycleTest {

    @RegisterExtension static CloudMockExtension cloudMock = new CloudMockExtension();

    @Test
    void portIsPositiveAfterStart() {
        assertTrue(cloudMock.port() > 0);
    }

    @Test
    void serverAcceptsConnectionsAfterStart() throws Exception {
        try (Socket socket = new Socket("localhost", cloudMock.port())) {
            assertTrue(socket.isConnected());
        }
    }
}
