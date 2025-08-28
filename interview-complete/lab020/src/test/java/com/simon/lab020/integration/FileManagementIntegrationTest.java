package com.simon.lab020.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.lab020.dto.ChunkUploadResponse;
import com.simon.lab020.dto.FileUploadResponse;
import com.simon.lab020.entity.FileAuditLog;
import com.simon.lab020.entity.FileMetadata;
import com.simon.lab020.entity.UploadSession;
import com.simon.lab020.repository.FileAuditLogRepository;
import com.simon.lab020.repository.FileMetadataRepository;
import com.simon.lab020.repository.UploadSessionRepository;
import com.simon.lab020.repository.FileChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests for the complete file management system
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class FileManagementIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UploadSessionRepository uploadSessionRepository;

    @Autowired
    private FileChunkRepository fileChunkRepository;

    @Autowired
    private FileAuditLogRepository fileAuditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCompleteFileLifecycle_SingleUpload() throws Exception {
        // Step 1: Upload a file
        String originalFilename = "lifecycle-test.txt";
        String content = "This is a complete lifecycle test file";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                originalFilename,
                contentType,
                content.getBytes()
        );

        // Upload file
        MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.originalFilename").value(originalFilename))
                .andExpect(jsonPath("$.fileSize").value(content.length()))
                .andExpect(jsonPath("$.contentType").value(contentType))
                .andReturn();

        FileUploadResponse uploadResponse = objectMapper.readValue(
                uploadResult.getResponse().getContentAsString(), FileUploadResponse.class);
        String documentId = uploadResponse.getDocumentId();

        // Step 2: Verify file metadata is stored
        Optional<FileMetadata> fileMetadata = fileMetadataRepository.findByDocumentId(documentId);
        assertThat(fileMetadata).isPresent();
        assertThat(fileMetadata.get().getOriginalFilename()).isEqualTo(originalFilename);
        assertThat(fileMetadata.get().getFileSize()).isEqualTo((long) content.length());
        assertThat(fileMetadata.get().getContentType()).isEqualTo(contentType);

        // Step 3: Verify audit log is created
        List<FileAuditLog> uploadLogs = fileAuditLogRepository.findByDocumentIdAndOperation(
                documentId, FileAuditLog.Operation.UPLOAD);
        assertThat(uploadLogs).hasSize(1);
        assertThat(uploadLogs.get(0).getDocumentId()).isEqualTo(documentId);

        // Step 4: Check file existence
        mockMvc.perform(head("/api/files/download/{documentId}", documentId))
                .andExpect(status().isOk());

        // Step 5: Get file metadata
        mockMvc.perform(get("/api/files/metadata/{documentId}", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentId").value(documentId))
                .andExpect(jsonPath("$.originalFilename").value(originalFilename))
                .andExpect(jsonPath("$.fileSize").value(content.length()));

        // Step 6: Download file
        MvcResult downloadResult = mockMvc.perform(get("/api/files/download/{documentId}", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", contentType))
                .andExpect(header().string("Content-Length", String.valueOf(content.length())))
                .andReturn();

        String downloadedContent = downloadResult.getResponse().getContentAsString();
        assertThat(downloadedContent).isEqualTo(content);

        // Step 7: Verify download audit log is created
        List<FileAuditLog> downloadLogs = fileAuditLogRepository.findByDocumentIdAndOperation(
                documentId, FileAuditLog.Operation.DOWNLOAD);
        assertThat(downloadLogs).hasSize(1);
        assertThat(downloadLogs.get(0).getDocumentId()).isEqualTo(documentId);

        // Step 8: Test inline download
        mockMvc.perform(get("/api/files/download/{documentId}", documentId)
                        .param("inline", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                        org.hamcrest.Matchers.containsString("inline")));
    }

    @Test
    void testCompleteFileLifecycle_ChunkUpload() throws Exception {
        // Step 1: Prepare chunked file data
        String originalFilename = "chunked-lifecycle-test.txt";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        // Create content that will be split into 3 chunks
        String fullContent = "Chunk 1: This is the first part of the file. ".repeat(50) +
                           "Chunk 2: This is the second part of the file. ".repeat(50) +
                           "Chunk 3: This is the third part of the file. ".repeat(50);
        
        byte[] fullBytes = fullContent.getBytes();
        String fileHash = calculateSHA256(fullBytes);
        
        int totalChunks = 3;
        int chunkSize = fullBytes.length / totalChunks;

        // Step 2: Initialize chunk upload session
        MvcResult initResult = mockMvc.perform(post("/api/files/upload/chunks/init")
                        .param("originalFilename", originalFilename)
                        .param("totalSize", String.valueOf(fullBytes.length))
                        .param("totalChunks", String.valueOf(totalChunks))
                        .param("contentType", contentType)
                        .param("fileHash", fileHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sessionId").exists())
                .andReturn();

        ChunkUploadResponse initResponse = objectMapper.readValue(
                initResult.getResponse().getContentAsString(), ChunkUploadResponse.class);
        String sessionId = initResponse.getSessionId();

        // Step 3: Verify upload session is created
        Optional<UploadSession> session = uploadSessionRepository.findBySessionId(sessionId);
        assertThat(session).isPresent();
        assertThat(session.get().getStatus()).isEqualTo(UploadSession.UploadStatus.IN_PROGRESS);

        // Step 4: Upload chunks
        String finalDocumentId = null;
        for (int i = 0; i < totalChunks; i++) {
            int startPos = i * chunkSize;
            int endPos = (i == totalChunks - 1) ? fullBytes.length : (i + 1) * chunkSize;
            byte[] chunkData = new byte[endPos - startPos];
            System.arraycopy(fullBytes, startPos, chunkData, 0, chunkData.length);
            
            String chunkHash = calculateSHA256(chunkData);
            
            MockMultipartFile chunkFile = new MockMultipartFile(
                    "chunkFile",
                    "chunk_" + i + ".part",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    chunkData
            );

            MvcResult chunkResult = mockMvc.perform(multipart("/api/files/upload/chunks")
                            .file(chunkFile)
                            .param("sessionId", sessionId)
                            .param("chunkNumber", String.valueOf(i))
                            .param("totalChunks", String.valueOf(totalChunks))
                            .param("chunkHash", chunkHash)
                            .param("originalFilename", originalFilename)
                            .param("totalFileSize", String.valueOf(fullBytes.length))
                            .param("contentType", contentType)
                            .param("fileHash", fileHash))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ChunkUploadResponse chunkResponse = objectMapper.readValue(
                    chunkResult.getResponse().getContentAsString(), ChunkUploadResponse.class);

            if (i == totalChunks - 1) {
                assertThat(chunkResponse.isCompleted()).isTrue();
                assertThat(chunkResponse.getDocumentId()).isNotNull();
                finalDocumentId = chunkResponse.getDocumentId();
            }
        }

        assertThat(finalDocumentId).isNotNull();

        // Step 5: Verify final file is created
        Optional<FileMetadata> finalFile = fileMetadataRepository.findByDocumentId(finalDocumentId);
        assertThat(finalFile).isPresent();
        assertThat(finalFile.get().getOriginalFilename()).isEqualTo(originalFilename);
        assertThat(finalFile.get().getFileSize()).isEqualTo(fullBytes.length);
        assertThat(finalFile.get().getFileHash()).isEqualTo(fileHash);

        // Step 6: Verify upload session is completed
        Optional<UploadSession> completedSession = uploadSessionRepository.findBySessionId(sessionId);
        assertThat(completedSession).isPresent();
        assertThat(completedSession.get().getStatus()).isEqualTo(UploadSession.UploadStatus.COMPLETED);

        // Step 7: Verify audit log
        List<FileAuditLog> uploadLogs = fileAuditLogRepository.findByDocumentIdAndOperation(
                finalDocumentId, FileAuditLog.Operation.UPLOAD);
        assertThat(uploadLogs).hasSize(1);

        // Step 8: Download and verify content
        MvcResult downloadResult = mockMvc.perform(get("/api/files/download/{documentId}", finalDocumentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", contentType))
                .andExpect(header().string("Content-Length", String.valueOf(fullBytes.length)))
                .andReturn();

        String downloadedContent = downloadResult.getResponse().getContentAsString();
        assertThat(downloadedContent).isEqualTo(fullContent);
        assertThat(downloadedContent.getBytes()).isEqualTo(fullBytes);

        // Step 9: Verify download audit log
        List<FileAuditLog> downloadLogs = fileAuditLogRepository.findByDocumentIdAndOperation(
                finalDocumentId, FileAuditLog.Operation.DOWNLOAD);
        assertThat(downloadLogs).hasSize(1);
    }

    @Test
    void testDuplicateFileHandling() throws Exception {
        // Step 1: Upload original file
        String originalFilename = "duplicate-test.txt";
        String content = "This file will be uploaded twice";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                originalFilename,
                contentType,
                content.getBytes()
        );

        MvcResult firstUploadResult = mockMvc.perform(multipart("/api/files/upload")
                        .file(file1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        FileUploadResponse firstUploadResponse = objectMapper.readValue(
                firstUploadResult.getResponse().getContentAsString(), FileUploadResponse.class);
        String firstDocumentId = firstUploadResponse.getDocumentId();

        // Step 2: Upload same file again
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                originalFilename,
                contentType,
                content.getBytes()
        );

        MvcResult secondUploadResult = mockMvc.perform(multipart("/api/files/upload")
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("File already exists"))
                .andReturn();

        FileUploadResponse secondUploadResponse = objectMapper.readValue(
                secondUploadResult.getResponse().getContentAsString(), FileUploadResponse.class);
        String secondDocumentId = secondUploadResponse.getDocumentId();

        // Step 3: Verify same document ID is returned
        assertThat(secondDocumentId).isEqualTo(firstDocumentId);

        // Step 4: Verify only one file metadata record exists
        List<FileMetadata> allFiles = fileMetadataRepository.findAll();
        long matchingFiles = allFiles.stream()
                .filter(f -> f.getOriginalFilename().equals(originalFilename))
                .count();
        assertThat(matchingFiles).isEqualTo(1);

        // Step 5: Verify both files can be downloaded with same content
        MvcResult downloadResult = mockMvc.perform(get("/api/files/download/{documentId}", firstDocumentId))
                .andExpect(status().isOk())
                .andReturn();

        String downloadedContent = downloadResult.getResponse().getContentAsString();
        assertThat(downloadedContent).isEqualTo(content);
    }

    @Test
    void testErrorHandling() throws Exception {
        // Test 1: Upload empty file
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Test 2: Download non-existent file
        mockMvc.perform(get("/api/files/download/{documentId}", "non-existent-id"))
                .andExpect(status().isNotFound());

        // Test 3: Get metadata for non-existent file
        mockMvc.perform(get("/api/files/metadata/{documentId}", "non-existent-id"))
                .andExpect(status().isNotFound());

        // Test 4: Check existence of non-existent file
        mockMvc.perform(head("/api/files/download/{documentId}", "non-existent-id"))
                .andExpect(status().isNotFound());

        // Test 5: Invalid chunk upload session
        MockMultipartFile chunkFile = new MockMultipartFile(
                "chunkFile",
                "chunk.part",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test chunk".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload/chunks")
                        .file(chunkFile)
                        .param("sessionId", "invalid-session")
                        .param("chunkNumber", "0")
                        .param("totalChunks", "1")
                        .param("chunkHash", "test-hash")
                        .param("originalFilename", "test.txt")
                        .param("totalFileSize", "100")
                        .param("contentType", MediaType.TEXT_PLAIN_VALUE)
                        .param("fileHash", "file-hash"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testHealthChecks() throws Exception {
        // Test upload service health
        mockMvc.perform(get("/api/files/upload/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("File upload service is healthy"));

        // Test download service health
        mockMvc.perform(get("/api/files/download/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("File download service is healthy"));
    }

    @Test
    void testConcurrentOperations() throws Exception {
        // This test simulates concurrent file operations
        String baseFilename = "concurrent-test";
        String content = "Content for concurrent test";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        // Upload multiple files concurrently (simulated)
        String[] documentIds = new String[5];
        
        for (int i = 0; i < 5; i++) {
            String filename = baseFilename + "-" + i + ".txt";
            String fileContent = content + " - File " + i;
            
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    filename,
                    contentType,
                    fileContent.getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/files/upload")
                            .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            FileUploadResponse uploadResponse = objectMapper.readValue(
                    uploadResult.getResponse().getContentAsString(), FileUploadResponse.class);
            documentIds[i] = uploadResponse.getDocumentId();
        }

        // Verify all files can be downloaded concurrently
        for (int i = 0; i < 5; i++) {
            String expectedContent = content + " - File " + i;
            
            MvcResult downloadResult = mockMvc.perform(get("/api/files/download/{documentId}", documentIds[i]))
                    .andExpect(status().isOk())
                    .andReturn();

            String downloadedContent = downloadResult.getResponse().getContentAsString();
            assertThat(downloadedContent).isEqualTo(expectedContent);
        }

        // Verify all metadata can be retrieved
        for (String documentId : documentIds) {
            mockMvc.perform(get("/api/files/metadata/{documentId}", documentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.documentId").value(documentId));
        }

        // Verify audit logs are created for all operations
        List<FileAuditLog> allLogs = fileAuditLogRepository.findAll();
        assertThat(allLogs.size()).isGreaterThanOrEqualTo(10); // At least 5 uploads + 5 downloads
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