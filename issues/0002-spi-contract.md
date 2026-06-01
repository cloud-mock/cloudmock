# Define and freeze the CloudMockService SPI contract

**Phase:** 1
**Type:** core

## Summary

Design and implement the two interfaces every service module must satisfy: `CloudMockService` (the entry point the core discovers via `ServiceLoader`) and `StubRegistrar` (the facade through which modules register stubs without touching WireMock directly). This contract must be reviewed and frozen before any module work begins — a breaking change after modules exist is expensive.

## Acceptance criteria

- [ ] `CloudMockService` interface is in `cloudmock-core` with `String serviceId()` and `void register(StubRegistrar registrar)` methods
- [ ] `StubRegistrar` interface is in `cloudmock-core` with `registerXmlFormStub(String actionName, String responseTemplate)`, `registerJsonTargetStub(String target, String responseTemplate)`, and `registerRestStub(HttpMethod method, String pathPattern, String responseTemplate)` methods
- [ ] `HttpMethod` enum (at minimum: GET, POST, PUT, DELETE, HEAD, PATCH) is defined in `cloudmock-core` — no WireMock type appears anywhere in the public API
- [ ] All public types carry Javadoc that explains the contract, not just the signature
- [ ] No WireMock type is reachable from any `cloudmock-core` public API surface; enforced by a Checkstyle or ArchUnit rule if feasible
- [ ] Open question 1 (escape hatch for raw WireMock `MappingBuilder`) is resolved and the decision is recorded in `docs/adr.md`
- [ ] Open question 2 (versioning and compatibility policy between core and module JARs) is resolved and recorded in `docs/adr.md`

## Dependencies

0001

## Notes

- The `StubRegistrar` is a pure facade — its implementation in `cloudmock-core` will delegate to WireMock internally, but the delegation must be invisible externally.
- If an escape hatch is added to resolve open question 1, it should be a separate method clearly marked `@Advanced` with documentation stating it is not covered by stability guarantees.
- The versioning decision (open question 2) shapes how module JARs declare their minimum core version. A manifest attribute (`CloudMock-Core-Min-Version`) in the module JAR is one lightweight approach.
- The package structure of the public API should be decided here and kept stable (e.g. `io.cloudmock.core.spi`). Internal WireMock delegation lives in a separate package (e.g. `io.cloudmock.core.internal`) that is not part of the API contract.