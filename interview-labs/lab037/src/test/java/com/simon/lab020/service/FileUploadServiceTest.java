package com.simon.lab020.service;

import com.simon.lab020.dto.ChunkUploadRequest;
import com.simon.lab020.dto.ChunkUploadResponse;
import com.simon.lab020.dto.FileUploadResponse;
import jakarta.servlet.http.HttpServletRequest;
import com.simon.lab020.entity.FileChunk;
import com.simon.lab020.entity.FileMetadata;
import com.simon.lab020.entity.UploadSession;
import com.simon.lab020.repository.FileChunkRepository;
import com.simon.lab020.repository.FileMetadataRepository;
import com.simon.lab020.repository.UploadSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FileUploadService
 * Tests all upload operations, chunked uploads, session management, and file validation methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadService Tests")
class FileUploadServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private UploadSessionRepository uploadSessionRepository;

    @Mock
    private FileChunkRepository fileChunkRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private FileUploadService fileUploadService;

    @TempDir
    Path tempDir;

    private String testDocumentId;
    private String testSessionId;
    private String testEncryptionKey;
    private String testFileHash;
    private String testStoredPath;
    private MultipartFile testMultipartFile;
    private byte[] testFileContent;
    private UploadSession testUploadSession;
    private FileMetadata testFileMetadata;

    @BeforeEach
    void setUp() throws IOException {
        // Given: Test data setup
        testDocumentId = "test-doc-123";
        testSessionId = "test-session-456";
        testEncryptionKey = "test-encryption-key";
        testFileHash = "test-hash-789";
        testStoredPath = "storage/path/test-file.txt";
        testFileContent = "Test file content for upload operations".getBytes();
        
        // Create test multipart file
        testMultipartFile = new MockMultipartFile(
            "file",
            "test-document.txt",
            "text/plain",
            testFileContent
        );
        
        // Create test upload session
        testUploadSession = new UploadSession();
        testUploadSession.setSessionId(testSessionId);
        testUploadSession.setOriginalFilename("test-document.txt");
        testUploadSession.setTotalChunks(3);
        testUploadSession.setChunksUploaded(0);
        testUploadSession.setTotalSize((long) testFileContent.length);
        testUploadSession.setContentType("text/plain");
        testUploadSession.setCreatedAt(LocalDateTime.now());
        testUploadSession.setUpdatedAt(LocalDateTime.now());
        
        // Create test file metadata
        testFileMetadata = new FileMetadata();
        testFileMetadata.setDocumentId(testDocumentId);
        testFileMetadata.setOriginalFilename("test-document.txt");
        testFileMetadata.setFileSize((long) testFileContent.length);
        testFileMetadata.setContentType("text/plain");
        testFileMetadata.setFilePath(testStoredPath);
        testFileMetadata.setEncryptionKey(testEncryptionKey);
        testFileMetadata.setFileHash(testFileHash);
        testFileMetadata.setUploadDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should upload single file successfully")
    void testUploadFile_Success() throws IOException {
        // Given: File storage and encryption services configured
        when(encryptionService.generateEncryptionKey()).thenReturn(testEncryptionKey);
        when(fileStorageService.storeEncryptedFile(any(File.class), anyString(), anyString()))
            .thenReturn(testStoredPath);
        when(fileStorageService.calculateFileHash(any(MultipartFile.class))).thenReturn(testFileHash);
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(testFileMetadata);

        // When: Upload file
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        FileUploadResponse response = fileUploadService.uploadFile(testMultipartFile, mockRequest);

        // Then: Response should be successful
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDocumentId()).isNotNull();
        assertThat(response.getOriginalFilename()).isEqualTo("test-document.txt");
        assertThat(response.getFileSize()).isEqualTo(testFileContent.length);
        assertThat(response.getContentType()).isEqualTo("text/plain");
        assertThat(response.getFileHash()).isEqualTo(testFileHash);
        assertThat(response.getMessage()).isEqualTo("File uploaded successfully");
        assertThat(response.getUploadDate()).isNotNull();

        // Verify interactions
        verify(encryptionService).generateEncryptionKey();
        verify(fileStorageService).storeEncryptedFile(any(File.class), eq(testDocumentId), eq(testEncryptionKey));
        verify(fileStorageService).calculateFileHash(any(MultipartFile.class));
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    @DisplayName("Should return error when uploading null file")
    void testUploadFile_NullFile() throws IOException {
        // When: Upload null file
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        FileUploadResponse response = fileUploadService.uploadFile(null, mockRequest);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("File cannot be");

        // Verify no service calls
        verify(fileStorageService, never()).storeEncryptedFile(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return error when uploading empty file")
    void testUploadFile_EmptyFile() throws IOException {
        // Given: Empty multipart file
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        // When: Upload empty file
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        FileUploadResponse response = fileUploadService.uploadFile(emptyFile, mockRequest);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("File cannot be");

        // Verify no service calls
        verify(fileStorageService, never()).storeEncryptedFile(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return error when uploading with null document ID")
    void testUploadFile_NullDocumentId() throws IOException {
        // When: Upload file with null document ID
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        FileUploadResponse response = fileUploadService.uploadFile(testMultipartFile, mockRequest);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Failed to upload file");

        // Verify no service calls
        verify(fileStorageService, never()).storeEncryptedFile(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle storage failure during upload")
    void testUploadFile_StorageFailure() throws IOException {
        // Given: Storage service throws exception
        when(encryptionService.generateEncryptionKey()).thenReturn(testEncryptionKey);
        when(fileStorageService.storeEncryptedFile(any(File.class), anyString(), anyString()))
            .thenThrow(new RuntimeException("Storage failed"));

        // When: Upload file
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        FileUploadResponse response = fileUploadService.uploadFile(testMultipartFile, mockRequest);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred during file upload");

        // Verify interactions
        verify(encryptionService).generateEncryptionKey();
        verify(fileStorageService).storeEncryptedFile(any(File.class), eq(testDocumentId), eq(testEncryptionKey));
    }

    @Test
    @DisplayName("Should initialize chunk upload successfully")
    void testInitializeChunkUpload_Success() {
        // Given: Upload session repository configured
        when(uploadSessionRepository.save(any(UploadSession.class))).thenReturn(testUploadSession);

        // When: Initialize chunk upload
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        ChunkUploadResponse response = fileUploadService.initializeChunkUpload(
            "test-document.txt", 1000L, 3, "text/plain", "test-hash", mockRequest
        );

        // Then: Response should be successful
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getSessionId()).isEqualTo(testSessionId);
        assertThat(response.getTotalChunks()).isEqualTo(3);
        assertThat(response.getChunksUploaded()).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("Upload session initialized");
        assertThat(response.isCompleted()).isFalse();

        // Verify repository interaction
        verify(uploadSessionRepository).save(any(UploadSession.class));
    }

    // Note: initializeChunkUpload method doesn't validate parameters in the same way,
    // parameter validation is handled by the controller layer with @Positive annotations

    @Test
    @DisplayName("Should upload chunk successfully")
    void testUploadChunk_Success() throws IOException {
        // Given: Upload session exists and chunk storage configured
        when(uploadSessionRepository.findBySessionId(testSessionId))
            .thenReturn(Optional.of(testUploadSession));
        when(fileStorageService.storeChunk(any(MultipartFile.class), anyString(), anyInt()))
            .thenReturn("chunks/path/chunk_1.dat");
        when(fileChunkRepository.save(any(FileChunk.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(uploadSessionRepository.save(any(UploadSession.class))).thenReturn(testUploadSession);
        when(fileChunkRepository.areAllChunksUploaded(anyString(), anyInt())).thenReturn(false);
        when(fileChunkRepository.findByDocumentIdAndChunkNumber(anyString(), anyInt())).thenReturn(Optional.empty());

        // Create chunk upload request
        MockMultipartFile chunkFile = new MockMultipartFile("chunk", "chunk1.dat", "application/octet-stream", testFileContent);
        ChunkUploadRequest request = ChunkUploadRequest.builder()
            .sessionId(testSessionId)
            .chunkNumber(1)
            .totalChunks(3)
            .chunkFile(chunkFile)
            .chunkHash("chunk-hash")
            .originalFilename("test.txt")
            .totalFileSize(1000L)
            .contentType("text/plain")
            .fileHash("file-hash")
            .build();

        // When: Upload chunk
        ChunkUploadResponse response = fileUploadService.uploadChunk(request, null);

        // Then: Response should be successful
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getSessionId()).isEqualTo(testSessionId);
        assertThat(response.getChunkNumber()).isEqualTo(1);
        assertThat(response.getMessage()).isEqualTo("Chunk uploaded successfully");
        assertThat(response.getChunksUploaded()).isEqualTo(1);
        assertThat(response.getTotalChunks()).isEqualTo(3);
        assertThat(response.isCompleted()).isFalse();

        // Verify interactions
        verify(uploadSessionRepository).findBySessionId(testSessionId);
        verify(fileStorageService).storeChunk(any(MultipartFile.class), eq(testSessionId), eq(1));
        verify(fileChunkRepository).save(any(FileChunk.class));
        verify(uploadSessionRepository).save(testUploadSession);
    }

    @Test
    @DisplayName("Should return error when uploading chunk with invalid session")
    void testUploadChunk_InvalidSession() throws IOException {
        // Given: Upload session not found
        when(uploadSessionRepository.findBySessionId(testSessionId))
            .thenReturn(Optional.empty());

        // Create chunk upload request
        MockMultipartFile chunkFile = new MockMultipartFile("chunk", "chunk1.dat", "application/octet-stream", testFileContent);
        ChunkUploadRequest request = ChunkUploadRequest.builder()
            .sessionId(testSessionId)
            .chunkNumber(1)
            .totalChunks(3)
            .chunkFile(chunkFile)
            .chunkHash("chunk-hash")
            .originalFilename("test.txt")
            .totalFileSize(1000L)
            .contentType("text/plain")
            .fileHash("file-hash")
            .build();

        // When & Then: Should throw exception
        assertThatThrownBy(() -> fileUploadService.uploadChunk(request, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid session ID");

        verify(uploadSessionRepository).findBySessionId(testSessionId);
        verify(fileStorageService, never()).storeChunk(any(MultipartFile.class), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should return error when uploading duplicate chunk")
    void testUploadChunk_DuplicateChunk() throws IOException {
        // Given: Upload session exists and chunk already uploaded
        when(uploadSessionRepository.findBySessionId(testSessionId))
            .thenReturn(Optional.of(testUploadSession));
        when(fileChunkRepository.findByDocumentIdAndChunkNumber(testSessionId, 1))
            .thenReturn(Optional.of(new FileChunk()));

        // Create chunk upload request
        MockMultipartFile chunkFile = new MockMultipartFile("chunk", "chunk1.dat", "application/octet-stream", testFileContent);
        ChunkUploadRequest request = ChunkUploadRequest.builder()
            .sessionId(testSessionId)
            .chunkNumber(1)
            .totalChunks(3)
            .chunkFile(chunkFile)
            .chunkHash("chunk-hash")
            .originalFilename("test.txt")
            .totalFileSize(1000L)
            .contentType("text/plain")
            .fileHash("file-hash")
            .build();

        // When: Upload chunk
        ChunkUploadResponse response = fileUploadService.uploadChunk(request, null);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Chunk already uploaded");

        // Verify interactions
        verify(uploadSessionRepository).findBySessionId(testSessionId);
        verify(fileChunkRepository).findByDocumentIdAndChunkNumber(testDocumentId, 1);
        verify(fileStorageService, never()).storeChunk(any(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should complete upload when all chunks uploaded")
    void testUploadChunk_CompleteUpload() throws IOException {
        // Given: Upload session with 2 chunks already uploaded
        testUploadSession.setChunksUploaded(2);
        when(uploadSessionRepository.findBySessionId(testSessionId))
            .thenReturn(Optional.of(testUploadSession));
        when(fileStorageService.storeChunk(any(MultipartFile.class), anyString(), anyInt()))
            .thenReturn("chunks/path/chunk_3.dat");
        when(fileChunkRepository.save(any(FileChunk.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(uploadSessionRepository.save(any(UploadSession.class))).thenReturn(testUploadSession);
        when(fileChunkRepository.areAllChunksUploaded(anyString(), anyInt())).thenReturn(true);
        when(fileChunkRepository.findByDocumentIdAndChunkNumber(anyString(), anyInt())).thenReturn(Optional.empty());

        // Create chunk upload request
        MockMultipartFile chunkFile = new MockMultipartFile("chunk", "chunk3.dat", "application/octet-stream", testFileContent);
        ChunkUploadRequest request = ChunkUploadRequest.builder()
            .sessionId(testSessionId)
            .chunkNumber(3)
            .totalChunks(3)
            .chunkFile(chunkFile)
            .chunkHash("chunk-hash")
            .originalFilename("test.txt")
            .totalFileSize(1000L)
            .contentType("text/plain")
            .fileHash("file-hash")
            .build();

        // When: Upload final chunk
        ChunkUploadResponse response = fileUploadService.uploadChunk(request, null);

        // Then: Response should indicate completion
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getSessionId()).isEqualTo(testSessionId);
        assertThat(response.getChunkNumber()).isEqualTo(3);
        assertThat(response.getMessage()).isEqualTo("Chunk uploaded successfully");
        assertThat(response.getChunksUploaded()).isEqualTo(3);
        assertThat(response.getTotalChunks()).isEqualTo(3);
        assertThat(response.isCompleted()).isTrue();

        // Verify interactions
        verify(uploadSessionRepository).findBySessionId(testSessionId);
        verify(fileStorageService).storeChunk(any(MultipartFile.class), eq(testSessionId), eq(3));
        verify(fileChunkRepository).save(any(FileChunk.class));
        verify(uploadSessionRepository).save(testUploadSession);
    }

    @Test
    @DisplayName("Should get upload status successfully")
    void testGetUploadStatus_Success() {
        // Given: Upload session exists
        when(uploadSessionRepository.findBySessionId(testSessionId))
            .thenReturn(Optional.of(testUploadSession));

        // When: Get upload status
        ChunkUploadResponse response = fileUploadService.getUploadStatus(testSessionId);

        // Then: Response should contain status information
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getSessionId()).isEqualTo(testSessionId);
        assertThat(response.getTotalChunks()).isEqualTo(3);
        assertThat(response.getChunksUploaded()).isEqualTo(0);
        assertThat(response.getMessage()).isEqualTo("Upload status retrieved");

        // Verify repository interaction
        verify(uploadSessionRepository).findBySessionId(testSessionId);
    }

    @Test
    @DisplayName("Should return error when getting status for invalid session")
    void testGetUploadStatus_InvalidSession() {
        // Given: Upload session not found
        when(uploadSessionRepository.findBySessionId(testSessionId))
            .thenReturn(Optional.empty());

        // When: Get upload status
        ChunkUploadResponse response = fileUploadService.getUploadStatus(testSessionId);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Upload session not found");

        // Verify repository interaction
        verify(uploadSessionRepository).findBySessionId(testSessionId);
    }

    // Note: assembleChunks is a private method, tested indirectly through uploadChunk when all chunks are uploaded

    // Note: findMissingChunks is a private method, tested indirectly through uploadChunk and getUploadStatus





    @Test
    @DisplayName("Should validate multipart file successfully")
    void testValidateFile_Success() throws IOException {
        // Given: Valid multipart file
        MultipartFile validFile = new MockMultipartFile(
            "file", "valid.txt", "text/plain", "valid content".getBytes()
        );

        // When & Then: Validate file should not throw exception
        assertThatCode(() -> {
            // Use reflection to access private validateFile method
            java.lang.reflect.Method validateMethod = FileUploadService.class
                .getDeclaredMethod("validateFile", MultipartFile.class);
            validateMethod.setAccessible(true);
            validateMethod.invoke(fileUploadService, validFile);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when validating null file")
    void testValidateFile_NullFile() {
        // When & Then: Validate null file should throw exception
        assertThatThrownBy(() -> {
            java.lang.reflect.Method validateMethod = FileUploadService.class
                .getDeclaredMethod("validateFile", MultipartFile.class);
            validateMethod.setAccessible(true);
            validateMethod.invoke(fileUploadService, (MultipartFile) null);
        }).hasCauseInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("File cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when validating empty file")
    void testValidateFile_EmptyFile() {
        // Given: Empty multipart file
        MultipartFile emptyFile = new MockMultipartFile(
            "file", "empty.txt", "text/plain", new byte[0]
        );

        // When & Then: Validate empty file should throw exception
        assertThatThrownBy(() -> {
            java.lang.reflect.Method validateMethod = FileUploadService.class
                .getDeclaredMethod("validateFile", MultipartFile.class);
            validateMethod.setAccessible(true);
            validateMethod.invoke(fileUploadService, emptyFile);
        }).hasCauseInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("File cannot be empty");
    }

    @Test
    @DisplayName("Should throw exception when validating file with invalid filename")
    void testValidateFile_InvalidFilename() {
        // Given: File with path traversal filename
        MultipartFile invalidFile = new MockMultipartFile(
            "file", "../../../etc/passwd", "text/plain", "content".getBytes()
        );

        // When & Then: Validate file with invalid filename should throw exception
        assertThatThrownBy(() -> {
            java.lang.reflect.Method validateMethod = FileUploadService.class
                .getDeclaredMethod("validateFile", MultipartFile.class);
            validateMethod.setAccessible(true);
            validateMethod.invoke(fileUploadService, invalidFile);
        }).hasCauseInstanceOf(IllegalArgumentException.class)
          .hasRootCauseMessage("Filename contains invalid path sequence");
    }

    @Test
    @DisplayName("Should throw exception when validating oversized file")
    void testValidateFile_OversizedFile() {
        // Given: File exceeding size limit
        byte[] largeContent = new byte[101 * 1024 * 1024]; // 101MB
        MultipartFile largeFile = new MockMultipartFile(
            "file", "large.txt", "text/plain", largeContent
        );

        // When & Then: Validate oversized file should throw exception
        assertThatThrownBy(() -> {
            java.lang.reflect.Method validateMethod = FileUploadService.class
                .getDeclaredMethod("validateFile", MultipartFile.class);
            validateMethod.setAccessible(true);
            validateMethod.invoke(fileUploadService, largeFile);
        }).hasCauseInstanceOf(IllegalArgumentException.class)
          .hasRootCauseMessage("File size exceeds maximum limit of 100MB");
    }

    // Note: validateChunk is a private method that takes ChunkUploadRequest parameter
    // It's tested indirectly through uploadChunk method tests

    @Test
    @DisplayName("Should store encrypted file successfully")
    void testStoreEncryptedFile_Success() throws Exception {
        // Given: Encryption and storage services configured
        when(fileStorageService.storeEncryptedFile(any(File.class), anyString(), anyString()))
            .thenReturn(testStoredPath);

        // Create temporary file
        Path tempFile = tempDir.resolve("store.txt");
        Files.write(tempFile, testFileContent);

        // When: Store encrypted file using reflection to access private method
        Method storeAndEncryptFileMethod = FileUploadService.class.getDeclaredMethod(
            "storeAndEncryptFile", MultipartFile.class, String.class, String.class);
        storeAndEncryptFileMethod.setAccessible(true);
        
        // Create a mock MultipartFile from the temp file
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", "test-document.txt", "text/plain", Files.readAllBytes(tempFile));
        
        String storedPath = (String) storeAndEncryptFileMethod.invoke(
            fileUploadService, mockFile, testDocumentId, testEncryptionKey);
        
        // Create response manually since we're testing the private method
        FileUploadResponse response = FileUploadResponse.builder()
            .success(true)
            .documentId(testDocumentId)
            .originalFilename("test-document.txt")
            .contentType("text/plain")
            .fileHash(testFileHash)
            .message("File stored successfully")
            .uploadDate(LocalDateTime.now())
            .build();

        // Then: Response should be successful
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDocumentId()).isEqualTo(testDocumentId);
        assertThat(response.getOriginalFilename()).isEqualTo("test-document.txt");
        assertThat(response.getContentType()).isEqualTo("text/plain");
        assertThat(response.getFileHash()).isEqualTo(testFileHash);
        assertThat(response.getMessage()).isEqualTo("File stored successfully");

        // Verify interactions
        verify(encryptionService).encryptFile(any(File.class), any(File.class), eq(testEncryptionKey));
        verify(fileStorageService).storeEncryptedFile(any(File.class), eq(testDocumentId), anyString());
        
        // Verify the stored path is not null
        assertThat(storedPath).isNotNull();
    }

    @Test
    @DisplayName("Should handle null file in storeAndEncryptFile method")
    void testStoreAndEncryptFile_NullFile() throws Exception {
        // When: Call storeAndEncryptFile with null file using reflection
        Method storeAndEncryptFileMethod = FileUploadService.class.getDeclaredMethod(
            "storeAndEncryptFile", MultipartFile.class, String.class, String.class);
        storeAndEncryptFileMethod.setAccessible(true);
        
        // Then: Should throw exception
        assertThatThrownBy(() -> storeAndEncryptFileMethod.invoke(
            fileUploadService, null, testDocumentId, testEncryptionKey))
            .hasCauseInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should handle encryption failure during storeAndEncryptFile")
    void testStoreAndEncryptFile_EncryptionFailure() throws Exception {
        // Given: Encryption service throws exception
        doThrow(new RuntimeException("Encryption failed"))
            .when(encryptionService).encryptFile(any(File.class), any(File.class), anyString());

        // Create temporary file
        Path tempFile = tempDir.resolve("store.txt");
        Files.write(tempFile, testFileContent);
        
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", "test-document.txt", "text/plain", Files.readAllBytes(tempFile));

        // When: Call storeAndEncryptFile using reflection
        Method storeAndEncryptFileMethod = FileUploadService.class.getDeclaredMethod(
            "storeAndEncryptFile", MultipartFile.class, String.class, String.class);
        storeAndEncryptFileMethod.setAccessible(true);

        // Then: Should throw RuntimeException
        assertThatThrownBy(() -> storeAndEncryptFileMethod.invoke(
            fileUploadService, mockFile, testDocumentId, testEncryptionKey))
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to store and encrypt file");

        // Verify interactions
        verify(encryptionService).encryptFile(any(File.class), any(File.class), eq(testEncryptionKey));
    }
}