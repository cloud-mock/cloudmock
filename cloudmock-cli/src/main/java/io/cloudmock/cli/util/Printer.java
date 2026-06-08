package io.cloudmock.cli.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Simple terminal output helpers. No ANSI colours — keeps output clean in scripts and pipes.
 */
public final class Printer {

    private Printer() {}

    public static void kv(String key, String value) {
        System.out.printf("%-20s %s%n", key + ":", value);
    }

    public static void header(String text) {
        System.out.println(text);
        System.out.println("-".repeat(text.length()));
    }

    public static void list(List<String> items) {
        if (items.isEmpty()) {
            System.out.println("(none)");
        } else {
            items.forEach(item -> System.out.println("  " + item));
        }
    }

    public static void json(JsonNode node) {
        System.out.println(node.toPrettyString());
    }

    public static void ok(String message) {
        System.out.println(message);
    }

    public static void error(String message) {
        System.err.println("Error: " + message);
    }

    public static void unavailable(String baseUrl) {
        System.err.println("CloudMock is not running at " + baseUrl + ".");
        System.err.println("Start it with: java -jar cloudmock-standalone.jar");
    }

    public static void serviceNotLoaded(String serviceId) {
        System.err.println("Service '" + serviceId + "' is not loaded in the running CloudMock instance.");
        System.err.println("Restart CloudMock with the " + serviceId + " module on the classpath.");
    }
}
