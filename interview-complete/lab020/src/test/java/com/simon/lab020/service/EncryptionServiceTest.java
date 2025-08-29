package com.simon.lab020.service;

import com.simon.lab020.config.EncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "file.encryption.algorithm=AES",
    "file.encryption.transformation=AES/CBC/PKCS5Padding",
    "file.encryption.key-length=256"
})
@DisplayName("EncryptionService Tests")
class EncryptionServiceTest {

    private EncryptionService encryptionService;
    private EncryptionProperties encryptionProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        encryptionProperties = new EncryptionProperties();
        encryptionProperties.setAlgorithm("AES");
        encryptionProperties.setTransformation("AES/CBC/PKCS5Padding");
        encryptionProperties.setKeyLength(256);
        
        encryptionService = new EncryptionService(encryptionProperties);
    }

    @Test
    @DisplayName("Should encrypt and decrypt file successfully")
    void testEncryptDecryptFile() throws IOException {
        // Create test file
        File originalFile = tempDir.resolve("test.txt").toFile();
        String testContent = "This is a test file for encryption and decryption.";
        try (FileWriter writer = new FileWriter(originalFile)) {
            writer.write(testContent);
        }

        // Generate encryption key
        String encryptionKey = encryptionService.generateEncryptionKey();

        // Encrypt file
        File encryptedFile = tempDir.resolve("test_encrypted.txt").toFile();
        encryptionService.encryptFile(originalFile, encryptedFile, encryptionKey);

        assertTrue(encryptedFile.exists());
        assertTrue(encryptedFile.length() > 0);

        // Decrypt file
        File decryptedFile = tempDir.resolve("test_decrypted.txt").toFile();
        encryptionService.decryptFile(encryptedFile, decryptedFile, encryptionKey);

        assertTrue(decryptedFile.exists());
        
        // Verify content
        String decryptedContent = Files.readString(decryptedFile.toPath());
        assertEquals(testContent, decryptedContent);
    }

    @Test
    @DisplayName("Should encrypt and decrypt small file successfully")
    void testEncryptDecryptSmallFile() throws IOException {
        // Create very small test file
        File originalFile = tempDir.resolve("small.txt").toFile();
        String testContent = "Hi";
        try (FileWriter writer = new FileWriter(originalFile)) {
            writer.write(testContent);
        }

        // Generate encryption key
        String encryptionKey = encryptionService.generateEncryptionKey();

        // Encrypt file
        File encryptedFile = tempDir.resolve("small_encrypted.txt").toFile();
        encryptionService.encryptFile(originalFile, encryptedFile, encryptionKey);

        assertTrue(encryptedFile.exists());
        assertTrue(encryptedFile.length() > 0);

        // Decrypt file
        File decryptedFile = tempDir.resolve("small_decrypted.txt").toFile();
        encryptionService.decryptFile(encryptedFile, decryptedFile, encryptionKey);

        assertTrue(decryptedFile.exists());
        
        // Verify content
        String decryptedContent = Files.readString(decryptedFile.toPath());
        assertEquals(testContent, decryptedContent);
    }

    @Test
    @DisplayName("Should generate valid encryption key")
    void testGenerateEncryptionKey_Success() {
        // When: Generate encryption key
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // Then: Key should be valid Base64 encoded string
        assertThat(encryptionKey).isNotNull();
        assertThat(encryptionKey).isNotEmpty();
        
        // Should be able to decode as Base64
        byte[] decodedKey = Base64.getDecoder().decode(encryptionKey);
        assertThat(decodedKey).hasSize(32); // 256 bits = 32 bytes
    }

    @Test
    @DisplayName("Should generate different keys each time")
    void testGenerateEncryptionKey_Uniqueness() {
        // When: Generate multiple keys
        String key1 = encryptionService.generateEncryptionKey();
        String key2 = encryptionService.generateEncryptionKey();
        String key3 = encryptionService.generateEncryptionKey();
        
        // Then: All keys should be different
        assertThat(key1).isNotEqualTo(key2);
        assertThat(key2).isNotEqualTo(key3);
        assertThat(key1).isNotEqualTo(key3);
    }

    @Test
    @DisplayName("Should encrypt and decrypt data successfully")
    void testEncryptDecryptData_Success() throws Exception {
        // Given: Test data and encryption key
        String testData = "This is sensitive data that needs encryption";
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When: Encrypt data
        String encryptedData = encryptionService.encryptData(testData, encryptionKey);
        
        // Then: Encrypted data should be different from original
        assertThat(encryptedData).isNotNull();
        assertThat(encryptedData).isNotEmpty();
        assertThat(encryptedData).isNotEqualTo(testData);
        
        // When: Decrypt data
        String decryptedData = encryptionService.decryptDataAsString(encryptedData, encryptionKey);
        
        // Then: Decrypted data should match original
        assertThat(decryptedData).isEqualTo(testData);
    }

    @Test
    @DisplayName("Should encrypt and decrypt empty data")
    void testEncryptDecryptData_EmptyData() throws Exception {
        // Given: Empty data
        String testData = "";
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When: Encrypt and decrypt empty data
        String encryptedData = encryptionService.encryptData(testData, encryptionKey);
        String decryptedData = encryptionService.decryptDataAsString(encryptedData, encryptionKey);
        
        // Then: Should handle empty data correctly
        assertThat(decryptedData).isEqualTo(testData);
    }

    @Test
    @DisplayName("Should encrypt and decrypt Unicode data")
    void testEncryptDecryptData_UnicodeData() throws Exception {
        // Given: Unicode test data
        String testData = "测试数据 🔐 Тест данные العربية";
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When: Encrypt and decrypt Unicode data
        String encryptedData = encryptionService.encryptData(testData, encryptionKey);
        String decryptedData = encryptionService.decryptDataAsString(encryptedData, encryptionKey);
        
        // Then: Should preserve Unicode characters
        assertThat(decryptedData).isEqualTo(testData);
    }

    @Test
    @DisplayName("Should encrypt and decrypt large data")
    void testEncryptDecryptData_LargeData() throws Exception {
        // Given: Large test data
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeData.append("This is line ").append(i).append(" of large test data.\n");
        }
        String testData = largeData.toString();
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When: Encrypt and decrypt large data
        String encryptedData = encryptionService.encryptData(testData, encryptionKey);
        String decryptedData = encryptionService.decryptDataAsString(encryptedData, encryptionKey);
        
        // Then: Should handle large data correctly
        assertThat(decryptedData).isEqualTo(testData);
    }

    @Test
    @DisplayName("Should throw exception when decrypting with wrong key")
    void testDecryptData_WrongKey() throws Exception {
        // Given: Encrypted data with one key
        String testData = "Secret data";
        String correctKey = encryptionService.generateEncryptionKey();
        String wrongKey = encryptionService.generateEncryptionKey();
        String encryptedData = encryptionService.encryptData(testData, correctKey);
        
        // When/Then: Decrypting with wrong key should throw exception
        assertThatThrownBy(() -> encryptionService.decryptDataAsString(encryptedData, wrongKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Decryption failed");
    }

    @Test
    @DisplayName("Should throw exception when decrypting invalid data")
    void testDecryptData_InvalidData() {
        // Given: Invalid encrypted data
        String invalidEncryptedData = "invalid-base64-data";
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When/Then: Decrypting invalid data should throw exception
        assertThatThrownBy(() -> encryptionService.decryptDataAsString(invalidEncryptedData, encryptionKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Decryption failed");
    }

    @Test
    @DisplayName("Should throw exception when encrypting with invalid key")
    void testEncryptData_InvalidKey() {
        // Given: Invalid encryption key
        String testData = "Test data";
        String invalidKey = "invalid-key";
        
        // When/Then: Encrypting with invalid key should throw exception
        assertThatThrownBy(() -> encryptionService.encryptData(testData, invalidKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Encryption failed");
    }

    @Test
    @DisplayName("Should handle null data gracefully")
    void testEncryptData_NullData() {
        // Given: Null data
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When/Then: Encrypting null data should throw exception
        assertThatThrownBy(() -> encryptionService.encryptData((String)null, encryptionKey))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle null key gracefully")
    void testEncryptData_NullKey() {
        // Given: Null key
        String testData = "Test data";
        
        // When/Then: Encrypting with null key should throw exception
        assertThatThrownBy(() -> encryptionService.encryptData(testData, null))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception when encrypting non-existent file")
    void testEncryptFile_NonExistentFile() {
        // Given: Non-existent source file
        File nonExistentFile = new File("non-existent-file.txt");
        File targetFile = tempDir.resolve("target.txt").toFile();
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When/Then: Encrypting non-existent file should throw exception
        assertThatThrownBy(() -> encryptionService.encryptFile(nonExistentFile, targetFile, encryptionKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("File encryption failed");
    }

    @Test
    @DisplayName("Should throw exception when decrypting non-existent file")
    void testDecryptFile_NonExistentFile() {
        // Given: Non-existent encrypted file
        File nonExistentFile = new File("non-existent-encrypted.txt");
        File targetFile = tempDir.resolve("target.txt").toFile();
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When/Then: Decrypting non-existent file should throw exception
        assertThatThrownBy(() -> encryptionService.decryptFile(nonExistentFile, targetFile, encryptionKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("File decryption failed");
    }

    @Test
    @DisplayName("Should throw exception when decrypting file with wrong key")
    void testDecryptFile_WrongKey() throws IOException {
        // Given: Encrypted file with one key
        File originalFile = tempDir.resolve("original.txt").toFile();
        File encryptedFile = tempDir.resolve("encrypted.txt").toFile();
        File decryptedFile = tempDir.resolve("decrypted.txt").toFile();
        
        String testContent = "Secret file content";
        try (FileWriter writer = new FileWriter(originalFile)) {
            writer.write(testContent);
        }
        
        String correctKey = encryptionService.generateEncryptionKey();
        String wrongKey = encryptionService.generateEncryptionKey();
        
        encryptionService.encryptFile(originalFile, encryptedFile, correctKey);
        
        // When/Then: Decrypting with wrong key should throw exception
        assertThatThrownBy(() -> encryptionService.decryptFile(encryptedFile, decryptedFile, wrongKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("File decryption failed");
    }

    @Test
    @DisplayName("Should encrypt and decrypt empty file")
    void testEncryptDecryptFile_EmptyFile() throws IOException {
        // Given: Empty file
        File originalFile = tempDir.resolve("empty.txt").toFile();
        originalFile.createNewFile(); // Create empty file
        
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When: Encrypt empty file
        File encryptedFile = tempDir.resolve("empty_encrypted.txt").toFile();
        encryptionService.encryptFile(originalFile, encryptedFile, encryptionKey);
        
        // Then: Encrypted file should exist
        assertThat(encryptedFile.exists()).isTrue();
        
        // When: Decrypt file
        File decryptedFile = tempDir.resolve("empty_decrypted.txt").toFile();
        encryptionService.decryptFile(encryptedFile, decryptedFile, encryptionKey);
        
        // Then: Decrypted file should be empty
        assertThat(decryptedFile.exists()).isTrue();
        assertThat(Files.readString(decryptedFile.toPath())).isEmpty();
    }

    @Test
    @DisplayName("Should encrypt and decrypt binary file")
    void testEncryptDecryptFile_BinaryFile() throws IOException {
        // Given: Binary file with random bytes
        File originalFile = tempDir.resolve("binary.dat").toFile();
        byte[] binaryData = new byte[1000];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) (i % 256);
        }
        Files.write(originalFile.toPath(), binaryData);
        
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When: Encrypt binary file
        File encryptedFile = tempDir.resolve("binary_encrypted.dat").toFile();
        encryptionService.encryptFile(originalFile, encryptedFile, encryptionKey);
        
        // Then: Encrypted file should exist and be different
        assertThat(encryptedFile.exists()).isTrue();
        assertThat(Files.readAllBytes(encryptedFile.toPath())).isNotEqualTo(binaryData);
        
        // When: Decrypt file
        File decryptedFile = tempDir.resolve("binary_decrypted.dat").toFile();
        encryptionService.decryptFile(encryptedFile, decryptedFile, encryptionKey);
        
        // Then: Decrypted file should match original
        assertThat(decryptedFile.exists()).isTrue();
        assertThat(Files.readAllBytes(decryptedFile.toPath())).isEqualTo(binaryData);
    }

    @Test
    @DisplayName("Should handle file encryption with invalid target directory")
    void testEncryptFile_InvalidTargetDirectory() throws IOException {
        // Given: Source file and invalid target directory
        File originalFile = tempDir.resolve("source.txt").toFile();
        try (FileWriter writer = new FileWriter(originalFile)) {
            writer.write("test content");
        }
        
        File invalidTargetFile = new File("/invalid/path/target.txt");
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // When/Then: Should throw exception for invalid target path
        assertThatThrownBy(() -> encryptionService.encryptFile(originalFile, invalidTargetFile, encryptionKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("File encryption failed");
    }
}