# cloudmock-sdk-v1 companion library

**Phase:** 3
**Type:** module

## Summary

AWS SDK v1 has no global endpoint override equivalent to the `aws.endpoint-url` system property used by SDK v2.
Redirecting SDK v1 clients to a CloudMock instance requires per-client configuration. Create a `cloudmock-sdk-v1`
companion library that provides a builder helper to redirect v1 clients to the CloudMock port. It should be a one-liner
in test setup — not zero-config like v2, but close enough that it does not slow adoption for teams still on SDK v1.

## Scope: connectivity, not response fidelity

The companion's job is strictly **endpoint redirection** — it points a v1 client at CloudMock's port. It does not, and
will not, guarantee that any given operation returns a matching response. The reason is protocol shape: first-party
modules (e.g. `cloudmock-sqs`) target the SDK v2 request format (JSON / `X-Amz-Target`), whereas SDK v1 SQS/SNS speak
the older XML/QUERY protocol with an `Action` form-body parameter. A v1 call against a v2-shaped stub will simply miss
and return 404.

This is **not** a blocker, because CloudMock is extensible by design. A v1 user who needs response fidelity authors
their own `CloudMockService` and registers stubs via `registerXmlFormStub(actionName, responseTemplate)` — the public
SPI method that exists precisely for the `Action`-form-body protocol that v1 clients use — then installs it via
`withService(...)` or `META-INF/services`. The companion handles connectivity; the user-authored stub handles the
response. Together they form a complete v1 story with zero first-party v1 stub maintenance.

Given the AWS Java SDK v1 reached end-of-support on 2025-12-31, building per-service v1 protocol coverage into the
shipped modules is explicitly a non-goal. The companion exists to support teams mid-migration, and "bring your own
stub via the SPI" is the supported path for v1 response behaviour.

The self-service path above depends entirely on `registerXmlFormStub` working end-to-end — which is the exact code
path validated by #0020. Until #0020 lands, the "author your own v1 stub" story is unverified; once it lands (via the
SNS module or the raw `Action` integration test), the v1 path is implicitly proven, since it is the same routing
primitive.

## Acceptance criteria

- [ ] A new `cloudmock-sdk-v1` subproject is added to `settings.gradle`
- [ ] The subproject declares `com.amazonaws:aws-java-sdk-core` as a `compileOnly` dependency — SDK v1 must not be forced onto consumers who do not need it
- [ ] A `CloudMockV1Endpoints` utility class provides a static helper that returns an `EndpointConfiguration` pointing at `http://localhost:<port>` with a dummy signing region (`us-east-1`)
- [ ] Usage requires exactly one extra line compared to a normal SDK v1 client setup — no XML, no properties file, no subclassing
- [ ] `cloudmock-sdk-v1` has no compile dependency on any other `cloudmock-*` module; the Gradle isolation check passes
- [ ] At least one integration test configures an SDK v1 client via the helper, sends a request to a running `CloudMock` instance, and asserts the response is served without a connection error
- [ ] The README "Limitations / out of scope" section is updated to reflect that SDK v1 is supported for **connectivity** via the companion library, and that v1 response fidelity is achieved by authoring stubs against the SPI (not shipped first-party)
- [ ] The README / docs make clear that first-party modules target the SDK v2 protocol shape, and that v1 users supply their own `registerXmlFormStub` stubs for response behaviour

## Dependencies

- #0003 (core engine must be stable — the companion targets its port API)
- #0005 (at least one service module should be live so the integration test uses a real stub)
- #0020 (validates `registerXmlFormStub` end-to-end — underwrites the "author your own v1 stub" path; not a hard build dependency, but the v1 response-fidelity story is unproven until it lands)

## Notes

- The `cloudmock-core` engine does not change. It has no knowledge of which SDK version the consumer is using.
- SDK v1 uses `com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration` to override the endpoint. The
  helper should accept either a `CloudMock` instance or a raw port integer — whichever is simpler to use from a test.
- A dummy signing region is required by SDK v1's endpoint configuration but has no effect on stub matching. Document
  this clearly so users are not confused by the hardcoded value.
- `cloudmock-sdk-v1` is intentionally thin. It must not reimplement any stub logic — all response behaviour is owned
  by the installed service modules (first-party, v2-shaped) or by user-authored `CloudMockService` implementations
  (for v1-shaped stubs via `registerXmlFormStub`).
- Whether to keep the companion at all is worth revisiting once internal v1 usage drops to zero. For now it is the
  migration bridge: tiny, complete, and the only practical way to redirect a v1 client (which has no global endpoint
  override).
