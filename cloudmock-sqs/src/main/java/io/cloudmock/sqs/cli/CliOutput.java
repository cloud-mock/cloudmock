package io.cloudmock.sqs.cli;

import java.util.List;

final class CliOutput {

    private CliOutput() {}

    static void kv(String key, String value) {
        System.out.printf("%-20s %s%n", key + ":", value);
    }

    static void header(String text) {
        System.out.println(text);
        System.out.println("-".repeat(text.length()));
    }

    static void list(List<String> items) {
        if (items.isEmpty()) {
            System.out.println("  (none)");
        } else {
            items.forEach(item -> System.out.println("  " + item));
        }
    }

    static void ok(String message) {
        System.out.println(message);
    }

    static void error(String message) {
        System.err.println("Error: " + message);
    }

    static void unavailable(String baseUrl) {
        System.err.println("CloudMock is not running at " + baseUrl + ".");
        System.err.println("Start it with: java -jar cloudmock-standalone.jar");
    }
}
