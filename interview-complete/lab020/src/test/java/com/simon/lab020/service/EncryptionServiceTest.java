package com.simon.lab020.service;

import com.simon.lab020.config.EncryptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "file.encryption.algorithm=AES",
    "file.encryption.transformation=AES/CBC/PKCS5Padding",
    "file.encryption.key-length=256"
})
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
}