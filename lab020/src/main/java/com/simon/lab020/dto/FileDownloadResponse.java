package com.simon.lab020.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;

/**
 * Response DTO for file download operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadResponse {

    /**
     * Document ID
     */
    private String documentId;

    /**
     * Original filename
     */
    private String originalFilename;

    /**
     * File size in bytes
     */
    private Long fileSize;

    /**
     * Content type of the file
     */
    private String contentType;

    /**
     * File resource for streaming
     */
    private Resource fileResource;

    /**
     * Upload timestamp
     */
    private LocalDateTime uploadDate;

    /**
     * Download timestamp
     */
    private LocalDateTime downloadDate;

    /**
     * File hash for integrity verification
     */
    private String fileHash;

    /**
     * Success indicator
     */
    private boolean success;

    /**
     * Status message
     */
    private String message;
}