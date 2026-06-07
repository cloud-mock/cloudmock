# REST API

When running in standalone mode CloudMock exposes a REST API on a secondary port (`4567` by default).
The API and the AWS mock port (`4566`) run in the same process — no extra server to start.

## Configuration

| Mechanism            | Example                   |
|----------------------|---------------------------|
| CLI flag             | `--api-port=9001`         |
| Environment variable | `CLOUDMOCK_API_PORT=9001` |
| Default              | `4567`                    |

Precedence: `--api-port` flag → `CLOUDMOCK_API_PORT` env var → default `4567`.

=== "CLI flag"

    ```
    java -jar cloudmock-standalone.jar --api-port=9001
    ```

=== "Environment variable"

    ```
    CLOUDMOCK_API_PORT=9001 java -jar cloudmock-standalone.jar
    ```

The API port is printed at startup alongside the mock port:

```
CloudMock started on port 4566
CloudMock API on port 4567
```

## Why a separate port?

AWS service stubs (especially S3) register broad catch-all path patterns. Serving the API on the
same port would cause those stubs to match `/api/*` paths before the API routes could. A dedicated
port keeps the two traffic streams cleanly separated.

## Endpoints

All responses are JSON. The base URL used in the examples below is `http://localhost:4567`.

---

### `GET /api/status`

Returns a snapshot of the running instance: ports, uptime, loaded modules with their registered
stubs, and the full list of available API routes.

**Response**

```json
{
  "port": 4566,
  "apiPort": 4567,
  "startedAt": "2026-06-06T10:00:00Z",
  "uptime": "PT5M30S",
  "modules": [
    {
      "id": "sqs",
      "stubs": [
        {
          "protocol": "JSON_TARGET",
          "matchKey": "AmazonSQS.SendMessage"
        },
        {
          "protocol": "JSON_TARGET",
          "matchKey": "AmazonSQS.ReceiveMessage"
        }
      ]
    }
  ],
  "routes": [
    {
      "method": "GET",
      "path": "/api/status",
      "description": "Running instance info: port, uptime, modules, routes"
    },
    {
      "method": "POST",
      "path": "/api/reset",
      "description": "Clear all state (or ?service=X for one service)"
    },
    {
      "method": "GET",
      "path": "/api/history",
      "description": "Captured request log (or ?service=X to filter)"
    }
  ]
}
```

Use `GET /api/status` as the discovery endpoint — the `routes` array tells you exactly what
operations are available without consulting documentation.

---

### `POST /api/reset`

Clears all state in the store. Use this to start each test scenario with a clean slate when
running tests against a long-lived standalone process.

A full reset (no `service`) also clears the request history. A single-service reset clears only
that service's state and leaves the history intact — the history is one shared journal with no
per-service partition.

=== "Clear everything"

    ```
    POST /api/reset
    ```

=== "Clear one service"

    ```
    POST /api/reset?service=sqs
    ```

**Response**

```json
{
  "status": "ok"
}
```

---

### `GET /api/history`

Returns the list of all requests served since startup, most recent first. Each entry shows
whether the request matched a registered stub, which service and operation handled it, and the
HTTP status code returned.

=== "All services"

    ```
    GET /api/history
    ```

=== "One service"

    ```
    GET /api/history?service=sqs
    ```

**Response**

```json
{
  "requests": [
    {
      "timestamp": "2026-06-06T10:01:00Z",
      "method": "POST",
      "url": "/?Action=SendMessage&...",
      "serviceId": "sqs",
      "operation": "AmazonSQS.SendMessage",
      "statusCode": 200,
      "matched": true
    },
    {
      "timestamp": "2026-06-06T10:00:55Z",
      "method": "GET",
      "url": "/unknown-path",
      "serviceId": null,
      "operation": null,
      "statusCode": 404,
      "matched": false
    }
  ]
}
```

Unmatched requests — those not handled by any registered stub — appear with `"matched": false`
and `"serviceId": null`. They are the most common source of integration issues; check the `url`
field to diagnose why a stub did not match.

The history is capped at the last 1000 requests by default so a long-lived process does not grow
without bound. Change the cap with `--max-history=<n>` (or `CLOUDMOCK_MAX_HISTORY`); pass
`--max-history=unlimited` to retain everything.

---

### `GET /api/openapi.json`

Returns an OpenAPI 3.0 spec auto-generated from all registered routes, including any routes
contributed by module-specific `CloudMockApiService` implementations.

```
GET /api/openapi.json
```

The spec updates automatically when modules are added or removed — no manual maintenance required.

## Module routes

Modules can expose their own routes under `/api/<serviceId>/…` by implementing the
`CloudMockApiService` SPI interface. If a module JAR is not on the classpath its routes do not
exist; adding it to the classpath makes them available automatically.

See [Module Authoring](module-authoring.md) for details on implementing `CloudMockApiService`.
