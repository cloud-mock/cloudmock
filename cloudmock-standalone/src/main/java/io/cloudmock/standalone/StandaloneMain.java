package io.cloudmock.standalone;

import io.cloudmock.core.CloudMock;
import io.cloudmock.core.spi.CloudMockService;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public final class StandaloneMain {

    private static final int DEFAULT_PORT = 4566;

    public static void main(String[] args) throws InterruptedException {
        int port = resolvePort(args);

        List<String> discovered = discoverServiceIds();
        if (discovered.isEmpty()) {
            System.out.println("[CloudMock] No service modules found on classpath.");
        } else {
            System.out.println("[CloudMock] Discovered modules: " + String.join(", ", discovered));
        }

        try (CloudMock cloudMock = new CloudMock().withPort(port)) {
            cloudMock.start();

            System.out.println("CloudMock started on port " + cloudMock.port());
            System.out.flush();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[CloudMock] Shutting down...");
                cloudMock.stop();
            }));

            Thread.currentThread().join();
        }
    }

    private static int resolvePort(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--port=")) {
                return Integer.parseInt(args[i].substring("--port=".length()));
            }
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                return Integer.parseInt(args[i + 1]);
            }
        }
        String envPort = System.getenv("CLOUDMOCK_PORT");
        if (envPort != null && !envPort.isBlank()) {
            return Integer.parseInt(envPort);
        }
        return DEFAULT_PORT;
    }

    private static List<String> discoverServiceIds() {
        List<String> ids = new ArrayList<>();
        ServiceLoader.load(CloudMockService.class, Thread.currentThread().getContextClassLoader())
                .forEach(s -> ids.add(s.serviceId()));
        return ids;
    }
}
