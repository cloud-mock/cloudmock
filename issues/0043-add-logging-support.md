# Add logging support

**Type:** core

## Summary

CloudMock has no structured logging. Developers have no visibility into what the framework is
doing — which modules were discovered, which stubs were registered, which requests were matched,
and which were not. Add logging throughout the core engine and modules using a standard JVM
logging facade. A `debug` mode should provide detailed request and response tracing to help
developers diagnose integration issues.

## Acceptance criteria

- [ ] Logging added using SLF4J as the facade — no hard dependency on a specific logging implementation
- [ ] Core engine logs at startup: port, discovered modules, registered stubs
- [ ] Core engine logs at shutdown
- [ ] Each request logs at INFO level: service, operation, matched stub
- [ ] Unmatched requests log at WARN level with enough detail to diagnose the mismatch
- [ ] Debug mode enabled via a `cloudmock.debug=true` system property or environment variable
- [ ] Debug mode logs full request and response payloads
- [ ] Debug mode logs stub registration details at startup
- [ ] Logging does not affect performance when debug mode is off
- [ ] Standalone mode outputs logs to stdout by default

## Notes

- SLF4J as the facade means consumers bring their own logging implementation (Logback, Log4j2,
  java.util.logging). CloudMock does not force a logging framework on the classpath.
- Unmatched requests are the most common source of confusion for new users — the WARN log should
  include the request path, headers, and body so developers know exactly why the match failed.
- Debug mode is primarily useful in standalone mode and during local development. In test mode
  it can be noisy — developers should opt in explicitly.
