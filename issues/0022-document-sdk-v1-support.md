# Document SDK v1 support on the docs site

**Phase:** 3
**Type:** dx

## Summary

The `cloudmock-sdk-v1` companion library was implemented under #0016, but the documentation site has no page covering
it. This issue adds a dedicated **SDK v1 Support** guide to the MkDocs site explaining how to redirect AWS SDK v1
clients to a running CloudMock instance, and — importantly — where the boundary of that support lies. It also delivers
a small, runnable example that shows a user authoring their own v1-shaped stub via the SPI, so the bring-your-own-stub
path is demonstrated by real, compiling code rather than described in prose.

## Acceptance criteria

- [x] A new page `docs/sdk-v1.md` is created and registered under the **Guides** section of `mkdocs.yml` nav (alongside JUnit 6, Spring Boot, Fault Injection)
- [x] The page includes Gradle/Maven dependency snippets for `cloudmock-sdk-v1`, a service module, and the relevant `com.amazonaws:aws-java-sdk-*` client
- [x] The page shows the one-line redirection pattern: `CloudMockV1Endpoints.forPort(cloudMock.port())` passed to `.withEndpointConfiguration(...)`, with a full `@BeforeAll`/`@AfterAll` example
- [x] The page documents the **connectivity vs response fidelity** boundary: the companion only redirects the endpoint; first-party modules target the SDK v2 protocol shape (JSON / `X-Amz-Target`), while SDK v1 SQS/SNS speak the XML/QUERY `Action`-form protocol, so a v1 call against a v2-shaped stub returns 404
- [x] The page documents the **bring-your-own-stub** path: a v1 user authors a `CloudMockService` and registers stubs via `registerXmlFormStub(actionName, responseTemplate)`, linking to `module-authoring.md` for the full SPI walkthrough
- [x] A **runnable example** is delivered: a user-authored `CloudMockService` that registers one or more XML/QUERY stubs via `registerXmlFormStub` (e.g. an SNS `Publish` or SQS `SendMessage` `Action`), installed via `withService(...)`
- [x] An accompanying test drives that stub with a real **SDK v1** client and asserts a populated response (not merely connectivity) — proving the v1 stub is matched and served end-to-end
- [x] The example compiles and the test passes in `./gradlew build`; the docs page embeds (or links to) this exact code so the guide cannot drift from working code
- [x] The page notes that the AWS Java SDK v1 reached end-of-support on 2025-12-31, that the companion exists to support teams mid-migration, and that per-service first-party v1 stubs are explicitly a non-goal
- [x] The page explains the dummy signing region (`us-east-1`) and anonymous credentials are required by SDK v1 but have no effect on stub matching
- [x] The `docs/index.md` and/or `getting-started.md` cross-link to the new page where SDK version support is mentioned

## Dependencies

- #0016 (the `cloudmock-sdk-v1` library being documented must exist — done)
- #0013 (the docs site and MkDocs nav structure)
- #0020 (validates `registerXmlFormStub` end-to-end). Note the overlap: the worked example in this issue exercises an `Action`-form stub with a real client and therefore **satisfies #0020's "raw `Action` integration test" option**. Coordinate so the work is not done twice — either this example fulfils #0020, or #0020's SNS-module route lands first and this example reuses it.

## Notes

- No change to `cloudmock-sdk-v1` *production* source is required; #0016 owns the library. The only code added here is
  the worked example (a `CloudMockService` plus its test) and the docs page.
- The example should live where it can be driven by both a v1 client and the SPI — e.g. an example/test source set
  rather than the thin `cloudmock-sdk-v1` main JAR, so the published companion stays free of example/stub code. If a
  `docs-examples` subproject exists or is planned (#0013), that is the natural home.
- Use the working example in `cloudmock-sdk-v1/src/test/java/io/cloudmock/sdkv1/CloudMockV1EndpointsTest.java` as the
  basis for the connectivity code samples so the docs match real, passing code.
- Frame the page honestly: connectivity is guaranteed and tested; response fidelity for v1 is a user-supplied concern
  via the SPI. Do not imply first-party v1 stubs are coming.
- Keep the "revisit keeping the companion once v1 usage hits zero" decision out of the user-facing guide — that is an
  internal maintenance note, not user documentation. It belongs in the ADR/VISION material (see #0023), if anywhere.
