package com.simon.lab020.service;

import com.simon.lab020.dto.FileDownloadResponse;
import com.simon.lab020.entity.FileMetadata;
import com.simon.lab020.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FileDownloadService
 * Tests all download operations, metadata retrieval, and file validation methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileDownloadService Tests")
class FileDownloadServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private FileDownloadService fileDownloadService;

    @TempDir
    Path tempDir;

    private FileMetadata testFileMetadata;
    private String testDocumentId;
    private String testEncryptionKey;
    private Path testFilePath;
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        // Given: Test data setup
        testDocumentId = "test-doc-123";
        testEncryptionKey = "test-encryption-key";
        testFilePath = tempDir.resolve("test-file.txt");
        testFile = testFilePath.toFile();
        
        // Create test file with content
        Files.writeString(testFilePath, "Test file content for download");
        
        testFileMetadata = new FileMetadata();
        testFileMetadata.setDocumentId(testDocumentId);
        testFileMetadata.setOriginalFilename("test-document.txt");
        testFileMetadata.setFileSize(26L);
        testFileMetadata.setContentType("text/plain");
        testFileMetadata.setFilePath("storage/path/test-file.txt");
        testFileMetadata.setEncryptionKey(testEncryptionKey);
        testFileMetadata.setFileHash("test-hash-123");
        testFileMetadata.setUploadDate(LocalDateTime.now().minusDays(1));
    }

    @Test
    @DisplayName("Should download file successfully")
    void testDownloadFile_Success() throws IOException {
        // Given: File exists and can be decrypted
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));
        when(fileStorageService.loadFile(testFileMetadata.getFilePath()))
            .thenReturn(testFilePath);
        when(fileStorageService.calculateFileHash(anyString()))
            .thenReturn(testFileMetadata.getFileHash());
        doAnswer(invocation -> {
            File targetFile = invocation.getArgument(1);
            Files.writeString(targetFile.toPath(), "Test file content for download");
            return null;
        }).when(encryptionService).decryptFile(any(File.class), any(File.class), anyString());

        // When: Download file
        FileDownloadResponse response = fileDownloadService.downloadFile(testDocumentId);

        // Then: Response should be successful
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDocumentId()).isEqualTo(testDocumentId);
        assertThat(response.getOriginalFilename()).isEqualTo("test-document.txt");
        assertThat(response.getFileSize()).isEqualTo(26L);
        assertThat(response.getContentType()).isEqualTo("text/plain");
        assertThat(response.getFileHash()).isEqualTo("test-hash-123");
        assertThat(response.getFileResource()).isNotNull();
        assertThat(response.getMessage()).isEqualTo("File downloaded successfully");
        assertThat(response.getDownloadDate()).isNotNull();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService).loadFile(testFileMetadata.getFilePath());
        verify(encryptionService).decryptFile(any(File.class), any(File.class), eq(testEncryptionKey));
    }

    @Test
    @DisplayName("Should return error when document ID is null")
    void testDownloadFile_NullDocumentId() throws IOException {
        // When: Download file with null document ID
        FileDownloadResponse response = fileDownloadService.downloadFile(null);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid document ID");

        // Verify no repository calls
        verify(fileMetadataRepository, never()).findByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should return error when document ID is empty")
    void testDownloadFile_EmptyDocumentId() throws IOException {
        // When: Download file with empty document ID
        FileDownloadResponse response = fileDownloadService.downloadFile("   ");

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid document ID");

        // Verify no repository calls
        verify(fileMetadataRepository, never()).findByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should return error when file not found")
    void testDownloadFile_FileNotFound() throws IOException {
        // Given: File metadata not found
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.empty());

        // When: Download file
        FileDownloadResponse response = fileDownloadService.downloadFile(testDocumentId);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("File not found");

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService, never()).loadFile(anyString());
    }

    @Test
    @DisplayName("Should return error when file storage fails")
    void testDownloadFile_StorageFailure() throws IOException {
        // Given: File metadata exists but storage fails
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));
        when(fileStorageService.loadFile(testFileMetadata.getFilePath()))
            .thenThrow(new RuntimeException("Storage error"));

        // When: Download file
        FileDownloadResponse response = fileDownloadService.downloadFile(testDocumentId);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred during file download");

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService).loadFile(testFileMetadata.getFilePath());
    }

    @Test
    @DisplayName("Should return error when decryption fails")
    void testDownloadFile_DecryptionFailure() throws IOException {
        // Given: File exists but decryption fails
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));
        when(fileStorageService.loadFile(testFileMetadata.getFilePath()))
            .thenReturn(testFilePath);
        doThrow(new RuntimeException("Decryption failed"))
            .when(encryptionService).decryptFile(any(File.class), any(File.class), anyString());

        // When: Download file
        FileDownloadResponse response = fileDownloadService.downloadFile(testDocumentId);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred during file download");

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService).loadFile(testFileMetadata.getFilePath());
        verify(encryptionService).decryptFile(any(File.class), any(File.class), eq(testEncryptionKey));
    }

    @Test
    @DisplayName("Should continue download when integrity check fails")
    void testDownloadFile_IntegrityCheckFailure() throws IOException {
        // Given: File exists but integrity check fails
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));
        when(fileStorageService.loadFile(testFileMetadata.getFilePath()))
            .thenReturn(testFilePath);
        when(fileStorageService.calculateFileHash(anyString()))
            .thenReturn("different-hash"); // Different hash to simulate integrity failure
        doAnswer(invocation -> {
            File targetFile = invocation.getArgument(1);
            Files.writeString(targetFile.toPath(), "Test file content for download");
            return null;
        }).when(encryptionService).decryptFile(any(File.class), any(File.class), anyString());

        // When: Download file
        FileDownloadResponse response = fileDownloadService.downloadFile(testDocumentId);

        // Then: Should return error due to integrity check failure
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("File integrity check failed");

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService).loadFile(testFileMetadata.getFilePath());
        verify(fileStorageService).calculateFileHash(anyString());
    }

    @Test
    @DisplayName("Should get file metadata successfully")
    void testGetFileMetadata_Success() {
        // Given: File metadata exists
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));

        // When: Get file metadata
        FileDownloadResponse response = fileDownloadService.getFileMetadata(testDocumentId);

        // Then: Should return metadata without file resource
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getDocumentId()).isEqualTo(testDocumentId);
        assertThat(response.getOriginalFilename()).isEqualTo("test-document.txt");
        assertThat(response.getFileSize()).isEqualTo(26L);
        assertThat(response.getContentType()).isEqualTo("text/plain");
        assertThat(response.getFileHash()).isEqualTo("test-hash-123");
        assertThat(response.getFileResource()).isNull(); // No file resource for metadata only
        assertThat(response.getMessage()).isEqualTo("File metadata retrieved successfully");
        assertThat(response.getDownloadDate()).isNotNull();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService, never()).loadFile(anyString());
    }

    @Test
    @DisplayName("Should return error when getting metadata for null document ID")
    void testGetFileMetadata_NullDocumentId() {
        // When: Get metadata with null document ID
        FileDownloadResponse response = fileDownloadService.getFileMetadata(null);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid document ID");

        // Verify no repository calls
        verify(fileMetadataRepository, never()).findByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should return error when getting metadata for non-existent file")
    void testGetFileMetadata_FileNotFound() {
        // Given: File metadata not found
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.empty());

        // When: Get file metadata
        FileDownloadResponse response = fileDownloadService.getFileMetadata(testDocumentId);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("File not found");

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should handle exception during metadata retrieval")
    void testGetFileMetadata_Exception() {
        // Given: Repository throws exception
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenThrow(new RuntimeException("Database error"));

        // When: Get file metadata
        FileDownloadResponse response = fileDownloadService.getFileMetadata(testDocumentId);

        // Then: Should return error response
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred during metadata retrieval");

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should check if file exists successfully")
    void testFileExists_Success() {
        // Given: File metadata exists
        when(fileMetadataRepository.existsByDocumentId(testDocumentId))
            .thenReturn(true);

        // When: Check if file exists
        boolean exists = fileDownloadService.fileExists(testDocumentId);

        // Then: Should return true
        assertThat(exists).isTrue();

        // Verify interactions
        verify(fileMetadataRepository).existsByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should return false when file does not exist")
    void testFileExists_NotFound() {
        // Given: File metadata does not exist
        when(fileMetadataRepository.existsByDocumentId(testDocumentId))
            .thenReturn(false);

        // When: Check if file exists
        boolean exists = fileDownloadService.fileExists(testDocumentId);

        // Then: Should return false
        assertThat(exists).isFalse();

        // Verify interactions
        verify(fileMetadataRepository).existsByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should return false when checking existence with null document ID")
    void testFileExists_NullDocumentId() {
        // When: Check existence with null document ID
        boolean exists = fileDownloadService.fileExists(null);

        // Then: Should return false
        assertThat(exists).isFalse();

        // Verify no repository calls
        verify(fileMetadataRepository, never()).existsByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should return false when checking existence with empty document ID")
    void testFileExists_EmptyDocumentId() {
        // When: Check existence with empty document ID
        boolean exists = fileDownloadService.fileExists("   ");

        // Then: Should return false
        assertThat(exists).isFalse();

        // Verify no repository calls
        verify(fileMetadataRepository, never()).existsByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should handle exception when checking file existence")
    void testFileExists_Exception() {
        // Given: Repository throws exception
        when(fileMetadataRepository.existsByDocumentId(testDocumentId))
            .thenThrow(new RuntimeException("Database error"));

        // When: Check if file exists
        boolean exists = fileDownloadService.fileExists(testDocumentId);

        // Then: Should return false
        assertThat(exists).isFalse();

        // Verify interactions
        verify(fileMetadataRepository).existsByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should get file size successfully")
    void testGetFileSize_Success() {
        // Given: File metadata exists
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));

        // When: Get file size
        Long fileSize = fileDownloadService.getFileSize(testDocumentId);

        // Then: Should return correct size
        assertThat(fileSize).isEqualTo(26L);

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should return null when getting size for non-existent file")
    void testGetFileSize_FileNotFound() {
        // Given: File metadata not found
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.empty());

        // When: Get file size
        Long fileSize = fileDownloadService.getFileSize(testDocumentId);

        // Then: Should return null
        assertThat(fileSize).isNull();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should return null when getting size with null document ID")
    void testGetFileSize_NullDocumentId() {
        // When: Get file size with null document ID
        Long fileSize = fileDownloadService.getFileSize(null);

        // Then: Should return null
        assertThat(fileSize).isNull();

        // Verify no repository calls
        verify(fileMetadataRepository, never()).findByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should handle exception when getting file size")
    void testGetFileSize_Exception() {
        // Given: Repository throws exception
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenThrow(new RuntimeException("Database error"));

        // When: Get file size
        Long fileSize = fileDownloadService.getFileSize(testDocumentId);

        // Then: Should return null
        assertThat(fileSize).isNull();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should get content type successfully")
    void testGetContentType_Success() {
        // Given: File metadata exists
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));

        // When: Get content type
        String contentType = fileDownloadService.getContentType(testDocumentId);

        // Then: Should return correct content type
        assertThat(contentType).isEqualTo("text/plain");

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should return null when getting content type for non-existent file")
    void testGetContentType_FileNotFound() {
        // Given: File metadata not found
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.empty());

        // When: Get content type
        String contentType = fileDownloadService.getContentType(testDocumentId);

        // Then: Should return null
        assertThat(contentType).isNull();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should return null when getting content type with null document ID")
    void testGetContentType_NullDocumentId() {
        // When: Get content type with null document ID
        String contentType = fileDownloadService.getContentType(null);

        // Then: Should return null
        assertThat(contentType).isNull();

        // Verify no repository calls
        verify(fileMetadataRepository, never()).findByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should handle exception when getting content type")
    void testGetContentType_Exception() {
        // Given: Repository throws exception
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenThrow(new RuntimeException("Database error"));

        // When: Get content type
        String contentType = fileDownloadService.getContentType(testDocumentId);

        // Then: Should return null
        assertThat(contentType).isNull();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }

    @Test
    @DisplayName("Should check file accessibility successfully")
    void testIsFileAccessible_Success() {
        // Given: File metadata exists and physical file is accessible
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));
        when(fileStorageService.loadFile(testFileMetadata.getFilePath()))
            .thenReturn(testFilePath);

        // When: Check file accessibility
        boolean accessible = fileDownloadService.isFileAccessible(testDocumentId);

        // Then: Should return true
        assertThat(accessible).isTrue();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService).loadFile(testFileMetadata.getFilePath());
    }

    @Test
    @DisplayName("Should return false when file metadata not found")
    void testIsFileAccessible_MetadataNotFound() {
        // Given: File metadata not found
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.empty());

        // When: Check file accessibility
        boolean accessible = fileDownloadService.isFileAccessible(testDocumentId);

        // Then: Should return false
        assertThat(accessible).isFalse();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService, never()).loadFile(anyString());
    }

    @Test
    @DisplayName("Should return false when physical file not accessible")
    void testIsFileAccessible_PhysicalFileNotAccessible() throws IOException {
        // Given: File metadata exists but physical file is not accessible
        Path nonExistentPath = tempDir.resolve("non-existent.txt");
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));
        when(fileStorageService.loadFile(testFileMetadata.getFilePath()))
            .thenReturn(nonExistentPath);

        // When: Check file accessibility
        boolean accessible = fileDownloadService.isFileAccessible(testDocumentId);

        // Then: Should return false
        assertThat(accessible).isFalse();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService).loadFile(testFileMetadata.getFilePath());
    }

    @Test
    @DisplayName("Should return false when checking accessibility with null document ID")
    void testIsFileAccessible_NullDocumentId() {
        // When: Check accessibility with null document ID
        boolean accessible = fileDownloadService.isFileAccessible(null);

        // Then: Should return false
        assertThat(accessible).isFalse();

        // Verify no repository calls
        verify(fileMetadataRepository, never()).findByDocumentId(anyString());
    }

    @Test
    @DisplayName("Should return false when storage service throws exception")
    void testIsFileAccessible_StorageException() {
        // Given: File metadata exists but storage service throws exception
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenReturn(Optional.of(testFileMetadata));
        when(fileStorageService.loadFile(testFileMetadata.getFilePath()))
            .thenThrow(new RuntimeException("Storage error"));

        // When: Check file accessibility
        boolean accessible = fileDownloadService.isFileAccessible(testDocumentId);

        // Then: Should return false
        assertThat(accessible).isFalse();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
        verify(fileStorageService).loadFile(testFileMetadata.getFilePath());
    }

    @Test
    @DisplayName("Should handle repository exception during accessibility check")
    void testIsFileAccessible_RepositoryException() {
        // Given: Repository throws exception
        when(fileMetadataRepository.findByDocumentId(testDocumentId))
            .thenThrow(new RuntimeException("Database error"));

        // When: Check file accessibility
        boolean accessible = fileDownloadService.isFileAccessible(testDocumentId);

        // Then: Should return false
        assertThat(accessible).isFalse();

        // Verify interactions
        verify(fileMetadataRepository).findByDocumentId(testDocumentId);
    }
}