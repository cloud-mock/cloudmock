# Integration tests for fault injection scenarios

**Phase:** 3
**Type:** testing

## Summary

Validate that each fault injection annotation produces the correct AWS SDK behaviour when used against real service modules, and that fault state does not leak between test methods. These tests are the safety net for the fault injection API — they must catch both incorrect fault application and incorrect cleanup.

## Acceptance criteria

- [ ] `@SimulateThrottle` on an SQS operation causes `SqsClient` to throw `SqsException` with error code `ThrottlingException`
- [ ] `@SimulateThrottle` on a Secrets Manager operation causes `SecretsManagerException` with error code `ThrottlingException`
- [ ] `@SimulateTimeout` causes the SDK call to throw `ApiCallTimeoutException` or `SdkClientException` within a test-configured short timeout
- [ ] `@SimulateNetworkBrownout(rate = 1.0)` causes every request to fail with a connection error
- [ ] `@SimulateNetworkBrownout(rate = 0.0)` causes every request to succeed normally
- [ ] A test method immediately following a `@SimulateThrottle`-annotated method completes successfully with a normal response (no fault leak)
- [ ] A test method immediately following a `@SimulateTimeout`-annotated method completes within the normal timeout (no fault leak)
- [ ] Tests are tagged (e.g. `@Tag("fault-injection")`) so they can be excluded from fast local runs if desired
- [ ] All tests run as part of `./gradlew test`

## Dependencies

0010, 0006, 0008

## Notes

- Use `@SimulateNetworkBrownout(rate = 1.0)` and `rate = 0.0` for all deterministic tests — avoid probabilistic assertions.
- For `@SimulateTimeout` tests, configure the SDK client with a short `apiCallTimeout` (e.g. 500ms) so the test does not take the full default timeout to complete.
- The fault-leak tests (assertions that normal behaviour resumes after a faulted method) are the most important tests in this suite.