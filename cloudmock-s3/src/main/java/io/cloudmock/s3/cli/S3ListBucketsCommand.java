package io.cloudmock.s3.cli;

import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list-buckets", description = "List S3 buckets")
public class S3ListBucketsCommand implements Callable<Integer> {

    @ParentCommand
    private CliContext ctx;

    @Override
    public Integer call() {
        try {
            String xml = S3Http.get(ctx, "/?x-id=ListBuckets");
            List<String> names = XmlUtil.extractAll(xml, "//Bucket/Name");
            CliOutput.header("Buckets");
            CliOutput.list(names);
            return 0;
        } catch (CliConnectionException e) {
            CliOutput.unavailable(ctx.mockBaseUrl());
            return 1;
        }
    }
}
