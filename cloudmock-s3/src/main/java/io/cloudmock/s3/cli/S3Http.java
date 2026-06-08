package io.cloudmock.s3.cli;

import io.cloudmock.core.spi.cli.CliContext;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class S3Http {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    private S3Http() {}

    static String get(CliContext ctx, String path) throws CliConnectionException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(ctx.mockBaseUrl() + path))
                .timeout(TIMEOUT)
                .GET()
                .build();
        return send(req, ctx.mockBaseUrl());
    }

    static String put(CliContext ctx, String path, String body) throws CliConnectionException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(ctx.mockBaseUrl() + path))
                .timeout(TIMEOUT)
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return send(req, ctx.mockBaseUrl());
    }

    private static String send(HttpRequest req, String baseUrl) throws CliConnectionException {
        try {
            return HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (ConnectException e) {
            throw new CliConnectionException(baseUrl);
        } catch (IOException | InterruptedException e) {
            throw new CliConnectionException(baseUrl, e);
        }
    }
}
