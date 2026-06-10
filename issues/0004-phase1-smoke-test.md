# Publish to Maven local and validate with a Phase 1 smoke test

**Phase:** 1
**Type:** testing

## Summary

Configure `publishToMavenLocal` for all subprojects and write a minimal smoke test proving the core engine works end-to-end with no modules installed. The test must start `CloudMock`, send a real HTTP request through the AWS SDK v2, and verify the request reaches the local WireMock instance without a connection error. This ticket is the gate that closes Phase 1 — no module work begins until it passes.

## Acceptance criteria

- [x] `./gradlew publishToMavenLocal` publishes `cloudmock-core` (and any other ready subprojects) with correct group, artifact ID, version, and POM metadata
- [x] A smoke test (in `cloudmock-core` or a dedicated test subproject) starts `CloudMock` and uses an AWS SDK v2 client with `EndpointOverride` pointing at the CloudMock port
- [x] The smoke test sends at least one AWS SDK request and asserts that no `SdkClientException` caused by a connection failure is thrown
- [x] The smoke test passes with zero `cloudmock-*` modules on the classpath beyond `cloudmock-core`
- [x] A WireMock 404 for an unregistered path is treated as a pass — the test only asserts the networking layer is alive, not that a meaningful AWS response is returned
- [x] The smoke test is included in `./gradlew test` and causes a build failure if it regresses
- [x] A CI workflow (GitHub Actions or equivalent) runs `./gradlew build` on every push to `main` and on every pull request

## Dependencies

0003

## Notes

- Any AWS SDK v2 client works for the smoke test. `SqsClient` or `S3Client` with a trivial call (e.g. `listQueues()` or `listBuckets()`) is sufficient.
- The goal of this test is narrow: prove the server boots, the SDK redirects, and the port is reachable. Asserting meaningful AWS response content is the job of Phase 2 module tests.
- Set up CI in this ticket if it was not set up in 0001. The CI job should cache the Gradle dependency cache to keep runs fast.
