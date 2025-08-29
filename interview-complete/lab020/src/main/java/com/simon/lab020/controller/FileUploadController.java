package com.simon.lab020.controller;

import com.simon.lab020.dto.*;
import com.simon.lab020.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * REST Controller for file upload operations
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * Upload a single file
     *
     * @param file the file to upload
     * @param request the HTTP request
     * @return upload response
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") @NotNull MultipartFile file,
            HttpServletRequest request) {
        
        try {
            log.info("Single file upload request: filename={}, size={} bytes", 
                    file.getOriginalFilename(), file.getSize());
            
            FileUploadResponse response = fileUploadService.uploadFile(file, request);
            
            if (response.isSuccess()) {
                log.info("File uploaded successfully: documentId={}", response.getDocumentId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("File upload failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            FileUploadResponse errorResponse = FileUploadResponse.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Initialize chunk upload session
     *
     * @param originalFilename the original filename
     * @param totalSize the total file size
     * @param totalChunks the total number of chunks
     * @param contentType the content type
     * @param fileHash the file hash
     * @param request the HTTP request
     * @return chunk upload response
     */
    @PostMapping("/upload/chunks/init")
    public ResponseEntity<ChunkUploadResponse> initializeChunkUpload(
            @RequestParam("originalFilename") @NotBlank String originalFilename,
            @RequestParam("totalSize") @Positive long totalSize,
            @RequestParam("totalChunks") @Positive int totalChunks,
            @RequestParam("contentType") @NotBlank String contentType,
            @RequestParam("fileHash") @NotBlank String fileHash,
            HttpServletRequest request) {
        
        try {
            log.info("Chunk upload initialization: filename={}, size={} bytes, chunks={}", 
                    originalFilename, totalSize, totalChunks);
            
            ChunkUploadResponse response = fileUploadService.initializeChunkUpload(
                    originalFilename, totalSize, totalChunks, contentType, fileHash, request);
            
            if (response.isSuccess()) {
                if (response.isCompleted()) {
                    log.info("File already exists: documentId={}", response.getDocumentId());
                } else {
                    log.info("Chunk upload session initialized: sessionId={}", response.getSessionId());
                }
                return ResponseEntity.ok(response);
            } else {
                log.warn("Chunk upload initialization failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Unexpected error during chunk upload initialization", e);
            ChunkUploadResponse errorResponse = ChunkUploadResponse.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Upload a file chunk
     *
     * @param sessionId the upload session ID
     * @param chunkNumber the chunk number
     * @param totalChunks the total number of chunks
     * @param chunkFile the chunk file data
     * @param chunkHash the chunk hash
     * @param originalFilename the original filename
     * @param totalFileSize the total file size
     * @param contentType the content type
     * @param fileHash the file hash
     * @param request the HTTP request
     * @return chunk upload response
     */
    @PostMapping(value = "/upload/chunks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChunkUploadResponse> uploadChunk(
            @RequestParam("sessionId") @NotBlank String sessionId,
            @RequestParam("chunkNumber") @Min(0) int chunkNumber,
            @RequestParam("totalChunks") @Positive int totalChunks,
            @RequestParam("chunkFile") @NotNull MultipartFile chunkFile,
            @RequestParam("chunkHash") @NotBlank String chunkHash,
            @RequestParam("originalFilename") @NotBlank String originalFilename,
            @RequestParam("totalFileSize") @Positive long totalFileSize,
            @RequestParam("contentType") @NotBlank String contentType,
            @RequestParam("fileHash") @NotBlank String fileHash,
            HttpServletRequest request) {
        
        try {
            log.debug("Chunk upload: sessionId={}, chunk={}/{}, size={} bytes", 
                    sessionId, chunkNumber, totalChunks, chunkFile.getSize());
            
            // Create chunk upload request
            ChunkUploadRequest chunkRequest = ChunkUploadRequest.builder()
                    .sessionId(sessionId)
                    .chunkNumber(chunkNumber)
                    .totalChunks(totalChunks)
                    .chunkFile(chunkFile)
                    .chunkHash(chunkHash)
                    .originalFilename(originalFilename)
                    .totalFileSize(totalFileSize)
                    .contentType(contentType)
                    .fileHash(fileHash)
                    .build();
            
            ChunkUploadResponse response = fileUploadService.uploadChunk(chunkRequest, request);
            
            if (response.isSuccess()) {
                if (response.isCompleted()) {
                    log.info("Chunk upload completed: sessionId={}, documentId={}", 
                            sessionId, response.getDocumentId());
                } else {
                    log.debug("Chunk uploaded: sessionId={}, progress={}%", 
                            sessionId, String.format("%.1f", response.getProgressPercentage()));
                }
                return ResponseEntity.ok(response);
            } else {
                log.warn("Chunk upload failed: sessionId={}, chunk={}, error={}", 
                        sessionId, chunkNumber, response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Unexpected error during chunk upload: sessionId={}, chunk={}", 
                     sessionId, chunkNumber, e);
            ChunkUploadResponse errorResponse = ChunkUploadResponse.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get upload session status
     *
     * @param sessionId the session ID
     * @return chunk upload response with current status
     */
    @GetMapping("/upload/chunks/{sessionId}/status")
    public ResponseEntity<ChunkUploadResponse> getUploadStatus(
            @PathVariable("sessionId") @NotBlank String sessionId) {
        
        try {
            log.debug("Getting upload status for session: {}", sessionId);
            
            ChunkUploadResponse response = fileUploadService.getUploadStatus(sessionId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Unexpected error getting upload status: sessionId={}", sessionId, e);
            ChunkUploadResponse errorResponse = ChunkUploadResponse.builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("File upload service is healthy");
    }

    /**
     * Handle validation errors
     *
     * @param e the validation exception
     * @return error response
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            jakarta.validation.ConstraintViolationException e) {
        
        log.warn("Validation error: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .details(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle illegal argument exceptions
     *
     * @param e the illegal argument exception
     * @return error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        
        log.warn("Invalid argument: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INVALID_ARGUMENT")
                .message("Invalid request parameter")
                .details(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle runtime exceptions
     *
     * @param e the runtime exception
     * @return error response
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        
        log.error("Runtime error in file upload controller", e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("Internal server error")
                .details("An unexpected error occurred while processing the request")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}