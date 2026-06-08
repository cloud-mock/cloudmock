package io.cloudmock.secretsmanager.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudmock.core.spi.cli.CliContext;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class SecretsHttp {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    private SecretsHttp() {}

    static JsonNode call(CliContext ctx, String operation, String jsonBody)
            throws CliConnectionException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(ctx.mockBaseUrl() + "/"))
                .header("X-Amz-Target", "secretsmanager." + operation)
                .header("Content-Type", "application/x-amz-json-1.1")
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return MAPPER.readTree(resp.body());
        } catch (ConnectException e) {
            throw new CliConnectionException(ctx.mockBaseUrl());
        } catch (IOException | InterruptedException e) {
            throw new CliConnectionException(ctx.mockBaseUrl(), e);
        }
    }
}
