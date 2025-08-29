package com.simon.lab020.service;

import com.simon.lab020.entity.FileChunk;
import com.simon.lab020.repository.FileChunkRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for FileStorageService
 * Tests all file storage operations, encryption, chunking, and file management methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageService Tests")
class FileStorageServiceTest {

    @Mock
    private FileChunkRepository fileChunkRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    private String testStorageBasePath;
    private String testDocumentId;
    private String testEncryptionKey;
    private File testFile;
    private Path testFilePath;
    private byte[] testFileContent;
    private MultipartFile testMultipartFile;

    @BeforeEach
    void setUp() throws IOException {
        // Given: Test data setup
        testStorageBasePath = tempDir.toString();
        testDocumentId = "test-doc-123";
        testEncryptionKey = "test-encryption-key";
        testFilePath = tempDir.resolve("test-file.txt");
        testFile = testFilePath.toFile();
        testFileContent = "Test file content for storage operations".getBytes();
        
        // Create test file with content
        Files.write(testFilePath, testFileContent);
        
        // Create MockMultipartFile for testing
        testMultipartFile = new MockMultipartFile(
            "file",
            "test-file.txt",
            "text/plain",
            testFileContent
        );
        
        // Set storage paths using reflection
        ReflectionTestUtils.setField(fileStorageService, "basePath", Paths.get(testStorageBasePath));
        ReflectionTestUtils.setField(fileStorageService, "tempPath", Paths.get(testStorageBasePath, "temp"));
        ReflectionTestUtils.setField(fileStorageService, "chunksPath", Paths.get(testStorageBasePath, "chunks"));
    }

    @Test
    @DisplayName("Should store file successfully")
    void testStoreFile_Success() throws IOException {
        // When: Store file
        String storedPath = fileStorageService.storeFile(testMultipartFile, testDocumentId);

        // Then: File should be stored with correct path
        assertThat(storedPath).isNotNull();
        assertThat(storedPath).contains(testDocumentId);
        
        // Verify stored file exists and has correct content
        Path storedFilePath = Paths.get(testStorageBasePath, storedPath);
        assertThat(Files.exists(storedFilePath)).isTrue();
        assertThat(Files.readAllBytes(storedFilePath)).isEqualTo(testFileContent);
    }

    @Test
    @DisplayName("Should throw exception when storing null file")
    void testStoreFile_NullFile() {
        // When & Then: Store null file should throw exception
        assertThatThrownBy(() -> fileStorageService.storeFile(null, testDocumentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when storing with null document ID")
    void testStoreFile_NullDocumentId() {
        // When & Then: Store file with null document ID should throw exception
        assertThatThrownBy(() -> fileStorageService.storeFile(testMultipartFile, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Document ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when storing with empty document ID")
    void testStoreFile_EmptyDocumentId() {
        // When & Then: Store file with empty document ID should throw exception
        assertThatThrownBy(() -> fileStorageService.storeFile(testMultipartFile, "   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Document ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when storing empty file")
    void testStoreFile_EmptyFile() {
        // Given: Empty file
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "empty.txt", "text/plain", new byte[0]);

        // When & Then: Store empty file should throw exception
        assertThatThrownBy(() -> fileStorageService.storeFile(emptyFile, testDocumentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Cannot store empty file");
    }

    @Test
    @DisplayName("Should store encrypted file successfully")
    void testStoreEncryptedFile_Success() throws IOException {
        // Given: Encryption service configured
        Path encryptedPath = tempDir.resolve("encrypted-file.enc");
        doAnswer(invocation -> {
            File targetFile = invocation.getArgument(1);
            Files.write(targetFile.toPath(), "encrypted content".getBytes());
            return null;
        }).when(encryptionService).encryptFile(any(File.class), any(File.class), anyString());

        // When: Store encrypted file
        String storedPath = fileStorageService.storeEncryptedFile(testFile, testDocumentId, testEncryptionKey);

        // Then: File should be stored with correct path
        assertThat(storedPath).isNotNull();
        assertThat(storedPath).contains(testDocumentId);
        
        // Verify encryption service was called
        verify(encryptionService).encryptFile(eq(testFile), any(File.class), eq(testEncryptionKey));
    }

    @Test
    @DisplayName("Should throw exception when storing encrypted file with null encryption key")
    void testStoreEncryptedFile_NullEncryptionKey() {
        // When & Then: Store encrypted file with null key should throw exception
        assertThatThrownBy(() -> fileStorageService.storeEncryptedFile(testFile, testDocumentId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Encryption key cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when encryption fails")
    void testStoreEncryptedFile_EncryptionFailure() throws IOException {
        // Given: Encryption service throws exception
        doThrow(new RuntimeException("Encryption failed"))
            .when(encryptionService).encryptFile(any(File.class), any(File.class), anyString());

        // When & Then: Store encrypted file should throw exception
        assertThatThrownBy(() -> fileStorageService.storeEncryptedFile(testFile, testDocumentId, testEncryptionKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to store encrypted file");
    }

    @Test
    @DisplayName("Should store chunk successfully")
    void testStoreChunk_Success() throws IOException {
        // Given: Chunk data
        byte[] chunkData = "chunk content".getBytes();
        MockMultipartFile chunkFile = new MockMultipartFile("chunk", "chunk.dat", "application/octet-stream", chunkData);
        int chunkNumber = 1;

        // When: Store chunk
        String chunkPath = fileStorageService.storeChunk(chunkFile, testDocumentId, chunkNumber);

        // Then: Chunk should be stored with correct path
        assertThat(chunkPath).isNotNull();
        assertThat(chunkPath).contains(testDocumentId);
        assertThat(chunkPath).contains("chunk_1");
        
        // Verify chunk file exists and has correct content
        Path storedChunkPath = Paths.get(testStorageBasePath, chunkPath);
        assertThat(Files.exists(storedChunkPath)).isTrue();
        assertThat(Files.readAllBytes(storedChunkPath)).isEqualTo(chunkData);
    }

    @Test
    @DisplayName("Should throw exception when storing null chunk data")
    void testStoreChunk_NullData() {
        // When & Then: Store null chunk data should throw exception
        assertThatThrownBy(() -> fileStorageService.storeChunk(null, testDocumentId, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when storing chunk with invalid chunk number")
    void testStoreChunk_InvalidChunkNumber() {
        // Given: Chunk data
        byte[] chunkData = "chunk content".getBytes();
        MockMultipartFile chunkFile = new MockMultipartFile("chunk", "chunk.dat", "application/octet-stream", chunkData);

        // When & Then: Store chunk with invalid number should throw exception
        assertThatThrownBy(() -> fileStorageService.storeChunk(chunkFile, testDocumentId, 0))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should assemble chunks successfully")
    void testAssembleChunks_Success() throws IOException {
        // Given: Multiple chunks stored
        FileChunk chunk1 = new FileChunk();
        chunk1.setDocumentId(testDocumentId);
        chunk1.setChunkNumber(1);
        chunk1.setChunkPath("chunks/" + testDocumentId + "/chunk_1.dat");
        
        FileChunk chunk2 = new FileChunk();
        chunk2.setDocumentId(testDocumentId);
        chunk2.setChunkNumber(2);
        chunk2.setChunkPath("chunks/" + testDocumentId + "/chunk_2.dat");
        
        List<FileChunk> chunks = Arrays.asList(chunk1, chunk2);
        
        // Create actual chunk files
        Path chunk1Path = Paths.get(testStorageBasePath, chunk1.getChunkPath());
        Path chunk2Path = Paths.get(testStorageBasePath, chunk2.getChunkPath());
        Files.createDirectories(chunk1Path.getParent());
        Files.write(chunk1Path, "chunk1 content".getBytes());
        Files.write(chunk2Path, "chunk2 content".getBytes());
        
        when(fileChunkRepository.findByDocumentIdOrderByChunkNumber(testDocumentId))
            .thenReturn(chunks);

        // When: Assemble chunks
        String assembledPath = fileStorageService.assembleChunks("test-session", 2, testDocumentId, "test.txt");

        // Then: File should be assembled correctly
        assertThat(assembledPath).isNotNull();
        assertThat(assembledPath).contains(testDocumentId);
        
        // Verify assembled file exists and has correct content
        Path assembledFilePath = Paths.get(testStorageBasePath, assembledPath);
        assertThat(Files.exists(assembledFilePath)).isTrue();
        String assembledContent = Files.readString(assembledFilePath);
        assertThat(assembledContent).isEqualTo("chunk1 contentchunk2 content");
        
        // Verify repository interaction
        verify(fileChunkRepository).findByDocumentIdOrderByChunkNumber(testDocumentId);
    }

    @Test
    @DisplayName("Should throw exception when assembling chunks with null document ID")
    void testAssembleChunks_NullDocumentId() {
        // When & Then: Assemble chunks with null session ID should throw exception
        assertThatThrownBy(() -> fileStorageService.assembleChunks(null, 2, testDocumentId, "test.txt"))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception when no chunks found for assembly")
    void testAssembleChunks_NoChunksFound() {
        // Given: No chunks found
        lenient().when(fileChunkRepository.findByDocumentIdOrderByChunkNumber(testDocumentId))
            .thenReturn(Arrays.asList());

        // When & Then: Assemble chunks should throw exception
        assertThatThrownBy(() -> fileStorageService.assembleChunks("test-session", 2, testDocumentId, "test.txt"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Missing chunk: 0");
    }

    @Test
    @DisplayName("Should load file successfully")
    void testLoadFile_Success() throws IOException {
        // Given: File stored in storage
        String relativePath = "files/" + testDocumentId + "/test-file.txt";
        Path storedFilePath = Paths.get(testStorageBasePath, relativePath);
        Files.createDirectories(storedFilePath.getParent());
        Files.write(storedFilePath, testFileContent);

        // When: Load file
        Path loadedPath = fileStorageService.loadFile(relativePath);

        // Then: File should be loaded correctly
        assertThat(loadedPath).isNotNull();
        assertThat(Files.exists(loadedPath)).isTrue();
        assertThat(Files.readAllBytes(loadedPath)).isEqualTo(testFileContent);
    }

    @Test
    @DisplayName("Should throw exception when loading null file path")
    void testLoadFile_NullPath() {
        // When & Then: Load file with null path should throw exception
        assertThatThrownBy(() -> fileStorageService.loadFile(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File path cannot be null or empty");
    }

    @Test
    @DisplayName("Should throw exception when loading non-existent file")
    void testLoadFile_NonExistentFile() {
        // Given: Non-existent file path
        String nonExistentPath = "files/non-existent.txt";

        // When & Then: Load non-existent file should throw exception
        assertThatThrownBy(() -> fileStorageService.loadFile(nonExistentPath))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("File not found: " + nonExistentPath);
    }

    @Test
    @DisplayName("Should delete file successfully")
    void testDeleteFile_Success() throws IOException {
        // Given: File stored in storage
        String relativePath = "files/" + testDocumentId + "/test-file.txt";
        Path storedFilePath = Paths.get(testStorageBasePath, relativePath);
        Files.createDirectories(storedFilePath.getParent());
        Files.write(storedFilePath, testFileContent);

        // When: Delete file
        fileStorageService.deleteFile(relativePath);

        // Then: File should be deleted
        assertThat(Files.exists(storedFilePath)).isFalse();
    }

    @Test
    @DisplayName("Should handle deleting null file path")
    void testDeleteFile_NullPath() {
        // When & Then: Delete file with null path should throw exception
        assertThatThrownBy(() -> fileStorageService.deleteFile(null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to delete file");
    }

    @Test
    @DisplayName("Should handle deleting non-existent file")
    void testDeleteFile_NonExistentFile() {
        // Given: Non-existent file path
        String nonExistentPath = "files/non-existent.txt";

        // When: Delete non-existent file (should complete without error)
        assertThatCode(() -> fileStorageService.deleteFile(nonExistentPath))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should calculate file hash successfully")
    void testCalculateFileHash_Success() throws IOException, NoSuchAlgorithmException {
        // When: Calculate file hash using file path
        String hash = fileStorageService.calculateFileHash(testFile.getAbsolutePath());

        // Then: Hash should be calculated correctly
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64); // SHA-256 hash length
        
        // Verify hash is correct by calculating manually
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] expectedHash = digest.digest(testFileContent);
        StringBuilder expectedHashString = new StringBuilder();
        for (byte b : expectedHash) {
            expectedHashString.append(String.format("%02x", b));
        }
        assertThat(hash).isEqualTo(expectedHashString.toString());
    }

    @Test
    @DisplayName("Should throw exception when calculating hash for null file path")
    void testCalculateFileHash_NullFilePath() {
        // When & Then: Calculate hash for null file path should throw exception
        assertThatThrownBy(() -> fileStorageService.calculateFileHash((String) null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to calculate file hash");
    }

    @Test
    @DisplayName("Should calculate hash for MultipartFile successfully")
    void testCalculateFileHash_MultipartFile() throws IOException {
        // Given: MultipartFile
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "test.txt", "text/plain", testFileContent
        );

        // When: Calculate file hash
        String hash = fileStorageService.calculateFileHash(multipartFile);

        // Then: Hash should be calculated correctly
        assertThat(hash).isNotNull();
        assertThat(hash).hasSize(64); // SHA-256 hash length
    }

    @Test
    @DisplayName("Should throw exception when calculating hash for null MultipartFile")
    void testCalculateFileHash_NullMultipartFile() {
        // When & Then: Calculate hash for null MultipartFile should throw exception
        assertThatThrownBy(() -> fileStorageService.calculateFileHash((MultipartFile) null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to calculate file hash");
    }

    @Test
    @DisplayName("Should throw exception when calculating hash for non-existent file")
    void testCalculateFileHash_NonExistentFile() {
        // Given: Non-existent file path
        String nonExistentFilePath = tempDir.resolve("non-existent.txt").toString();

        // When & Then: Calculate hash for non-existent file should throw exception
        assertThatThrownBy(() -> fileStorageService.calculateFileHash(nonExistentFilePath))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to calculate file hash");
    }

    @Test
    @DisplayName("Should get file size successfully")
    void testGetFileSize_Success() throws IOException {
        // Given: Store file first to get relative path
        String relativePath = fileStorageService.storeFile(testMultipartFile, testDocumentId);
        
        // When: Get file size
        long size = fileStorageService.getFileSize(relativePath);

        // Then: Size should be correct
        assertThat(size).isEqualTo(testFileContent.length);
    }

    @Test
    @DisplayName("Should throw exception when getting size for null file path")
    void testGetFileSize_NullFilePath() {
        // When & Then: Get size for null file path should throw exception
        assertThatThrownBy(() -> fileStorageService.getFileSize(null))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to load file");
    }

    @Test
    @DisplayName("Should throw exception when getting size for non-existent file")
    void testGetFileSize_NonExistentFile() {
        // Given: Non-existent file path
        String nonExistentPath = "files/non-existent.txt";

        // When & Then: Get size for non-existent file should throw exception
        assertThatThrownBy(() -> fileStorageService.getFileSize(nonExistentPath))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to load file");
    }

    @Test
    @DisplayName("Should clean up chunks successfully")
    void testCleanupChunks_Success() throws IOException {
        // Given: Chunks stored
        FileChunk chunk1 = new FileChunk();
        chunk1.setDocumentId(testDocumentId);
        chunk1.setChunkNumber(1);
        chunk1.setChunkPath("chunks/" + testDocumentId + "/chunk_1.dat");
        
        FileChunk chunk2 = new FileChunk();
        chunk2.setDocumentId(testDocumentId);
        chunk2.setChunkNumber(2);
        chunk2.setChunkPath("chunks/" + testDocumentId + "/chunk_2.dat");
        
        List<FileChunk> chunks = Arrays.asList(chunk1, chunk2);
        
        // Create actual chunk files
        Path chunk1Path = Paths.get(testStorageBasePath, chunk1.getChunkPath());
        Path chunk2Path = Paths.get(testStorageBasePath, chunk2.getChunkPath());
        Files.createDirectories(chunk1Path.getParent());
        Files.write(chunk1Path, "chunk1 content".getBytes());
        Files.write(chunk2Path, "chunk2 content".getBytes());
        
        when(fileChunkRepository.findByDocumentIdOrderByChunkNumber(testDocumentId))
            .thenReturn(chunks);

        // When: Clean up chunks
        fileStorageService.cleanupChunks(testDocumentId);

        // Then: Chunks should be deleted from storage and database
        assertThat(Files.exists(chunk1Path)).isFalse();
        assertThat(Files.exists(chunk2Path)).isFalse();
        
        // Verify repository interactions
        verify(fileChunkRepository).findByDocumentIdOrderByChunkNumber(testDocumentId);
        verify(fileChunkRepository).deleteAll(chunks);
    }

    @Test
    @DisplayName("Should handle cleanup when no chunks exist")
    void testCleanupChunks_NoChunks() {
        // Given: No chunks found
        when(fileChunkRepository.findByDocumentIdOrderByChunkNumber(testDocumentId))
            .thenReturn(Arrays.asList());

        // When: Clean up chunks
        fileStorageService.cleanupChunks(testDocumentId);

        // Then: Should complete without error
        verify(fileChunkRepository).findByDocumentIdOrderByChunkNumber(testDocumentId);
        verify(fileChunkRepository, never()).deleteAll(any());
    }

    @Test
    @DisplayName("Should throw exception when cleaning up chunks with null document ID")
    void testCleanupChunks_NullDocumentId() {
        // When & Then: Clean up chunks with null document ID should throw exception
        assertThatThrownBy(() -> fileStorageService.cleanupChunks(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Document ID cannot be null or empty");
    }

    @Test
    @DisplayName("Should continue cleanup even when some chunk files don't exist")
    void testCleanupChunks_SomeFilesNotExist() throws IOException {
        // Given: Chunks in database but some files don't exist
        FileChunk chunk1 = new FileChunk();
        chunk1.setDocumentId(testDocumentId);
        chunk1.setChunkNumber(1);
        chunk1.setChunkPath("chunks/" + testDocumentId + "/chunk_1.dat");
        
        FileChunk chunk2 = new FileChunk();
        chunk2.setDocumentId(testDocumentId);
        chunk2.setChunkNumber(2);
        chunk2.setChunkPath("chunks/" + testDocumentId + "/chunk_2.dat");
        
        List<FileChunk> chunks = Arrays.asList(chunk1, chunk2);
        
        // Create only one chunk file
        Path chunk1Path = Paths.get(testStorageBasePath, chunk1.getChunkPath());
        Files.createDirectories(chunk1Path.getParent());
        Files.write(chunk1Path, "chunk1 content".getBytes());
        // chunk2 file is not created
        
        when(fileChunkRepository.findByDocumentIdOrderByChunkNumber(testDocumentId))
            .thenReturn(chunks);

        // When: Clean up chunks
        fileStorageService.cleanupChunks(testDocumentId);

        // Then: Should complete cleanup and delete database records
        assertThat(Files.exists(chunk1Path)).isFalse();
        
        verify(fileChunkRepository).findByDocumentIdOrderByChunkNumber(testDocumentId);
        verify(fileChunkRepository).deleteAll(chunks);
    }


}