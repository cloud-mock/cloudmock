# Write AWS SDK v2 round-trip tests for cloudmock-sqs

**Phase:** 2
**Type:** testing

## Summary

Cover every operation stubbed in `cloudmock-sqs` with an AWS SDK v2 integration test that drives `SqsClient` against a live `CloudMock` instance. Tests must assert on response field values — not just that the call did not throw — so that stub regressions are caught immediately.

## Acceptance criteria

- [ ] Each of the seven operations from ticket 0005 has at least one corresponding test
- [ ] Tests use `SqsClient` (AWS SDK v2) with `AnonymousCredentialsProvider` and `EndpointOverride` pointing at the CloudMock port — no raw HTTP
- [ ] A `CloudMock` instance is started in `@BeforeAll` and stopped in `@AfterAll`
- [ ] `CreateQueue` test asserts the returned `QueueUrl` is non-null and contains the queue name
- [ ] `GetQueueUrl` test asserts a non-null `QueueUrl` is returned
- [ ] `SendMessage` test asserts a non-null, non-empty `MessageId` is returned
- [ ] `ReceiveMessage` test asserts at least one message is returned and each has a non-null `MessageId`, `ReceiptHandle`, and `Body`
- [ ] `DeleteMessage` and `DeleteQueue` tests assert the call completes without exception
- [ ] `ListQueues` test asserts the response is non-null (queue list may be empty for a stateless stub)
- [ ] All tests run as part of `./gradlew :cloudmock-sqs:test` and are included in the root `./gradlew test`

## Dependencies

0005

## Notes

- Tests intentionally do not assert stateful behaviour — a `ReceiveMessage` call is not expected to return a message previously sent with `SendMessage`.
- The AWS SDK v2 performs MD5 validation on `ReceiveMessage` responses by default. If the stub returns a hardcoded MD5, ensure it matches the hardcoded `Body`, or disable SDK-side MD5 validation in the client configuration.
- These tests serve as living documentation of the XML/Form URL module pattern. Keep them readable — future module authors will read them as the reference example.