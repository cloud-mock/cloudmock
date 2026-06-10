# Implement the JUnit 6 CloudMockExtension lifecycle hook

**Phase:** 3
**Type:** dx

## Summary

Deliver `CloudMockExtension` so that test classes can be annotated with `@ExtendWith(CloudMockExtension.class)` and require no manual `start()`/`stop()` calls. CloudMock boots before the first test method and shuts down after the last. This is the primary integration path for day-to-day CloudMock users and the foundation that the fault injection API builds on.

## Acceptance criteria

- [x] `CloudMockExtension` implements JUnit 6 `BeforeAllCallback` and `AfterAllCallback`
- [x] Annotating a test class with `@ExtendWith(CloudMockExtension.class)` starts `CloudMock` before any test method runs and stops it after the last
- [x] The running `CloudMock` instance is accessible to test methods via a `@RegisterExtension` static field pattern (e.g. `static CloudMockExtension cloudMock = new CloudMockExtension()`) so tests can read the port
- [x] Both `@ExtendWith` (no-instance access needed) and `@RegisterExtension` (instance access needed) usage modes are supported and documented
- [x] Multiple test classes running in the same JVM each get an independent lifecycle — one class's `stop()` does not affect another's running instance
- [x] `CloudMockExtension` is packaged in a new `cloudmock-junit6` module so that JUnit 6 is not a required dependency for consumers who manage the lifecycle manually
- [x] `cloudmock-junit6` declares `cloudmock-core` as `implementation` and JUnit 6 as `compileOnly`
- [x] Integration tests cover: class-level lifecycle starts and stops correctly, `@RegisterExtension` exposes the port, two test classes in the same run are isolated

## Dependencies

0003

## Notes

- The choice between class-level lifecycle (one `CloudMock` instance per class) and method-level lifecycle (one per test method) has a speed/isolation trade-off. Default to class-level; document how to opt into method-level via `@RegisterExtension` with `BeforeEachCallback`.
- Parallel test execution is a future concern — do not design against it in this ticket, but avoid global state that would make it impossible later.
