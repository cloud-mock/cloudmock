package io.cloudmock.cli.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin client for the CloudMock REST API (api-port).
 */
public class ApiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public JsonNode getStatus() throws CloudMockUnavailableException {
        return get("/api/status");
    }

    public JsonNode reset(String service) throws CloudMockUnavailableException {
        String path = service != null && !service.isBlank()
                ? "/api/reset?service=" + service
                : "/api/reset";
        return post(path, "");
    }

    private JsonNode get(String path) throws CloudMockUnavailableException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .GET()
                .build();
        return send(req);
    }

    private JsonNode post(String path, String body) throws CloudMockUnavailableException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return send(req);
    }

    private JsonNode send(HttpRequest req) throws CloudMockUnavailableException {
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return MAPPER.readTree(resp.body());
        } catch (ConnectException e) {
            throw new CloudMockUnavailableException(baseUrl);
        } catch (IOException | InterruptedException e) {
            throw new CloudMockUnavailableException(baseUrl, e);
        }
    }
}
