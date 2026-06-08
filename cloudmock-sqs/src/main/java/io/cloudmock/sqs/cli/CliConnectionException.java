package io.cloudmock.sqs.cli;

class CliConnectionException extends Exception {

    CliConnectionException(String baseUrl) {
        super("CloudMock is not reachable at " + baseUrl);
    }

    CliConnectionException(String baseUrl, Throwable cause) {
        super("CloudMock is not reachable at " + baseUrl + ": " + cause.getMessage(), cause);
    }
}
