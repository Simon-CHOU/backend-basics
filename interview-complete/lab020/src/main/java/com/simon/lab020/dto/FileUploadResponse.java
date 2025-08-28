package com.simon.lab020.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for file upload operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * Unique document ID for the uploaded file
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
     * Upload timestamp
     */
    private LocalDateTime uploadDate;

    /**
     * File hash for integrity verification
     */
    private String fileHash;

    /**
     * Upload status message
     */
    private String message;

    /**
     * Success indicator
     */
    private boolean success;
}