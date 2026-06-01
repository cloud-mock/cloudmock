# Set up Gradle multi-project monorepo

**Phase:** 1
**Type:** setup

## Summary

Establish the Gradle multi-project build that all subsequent work depends on. The build must enforce strict module isolation — no `cloudmock-*` module may take a compile or runtime dependency on another — and CI must fail if this constraint is violated. Java 17 is the baseline for all subprojects.

## Acceptance criteria

- [ ] `settings.gradle` declares the initial subprojects: `cloudmock-core`, `cloudmock-sqs`, `cloudmock-secretsmanager`
- [ ] Root `build.gradle` sets Java 17 source and target compatibility across all subprojects
- [ ] A build-level constraint prevents any `cloudmock-*` module from declaring a compile or runtime dependency on another `cloudmock-*` module; `./gradlew build` fails if this constraint is violated
- [ ] `./gradlew build` from the repository root compiles all subprojects successfully
- [ ] `./gradlew test` runs all subproject test suites
- [ ] The `maven-publish` plugin is applied to all subprojects with coordinates following `io.cloudmock:cloudmock-<name>`
- [ ] `.gitignore` is updated to exclude `.gradle/` and all `build/` directories
- [ ] Subprojects that have no code yet contain a minimal `build.gradle` so the directory structure is established

## Dependencies

none

## Notes

- Prefer Gradle version catalogs (`libs.versions.toml`) for dependency version management — all WireMock, AWS SDK, and JUnit versions live there.
- The inter-module isolation constraint is the most important build invariant in the project. An easy enforcement mechanism is a root-level `subprojects { configurations.all { ... } }` block that walks resolution and fails on any `io.cloudmock` transitive; document the rule clearly in the build file.
- Subproject directories for Phase 2/3 modules can be stubbed out now so the structure is visible, even if they contain only a `build.gradle` with no source.