package com.simon.lab020.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for chunk upload operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkUploadRequest {

    /**
     * Upload session ID
     */
    @NotBlank(message = "Session ID is required")
    private String sessionId;

    /**
     * Chunk number (0-based)
     */
    @NotNull(message = "Chunk number is required")
    @Min(value = 0, message = "Chunk number must be non-negative")
    private Integer chunkNumber;

    /**
     * Total number of chunks
     */
    @NotNull(message = "Total chunks is required")
    @Min(value = 1, message = "Total chunks must be at least 1")
    private Integer totalChunks;

    /**
     * Chunk file data
     */
    @NotNull(message = "Chunk file is required")
    private MultipartFile chunkFile;

    /**
     * Hash of the chunk for integrity verification
     */
    @NotBlank(message = "Chunk hash is required")
    private String chunkHash;

    /**
     * Original filename (for the first chunk)
     */
    private String originalFilename;

    /**
     * Total file size (for the first chunk)
     */
    @Min(value = 1, message = "Total file size must be positive")
    private Long totalFileSize;

    /**
     * Content type (for the first chunk)
     */
    private String contentType;

    /**
     * Overall file hash (for integrity verification)
     */
    private String fileHash;
}