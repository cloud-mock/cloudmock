package io.cloudmock.secretsmanager.cli;

import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.ParentCommand;

@Command(
    name = "secrets",
    description = "AWS Secrets Manager commands",
    subcommands = {
        HelpCommand.class,
        SecretsListCommand.class,
        SecretsGetCommand.class,
        SecretsPutCommand.class
    }
)
public class SecretsCommand implements CliContext {

    @ParentCommand
    private CliContext parent;

    @Override public String host()     { return parent.host(); }
    @Override public int mockPort()    { return parent.mockPort(); }
    @Override public int apiPort()     { return parent.apiPort(); }
}
