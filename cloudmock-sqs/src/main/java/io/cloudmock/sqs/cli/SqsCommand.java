package io.cloudmock.sqs.cli;

import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.ParentCommand;

@Command(
    name = "sqs",
    description = "Amazon SQS commands",
    subcommands = {
        HelpCommand.class,
        SqsListQueuesCommand.class,
        SqsSendMessageCommand.class,
        SqsReceiveMessageCommand.class,
        SqsPurgeQueueCommand.class
    }
)
public class SqsCommand implements CliContext {

    @ParentCommand
    private CliContext parent;

    @Override public String host()     { return parent.host(); }
    @Override public int mockPort()    { return parent.mockPort(); }
    @Override public int apiPort()     { return parent.apiPort(); }
}
