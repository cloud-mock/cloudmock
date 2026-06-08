package io.cloudmock.sqs.cli;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list-queues", description = "List SQS queues")
public class SqsListQueuesCommand implements Callable<Integer> {

    @ParentCommand
    private CliContext ctx;

    @Override
    public Integer call() {
        try {
            JsonNode resp = SqsHttp.call(ctx, "ListQueues", "{}");
            List<String> urls = new ArrayList<>();
            for (JsonNode url : resp.path("QueueUrls")) {
                urls.add(url.asText());
            }
            CliOutput.header("Queues");
            CliOutput.list(urls);
            return 0;
        } catch (CliConnectionException e) {
            CliOutput.unavailable(ctx.mockBaseUrl());
            return 1;
        }
    }
}
