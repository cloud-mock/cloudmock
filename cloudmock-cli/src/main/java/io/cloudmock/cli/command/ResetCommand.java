package io.cloudmock.cli.command;

import io.cloudmock.cli.http.ApiClient;
import io.cloudmock.cli.http.CloudMockUnavailableException;
import io.cloudmock.cli.util.Printer;
import io.cloudmock.core.spi.cli.CliContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "reset", description = "Clear all state, or a single service with --service")
public class ResetCommand implements Callable<Integer> {

    @ParentCommand
    CliContext ctx;

    @Option(names = {"--service", "-s"}, description = "Service ID to reset (e.g. sqs, s3)")
    String service;

    @Override
    public Integer call() {
        ApiClient api = new ApiClient(ctx.apiBaseUrl());
        try {
            api.reset(service);
            if (service != null && !service.isBlank()) {
                Printer.ok("Reset: " + service);
            } else {
                Printer.ok("Reset: all services");
            }
            return 0;
        } catch (CloudMockUnavailableException e) {
            Printer.unavailable(ctx.apiBaseUrl());
            return 1;
        }
    }
}
