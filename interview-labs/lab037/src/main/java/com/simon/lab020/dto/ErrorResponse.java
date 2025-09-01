package com.simon.lab020.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Error code
     */
    private String errorCode;

    /**
     * Error message
     */
    private String message;

    /**
     * Detailed error description
     */
    private String details;

    /**
     * HTTP status code
     */
    private int status;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;

    /**
     * Validation errors (for validation failures)
     */
    private List<ValidationError> validationErrors;

    /**
     * Request ID for tracking
     */
    private String requestId;

    /**
     * Nested class for validation errors
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}