package com.simon.lab020.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for file storage
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
@Validated
public class FileStorageProperties {

    /**
     * Base path for file storage
     */
    @NotBlank(message = "Base path is required")
    private String basePath = "uploads/";

    /**
     * Temporary path for file processing
     */
    @NotBlank(message = "Temp path is required")
    private String tempPath = "temp/";

    /**
     * Path for storing file chunks
     */
    @NotBlank(message = "Chunks path is required")
    private String chunksPath = "chunks/";

    // Manual getters since Lombok is not working properly
    public String getBasePath() {
        return basePath;
    }

    public String getTempPath() {
        return tempPath;
    }

    public String getChunksPath() {
        return chunksPath;
    }
}