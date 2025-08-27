package com.simon.lab020.service;

import com.simon.lab020.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for file storage operations
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final FileStorageProperties storageProperties;
    private Path basePath;
    private Path tempPath;
    private Path chunksPath;

    @PostConstruct
    public void init() {
        try {
            this.basePath = Paths.get(storageProperties.getBasePath()).toAbsolutePath().normalize();
            this.tempPath = Paths.get(storageProperties.getTempPath()).toAbsolutePath().normalize();
            this.chunksPath = Paths.get(storageProperties.getChunksPath()).toAbsolutePath().normalize();

            // Create directories if they don't exist
            Files.createDirectories(basePath);
            Files.createDirectories(tempPath);
            Files.createDirectories(chunksPath);

            log.info("File storage initialized:");
            log.info("  Base path: {}", basePath);
            log.info("  Temp path: {}", tempPath);
            log.info("  Chunks path: {}", chunksPath);

        } catch (IOException e) {
            log.error("Failed to initialize file storage", e);
            throw new RuntimeException("Could not initialize file storage", e);
        }
    }

    /**
     * Store a file and return the storage path
     *
     * @param file the multipart file to store
     * @param documentId the document ID for organizing files
     * @return the relative storage path
     */
    public String storeFile(MultipartFile file, String documentId) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty file");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            if (originalFilename.contains("..")) {
                throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFilename);
            }

            // Generate storage path: /year/month/day/documentId/filename
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filename = generateUniqueFilename(originalFilename);
            Path targetPath = basePath.resolve(datePath).resolve(documentId).resolve(filename);

            // Create directories
            Files.createDirectories(targetPath.getParent());

            // Copy file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = basePath.relativize(targetPath).toString().replace('\\', '/');
            log.debug("File stored: {} -> {}", originalFilename, relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    /**
     * Store a file chunk
     *
     * @param chunk the chunk data
     * @param sessionId the upload session ID
     * @param chunkNumber the chunk number
     * @return the storage path for the chunk
     */
    public String storeChunk(MultipartFile chunk, String sessionId, int chunkNumber) {
        try {
            if (chunk.isEmpty()) {
                throw new IllegalArgumentException("Cannot store empty chunk");
            }

            // Generate chunk filename: sessionId_chunkNumber.chunk
            String chunkFilename = String.format("%s_%d.chunk", sessionId, chunkNumber);
            Path chunkPath = chunksPath.resolve(sessionId).resolve(chunkFilename);

            // Create directories
            Files.createDirectories(chunkPath.getParent());

            // Store chunk
            try (InputStream inputStream = chunk.getInputStream()) {
                Files.copy(inputStream, chunkPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = chunksPath.relativize(chunkPath).toString().replace('\\', '/');
            log.debug("Chunk stored: session={}, chunk={}, path={}", sessionId, chunkNumber, relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store chunk: session={}, chunk={}", sessionId, chunkNumber, e);
            throw new RuntimeException("Failed to store chunk", e);
        }
    }

    /**
     * Assemble chunks into a complete file
     *
     * @param sessionId the upload session ID
     * @param totalChunks the total number of chunks
     * @param documentId the document ID
     * @param originalFilename the original filename
     * @return the storage path for the assembled file
     */
    public String assembleChunks(String sessionId, int totalChunks, String documentId, String originalFilename) {
        try {
            // Generate target path
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filename = generateUniqueFilename(originalFilename);
            Path targetPath = basePath.resolve(datePath).resolve(documentId).resolve(filename);
            Files.createDirectories(targetPath.getParent());

            // Assemble chunks
            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                for (int i = 0; i < totalChunks; i++) {
                    String chunkFilename = String.format("%s_%d.chunk", sessionId, i);
                    Path chunkPath = chunksPath.resolve(sessionId).resolve(chunkFilename);
                    
                    if (!Files.exists(chunkPath)) {
                        throw new RuntimeException("Missing chunk: " + i);
                    }
                    
                    try (FileInputStream fis = new FileInputStream(chunkPath.toFile())) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }

            // Clean up chunks
            cleanupChunks(sessionId);

            String relativePath = basePath.relativize(targetPath).toString().replace('\\', '/');
            log.info("Chunks assembled: session={}, file={}", sessionId, relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to assemble chunks: session={}", sessionId, e);
            throw new RuntimeException("Failed to assemble chunks", e);
        }
    }

    /**
     * Load a file as Resource
     *
     * @param filePath the relative file path
     * @return the file as Resource
     */
    public Path loadFile(String filePath) {
        try {
            Path file = basePath.resolve(filePath).normalize();
            
            // Security check: ensure the file is within the base path
            if (!file.startsWith(basePath)) {
                throw new SecurityException("File path is outside the allowed directory");
            }
            
            if (!Files.exists(file) || !Files.isReadable(file)) {
                throw new FileNotFoundException("File not found or not readable: " + filePath);
            }
            
            return file;
            
        } catch (Exception e) {
            log.error("Failed to load file: {}", filePath, e);
            throw new RuntimeException("Failed to load file", e);
        }
    }

    /**
     * Delete a file
     *
     * @param filePath the relative file path
     */
    public void deleteFile(String filePath) {
        try {
            Path file = basePath.resolve(filePath).normalize();
            
            // Security check
            if (!file.startsWith(basePath)) {
                throw new SecurityException("File path is outside the allowed directory");
            }
            
            if (Files.exists(file)) {
                Files.delete(file);
                log.debug("File deleted: {}", filePath);
                
                // Try to delete empty parent directories
                cleanupEmptyDirectories(file.getParent());
            }
            
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    /**
     * Calculate file hash (SHA-256)
     *
     * @param file the file to hash
     * @return the hex-encoded hash
     */
    public String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Failed to calculate file hash", e);
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }

    /**
     * Calculate hash for a file path
     *
     * @param filePath the file path
     * @return the hex-encoded hash
     */
    public String calculateFileHash(String filePath) {
        try {
            Path file = loadFile(filePath);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            try (InputStream inputStream = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Failed to calculate file hash for path: {}", filePath, e);
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }

    /**
     * Get file size
     *
     * @param filePath the file path
     * @return the file size in bytes
     */
    public long getFileSize(String filePath) {
        try {
            Path file = loadFile(filePath);
            return Files.size(file);
        } catch (IOException e) {
            log.error("Failed to get file size: {}", filePath, e);
            throw new RuntimeException("Failed to get file size", e);
        }
    }

    /**
     * Clean up chunks for a session
     *
     * @param sessionId the session ID
     */
    public void cleanupChunks(String sessionId) {
        try {
            Path sessionPath = chunksPath.resolve(sessionId);
            if (Files.exists(sessionPath)) {
                FileUtils.deleteDirectory(sessionPath.toFile());
                log.debug("Chunks cleaned up for session: {}", sessionId);
            }
        } catch (IOException e) {
            log.warn("Failed to cleanup chunks for session: {}", sessionId, e);
        }
    }

    /**
     * Generate a unique filename
     *
     * @param originalFilename the original filename
     * @return a unique filename
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        String baseName = originalFilename;
        
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
            baseName = originalFilename.substring(0, lastDotIndex);
        }
        
        return String.format("%s_%s%s", baseName, UUID.randomUUID().toString().substring(0, 8), extension);
    }

    /**
     * Clean up empty directories
     *
     * @param directory the directory to clean up
     */
    private void cleanupEmptyDirectories(Path directory) {
        try {
            if (directory != null && !directory.equals(basePath) && Files.exists(directory)) {
                try (var stream = Files.list(directory)) {
                    if (stream.findAny().isEmpty()) {
                        Files.delete(directory);
                        cleanupEmptyDirectories(directory.getParent());
                    }
                }
            }
        } catch (IOException e) {
            // Ignore cleanup errors
            log.debug("Failed to cleanup empty directory: {}", directory, e);
        }
    }
}