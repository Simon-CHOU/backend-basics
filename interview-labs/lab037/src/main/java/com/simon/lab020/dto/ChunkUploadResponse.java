package com.simon.lab020.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for chunk upload operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkUploadResponse {

    /**
     * Upload session ID
     */
    private String sessionId;

    /**
     * Chunk number that was uploaded
     */
    private Integer chunkNumber;

    /**
     * Total number of chunks expected
     */
    private Integer totalChunks;

    /**
     * Number of chunks uploaded so far
     */
    private Integer chunksUploaded;

    /**
     * Upload progress percentage (0-100)
     */
    @com.fasterxml.jackson.annotation.JsonProperty("progress")
    private Double progressPercentage;

    /**
     * Indicates if this was the last chunk and file is complete
     */
    private boolean completed;

    /**
     * Document ID (only available when upload is complete)
     */
    private String documentId;

    /**
     * Upload timestamp
     */
    private LocalDateTime uploadDate;

    /**
     * Status message
     */
    private String message;

    /**
     * Success indicator
     */
    private boolean success;

    /**
     * List of missing chunk numbers (if any)
     */
    private java.util.List<Integer> missingChunks;
}