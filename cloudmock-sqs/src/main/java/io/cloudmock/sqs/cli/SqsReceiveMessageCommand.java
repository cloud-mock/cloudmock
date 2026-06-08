package io.cloudmock.sqs.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "receive-message", description = "Receive a message from an SQS queue")
public class SqsReceiveMessageCommand implements Callable<Integer> {

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
            JsonNode resp = SqsHttp.call(ctx, "ReceiveMessage", MAPPER.writeValueAsString(req));
            JsonNode messages = resp.path("Messages");
            if (messages.isEmpty()) {
                CliOutput.ok("No messages");
            } else {
                for (JsonNode msg : messages) {
                    System.out.println("---");
                    CliOutput.kv("MessageId", msg.path("MessageId").asText());
                    CliOutput.kv("ReceiptHandle", msg.path("ReceiptHandle").asText());
                    CliOutput.kv("Body", msg.path("Body").asText());
                }
            }
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
