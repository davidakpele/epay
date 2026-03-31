package pesco.deposit_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ErrorHandler {
    private String message;
    private int statusCode;
    private String details;

    public ErrorHandler() {
    }

    public ErrorHandler(String message, int statusCode, String details) {
        this.message = message;
        this.statusCode = statusCode;
        this.details = details;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }


}