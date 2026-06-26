package pesco.example.withdraw_service.dtos;

import lombok.Data;
import java.time.Instant;
import java.util.Map;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Instant timestamp;
    private Map<String, Object> metadata;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data, Instant timestamp, Map<String,Object> metadata) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        response.timestamp = Instant.now();
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, Object> metadata) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.timestamp = Instant.now();
        response.metadata = metadata;
        return response;
    }
}