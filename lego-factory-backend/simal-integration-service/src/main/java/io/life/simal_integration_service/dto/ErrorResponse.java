package io.life.simal_integration_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Standardized error response DTO for all API endpoints.
 * Provides consistent error information across all services.
 */
public class ErrorResponse {
    
    @JsonProperty("status")
    private int status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("details")
    private String details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message, String error, String path) {
        this();
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
    }

    public ErrorResponse(int status, String message, String error, String path, String details) {
        this(status, message, error, path);
        this.details = details;
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
