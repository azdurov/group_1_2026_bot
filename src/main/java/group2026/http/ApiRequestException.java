package group2026.http;

public class ApiRequestException extends RuntimeException {

    private final int statusCode;

    public ApiRequestException(String message) {
        this(message, null, -1);
    }

    public ApiRequestException(String message, Throwable cause) {
        this(message, cause, -1);
    }

    public ApiRequestException(String message, int statusCode) {
        this(message, null, statusCode);
    }

    private ApiRequestException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }
}