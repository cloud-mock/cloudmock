package io.cloudmock.sqs;

import io.cloudmock.core.spi.CloudMockCliPlugin;
import io.cloudmock.sqs.cli.SqsCommand;

public class CloudMockSqsCliPlugin implements CloudMockCliPlugin {

    @Override
    public String serviceId() {
        return "sqs";
    }

    @Override
    public Object cliCommand() {
        return new SqsCommand();
    }
}
