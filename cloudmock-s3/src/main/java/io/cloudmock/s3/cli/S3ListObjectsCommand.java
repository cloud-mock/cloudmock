package io.cloudmock.s3.cli;

import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list-objects", description = "List objects in an S3 bucket")
public class S3ListObjectsCommand implements Callable<Integer> {

    @ParentCommand
    private CliContext ctx;

    @Option(names = {"--bucket", "-b"}, required = true, description = "Bucket name")
    String bucket;

    @Override
    public Integer call() {
        try {
            String xml = S3Http.get(ctx, "/" + bucket + "?list-type=2");
            List<String> keys = XmlUtil.extractAll(xml, "//Contents/Key");
            CliOutput.header("Objects in " + bucket);
            CliOutput.list(keys);
            return 0;
        } catch (CliConnectionException e) {
            CliOutput.unavailable(ctx.mockBaseUrl());
            return 1;
        }
    }
}
