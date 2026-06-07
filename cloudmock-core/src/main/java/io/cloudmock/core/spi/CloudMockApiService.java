package io.cloudmock.core.spi;

import io.cloudmock.core.spi.restapi.ApiRouteRegistrar;

/**
 * Optional companion to {@link CloudMockService} for modules that want to expose API routes
 * under {@code /api/<serviceId>/…}.
 *
 * <p>Discovered at startup via {@code ServiceLoader.load(CloudMockApiService.class)}.
 * Modules that have nothing to expose simply do not implement this interface.
 *
 * <p>Registered via {@code META-INF/services/io.cloudmock.core.spi.CloudMockApiService}.
 */
public interface CloudMockApiService {

    /** Must match the {@link CloudMockService#serviceId()} of the same module. */
    String serviceId();

    /**
     * Called at API server startup. Register all module-specific routes through
     * {@code registrar}. Routes are mounted at {@code /api/<serviceId><path>}.
     */
    void registerRoutes(ApiRouteRegistrar registrar);
}
