package io.cloudmock.secretsmanager;

import io.cloudmock.core.spi.CloudMockCliPlugin;
import io.cloudmock.secretsmanager.cli.SecretsCommand;

public class CloudMockSecretsManagerCliPlugin implements CloudMockCliPlugin {

    @Override
    public String serviceId() {
        return "secretsmanager";
    }

    @Override
    public Object cliCommand() {
        return new SecretsCommand();
    }
}
