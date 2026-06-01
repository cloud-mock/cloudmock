# Implement cloudmock-sqs module

**Phase:** 2
**Type:** module

## Summary

Build the first reference service module, implementing XML/Form URL stubs for the core SQS operations. Every routing, templating, and registration pattern established here sets the standard for all future XML/Form URL services (SNS, SES, and others), so clarity of design matters as much as correctness of output.

## Acceptance criteria

- [ ] `cloudmock-sqs` is registered via `META-INF/services/io.cloudmock.core.spi.CloudMockService`
- [ ] `serviceId()` returns `"sqs"`
- [ ] The following operations are stubbed and return well-formed XML responses that `SqsClient` (AWS SDK v2) parses without error:
  - `CreateQueue` → returns `QueueUrl`
  - `GetQueueUrl` → returns `QueueUrl`
  - `SendMessage` → returns `MessageId`, `MD5OfMessageBody`
  - `ReceiveMessage` → returns at least one message with `MessageId`, `ReceiptHandle`, `Body`, `MD5OfBody`
  - `DeleteMessage` → returns HTTP 200 with empty body
  - `DeleteQueue` → returns HTTP 200 with empty body
  - `ListQueues` → returns a `QueueUrl` list
- [ ] Response templates use Handlebars to generate or echo back all correlation identifiers required by the SDK
- [ ] `QueueUrl` in responses is a plausible URL (e.g. `http://localhost:<port>/000000000000/<QueueName>`) that the SDK accepts
- [ ] Stub registration uses only `StubRegistrar` — no WireMock type is referenced in module source
- [ ] `cloudmock-sqs` has no compile dependency on any other `cloudmock-*` module
- [ ] The build constraint from ticket 0001 passes with `cloudmock-sqs` present

## Dependencies

0004

## Notes

- SQS requests arrive as `POST /` with `Content-Type: application/x-www-form-urlencoded`; the `Action` form parameter identifies the operation. Matching is done on this parameter via `registerXmlFormStub`.
- Stubs are stateless — `ReceiveMessage` returns a synthetic message regardless of any prior `SendMessage` calls. Do not attempt to simulate queue state.
- The Handlebars templates written here become the reference pattern for all future XML/Form URL modules. Write them to be readable and well-commented so module authors can follow them.
- `MD5OfMessageBody` in `SendMessage` and `ReceiveMessage` responses must be a valid MD5 hex string; a hardcoded placeholder is acceptable as long as the SDK does not verify it against the body.