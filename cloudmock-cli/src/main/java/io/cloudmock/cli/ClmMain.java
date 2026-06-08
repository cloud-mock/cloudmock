package io.cloudmock.cli;

import io.cloudmock.cli.command.ResetCommand;
import io.cloudmock.cli.command.StatusCommand;
import io.cloudmock.core.spi.CloudMockCliPlugin;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

import java.util.ServiceLoader;

@Command(
    name = "clm",
    mixinStandardHelpOptions = true,
    version = "cloudmock-cli 0.1.0",
    description = "CLI for interacting with a running CloudMock instance",
    subcommands = {
        HelpCommand.class,
        StatusCommand.class,
        ResetCommand.class
    }
)
public class ClmMain implements CliContext {

    @Option(names = {"--host"},
            scope = ScopeType.INHERIT,
            defaultValue = "${CLOUDMOCK_HOST:-localhost}",
            description = "CloudMock hostname (env: CLOUDMOCK_HOST, default: ${DEFAULT-VALUE})")
    private String host;

    @Option(names = {"--port"},
            scope = ScopeType.INHERIT,
            defaultValue = "${CLOUDMOCK_PORT:-4566}",
            description = "Mock port (env: CLOUDMOCK_PORT, default: ${DEFAULT-VALUE})")
    private int port;

    @Option(names = {"--api-port"},
            scope = ScopeType.INHERIT,
            defaultValue = "${CLOUDMOCK_API_PORT:-4567}",
            description = "REST API port (env: CLOUDMOCK_API_PORT, default: ${DEFAULT-VALUE})")
    private int apiPort;

    @Override public String host()     { return host; }
    @Override public int mockPort()    { return port; }
    @Override public int apiPort()     { return apiPort; }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new ClmMain());

        ServiceLoader.load(CloudMockCliPlugin.class).forEach(plugin ->
            cmd.addSubcommand(plugin.cliCommand())
        );

        System.exit(cmd.execute(args));
    }
}
