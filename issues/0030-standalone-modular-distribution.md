# Standalone modular distribution (plugin directory)

**Phase:** 3
**Type:** feature

## Summary

The standalone server currently ships as a single ~18 MB fat JAR with **every** service module baked in
(`cloudmock-sqs`, `cloudmock-sns`, `cloudmock-secretsmanager`, `cloudmock-s3`). The `--modules` flag added in #0021
only filters which of those already-bundled modules are *registered* at runtime — it does not change what is downloaded.

This contradicts the intended model: a developer should download a small **server runtime** (launcher + core) once, then
add only the **module jars** they actually want. Core is required (without it the modules are worthless); modules are
optional add-ons discovered at runtime. Each module jar is tiny (~2 KB — just the `CloudMockService` class and its
`META-INF/services` registration), so this split is cheap and natural.

Decision already taken (see #0021 discussion): modules are attached via a **plugin directory** that the launcher scans,
rather than manual classpath assembly. This keeps the clean `java -jar` invocation and a LocalStack-like "drop a jar in
the folder" UX.

## Acceptance criteria

- [ ] `cloudmock-standalone` no longer bundles service modules — the shadow JAR contains only the launcher and
  `cloudmock-core` (with its shaded WireMock/Jetty). Service module `runtimeOnly` dependencies are removed.
- [ ] The launcher loads module jars from a plugin directory: default `./modules`, overridable with `--modules-dir=<path>`
  or `CLOUDMOCK_MODULES_DIR`. Jars in that directory are loaded via a `URLClassLoader` whose parent is the app
  classloader (so the `io.cloudmock.core.spi` types resolve to the same classes core uses).
- [ ] Module discovery still uses `ServiceLoader.load(CloudMockService.class, <plugin-classloader>)` — the SPI contract
  is unchanged and no module code needs to change.
- [ ] The existing `--modules` enable-filter still composes: it selects a subset of the modules present in the directory.
- [ ] An explicitly provided `--modules-dir` that does not exist fails fast with a clear message; a missing **default**
  `./modules` directory (or an empty one) is not fatal — the server starts and warns that no services will be served.
- [ ] Startup log reports the resolved plugin directory and the modules loaded from it.
- [ ] Module isolation: the standalone server depends only on `cloudmock-core`. The module-isolation exemption added for
  `cloudmock-standalone` in the root `build.gradle` can be removed (test-only task dependencies on module jars do not
  count as compile/runtime dependencies).
- [ ] Integration tests are updated: module jars are copied into a temp directory and the server is launched with
  `--modules-dir`. At least one test proves a module is served only when its jar is present, and that `--modules`
  filters among present modules.
- [ ] `docs/standalone.md`, `README.md`, and CLAUDE.md document the new distribution model: download the server jar,
  download the module jars you want, point `--modules-dir` at them.

## Dependencies

- #0021 (standalone mode — provides the launcher, `PortResolver`, `ModuleSelector`, and the `--modules` filter this builds on)

## Notes

- Implementation deferred — this is the follow-up to the distribution-model discussion on #0021. The fat "batteries
  included" jar could optionally be kept as a second convenience artifact, but the primary direction is the thin server +
  plugin directory.
- Modules already declare `compileOnly` on `cloudmock-core`, so they are not self-contained executables — they rely on
  the server providing core at runtime. The plugin `URLClassLoader` parent must therefore be the app classloader (which
  contains core), not a fresh/null parent.
- The `--modules-dir` vs `--modules` distinction should be explained clearly in the docs: `--modules-dir` controls what
  is *available* (which jars are on the classpath); `--modules` optionally narrows what is *enabled* among those.
- Watch the classloader/shaded-types interaction: WireMock and Jetty are relocated to `io.cloudmock.shaded.*` inside
  core and modules never reference them, so the plugin loader only needs the unshaded `io.cloudmock.core.spi` types from
  its parent. No relocation concerns for module jars.
