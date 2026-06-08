package io.cloudmock.s3.cli;

import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "get-object", description = "Download an object from an S3 bucket")
public class S3GetObjectCommand implements Callable<Integer> {

    @ParentCommand
    private CliContext ctx;

    @Option(names = {"--bucket", "-b"}, required = true, description = "Bucket name")
    String bucket;

    @Option(names = {"--key", "-k"}, required = true, description = "Object key")
    String key;

    @Override
    public Integer call() {
        try {
            String content = S3Http.get(ctx, "/" + bucket + "/" + key);
            System.out.println(content);
            return 0;
        } catch (CliConnectionException e) {
            CliOutput.unavailable(ctx.mockBaseUrl());
            return 1;
        }
    }
}
