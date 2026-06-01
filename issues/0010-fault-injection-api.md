# Implement the fault injection annotation API

**Phase:** 3
**Type:** dx

## Summary

Add a declarative fault injection API that lets test authors simulate AWS error conditions by annotating test methods rather than writing custom stubs. The initial three scenarios are request throttling, response timeout, and intermittent network brownout. Faults are applied for the duration of the annotated test method and removed cleanly afterwards.

## Acceptance criteria

- [ ] `@SimulateThrottle(service = "sqs")` causes all stub responses for the named service to return an AWS-style throttling error (HTTP 400, `ThrottlingException` error code) for the duration of the annotated test method
- [ ] `@SimulateTimeout(service = "sqs")` causes all stub responses for the named service to be delayed beyond the AWS SDK's call timeout, triggering a timeout exception at the SDK level
- [ ] `@SimulateNetworkBrownout(service = "sqs", rate = 0.5)` causes approximately `rate` fraction of requests to the named service to fail with a connection reset, while the remainder are served normally
- [ ] Fault state is cleaned up after each annotated test method — subsequent test methods see normal stub behaviour
- [ ] Multiple fault annotations can be applied to the same test method and all take effect simultaneously
- [ ] Fault injection is wired via a JUnit 6 extension (`FaultInjectionExtension`, or integrated as an opt-in mode of `CloudMockExtension`) that intercepts `BeforeTestExecution` and `AfterTestExecution`
- [ ] A programmatic API (`CloudMock.simulateThrottle(String service)`, etc.) is available beneath the annotations for use outside JUnit 6
- [ ] The API works with any installed service module — it is not coupled to SQS or Secrets Manager specifically

## Dependencies

0009

## Notes

- Fault state must not leak between tests. Guard this with a finally-block or JUnit 6 extension cleanup that always removes faults even if the test throws.
- The `rate` parameter for `@SimulateNetworkBrownout` is statistical. Tests asserting on brownout behaviour should use `rate = 0.0` or `rate = 1.0` for deterministic results; see ticket 0011.
- The programmatic API beneath the annotations enables non-JUnit consumers (e.g. Kotlin coroutine tests, TestNG) to use fault injection without the JUnit 6 extension.
- WireMock's fault simulation (`Fault.CONNECTION_RESET_BY_PEER`, fixed delays) can implement the underlying mechanism — keep this in the `internal` package, not on the public API.