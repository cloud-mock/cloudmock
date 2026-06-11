# Fix standalone mode service loading: rename --modules to --services and fix default behaviour

**Type:** core

## Summary

Two related fixes needed in standalone mode. First, the `--modules` flag and `CLOUDMOCK_MODULES`
environment variable should be renamed to `--services` and `CLOUDMOCK_SERVICES` to stay consistent
with the programmatic API method `withService`. Second, the default behaviour is wrong — standalone
mode currently loads all bundled services unless a subset is specified. Fix it so no services load
unless explicitly declared, consistent with the core design principle.

## Correct behaviour

- No `--services` flag — CloudMock starts but no services are loaded
- `--services=sqs` — only SQS loads
- `--services=sqs,secretsmanager` — only those two load

## Acceptance criteria

- [ ] `--modules` renamed to `--services` throughout standalone mode
- [ ] `CLOUDMOCK_MODULES` renamed to `CLOUDMOCK_SERVICES`
- [ ] Standalone mode starts with no services loaded when `--services` is not specified
- [ ] Services load only when explicitly listed via `--services` or `CLOUDMOCK_SERVICES`
- [ ] Starting with no services logs a clear warning so the developer knows nothing is active
- [ ] The warning is actionable — tells the developer exactly how to enable services
- [ ] Documentation and CLAUDE.md updated to reflect both changes

## Notes

- `--services` is consistent with the programmatic API method `withService` — same vocabulary
  across the CLI and programmatic interfaces.
- This aligns standalone mode with the embedded mode principle — in embedded mode only
  modules on the classpath load, in standalone mode only explicitly declared services load.
  Same principle, different mechanism.
