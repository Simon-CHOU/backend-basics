package com.simon.lab020.service;

import com.simon.lab020.dto.FileDownloadResponse;
import com.simon.lab020.entity.FileMetadata;
import com.simon.lab020.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for handling file download operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FileDownloadService {


    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final EncryptionService encryptionService;

    /**
     * Download file by document ID
     *
     * @param documentId the document ID
     * @return FileDownloadResponse containing file data and metadata
     * @throws IOException if file cannot be read
     */
    public FileDownloadResponse downloadFile(String documentId) throws IOException {
        log.info("Starting file download for document ID: {}", documentId);
        
        // Validate document ID
        if (documentId == null || documentId.trim().isEmpty()) {
            log.warn("Invalid document ID provided: {}", documentId);
            return createErrorResponse("Invalid document ID");
        }
        
        try {
            // Find file metadata
            Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByDocumentId(documentId);
            
            if (fileMetadataOpt.isEmpty()) {
                log.warn("File not found for document ID: {}", documentId);
                return createErrorResponse("File not found");
            }
            
            FileMetadata fileMetadata = fileMetadataOpt.get();
            log.info("Found file metadata for document ID: {}, filename: {}, size: {} bytes", 
                    documentId, fileMetadata.getOriginalFilename(), fileMetadata.getFileSize());
            
            // Load encrypted file from storage
            Path encryptedFilePath = fileStorageService.loadFile(fileMetadata.getFilePath());
            Resource encryptedResource = new FileSystemResource(encryptedFilePath);
            
            if (!encryptedResource.exists() || !encryptedResource.isReadable()) {
                log.error("File not accessible at path: {}", fileMetadata.getFilePath());
                return createErrorResponse("File not accessible");
            }
            
            // Decrypt file content
            File decryptedResourceFile;
            Resource decryptedResource;
            try {
                File encryptedFile = encryptedResource.getFile();
                // Create temporary file for decrypted content
                decryptedResourceFile = File.createTempFile("decrypted_" + documentId, ".tmp");
                decryptedResourceFile.deleteOnExit();
                
                // Decrypt the file
                encryptionService.decryptFile(encryptedFile, decryptedResourceFile, fileMetadata.getEncryptionKey());
                decryptedResource = new FileSystemResource(decryptedResourceFile);
                log.debug("File decrypted successfully for document ID: {}", documentId);
            } catch (Exception e) {
                log.error("Failed to decrypt file for document ID: {}", documentId, e);
                return createErrorResponse("Failed to decrypt file");
            }
            
            // Verify file integrity (optional but recommended)
            try {
                String actualHash = fileStorageService.calculateFileHash(decryptedResourceFile.getPath());
                if (!actualHash.equals(fileMetadata.getFileHash())) {
                    log.error("File integrity check failed for document ID: {}. Expected: {}, Actual: {}", 
                            documentId, fileMetadata.getFileHash(), actualHash);
                    return createErrorResponse("File integrity check failed");
                }
                log.debug("File integrity verified for document ID: {}", documentId);
            } catch (Exception e) {
                log.warn("Could not verify file integrity for document ID: {}", documentId, e);
                // Continue with download even if integrity check fails
            }
            
            // Create successful response
            FileDownloadResponse response = FileDownloadResponse.builder()
                    .documentId(documentId)
                    .originalFilename(fileMetadata.getOriginalFilename())
                    .fileSize(fileMetadata.getFileSize())
                    .contentType(fileMetadata.getContentType())
                    .fileResource(decryptedResource)
                    .uploadDate(fileMetadata.getUploadDate())
                    .downloadDate(LocalDateTime.now())
                    .fileHash(fileMetadata.getFileHash())
                    .success(true)
                    .message("File downloaded successfully")
                    .build();
            
            log.info("File download completed successfully for document ID: {}", documentId);
            return response;
            
        } catch (Exception e) {
            log.error("Unexpected error during file download for document ID: {}", documentId, e);
            return createErrorResponse("An unexpected error occurred during file download");
        }
    }
    
    /**
     * Get file metadata without downloading the actual file
     *
     * @param documentId the document ID
     * @return FileDownloadResponse containing only metadata
     */
    public FileDownloadResponse getFileMetadata(String documentId) {
        log.info("Retrieving file metadata for document ID: {}", documentId);
        
        // Validate document ID
        if (documentId == null || documentId.trim().isEmpty()) {
            log.warn("Invalid document ID provided: {}", documentId);
            return createErrorResponse("Invalid document ID");
        }
        
        try {
            // Find file metadata
            Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByDocumentId(documentId);
            
            if (fileMetadataOpt.isEmpty()) {
                log.warn("File metadata not found for document ID: {}", documentId);
                return createErrorResponse("File not found");
            }
            
            FileMetadata fileMetadata = fileMetadataOpt.get();
            log.info("Retrieved file metadata for document ID: {}, filename: {}", 
                    documentId, fileMetadata.getOriginalFilename());
            
            // Create response with metadata only (no file resource)
            return FileDownloadResponse.builder()
                    .documentId(documentId)
                    .originalFilename(fileMetadata.getOriginalFilename())
                    .fileSize(fileMetadata.getFileSize())
                    .contentType(fileMetadata.getContentType())
                    .uploadDate(fileMetadata.getUploadDate())
                    .downloadDate(LocalDateTime.now())
                    .fileHash(fileMetadata.getFileHash())
                    .success(true)
                    .message("File metadata retrieved successfully")
                    .build();
                    
        } catch (Exception e) {
            log.error("Unexpected error during metadata retrieval for document ID: {}", documentId, e);
            return createErrorResponse("An unexpected error occurred during metadata retrieval");
        }
    }
    
    /**
     * Check if file exists by document ID
     *
     * @param documentId the document ID
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String documentId) {
        log.debug("Checking file existence for document ID: {}", documentId);
        
        // Validate document ID
        if (documentId == null || documentId.trim().isEmpty()) {
            log.warn("Invalid document ID provided for existence check: {}", documentId);
            return false;
        }
        
        try {
            boolean exists = fileMetadataRepository.findByDocumentId(documentId).isPresent();
            log.debug("File existence check for document ID: {} - exists: {}", documentId, exists);
            return exists;
            
        } catch (Exception e) {
            log.error("Error during file existence check for document ID: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Get file size by document ID
     *
     * @param documentId the document ID
     * @return file size in bytes, or null if file not found
     */
    public Long getFileSize(String documentId) {
        log.debug("Getting file size for document ID: {}", documentId);
        
        if (documentId == null || documentId.trim().isEmpty()) {
            return null;
        }
        
        try {
            Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByDocumentId(documentId);
            
            if (fileMetadataOpt.isPresent()) {
                Long fileSize = fileMetadataOpt.get().getFileSize();
                log.debug("File size for document ID: {} is {} bytes", documentId, fileSize);
                return fileSize;
            } else {
                log.debug("File not found for document ID: {}", documentId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error getting file size for document ID: {}", documentId, e);
            return null;
        }
    }
    
    /**
     * Get file content type by document ID
     *
     * @param documentId the document ID
     * @return content type, or null if file not found
     */
    public String getContentType(String documentId) {
        log.debug("Getting content type for document ID: {}", documentId);
        
        if (documentId == null || documentId.trim().isEmpty()) {
            return null;
        }
        
        try {
            Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByDocumentId(documentId);
            
            if (fileMetadataOpt.isPresent()) {
                String contentType = fileMetadataOpt.get().getContentType();
                log.debug("Content type for document ID: {} is {}", documentId, contentType);
                return contentType;
            } else {
                log.debug("File not found for document ID: {}", documentId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error getting content type for document ID: {}", documentId, e);
            return null;
        }
    }
    
    /**
     * Validate file accessibility (exists and readable)
     *
     * @param documentId the document ID
     * @return true if file is accessible, false otherwise
     */
    public boolean isFileAccessible(String documentId) {
        log.debug("Checking file accessibility for document ID: {}", documentId);
        
        if (documentId == null || documentId.trim().isEmpty()) {
            return false;
        }
        
        try {
            Optional<FileMetadata> fileMetadataOpt = fileMetadataRepository.findByDocumentId(documentId);
            
            if (fileMetadataOpt.isEmpty()) {
                log.debug("File metadata not found for document ID: {}", documentId);
                return false;
            }
            
            FileMetadata fileMetadata = fileMetadataOpt.get();
            
            // Check if physical file exists and is readable
            try {
                Path filePath = fileStorageService.loadFile(fileMetadata.getFilePath());
                boolean accessible = Files.exists(filePath) && Files.isReadable(filePath);
                log.debug("File accessibility for document ID: {} - accessible: {}", documentId, accessible);
                return accessible;
            } catch (Exception e) {
                log.warn("Error checking file accessibility for document ID: {}", documentId, e);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error during file accessibility check for document ID: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Create error response
     */
    private FileDownloadResponse createErrorResponse(String message) {
        return FileDownloadResponse.builder()
                .success(false)
                .message(message)
                .downloadDate(LocalDateTime.now())
                .build();
    }
}