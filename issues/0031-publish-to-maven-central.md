# Publish the libraries to Maven Central under io.github.cloud-mock

**Phase:** 3
**Type:** feature

## Summary

CloudMock is only consumable today via `./gradlew publishToMavenLocal`. The artifacts carry the placeholder group
`io.cloudmock` and version `0.1.0-SNAPSHOT`, and the publication has no POM metadata, no signing, and no remote
repository target. To let real consumers add CloudMock with a single Gradle/Maven coordinate, the libraries must be
published to **Maven Central**.

Central requires an owned, verifiable namespace. We will use the `io.github.cloud-mock` group, which is granted
automatically to the owner of the `cloud-mock` GitHub organization via the GitHub-vanity-domain verification path —
no DNS TXT record needed. The current `io.cloudmock` group is changed to `io.github.cloud-mock` across the build.

Each service/tooling module is published as its **own artifact** (one Maven coordinate per module), so consumers depend
only on the services they use. The publishable set is the libraries consumers compile or run against:

- `io.github.cloud-mock:cloudmock-core`
- `io.github.cloud-mock:cloudmock-junit6`
- `io.github.cloud-mock:cloudmock-sqs`
- `io.github.cloud-mock:cloudmock-sns`
- `io.github.cloud-mock:cloudmock-secretsmanager`
- `io.github.cloud-mock:cloudmock-s3`
- `io.github.cloud-mock:cloudmock-dynamodb` *(when implemented)*
- `io.github.cloud-mock:cloudmock-lambda` *(when implemented)*
- `io.github.cloud-mock:cloudmock-sdk-v1` *(opt-in companion library for AWS SDK v1 consumers)*
- `io.github.cloud-mock:cloudmock-codegen` *(published as a runnable tool JAR, like standalone — downloaded and run with `java -jar`, not compiled against)*
- `io.github.cloud-mock:cloudmock-standalone`

`cloudmock-example` is **not** published (it is a demonstration app, not a library).

## Standalone ships with core

Unlike the **runtime** distribution model in #0030 (thin server + plugin directory of optional module jars), the
**published Maven artifact** for `cloudmock-standalone` must be self-bootstrapping: its shadow JAR bundles
`cloudmock-core` (and core's shaded WireMock/Jetty) so that `java -jar cloudmock-standalone.jar` works straight off
Central with no separate core download. This is consistent with #0030, which already states the standalone JAR contains
"only the launcher and `cloudmock-core`". Service modules remain optional and are still attached via the `--modules-dir`
plugin directory described in #0030 — Central simply hosts core-bundled-with-launcher plus each module jar separately.

## Acceptance criteria

- [ ] The project group is changed from `io.cloudmock` to `io.github.cloud-mock` in the root `build.gradle`; all
  internal references (the module-isolation checks in `build.gradle` that match on `group == 'io.cloudmock'`, docs, and
  any hardcoded coordinates) are updated accordingly.
- [ ] Every publishable module produces a Maven publication with a complete POM: `name`, `description`, project URL,
  SCM (`scm.url` / `scm.connection`), an OSI license (license name + URL), and at least one developer entry. Central
  rejects publications missing any of these.
- [ ] Each publication ships the three required artifacts: the primary JAR, a `-sources` JAR, and a `-javadoc` JAR.
  (`cloudmock-core` and `cloudmock-standalone` publish their shadow JAR as the primary artifact, consistent with the
  existing `components.shadow` handling.)
- [ ] All artifacts are PGP-signed (Gradle `signing` plugin). The signing key and passphrase are supplied via
  environment/Gradle properties (in-memory key for CI), never committed.
- [ ] A release (non-`SNAPSHOT`) version is published. The initial release is `0.1.0-beta.1` (a pre-release qualifier
  signalling the API is not yet stable; Central accepts it because it is not a `-SNAPSHOT`), graduating to `0.1.0` for
  the first stable release. Keep `-SNAPSHOT` only for ongoing development; document the snapshot/beta/release distinction.
  Note Maven version ordering: `0.1.0-beta.1` < `0.1.0`, so the stable release correctly supersedes the beta, and the
  numbered suffix allows further betas (`-beta.2`, …) before graduating.
- [ ] Publication targets the Central Publisher Portal (Sonatype) repository. Credentials come from environment/Gradle
  properties; no secrets in the repo.
- [ ] `cloudmock-standalone`'s published JAR bundles `cloudmock-core` and runs via `java -jar` with no extra core
  dependency (see "Standalone ships with core" above). Service modules are **not** bundled into it.
- [ ] `cloudmock-example` is excluded from publishing.
- [ ] A documented release procedure exists (manual `./gradlew publish` to the staging repo + close/release, or a
  CI release workflow). Reproducible from a clean checkout with only the documented secrets.
- [ ] `README.md`, `docs/`, and CLAUDE.md show the real `io.github.cloud-mock:<module>:<version>` coordinates for both
  Gradle and Maven, replacing any `publishToMavenLocal`-only guidance, and note that standalone is downloadable as a
  runnable JAR.

## Dependencies

- #0030 (standalone modular distribution) — defines the standalone bundle as launcher + core and the `--modules-dir`
  plugin model that the published distribution mirrors. This issue should land on top of, or in coordination with, #0030
  so the published standalone artifact matches the agreed distribution shape.

## Notes

- **Namespace choice:** `io.github.cloud-mock` is verifiable through GitHub org ownership, avoiding the DNS round-trip a
  custom domain (`io.cloudmock`) would require. If a custom domain is later acquired, the group can be migrated, but that
  is out of scope here.
- The existing publishing block already prefers `components.shadow` over `components.java` for shadowed modules — extend
  that block rather than replacing it. Sources/javadoc JAR wiring must coexist with the shadow publication.
- Signing should be conditional so that local `publishToMavenLocal` and ordinary `build` do not require a key; only the
  Central publish path enforces signing.
- Keep secrets out of the repo: signing key, signing passphrase, and Central credentials are injected at release time
  (CI secrets or `~/.gradle/gradle.properties` on a maintainer machine).
- Verify the module-isolation guard in `build.gradle` still functions after the group rename — its `dep.group ==`
  comparisons must be updated to `io.github.cloud-mock`, or the check silently stops enforcing isolation.
- Consider a Gradle convention/`subprojects` block for the shared POM/signing config so each module does not repeat it;
  list the non-published modules (`cloudmock-example`) explicitly as exclusions.