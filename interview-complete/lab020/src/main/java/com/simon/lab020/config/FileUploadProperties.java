package com.simon.lab020.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

/**
 * Configuration properties for file upload
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
@Validated
public class FileUploadProperties {

    /**
     * Default chunk size for file uploads
     */
    @NotBlank(message = "Chunk size is required")
    private String chunkSize = "10MB";

    /**
     * Session timeout for multi-part uploads
     */
    @NotBlank(message = "Session timeout is required")
    private String sessionTimeout = "24h";

    /**
     * Maximum concurrent chunks per upload session
     */
    @Positive(message = "Max concurrent chunks must be positive")
    private int maxConcurrentChunks = 5;

    /**
     * Get chunk size in bytes
     */
    public long getChunkSizeInBytes() {
        return parseSize(chunkSize);
    }

    /**
     * Get session timeout as Duration
     */
    public Duration getSessionTimeoutDuration() {
        return parseDuration(sessionTimeout);
    }

    /**
     * Parse size string to bytes (e.g., "10MB" -> 10485760)
     */
    private long parseSize(String size) {
        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException("Size cannot be null or empty");
        }
        
        size = size.trim().toUpperCase();
        long multiplier = 1;
        
        if (size.endsWith("KB")) {
            multiplier = 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("MB")) {
            multiplier = 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("GB")) {
            multiplier = 1024 * 1024 * 1024;
            size = size.substring(0, size.length() - 2);
        } else if (size.endsWith("B")) {
            size = size.substring(0, size.length() - 1);
        }
        
        try {
            return Long.parseLong(size.trim()) * multiplier;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid size format: " + size, e);
        }
    }

    /**
     * Parse duration string (e.g., "24h" -> Duration.ofHours(24))
     */
    private Duration parseDuration(String duration) {
        if (duration == null || duration.trim().isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be null or empty");
        }
        
        duration = duration.trim().toLowerCase();
        
        if (duration.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(duration.substring(0, duration.length() - 1)));
        } else if (duration.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(duration.substring(0, duration.length() - 1)));
        } else if (duration.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(duration.substring(0, duration.length() - 1)));
        } else if (duration.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(duration.substring(0, duration.length() - 1)));
        } else {
            // Assume seconds if no unit specified
            return Duration.ofSeconds(Long.parseLong(duration));
        }
    }
}