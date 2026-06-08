package io.cloudmock.s3.cli;

import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.ParentCommand;

@Command(
    name = "s3",
    description = "Amazon S3 commands",
    subcommands = {
        HelpCommand.class,
        S3ListBucketsCommand.class,
        S3ListObjectsCommand.class,
        S3PutObjectCommand.class,
        S3GetObjectCommand.class
    }
)
public class S3Command implements CliContext {

    @ParentCommand
    private CliContext parent;

    @Override public String host()     { return parent.host(); }
    @Override public int mockPort()    { return parent.mockPort(); }
    @Override public int apiPort()     { return parent.apiPort(); }
}
