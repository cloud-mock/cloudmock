package io.cloudmock.standalone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.cloudmock.core.CloudMock;
import io.cloudmock.core.restapi.ModuleStatus;
import io.cloudmock.core.restapi.RequestRecord;
import io.cloudmock.core.spi.restapi.ApiHandler;
import io.cloudmock.core.spi.restapi.ApiRequest;
import io.cloudmock.core.spi.restapi.ApiResponse;
import io.cloudmock.core.spi.restapi.ApiRouteRegistrar;
import io.cloudmock.core.spi.CloudMockApiService;
import io.cloudmock.core.spi.HttpMethod;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Lightweight REST API server for standalone mode.
 *
 * <p>Serves on a secondary port (default {@value ApiPortResolver#DEFAULT_API_PORT}) so API
 * traffic is always separate from the AWS mock port. Routes:
 * <ul>
 *   <li>{@code GET  /api/status}           — port, uptime, loaded modules and their stubs, all routes
 *   <li>{@code POST /api/reset}             — clear all state
 *   <li>{@code POST /api/reset?service=X}   — clear state for service X
 *   <li>{@code GET  /api/history}           — all captured requests
 *   <li>{@code GET  /api/history?service=X} — requests filtered by service
 *   <li>{@code GET  /api/openapi.json}      — OpenAPI 3.0 spec auto-generated from registered routes
 * </ul>
 *
 * <p>Modules register additional routes by implementing {@link CloudMockApiService}.
 */
public final class ApiServer implements Closeable {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final CloudMock cloudMock;
    private final int port;
    private final List<CloudMockApiService> moduleServices;
    private final ObjectMapper mapper = new ObjectMapper();

    /** Routes known at start time, used for /api/status and OpenAPI generation. */
    private final List<RouteDescriptor> routes = new ArrayList<>();

    private HttpServer server;

    public ApiServer(CloudMock cloudMock, int port, List<CloudMockApiService> moduleServices) {
        this.cloudMock = cloudMock;
        this.port = port;
        this.moduleServices = moduleServices;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        registerCoreRoutes();
        registerModuleRoutes();

        server.createContext("/api/openapi.json", this::handleOpenApi);

        server.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "cloudmock-api");
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    @Override
    public void close() {
        stop();
    }

    public int port() {
        return port;
    }

    // -------------------------------------------------------------------------
    // Route registration
    // -------------------------------------------------------------------------

    private static final QueryParam SERVICE_PARAM = new QueryParam(
            "service", "Service ID to target (e.g. sqs, s3). Omit to apply to all services.");

    private void registerCoreRoutes() {
        addRoute("GET",  "/api/status",  "Running instance info: port, uptime, modules, routes",
                List.of(), this::handleStatus);
        addRoute("POST", "/api/reset",   "Clear all state, or a single service with ?service=X",
                List.of(SERVICE_PARAM), this::handleReset);
        addRoute("GET",  "/api/history", "Captured request log, optionally filtered with ?service=X",
                List.of(SERVICE_PARAM), this::handleHistory);
    }

    private void registerModuleRoutes() {
        for (CloudMockApiService svc : moduleServices) {
            svc.registerRoutes(new ApiRouteRegistrar() {
                @Override
                public void register(HttpMethod method, String path, String description,
                                     ApiHandler handler) {
                    String fullPath = "/api/" + svc.serviceId() + path;
                    addRoute(method.name(), fullPath, description, List.of(),
                            req -> handler.handle(req));
                }
            });
        }
    }

    private void addRoute(String method, String path, String description,
                          List<QueryParam> queryParams, RouteHandler handler) {
        routes.add(new RouteDescriptor(method, path, description, queryParams));
        server.createContext(path, exchange -> {
            if (!method.equals(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method Not Allowed");
                return;
            }
            Map<String, String> reqParams = parseQuery(exchange.getRequestURI().getQuery());
            ApiRequest req = new ApiRequest(method, path, reqParams);
            try {
                ApiResponse resp = handler.handle(req);
                sendJson(exchange, resp.statusCode(), resp.body());
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Core route handlers
    // -------------------------------------------------------------------------

    private ApiResponse handleStatus(ApiRequest req) {
        Instant now = Instant.now();
        Instant started = cloudMock.startedAt();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("port", cloudMock.port());
        body.put("apiPort", port);
        body.put("startedAt", started.toString());
        body.put("uptime", Duration.between(started, now).toString());
        body.put("modules", serializeModules(cloudMock.modules()));
        body.put("routes", serializeRoutes());

        return new ApiResponse(200, body);
    }

    private ApiResponse handleReset(ApiRequest req) {
        String service = req.queryParams().get("service");
        if (service != null && !service.isBlank()) {
            cloudMock.stateStore().clear(service + "/");
        } else {
            cloudMock.stateStore().clearAll();
        }
        return new ApiResponse(200, Map.of("status", "ok"));
    }

    private ApiResponse handleHistory(ApiRequest req) {
        String service = req.queryParams().get("service");
        List<RequestRecord> records = service != null && !service.isBlank()
                ? cloudMock.requestHistory(service)
                : cloudMock.requestHistory();

        List<Map<String, Object>> items = records.stream()
                .map(this::serializeRecord)
                .toList();

        return new ApiResponse(200, Map.of("requests", items));
    }

    // -------------------------------------------------------------------------
    // OpenAPI handler (outside the addRoute mechanism — no ApiResponse wrapping)
    // -------------------------------------------------------------------------

    private void handleOpenApi(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }
        sendJson(exchange, 200, buildOpenApiSpec());
    }

    // -------------------------------------------------------------------------
    // Serialisation helpers
    // -------------------------------------------------------------------------

    private List<Map<String, Object>> serializeModules(List<ModuleStatus> modules) {
        return modules.stream().map(m -> {
            Map<String, Object> mod = new LinkedHashMap<>();
            mod.put("id", m.id());
            mod.put("stubs", m.stubs().stream()
                    .map(s -> Map.of("protocol", s.protocol(), "matchKey", s.matchKey()))
                    .toList());
            return mod;
        }).toList();
    }

    private List<Map<String, Object>> serializeRoutes() {
        return routes.stream().map(r -> {
            Map<String, Object> route = new LinkedHashMap<>();
            route.put("method", r.method());
            route.put("path", r.path());
            route.put("description", r.description());
            if (!r.queryParams().isEmpty()) {
                route.put("queryParams", r.queryParams().stream()
                        .map(p -> Map.of("name", p.name(), "description", p.description()))
                        .toList());
            }
            return route;
        }).toList();
    }

    private Map<String, Object> serializeRecord(RequestRecord r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("timestamp", r.timestamp());
        m.put("method", r.method());
        m.put("url", r.url());
        m.put("serviceId", r.serviceId());
        m.put("operation", r.operation());
        m.put("statusCode", r.statusCode());
        m.put("matched", r.matched());
        return m;
    }

    private Map<String, Object> buildOpenApiSpec() {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", "3.0.3");
        spec.put("info", Map.of("title", "CloudMock API", "version", "1.0.0"));

        Map<String, Map<String, Object>> paths = new LinkedHashMap<>();
        for (RouteDescriptor r : routes) {
            Map<String, Object> operation = new LinkedHashMap<>();
            operation.put("summary", r.description());
            if (!r.queryParams().isEmpty()) {
                operation.put("parameters", r.queryParams().stream()
                        .map(p -> {
                            Map<String, Object> param = new LinkedHashMap<>();
                            param.put("name", p.name());
                            param.put("in", "query");
                            param.put("required", false);
                            param.put("description", p.description());
                            param.put("schema", Map.of("type", "string"));
                            return param;
                        })
                        .toList());
            }
            operation.put("responses", Map.of("200", Map.of("description", "OK")));
            paths.computeIfAbsent(r.path(), k -> new LinkedHashMap<>())
                    .put(r.method().toLowerCase(), operation);
        }
        spec.put("paths", paths);
        return spec;
    }

    // -------------------------------------------------------------------------
    // HTTP utilities
    // -------------------------------------------------------------------------

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set(CONTENT_TYPE, APPLICATION_JSON);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(Map.of("error", message));
        exchange.getResponseHeaders().set(CONTENT_TYPE, APPLICATION_JSON);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                params.put(pair.substring(0, eq), pair.substring(eq + 1));
            } else {
                params.put(pair, "");
            }
        }
        return params;
    }

    // -------------------------------------------------------------------------
    // Internal types
    // -------------------------------------------------------------------------

    @FunctionalInterface
    private interface RouteHandler {
        ApiResponse handle(ApiRequest request);
    }

    private record QueryParam(String name, String description) {}

    private record RouteDescriptor(String method, String path, String description,
                                   List<QueryParam> queryParams) {}
}
