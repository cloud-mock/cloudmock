package io.cloudmock.secretsmanager.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "put", description = "Create or update a secret value")
public class SecretsPutCommand implements Callable<Integer> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ParentCommand
    private CliContext ctx;

    @Option(names = {"--name", "-n"}, required = true, description = "Secret name")
    String name;

    @Option(names = {"--value", "-v"}, required = true, description = "Secret value")
    String value;

    @Override
    public Integer call() {
        try {
            ObjectNode req = MAPPER.createObjectNode();
            req.put("SecretId", name);
            req.put("SecretString", value);
            JsonNode resp = SecretsHttp.call(ctx, "PutSecretValue", MAPPER.writeValueAsString(req));
            CliOutput.kv("Name", resp.path("Name").asText());
            CliOutput.kv("ARN", resp.path("ARN").asText());
            CliOutput.kv("VersionId", resp.path("VersionId").asText());
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
