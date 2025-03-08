package api;

public class ErrorResponse {
    int errorCode;
    String url;
    String message;

    public ErrorResponse(int errorCode, String url, String message) {
        this.errorCode = errorCode;
        this.url = url;
        this.message = message;
    }
}
