package com.simon.lab020.service;

import com.simon.lab020.dto.*;
import com.simon.lab020.entity.*;
import com.simon.lab020.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling file upload operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileChunkRepository fileChunkRepository;
    private final UploadSessionRepository uploadSessionRepository;
    private final FileStorageService fileStorageService;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    /**
     * Upload a single file
     *
     * @param file the file to upload
     * @param request the HTTP request
     * @return upload response
     */
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, HttpServletRequest request) {
        try {
            // Validate file
            validateFile(file);

            // Generate document ID
            String documentId = UUID.randomUUID().toString();

            // Calculate file hash
            String fileHash = fileStorageService.calculateFileHash(file);

            // Check for duplicate files
            Optional<FileMetadata> existingFile = fileMetadataRepository.findByFileHash(fileHash);
            if (existingFile.isPresent()) {
                log.info("Duplicate file detected: {}, returning existing document: {}", 
                        file.getOriginalFilename(), existingFile.get().getDocumentId());
                
                // Log the upload attempt
                auditLogService.logUpload(existingFile.get().getDocumentId(), request, 
                                        file.getOriginalFilename(), file.getSize());
                
                return createUploadResponse(existingFile.get(), "File already exists", true);
            }

            // Generate encryption key
            String encryptionKey = encryptionService.generateEncryptionKey();

            // Store and encrypt file
            String storagePath = storeAndEncryptFile(file, documentId, encryptionKey);

            // Create file metadata
            FileMetadata metadata = new FileMetadata();
            metadata.setDocumentId(documentId);
            metadata.setOriginalFilename(file.getOriginalFilename());
            metadata.setFileSize(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setFilePath(storagePath);
            metadata.setEncryptionKey(encryptionKey);
            metadata.setFileHash(fileHash);
            metadata.setUploadDate(LocalDateTime.now());
            metadata.setCreatedAt(LocalDateTime.now());
            metadata.setUpdatedAt(LocalDateTime.now());

            // Save metadata
            fileMetadataRepository.save(metadata);

            // Log the upload
            auditLogService.logUpload(documentId, request, file.getOriginalFilename(), file.getSize());

            log.info("File uploaded successfully: {} -> {}", file.getOriginalFilename(), documentId);
            return createUploadResponse(metadata, "File uploaded successfully", true);

        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            return FileUploadResponse.builder()
                    .success(false)
                    .message("Failed to upload file: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
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
    @Transactional
    public ChunkUploadResponse initializeChunkUpload(String originalFilename, long totalSize, 
                                                   int totalChunks, String contentType, 
                                                   String fileHash, HttpServletRequest request) {
        try {
            // Check for duplicate files
            Optional<FileMetadata> existingFile = fileMetadataRepository.findByFileHash(fileHash);
            if (existingFile.isPresent()) {
                log.info("Duplicate file detected during chunk upload initialization: {}", originalFilename);
                return ChunkUploadResponse.builder()
                        .success(true)
                        .completed(true)
                        .documentId(existingFile.get().getDocumentId())
                        .message("File already exists")
                        .uploadDate(LocalDateTime.now())
                        .build();
            }

            // Generate session ID
            String sessionId = UUID.randomUUID().toString();

            // Create upload session
            UploadSession session = new UploadSession();
            session.setSessionId(sessionId);
            session.setOriginalFilename(originalFilename);
            session.setTotalSize(totalSize);
            session.setTotalChunks(totalChunks);
            session.setContentType(contentType);
            session.setFileHash(fileHash);
            session.setChunksUploaded(0);
            session.setStatus(UploadSession.UploadStatus.IN_PROGRESS);
            session.setCreatedAt(LocalDateTime.now());
            session.setUpdatedAt(LocalDateTime.now());
            session.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24 hours expiry

            uploadSessionRepository.save(session);

            log.info("Chunk upload session initialized: {} for file: {}", sessionId, originalFilename);
            return ChunkUploadResponse.builder()
                    .sessionId(sessionId)
                    .chunkNumber(0)
                    .totalChunks(totalChunks)
                    .chunksUploaded(0)
                    .progressPercentage(0.0)
                    .completed(false)
                    .success(true)
                    .message("Upload session initialized")
                    .uploadDate(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to initialize chunk upload for file: {}", originalFilename, e);
            return ChunkUploadResponse.builder()
                    .success(false)
                    .message("Failed to initialize upload session: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Upload a file chunk
     *
     * @param request the chunk upload request
     * @param httpRequest the HTTP request
     * @return chunk upload response
     */
    @Transactional
    public ChunkUploadResponse uploadChunk(ChunkUploadRequest request, HttpServletRequest httpRequest) {
        try {
            // Validate chunk
            validateChunk(request);

            // Get upload session
            Optional<UploadSession> sessionOpt = uploadSessionRepository.findBySessionId(request.getSessionId());
            if (sessionOpt.isEmpty()) {
                return ChunkUploadResponse.builder()
                        .success(false)
                        .message("Upload session not found")
                        .uploadDate(LocalDateTime.now())
                        .build();
            }

            UploadSession session = sessionOpt.get();

            // Check session status
            if (session.getStatus() != UploadSession.UploadStatus.IN_PROGRESS) {
                return ChunkUploadResponse.builder()
                        .success(false)
                        .message("Upload session is not active")
                        .uploadDate(LocalDateTime.now())
                        .build();
            }

            // Check if chunk already exists
            Optional<FileChunk> existingChunk = fileChunkRepository
                    .findByDocumentIdAndChunkNumber(request.getSessionId(), request.getChunkNumber());
            if (existingChunk.isPresent()) {
                log.debug("Chunk {} already uploaded for session {}", request.getChunkNumber(), request.getSessionId());
            } else {
                // Store chunk
                String chunkPath = fileStorageService.storeChunk(request.getChunkFile(), 
                                                               request.getSessionId(), 
                                                               request.getChunkNumber());

                // Create chunk metadata
                FileChunk chunk = new FileChunk();
                chunk.setChunkId(UUID.randomUUID().toString());
                chunk.setDocumentId(request.getSessionId()); // Using session ID as document ID for chunks
                chunk.setChunkNumber(request.getChunkNumber());
                chunk.setTotalChunks(request.getTotalChunks());
                chunk.setChunkSize(request.getChunkFile().getSize());
                chunk.setChunkPath(chunkPath);
                chunk.setChunkHash(request.getChunkHash());
                chunk.setUploadDate(LocalDateTime.now());
                chunk.setIsAssembled(false);
                chunk.setCreatedAt(LocalDateTime.now());
                chunk.setUpdatedAt(LocalDateTime.now());

                fileChunkRepository.save(chunk);

                // Update session
                session.setChunksUploaded(session.getChunksUploaded() + 1);
                session.setUpdatedAt(LocalDateTime.now());
                uploadSessionRepository.save(session);
            }

            // Check if all chunks are uploaded
            boolean allChunksUploaded = fileChunkRepository.areAllChunksUploaded(
                    request.getSessionId(), request.getTotalChunks());

            ChunkUploadResponse.ChunkUploadResponseBuilder responseBuilder = ChunkUploadResponse.builder()
                    .sessionId(request.getSessionId())
                    .chunkNumber(request.getChunkNumber())
                    .totalChunks(request.getTotalChunks())
                    .chunksUploaded(session.getChunksUploaded())
                    .progressPercentage((double) session.getChunksUploaded() / request.getTotalChunks() * 100)
                    .completed(allChunksUploaded)
                    .success(true)
                    .uploadDate(LocalDateTime.now());

            if (allChunksUploaded) {
                // Assemble chunks into final file
                String documentId = assembleChunks(session, httpRequest);
                responseBuilder.documentId(documentId)
                              .message("File upload completed successfully");
            } else {
                // Find missing chunks
                List<Integer> missingChunks = findMissingChunks(request.getSessionId(), request.getTotalChunks());
                responseBuilder.missingChunks(missingChunks)
                              .message("Chunk uploaded successfully");
            }

            return responseBuilder.build();

        } catch (Exception e) {
            log.error("Failed to upload chunk: session={}, chunk={}", 
                     request.getSessionId(), request.getChunkNumber(), e);
            return ChunkUploadResponse.builder()
                    .success(false)
                    .message("Failed to upload chunk: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Get upload session status
     *
     * @param sessionId the session ID
     * @return chunk upload response with current status
     */
    @Transactional(readOnly = true)
    public ChunkUploadResponse getUploadStatus(String sessionId) {
        try {
            Optional<UploadSession> sessionOpt = uploadSessionRepository.findBySessionId(sessionId);
            if (sessionOpt.isEmpty()) {
                return ChunkUploadResponse.builder()
                        .success(false)
                        .message("Upload session not found")
                        .uploadDate(LocalDateTime.now())
                        .build();
            }

            UploadSession session = sessionOpt.get();
            boolean completed = session.getStatus() == UploadSession.UploadStatus.COMPLETED;
            List<Integer> missingChunks = completed ? Collections.emptyList() : 
                    findMissingChunks(sessionId, session.getTotalChunks());

            return ChunkUploadResponse.builder()
                    .sessionId(sessionId)
                    .totalChunks(session.getTotalChunks())
                    .chunksUploaded(session.getChunksUploaded())
                    .progressPercentage((double) session.getChunksUploaded() / session.getTotalChunks() * 100)
                    .completed(completed)
                    .missingChunks(missingChunks)
                    .success(true)
                    .message("Upload status retrieved")
                    .uploadDate(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to get upload status for session: {}", sessionId, e);
            return ChunkUploadResponse.builder()
                    .success(false)
                    .message("Failed to get upload status: " + e.getMessage())
                    .uploadDate(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Assemble chunks into final file
     *
     * @param session the upload session
     * @param request the HTTP request
     * @return the document ID of the assembled file
     */
    private String assembleChunks(UploadSession session, HttpServletRequest request) {
        try {
            // Generate document ID
            String documentId = UUID.randomUUID().toString();

            // Assemble chunks
            String assembledPath = fileStorageService.assembleChunks(
                    session.getSessionId(),
                    session.getTotalChunks(),
                    documentId,
                    session.getOriginalFilename()
            );

            // Generate encryption key
            String encryptionKey = encryptionService.generateEncryptionKey();

            // Encrypt the assembled file
            Path assembledFilePath = fileStorageService.loadFile(assembledPath);
            File assembledFile = assembledFilePath.toFile();
            File encryptedFile = new File(assembledFile.getAbsolutePath() + ".encrypted");
            encryptionService.encryptFile(assembledFile, encryptedFile, encryptionKey);
            
            // Store the encrypted file
            String encryptedPath = fileStorageService.storeEncryptedFile(encryptedFile, documentId, 
                session.getOriginalFilename());
            
            // Clean up temporary files
            if (encryptedFile.exists()) {
                encryptedFile.delete();
            }

            // Delete the original assembled file (unencrypted)
            if (assembledFile.exists()) {
                assembledFile.delete();
            }

            // Create file metadata
            FileMetadata metadata = new FileMetadata();
            metadata.setDocumentId(documentId);
            metadata.setOriginalFilename(session.getOriginalFilename());
            metadata.setFileSize(session.getTotalSize());
            metadata.setContentType(session.getContentType());
            metadata.setFilePath(encryptedPath);
            metadata.setEncryptionKey(encryptionKey);
            metadata.setFileHash(session.getFileHash());
            metadata.setUploadDate(LocalDateTime.now());
            metadata.setCreatedAt(LocalDateTime.now());
            metadata.setUpdatedAt(LocalDateTime.now());

            fileMetadataRepository.save(metadata);

            // Mark chunks as assembled
            fileChunkRepository.markChunksAsAssembled(session.getSessionId());

            // Update session status
            session.setStatus(UploadSession.UploadStatus.COMPLETED);
            session.setUpdatedAt(LocalDateTime.now());
            uploadSessionRepository.save(session);

            // Log the upload
            auditLogService.logUpload(documentId, request, session.getOriginalFilename(), session.getTotalSize());

            log.info("Chunks assembled successfully: session={} -> document={}", 
                    session.getSessionId(), documentId);
            return documentId;

        } catch (Exception e) {
            log.error("Failed to assemble chunks for session: {}", session.getSessionId(), e);
            throw new RuntimeException("Failed to assemble chunks", e);
        }
    }

    /**
     * Find missing chunks for a session
     *
     * @param sessionId the session ID
     * @param totalChunks the total number of chunks
     * @return list of missing chunk numbers
     */
    private List<Integer> findMissingChunks(String sessionId, int totalChunks) {
        List<Integer> uploadedChunks = fileChunkRepository.findUploadedChunkNumbers(sessionId);
        List<Integer> allChunks = new ArrayList<>();
        for (int i = 0; i < totalChunks; i++) {
            allChunks.add(i);
        }
        allChunks.removeAll(uploadedChunks);
        return allChunks;
    }

    /**
     * Validate uploaded file
     *
     * @param file the file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        // Validate filename for path traversal attacks
        String filename = file.getOriginalFilename();
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence");
        }

        // Add more validation as needed (file size, type, etc.)
        if (file.getSize() > 100 * 1024 * 1024) { // 100MB limit for single file
            throw new IllegalArgumentException("File size exceeds maximum limit of 100MB");
        }
    }

    /**
     * Validate chunk upload request
     *
     * @param request the chunk upload request
     */
    private void validateChunk(ChunkUploadRequest request) {
        if (request.getChunkFile() == null || request.getChunkFile().isEmpty()) {
            throw new IllegalArgumentException("Chunk file cannot be empty");
        }

        if (request.getChunkNumber() < 0) {
            throw new IllegalArgumentException("Chunk number cannot be negative");
        }

        if (request.getTotalChunks() <= 0) {
            throw new IllegalArgumentException("Total chunks must be positive");
        }

        if (request.getChunkNumber() >= request.getTotalChunks()) {
            throw new IllegalArgumentException("Chunk number cannot be greater than or equal to total chunks");
        }
    }

    /**
     * Store and encrypt file
     *
     * @param file the multipart file to store
     * @param documentId the document ID
     * @param encryptionKey the encryption key
     * @return the storage path
     */
    private String storeAndEncryptFile(MultipartFile file, String documentId, String encryptionKey) {
        try {
            // Create temporary file for original content
            File tempOriginalFile = File.createTempFile("upload_original_", ".tmp");
            tempOriginalFile.deleteOnExit();
            
            // Write original file content to temporary file
            try (InputStream inputStream = file.getInputStream();
                 FileOutputStream fos = new FileOutputStream(tempOriginalFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            
            // Create temporary file for encrypted content
            File tempEncryptedFile = File.createTempFile("upload_encrypted_", ".tmp");
            tempEncryptedFile.deleteOnExit();
            
            // Encrypt the file
            encryptionService.encryptFile(tempOriginalFile, tempEncryptedFile, encryptionKey);
            
            // Store the encrypted file using FileStorageService's internal logic
            String storagePath = fileStorageService.storeEncryptedFile(tempEncryptedFile, documentId, file.getOriginalFilename());
            
            // Clean up temporary files
            tempOriginalFile.delete();
            tempEncryptedFile.delete();
            
            return storagePath;
            
        } catch (Exception e) {
            log.error("Failed to store and encrypt file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store and encrypt file", e);
        }
    }

    /**
     * Create upload response
     *
     * @param metadata the file metadata
     * @param message the response message
     * @param success whether the operation was successful
     * @return upload response
     */
    private FileUploadResponse createUploadResponse(FileMetadata metadata, String message, boolean success) {
        return FileUploadResponse.builder()
                .documentId(metadata.getDocumentId())
                .originalFilename(metadata.getOriginalFilename())
                .fileSize(metadata.getFileSize())
                .contentType(metadata.getContentType())
                .uploadDate(metadata.getUploadDate())
                .fileHash(metadata.getFileHash())
                .message(message)
                .success(success)
                .build();
    }
}