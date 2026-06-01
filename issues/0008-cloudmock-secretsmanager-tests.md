# Write AWS SDK v2 round-trip tests for cloudmock-secretsmanager

**Phase:** 2
**Type:** testing

## Summary

Cover every operation stubbed in `cloudmock-secretsmanager` with an AWS SDK v2 integration test that drives `SecretsManagerClient` against a live `CloudMock` instance. These tests serve as both correctness verification and the canonical reference example of the JSON/X-Amz-Target module test pattern.

## Acceptance criteria

- [ ] Each of the five operations from ticket 0007 has at least one corresponding test
- [ ] Tests use `SecretsManagerClient` (AWS SDK v2) with `AnonymousCredentialsProvider` and `EndpointOverride` pointing at the CloudMock port — no raw HTTP
- [ ] A `CloudMock` instance is started in `@BeforeAll` and stopped in `@AfterAll`
- [ ] `CreateSecret` test asserts the returned `ARN` is non-null and the `Name` matches the input
- [ ] `GetSecretValue` test asserts `SecretString` is non-null and non-empty
- [ ] `PutSecretValue` test asserts a non-null `VersionId` is returned
- [ ] `DeleteSecret` test asserts the call completes without exception and returns a non-null `ARN`
- [ ] `ListSecrets` test asserts the response is non-null
- [ ] All tests run as part of `./gradlew :cloudmock-secretsmanager:test` and are included in the root `./gradlew test`

## Dependencies

0007

## Notes

- Tests do not assert stateful behaviour — `GetSecretValue` is not expected to return a value previously set by `CreateSecret`.
- Keep tests readable; future module authors will use them as the reference example for JSON/target-header integration tests.