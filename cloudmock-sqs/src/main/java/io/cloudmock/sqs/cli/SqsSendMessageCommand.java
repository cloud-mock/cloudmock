package io.cloudmock.sqs.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "send-message", description = "Send a message to an SQS queue")
public class SqsSendMessageCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ParentCommand
    private CliContext ctx;

    @Option(names = {"--queue", "-q"}, required = true, description = "Queue name")
    String queue;

    @Option(names = {"--body", "-b"}, required = true, description = "Message body")
    String body;

    @Override
    public Integer call() {
        try {
            String queueUrl = ctx.mockBaseUrl() + "/000000000000/" + queue;
            ObjectNode req = MAPPER.createObjectNode();
            req.put("QueueUrl", queueUrl);
            req.put("MessageBody", body);
            JsonNode resp = SqsHttp.call(ctx, "SendMessage", MAPPER.writeValueAsString(req));
            CliOutput.kv("MessageId", resp.path("MessageId").asText());
            CliOutput.kv("MD5", resp.path("MD5OfMessageBody").asText());
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
