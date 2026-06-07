package io.cloudmock.core.spi.restapi;

import io.cloudmock.core.spi.CloudMockApiService;
import io.cloudmock.core.spi.HttpMethod;

/**
 * Passed to {@link CloudMockApiService#registerRoutes} so modules can declare API routes
 * without depending on any HTTP transport type.
 */
public interface ApiRouteRegistrar {

    /**
     * Register a route under {@code /api/<serviceId>/<path>}.
     *
     * @param method      HTTP method (GET, POST, …)
     * @param path        path relative to {@code /api/<serviceId>}, e.g. {@code "/queues"}
     * @param description one-line description used in the OpenAPI spec
     * @param handler     invoked on every matching request
     */
    void register(HttpMethod method, String path, String description, ApiHandler handler);
}
