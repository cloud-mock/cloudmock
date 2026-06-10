# Implement the stub generation agent (Smithy model → module skeleton)

**Phase:** 3
**Type:** agent

## Summary

Build the first version of the stub generation agent: a tool that accepts an AWS Smithy service model and emits a complete, compilable `cloudmock-*` module skeleton. The output includes the `build.gradle`, `CloudMockService` implementation, stub registrations with placeholder Handlebars response templates, `META-INF/services` entry, and a test skeleton. The goal is to collapse new module development from days to hours.

## Acceptance criteria

- [x] Agent accepts a `.smithy` model file (or directory) as input via CLI (`--model <path>`)
- [x] Agent identifies the service protocol from Smithy protocol traits: `aws.protocols#query` / `restXml` → XML/Form, `aws.protocols#awsJson1_0` / `awsJson1_1` → JSON/Target, `aws.protocols#restJson1` → REST path
- [x] For each operation in the model, agent generates a `StubRegistrar` call using the appropriate `register*Stub` method
- [x] Response templates are generated with placeholder Handlebars expressions for all required output shape fields, derived from the Smithy model
- [x] Generated output includes:
  - `build.gradle` with `cloudmock-core` dependency and correct module coordinates
  - `CloudMockService` implementation with `serviceId()` derived from the model service name
  - `META-INF/services/io.cloudmock.core.spi.CloudMockService` registration file
  - One Handlebars response template file per operation
  - A test skeleton class with one `@Test` method stub per operation
- [x] The generated module compiles against the published `cloudmock-core` without modification
- [x] Agent is runnable as a standalone executable JAR (`java -jar cloudmock-agent.jar --model ...`)

## Dependencies

0002

## Notes

- Use the Smithy Java library (`software.amazon.smithy:smithy-model`) to parse models — do not write a custom parser.
- Generated response templates are starting points, not finished stubs. Document prominently (in generated comments and in the user guide) that human review is required before the templates are production-ready.
- Protocol detection: prefer reading the `@protocols` trait on the service shape; fall back to inspecting operation input shapes if the trait is absent.
- This ticket covers the stub generation agent only. The interaction capture agent, test assertion agent, and fault scenario generator are separate future deliverables not in Phase 3 scope.
