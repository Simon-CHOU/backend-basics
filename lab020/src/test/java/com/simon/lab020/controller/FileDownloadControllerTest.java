package com.simon.lab020.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.lab020.dto.FileDownloadResponse;
import com.simon.lab020.entity.FileMetadata;
import com.simon.lab020.repository.FileMetadataRepository;
import com.simon.lab020.service.FileStorageService;
import com.simon.lab020.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for file download functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class FileDownloadControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testDownloadFile_Success() throws Exception {
        // Prepare test file
        String documentId = "test-doc-123";
        String originalFilename = "test-download.txt";
        String content = "This is test content for download";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        // Create and save file
        FileMetadata fileMetadata = createTestFile(documentId, originalFilename, content, contentType);
        
        // Test download
        MvcResult result = mockMvc.perform(get("/api/files/download/{documentId}", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", contentType))
                .andExpect(header().string("Content-Length", String.valueOf(content.length())))
                .andExpect(header().exists("Content-Disposition"))
                .andReturn();
        
        // Verify content
        String downloadedContent = result.getResponse().getContentAsString();
        assertThat(downloadedContent).isEqualTo(content);
        
        // Verify Content-Disposition header for attachment
        String contentDisposition = result.getResponse().getHeader("Content-Disposition");
        assertThat(contentDisposition).contains("attachment");
        assertThat(contentDisposition).contains(originalFilename.replace(" ", "%20"));
    }

    @Test
    void testDownloadFile_Inline() throws Exception {
        // Prepare test file
        String documentId = "test-doc-inline";
        String originalFilename = "inline-test.txt";
        String content = "This is inline content";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        createTestFile(documentId, originalFilename, content, contentType);
        
        // Test inline download
        MvcResult result = mockMvc.perform(get("/api/files/download/{documentId}", documentId)
                        .param("inline", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", contentType))
                .andReturn();
        
        // Verify Content-Disposition header for inline
        String contentDisposition = result.getResponse().getHeader("Content-Disposition");
        assertThat(contentDisposition).contains("inline");
        assertThat(contentDisposition).contains(originalFilename);
        
        // Verify content
        String downloadedContent = result.getResponse().getContentAsString();
        assertThat(downloadedContent).isEqualTo(content);
    }

    @Test
    void testDownloadFile_NotFound() throws Exception {
        String nonExistentDocumentId = "non-existent-doc";
        
        mockMvc.perform(get("/api/files/download/{documentId}", nonExistentDocumentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("FILE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("File not found"));
    }

    @Test
    void testDownloadFile_InvalidDocumentId() throws Exception {
        // Test with empty document ID
        mockMvc.perform(get("/api/files/download/{documentId}", ""))
                .andExpect(status().isNotFound()); // Spring will return 404 for empty path variable
        
        // Test with null-like document ID
        mockMvc.perform(get("/api/files/download/{documentId}", "null"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetFileMetadata_Success() throws Exception {
        // Prepare test file
        String documentId = "test-metadata-doc";
        String originalFilename = "metadata-test.pdf";
        String content = "PDF content for metadata test";
        String contentType = MediaType.APPLICATION_PDF_VALUE;
        
        FileMetadata fileMetadata = createTestFile(documentId, originalFilename, content, contentType);
        
        // Test metadata retrieval
        MvcResult result = mockMvc.perform(get("/api/files/metadata/{documentId}", documentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.originalFilename").value(originalFilename))
                .andExpect(jsonPath("$.fileSize").value(content.length()))
                .andExpect(jsonPath("$.contentType").value(contentType))
                .andExpect(jsonPath("$.fileHash").value(fileMetadata.getFileHash()))
                .andExpect(jsonPath("$.message").value("File metadata retrieved successfully"))
                .andExpect(jsonPath("$.uploadDate").exists())
                .andExpect(jsonPath("$.downloadDate").exists())
                .andReturn();
        
        // Verify response structure
        FileDownloadResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), FileDownloadResponse.class);
        
        assertThat(response.getDocumentId()).isEqualTo(documentId);
        assertThat(response.getOriginalFilename()).isEqualTo(originalFilename);
        assertThat(response.getFileSize()).isEqualTo((long) content.length());
        assertThat(response.getContentType()).isEqualTo(contentType);
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getFileResource()).isNull(); // Should not include file resource in metadata
    }

    @Test
    void testGetFileMetadata_NotFound() throws Exception {
        String nonExistentDocumentId = "non-existent-metadata-doc";
        
        mockMvc.perform(get("/api/files/metadata/{documentId}", nonExistentDocumentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("FILE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("File not found"));
    }

    @Test
    void testCheckFileExists_Success() throws Exception {
        // Prepare test file
        String documentId = "test-exists-doc";
        String originalFilename = "exists-test.txt";
        String content = "Content for existence test";
        
        createTestFile(documentId, originalFilename, content, MediaType.TEXT_PLAIN_VALUE);
        
        // Test file existence check
        mockMvc.perform(head("/api/files/download/{documentId}", documentId))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckFileExists_NotFound() throws Exception {
        String nonExistentDocumentId = "non-existent-exists-doc";
        
        mockMvc.perform(head("/api/files/download/{documentId}", nonExistentDocumentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCheckFileExists_InvalidDocumentId() throws Exception {
        // Test with empty document ID
        mockMvc.perform(head("/api/files/download/{documentId}", ""))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDownloadFile_DifferentContentTypes() throws Exception {
        // Test various content types
        String[][] testCases = {
                {"image-doc", "test.jpg", "fake-image-content", MediaType.IMAGE_JPEG_VALUE},
                {"json-doc", "data.json", "{\"key\": \"value\"}", MediaType.APPLICATION_JSON_VALUE},
                {"xml-doc", "config.xml", "<root><item>value</item></root>", MediaType.APPLICATION_XML_VALUE},
                {"binary-doc", "file.bin", "binary-content-123", MediaType.APPLICATION_OCTET_STREAM_VALUE}
        };
        
        for (String[] testCase : testCases) {
            String documentId = testCase[0];
            String filename = testCase[1];
            String content = testCase[2];
            String contentType = testCase[3];
            
            createTestFile(documentId, filename, content, contentType);
            
            mockMvc.perform(get("/api/files/download/{documentId}", documentId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", contentType))
                    .andExpect(header().string("Content-Length", String.valueOf(content.length())))
                    .andExpect(content().string(content));
        }
    }

    @Test
    void testDownloadFile_SpecialCharactersInFilename() throws Exception {
        // Test filename with special characters
        String documentId = "special-chars-doc";
        String originalFilename = "测试文件 (1) [copy].txt"; // Chinese characters, spaces, parentheses, brackets
        String content = "Content with special filename";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        createTestFile(documentId, originalFilename, content, contentType);
        
        MvcResult result = mockMvc.perform(get("/api/files/download/{documentId}", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", contentType))
                .andReturn();
        
        // Verify Content-Disposition header handles special characters
        String contentDisposition = result.getResponse().getHeader("Content-Disposition");
        assertThat(contentDisposition).contains("attachment");
        assertThat(contentDisposition).contains("filename*=UTF-8''");
        
        // Verify content
        String downloadedContent = result.getResponse().getContentAsString();
        assertThat(downloadedContent).isEqualTo(content);
    }

    @Test
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/files/download/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("File download service is healthy"));
    }

    @Test
    void testDownloadFile_LargeFile() throws Exception {
        // Test with larger content
        String documentId = "large-file-doc";
        String originalFilename = "large-test.txt";
        StringBuilder contentBuilder = new StringBuilder();
        
        // Create content of about 10KB
        for (int i = 0; i < 1000; i++) {
            contentBuilder.append("This is line ").append(i).append(" of the large test file.\n");
        }
        String content = contentBuilder.toString();
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        createTestFile(documentId, originalFilename, content, contentType);
        
        MvcResult result = mockMvc.perform(get("/api/files/download/{documentId}", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", contentType))
                .andExpect(header().string("Content-Length", String.valueOf(content.length())))
                .andReturn();
        
        // Verify content
        String downloadedContent = result.getResponse().getContentAsString();
        assertThat(downloadedContent).isEqualTo(content);
        assertThat(downloadedContent.length()).isGreaterThan(10000);
    }

    /**
     * Helper method to create and save test file
     */
    private FileMetadata createTestFile(String documentId, String originalFilename, 
                                      String content, String contentType) throws Exception {
        
        // Calculate file hash
        String fileHash = calculateSHA256(content.getBytes());
        
        // Generate encryption key
        String encryptionKey = encryptionService.generateEncryptionKey();
        
        // Create temporary files for encryption
        File tempInputFile = File.createTempFile("test-input-", ".tmp");
        File tempEncryptedFile = File.createTempFile("test-encrypted-", ".tmp");
        String filePath;
        
        try {
            // Write content to temporary input file
            try (FileOutputStream fos = new FileOutputStream(tempInputFile)) {
                fos.write(content.getBytes());
            }
            
            // Encrypt the file
            encryptionService.encryptFile(tempInputFile, tempEncryptedFile, encryptionKey);
            
            // Create MockMultipartFile from encrypted file
            byte[] encryptedBytes = Files.readAllBytes(tempEncryptedFile.toPath());
            MockMultipartFile encryptedMultipartFile = new MockMultipartFile(
                "file", originalFilename, contentType, encryptedBytes);
            
            // Store the encrypted file
            filePath = fileStorageService.storeFile(encryptedMultipartFile, documentId);
        } finally {
            // Clean up temporary files
            tempInputFile.delete();
            tempEncryptedFile.delete();
        }
        
        // Create and save file metadata
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setDocumentId(documentId);
        fileMetadata.setOriginalFilename(originalFilename);
        fileMetadata.setFileSize((long) content.length());
        fileMetadata.setContentType(contentType);
        fileMetadata.setFilePath(filePath);
        fileMetadata.setEncryptionKey(encryptionKey);
        fileMetadata.setFileHash(fileHash);
        fileMetadata.setUploadDate(LocalDateTime.now());
        
        return fileMetadataRepository.save(fileMetadata);
    }

    /**
     * Calculate SHA-256 hash of data
     */
    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }
}