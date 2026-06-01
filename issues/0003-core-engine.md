# Implement the cloudmock-core engine

**Phase:** 1
**Type:** core

## Summary

Implement the three responsibilities of `cloudmock-core`: start and stop an embedded WireMock server on a random available port; inject `aws.endpoint-url` so AWS SDK v2 routes all traffic to that port; and run `ServiceLoader` discovery to find and initialise all installed service modules. This layer must have zero compile-time knowledge of any specific AWS service.

## Acceptance criteria

- [ ] A `CloudMock` class exposes `start()` and `stop()` methods as the public lifecycle API
- [ ] `start()` boots an embedded WireMock server bound to a random available port (port 0 binding)
- [ ] `start()` sets the `aws.endpoint-url` system property to `http://localhost:<actual-port>`
- [ ] `stop()` shuts down WireMock cleanly and removes the `aws.endpoint-url` system property
- [ ] `start()` calls `ServiceLoader.load(CloudMockService.class)` using the context class loader and invokes `register(StubRegistrar)` on every discovered service
- [ ] The `StubRegistrar` implementation passed to modules delegates to the live WireMock instance
- [ ] WireMock is declared as an `implementation` (not `api`) dependency so it does not appear on the API classpath of consumers
- [ ] Calling `start()` on an already-started instance throws `IllegalStateException` with a clear message rather than silently opening a second port
- [ ] `stop()` on an instance that was never started is a no-op
- [ ] Unit tests cover: normal start/stop cycle, double-start guard, stop-before-start no-op, and that the system property is set after start and absent after stop

## Dependencies

0002

## Notes

- Binding to port 0 and reading the actual port back (`WireMockServer.port()`) is the correct approach — never hardcode a port.
- The `StubRegistrar` implementation must not be usable before `start()` is called; guard against premature registration in the implementation.
- WireMock's `WireMockConfiguration.options().dynamicPort()` is the recommended configuration entry point. Keep all WireMock configuration in one private factory method so it is easy to extend later.
- `CloudMock` should be safe to use in try-with-resources by implementing `AutoCloseable` (`close()` delegates to `stop()`).