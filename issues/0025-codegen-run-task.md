# Add a Gradle `run` task to cloudmock-codegen

**Phase:** 3
**Type:** dx

## Summary

Today the codegen is invoked by building the Shadow fat JAR and running it with `java -jar`. For a stub author working
inside the monorepo that is two steps and requires knowing the JAR path under `build/libs`. Apply the Gradle
`application` plugin to `cloudmock-codegen` so the tool can be run in one step via `./gradlew :cloudmock-codegen:run`,
while keeping the fat JAR for distribution and CI. This is an additive DX change — no change to the codegen logic or
its CLI flags.

## Acceptance criteria

- [ ] The `application` plugin is applied to `cloudmock-codegen` with `mainClass = 'io.cloudmock.codegen.Main'`
- [ ] The Shadow fat JAR (`./gradlew :cloudmock-codegen:shadowJar`) still builds and runs exactly as before — the two plugins coexist
- [ ] `./gradlew :cloudmock-codegen:run --args="--model <path-or-url> --output <dir>"` generates a module identical to the `java -jar` path for the same inputs
- [ ] The `run` task's working directory is pinned to the repo root (`run { workingDir = rootProject.projectDir }`) so relative `--model` / `--output` paths resolve from the repo root, consistent with the `java -jar` invocation
- [ ] The publishing configuration in the root `build.gradle` is unaffected — `cloudmock-codegen` still publishes the shadow JAR (the isolation/publish checks pass)
- [ ] `docs/codegen.md` documents `run` as the primary in-repo workflow and retains `java -jar` as the standalone/distribution path
- [ ] The `CLAUDE.md` Standard commands block lists the `run` invocation

## Dependencies

- #0012 (the codegen tool and its CLI flag parsing must exist — done)

## Notes

- **Working-directory footgun:** without pinning `workingDir`, the `run` task executes with the working directory set
  to the `cloudmock-codegen/` subproject, whereas `LocalModelResolver` resolves relative paths against the JVM's cwd.
  A relative `--model models/x.smithy` would then resolve under `cloudmock-codegen/` instead of the repo root. Pinning
  `workingDir = rootProject.projectDir` makes `run` and `java -jar` behave identically from the repo root.
- The Shadow plugin integrates with the `application` plugin (it contributes `runShadow`/`installShadowDist`); applying
  both is supported and common. No conflict with the existing `shadowJar` configuration is expected.
- `--args` quoting stays slightly verbose (`--args="--model x --output y"`); that is inherent to Gradle's `JavaExec`
  argument passing and is not something this issue can remove.
- Keep both invocation paths documented. The fat JAR remains the artifact for users outside the monorepo and for CI;
  `run` is the convenience path for authors developing modules inside the repo.
- This is unrelated to the SDK v1 work — develop it on its own branch off `main` (e.g. `feature/codegen-run-task`).
