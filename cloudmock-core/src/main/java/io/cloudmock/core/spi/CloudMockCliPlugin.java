package io.cloudmock.core.spi;

/**
 * Optional CLI companion to {@link CloudMockService}.
 *
 * <p>Modules that want to expose {@code clm <service> ...} subcommands implement this interface
 * and register via {@code META-INF/services/io.cloudmock.core.spi.CloudMockCliPlugin}.
 *
 * <p>The returned object must be annotated with {@code @picocli.CommandLine.Command}. The CLI
 * launcher adds it as a subcommand at startup. Connection settings (host, port) are available
 * via {@code @picocli.CommandLine.ParentCommand CliContext ctx} injection in any command class
 * within the hierarchy.
 *
 * <p>Modules that have no CLI interaction simply do not implement this interface.
 */
public interface CloudMockCliPlugin {

    /** Must match the {@link CloudMockService#serviceId()} of the same module. */
    String serviceId();

    /**
     * Returns the top-level picocli {@code @Command}-annotated object for this service.
     * Called once at CLI startup.
     */
    Object cliCommand();
}
