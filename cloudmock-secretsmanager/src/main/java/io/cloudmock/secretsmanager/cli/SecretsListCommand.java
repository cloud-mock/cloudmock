package io.cloudmock.secretsmanager.cli;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "list", description = "List secrets")
public class SecretsListCommand implements Callable<Integer> {

    @ParentCommand
    private CliContext ctx;

    @Override
    public Integer call() {
        try {
            JsonNode resp = SecretsHttp.call(ctx, "ListSecrets", "{}");
            List<String> names = new ArrayList<>();
            for (JsonNode secret : resp.path("SecretList")) {
                names.add(secret.path("Name").asText());
            }
            CliOutput.header("Secrets");
            CliOutput.list(names);
            return 0;
        } catch (CliConnectionException e) {
            CliOutput.unavailable(ctx.mockBaseUrl());
            return 1;
        }
    }
}
