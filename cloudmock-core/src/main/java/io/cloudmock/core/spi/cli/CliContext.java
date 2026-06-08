package io.cloudmock.core.spi.cli;

/**
 * Connection settings for a running CloudMock instance.
 *
 * <p>Implemented by the CLI root command. Module command classes declare
 * {@code @ParentCommand CliContext ctx} to access host and port values that were
 * parsed from the command line without depending on the CLI entry-point class.
 */
public interface CliContext {

    String host();

    int mockPort();

    int apiPort();

    default String mockBaseUrl() {
        return "http://" + host() + ":" + mockPort();
    }

    default String apiBaseUrl() {
        return "http://" + host() + ":" + apiPort();
    }
}
