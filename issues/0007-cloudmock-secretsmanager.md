# Implement cloudmock-secretsmanager module

**Phase:** 2
**Type:** module

## Summary

Build the second reference service module, implementing JSON/X-Amz-Target stubs for the core Secrets Manager operations. This module establishes the standard for all future JSON/target-header services (DynamoDB, Lambda, EventBridge, and others) in the same way `cloudmock-sqs` established the standard for XML/Form URL services.

## Acceptance criteria

- [x] `cloudmock-secretsmanager` is registered via `META-INF/services/io.cloudmock.core.spi.CloudMockService`
- [x] `serviceId()` returns `"secretsmanager"`
- [x] The following operations are stubbed and return well-formed JSON responses that `SecretsManagerClient` (AWS SDK v2) parses without error:
  - `CreateSecret` → returns `ARN`, `Name`, `VersionId`
  - `GetSecretValue` → returns `ARN`, `Name`, `SecretString`, `VersionId`
  - `PutSecretValue` → returns `ARN`, `Name`, `VersionId`
  - `DeleteSecret` → returns `ARN`, `Name`, `DeletionDate`
  - `ListSecrets` → returns a list with at least one entry containing `ARN` and `Name`
- [x] Matching is done on the `X-Amz-Target` header (e.g. `secretsmanager.GetSecretValue`) via `registerJsonTargetStub`
- [x] Response templates use Handlebars to echo `SecretId` from the request into the `Name` field and to construct a plausible `ARN` (e.g. `arn:aws:secretsmanager:us-east-1:000000000000:secret:<SecretId>`)
- [x] Stub registration uses only `StubRegistrar` — no WireMock type is referenced in module source
- [x] `cloudmock-secretsmanager` has no compile dependency on any other `cloudmock-*` module
- [x] The build constraint from ticket 0001 passes with `cloudmock-secretsmanager` present

## Dependencies

0004

## Notes

- Secrets Manager requests arrive as `POST /` with `Content-Type: application/x-amz-json-1.1`; the `X-Amz-Target` header value is `secretsmanager.<OperationName>`.
- Stubs are stateless — `GetSecretValue` returns a fixed `SecretString` regardless of what was passed to `CreateSecret`.
- The `SecretString` in `GetSecretValue` can be any non-empty JSON string (e.g. `{"username":"test","password":"test"}`); the SDK does not validate its contents.
- The Handlebars templates written here become the reference pattern for `cloudmock-dynamodb`, `cloudmock-lambda`, and all other JSON/target-header modules.
