package io.cloudmock.cli.http;

public class CloudMockUnavailableException extends Exception {

    private final String baseUrl;

    public CloudMockUnavailableException(String baseUrl) {
        super("CloudMock is not reachable at " + baseUrl);
        this.baseUrl = baseUrl;
    }

    public CloudMockUnavailableException(String baseUrl, Throwable cause) {
        super("CloudMock is not reachable at " + baseUrl + ": " + cause.getMessage(), cause);
        this.baseUrl = baseUrl;
    }

    public String baseUrl() {
        return baseUrl;
    }
}
