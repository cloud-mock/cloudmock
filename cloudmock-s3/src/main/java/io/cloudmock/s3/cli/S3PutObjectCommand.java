package io.cloudmock.s3.cli;

import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "put-object", description = "Upload an object to an S3 bucket")
public class S3PutObjectCommand implements Callable<Integer> {

    @ParentCommand
    private CliContext ctx;

    @Option(names = {"--bucket", "-b"}, required = true, description = "Bucket name")
    String bucket;

    @Option(names = {"--key", "-k"}, required = true, description = "Object key")
    String key;

    @Option(names = {"--body"}, required = true, description = "Object content")
    String body;

    @Override
    public Integer call() {
        try {
            S3Http.put(ctx, "/" + bucket + "/" + key, body);
            CliOutput.ok("Uploaded: s3://" + bucket + "/" + key);
            return 0;
        } catch (CliConnectionException e) {
            CliOutput.unavailable(ctx.mockBaseUrl());
            return 1;
        }
    }
}
