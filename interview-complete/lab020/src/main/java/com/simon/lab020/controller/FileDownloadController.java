package com.simon.lab020.controller;

import com.simon.lab020.dto.FileDownloadResponse;
import com.simon.lab020.dto.ErrorResponse;
import com.simon.lab020.service.FileDownloadService;
import com.simon.lab020.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST Controller for file download operations
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Download", description = "File download operations")
public class FileDownloadController {
    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    private final FileDownloadService fileDownloadService;
    private final AuditLogService auditLogService;

    /**
     * Download file by document ID
     *
     * @param documentId the document ID
     * @param inline whether to display inline or as attachment
     * @param request HTTP request for audit logging
     * @return file content as ResponseEntity
     */
    @GetMapping("/download/{documentId}")
    @Operation(summary = "Download file by document ID", 
               description = "Downloads a file using its document ID. Supports both inline display and attachment download.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> downloadFile(
            @Parameter(description = "Document ID of the file to download", required = true)
            @PathVariable String documentId,
            @Parameter(description = "Whether to display file inline (true) or as attachment (false)")
            @RequestParam(defaultValue = "false") boolean inline,
            HttpServletRequest request) {
        
        try {
            log.info("Download request for document ID: {}, inline: {}", documentId, inline);
            
            // Get file download response
            FileDownloadResponse downloadResponse = fileDownloadService.downloadFile(documentId);
            
            if (!downloadResponse.isSuccess()){
                log.warn("File download failed for document ID: {}, message: {}", 
                        documentId, downloadResponse.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createErrorResponse("FILE_NOT_FOUND", downloadResponse.getMessage(), 
                                request.getRequestURI()));
            }
            
            Resource fileResource = downloadResponse.getFileResource();
            String originalFilename = downloadResponse.getOriginalFilename();
            String contentType = downloadResponse.getContentType();
            Long fileSize = downloadResponse.getFileSize();
            
            // Log download operation
            auditLogService.logDownload(documentId, request, originalFilename);
            
            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            
            // Set content type
            if (contentType != null && !contentType.isEmpty()) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            
            // Set content length if available
            if (fileSize != null) {
                headers.setContentLength(fileSize);
            }
            
            // Set content disposition
            String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            if (inline) {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                        String.format("inline; filename*=UTF-8''%s", encodedFilename));
            } else {
                headers.set(HttpHeaders.CONTENT_DISPOSITION, 
                        String.format("attachment; filename*=UTF-8''%s", encodedFilename));
            }
            
            // Set cache control headers
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            log.info("File download successful for document ID: {}, filename: {}, size: {} bytes", 
                    documentId, originalFilename, fileSize);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileResource);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid document ID: {}", documentId, e);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createErrorResponse("INVALID_DOCUMENT_ID", e.getMessage(), 
                            request.getRequestURI()));
        } catch (IOException e) {
            log.error("IO error during file download for document ID: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createErrorResponse("FILE_READ_ERROR", 
                            "Failed to read file content", request.getRequestURI()));
        } catch (Exception e) {
            log.error("Unexpected error during file download for document ID: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createErrorResponse("INTERNAL_ERROR", 
                            "An unexpected error occurred", request.getRequestURI()));
        }
    }
    
    /**
     * Get file metadata by document ID
     *
     * @param documentId the document ID
     * @param request HTTP request for audit logging
     * @return file metadata as JSON
     */
    @GetMapping("/metadata/{documentId}")
    @Operation(summary = "Get file metadata", 
               description = "Retrieves metadata information for a file without downloading the actual content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metadata retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = FileDownloadResponse.class))),
            @ApiResponse(responseCode = "404", description = "File not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> getFileMetadata(
            @Parameter(description = "Document ID of the file", required = true)
            @PathVariable String documentId,
            HttpServletRequest request) {
        
        try {
            log.info("Metadata request for document ID: {}", documentId);
            
            FileDownloadResponse metadataResponse = fileDownloadService.getFileMetadata(documentId);
            
            if (!metadataResponse.isSuccess()) {
                log.warn("File metadata not found for document ID: {}", documentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("FILE_NOT_FOUND", metadataResponse.getMessage(), 
                                request.getRequestURI()));
            }
            
            log.info("File metadata retrieved for document ID: {}, filename: {}", 
                    documentId, metadataResponse.getOriginalFilename());
            
            return ResponseEntity.ok(metadataResponse);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid document ID: {}", documentId, e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("INVALID_DOCUMENT_ID", e.getMessage(), 
                            request.getRequestURI()));
        } catch (Exception e) {
            log.error("Unexpected error during metadata retrieval for document ID: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", 
                            "An unexpected error occurred", request.getRequestURI()));
        }
    }
    
    /**
     * Check if file exists by document ID
     *
     * @param documentId the document ID
     * @return existence status
     */
    @RequestMapping(value = "/download/{documentId}", method = RequestMethod.HEAD)
    @Operation(summary = "Check file existence", 
               description = "Checks if a file exists without downloading it. Returns 200 if exists, 404 if not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File exists"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<Void> checkFileExists(
            @Parameter(description = "Document ID of the file", required = true)
            @PathVariable String documentId) {
        
        try {
            log.debug("Existence check for document ID: {}", documentId);
            
            boolean exists = fileDownloadService.fileExists(documentId);
            
            if (exists) {
                log.debug("File exists for document ID: {}", documentId);
                return ResponseEntity.ok().build();
            } else {
                log.debug("File not found for document ID: {}", documentId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid document ID: {}", documentId, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during existence check for document ID: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     *
     * @return health status
     */
    @GetMapping("/download/health")
    @Operation(summary = "Download service health check", 
               description = "Checks the health status of the file download service.")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("File download service is healthy");
    }
    
    /**
     * Create standardized error response
     */
    private ErrorResponse createErrorResponse(String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .requestId(UUID.randomUUID().toString())
                .build();
    }
}