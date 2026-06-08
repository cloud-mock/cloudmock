package io.cloudmock.sqs.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "purge-queue", description = "Purge all messages from an SQS queue")
public class SqsPurgeQueueCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ParentCommand
    private CliContext ctx;

    @Option(names = {"--queue", "-q"}, required = true, description = "Queue name")
    String queue;

    @Override
    public Integer call() {
        try {
            String queueUrl = ctx.mockBaseUrl() + "/000000000000/" + queue;
            ObjectNode req = MAPPER.createObjectNode();
            req.put("QueueUrl", queueUrl);
            SqsHttp.call(ctx, "PurgeQueue", MAPPER.writeValueAsString(req));
            CliOutput.ok("Purged: " + queue);
            return 0;
        } catch (CliConnectionException e) {
            CliOutput.unavailable(ctx.mockBaseUrl());
            return 1;
        } catch (Exception e) {
            CliOutput.error(e.getMessage());
            return 1;
        }
    }
}
