package io.cloudmock.cli.command;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudmock.cli.http.ApiClient;
import io.cloudmock.cli.http.CloudMockUnavailableException;
import io.cloudmock.cli.util.Printer;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "status", description = "Show running instance info: port, uptime, and loaded modules")
public class StatusCommand implements Callable<Integer> {

    @ParentCommand
    CliContext ctx;

    @Override
    public Integer call() {
        ApiClient api = new ApiClient(ctx.apiBaseUrl());
        try {
            JsonNode status = api.getStatus();
            Printer.kv("mock port", status.path("port").asText());
            Printer.kv("api port", status.path("apiPort").asText());
            Printer.kv("started at", status.path("startedAt").asText());
            Printer.kv("uptime", status.path("uptime").asText());
            System.out.println();
            Printer.header("Modules");
            JsonNode modules = status.path("modules");
            if (modules.isEmpty()) {
                System.out.println("  (none)");
            } else {
                for (JsonNode module : modules) {
                    int stubCount = module.path("stubs").size();
                    System.out.printf("  %-20s %d stub(s)%n",
                            module.path("id").asText(), stubCount);
                }
            }
            return 0;
        } catch (CloudMockUnavailableException e) {
            Printer.unavailable(ctx.apiBaseUrl());
            return 1;
        }
    }
}
