package io.cloudmock.core.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import io.cloudmock.core.spi.HttpMethod;
import io.cloudmock.core.spi.StubRequest;

/**
 * Adapts a WireMock {@link Request} to the public {@link StubRequest} SPI view, so that
 * {@link io.cloudmock.core.spi.StubHandler}s never see a WireMock or JSON-library type.
 *
 * <p>The body string and its parsed JSON tree are read/parsed lazily and cached, so a handler that
 * pulls several fields out of one request pays for the read and parse only once.
 */
final class WireMockStubRequest implements StubRequest {

    /** Shared, thread-safe once configured; jackson is shaded into {@code io.cloudmock.shaded}. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Request request;

    private String bodyString;
    private boolean bodyParsed;
    private JsonNode bodyTree;

    WireMockStubRequest(Request request) {
        this.request = request;
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.valueOf(request.getMethod().value());
    }

    @Override
    public String path() {
        String url = request.getUrl();
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    @Override
    public String body() {
        if (bodyString == null) {
            String raw = request.getBodyAsString();
            bodyString = raw != null ? raw : "";
        }
        return bodyString;
    }

    @Override
    public String header(String name) {
        return request.getHeader(name);
    }

    @Override
    public String queryParam(String name) {
        QueryParameter param = request.queryParameter(name);
        return param != null && param.isPresent() ? param.firstValue() : null;
    }

    @Override
    public String jsonField(String path) {
        JsonNode node = bodyTree();
        if (node == null || path == null) {
            return null;
        }
        String dotted = path.startsWith("$.") ? path.substring(2) : path;
        for (String segment : dotted.split("\\.")) {
            if (segment.isEmpty()) {
                continue;
            }
            node = node.get(segment);
            if (node == null) {
                return null;
            }
        }
        // Only scalars are returned as text; objects/arrays/explicit null map to absent.
        return node.isValueNode() && !node.isNull() ? node.asText() : null;
    }

    /** Lazily parses the body once; a non-JSON or empty body yields {@code null} (never throws). */
    private JsonNode bodyTree() {
        if (!bodyParsed) {
            bodyParsed = true;
            String raw = body();
            if (!raw.isEmpty()) {
                try {
                    bodyTree = MAPPER.readTree(raw);
                } catch (Exception ignored) {
                    bodyTree = null;
                }
            }
        }
        return bodyTree;
    }
}
