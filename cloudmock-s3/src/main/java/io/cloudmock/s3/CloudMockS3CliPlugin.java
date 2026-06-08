package io.cloudmock.s3;

import io.cloudmock.core.spi.CloudMockCliPlugin;
import io.cloudmock.s3.cli.S3Command;

public class CloudMockS3CliPlugin implements CloudMockCliPlugin {

    @Override
    public String serviceId() {
        return "s3";
    }

    @Override
    public Object cliCommand() {
        return new S3Command();
    }
}
