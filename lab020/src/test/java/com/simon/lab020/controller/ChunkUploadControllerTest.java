package com.simon.lab020.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.lab020.dto.ChunkUploadResponse;
import com.simon.lab020.entity.FileMetadata;
import com.simon.lab020.entity.UploadSession;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for chunk upload functionality
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class ChunkUploadControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private UploadSessionRepository uploadSessionRepository;

    @Autowired
    private FileChunkRepository fileChunkRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testChunkUpload_CompleteFlow() throws Exception {
        // Prepare test data
        String originalFilename = "large-test-file.txt";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        
        // Create test content (split into 3 chunks)
        String fullContent = "This is chunk 1 content. ".repeat(100) +
                           "This is chunk 2 content. ".repeat(100) +
                           "This is chunk 3 content. ".repeat(100);
        
        byte[] fullBytes = fullContent.getBytes();
        String fileHash = calculateSHA256(fullBytes);
        
        int totalChunks = 3;
        int chunkSize = fullBytes.length / totalChunks;
        
        // Step 1: Initialize chunk upload session
        MvcResult initResult = mockMvc.perform(post("/api/files/upload/chunks/init")
                        .param("originalFilename", originalFilename)
                        .param("totalSize", String.valueOf(fullBytes.length))
                        .param("totalChunks", String.valueOf(totalChunks))
                        .param("contentType", contentType)
                        .param("fileHash", fileHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.totalChunks").value(totalChunks))
                .andExpect(jsonPath("$.chunksUploaded").value(0))
                .andExpect(jsonPath("$.progress").value(0.0))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn();

        ChunkUploadResponse initResponse = objectMapper.readValue(
                initResult.getResponse().getContentAsString(), ChunkUploadResponse.class);
        String sessionId = initResponse.getSessionId();

        // Verify session is created in database
        Optional<UploadSession> session = uploadSessionRepository.findBySessionId(sessionId);
        assertThat(session).isPresent();
        assertThat(session.get().getOriginalFilename()).isEqualTo(originalFilename);
        assertThat(session.get().getTotalChunks()).isEqualTo(totalChunks);
        assertThat(session.get().getStatus()).isEqualTo(UploadSession.UploadStatus.IN_PROGRESS);

        // Step 2: Upload chunks
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
                    .andExpect(jsonPath("$.sessionId").value(sessionId))
                    .andExpect(jsonPath("$.chunkNumber").value(i))
                    .andExpect(jsonPath("$.uploadedChunks").value(i + 1))
                    .andReturn();

            ChunkUploadResponse chunkResponse = objectMapper.readValue(
                    chunkResult.getResponse().getContentAsString(), ChunkUploadResponse.class);

            // Check progress
            double expectedProgress = ((double) (i + 1) / totalChunks) * 100;
            assertThat(chunkResponse.getProgressPercentage()).isCloseTo(expectedProgress, within(0.1));

            // Check if completed on last chunk
            if (i == totalChunks - 1) {
                assertThat(chunkResponse.isCompleted()).isTrue();
                assertThat(chunkResponse.getDocumentId()).isNotNull();
                assertThat(chunkResponse.getMessage()).contains("completed successfully");

                // Verify final file is created
                Optional<FileMetadata> finalFile = fileMetadataRepository
                        .findByDocumentId(chunkResponse.getDocumentId());
                assertThat(finalFile).isPresent();
                assertThat(finalFile.get().getOriginalFilename()).isEqualTo(originalFilename);
                assertThat(finalFile.get().getFileSize()).isEqualTo(fullBytes.length);
                assertThat(finalFile.get().getFileHash()).isEqualTo(fileHash);
            } else {
                assertThat(chunkResponse.isCompleted()).isFalse();
                assertThat(chunkResponse.getDocumentId()).isNull();
            }
        }

        // Step 3: Verify session status is completed
        Optional<UploadSession> completedSession = uploadSessionRepository.findBySessionId(sessionId);
        assertThat(completedSession).isPresent();
        assertThat(completedSession.get().getStatus()).isEqualTo(UploadSession.UploadStatus.COMPLETED);
        assertThat(completedSession.get().getChunksUploaded()).isEqualTo(totalChunks);
    }

    @Test
    void testChunkUpload_GetStatus() throws Exception {
        // Initialize session
        String originalFilename = "status-test.txt";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        String fileHash = "test-hash-123";
        int totalChunks = 5;
        long totalSize = 1000L;

        MvcResult initResult = mockMvc.perform(post("/api/files/upload/chunks/init")
                        .param("originalFilename", originalFilename)
                        .param("totalSize", String.valueOf(totalSize))
                        .param("totalChunks", String.valueOf(totalChunks))
                        .param("contentType", contentType)
                        .param("fileHash", fileHash))
                .andExpect(status().isOk())
                .andReturn();

        ChunkUploadResponse initResponse = objectMapper.readValue(
                initResult.getResponse().getContentAsString(), ChunkUploadResponse.class);
        String sessionId = initResponse.getSessionId();

        // Get initial status
        mockMvc.perform(get("/api/files/upload/chunks/{sessionId}/status", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.totalChunks").value(totalChunks))
                .andExpect(jsonPath("$.chunksUploaded").value(0))
                .andExpect(jsonPath("$.progress").value(0.0))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void testChunkUpload_InvalidSession() throws Exception {
        String invalidSessionId = "invalid-session-id";

        // Try to get status for non-existent session
        mockMvc.perform(get("/api/files/upload/chunks/{sessionId}/status", invalidSessionId))
                .andExpect(status().isNotFound());

        // Try to upload chunk for non-existent session
        MockMultipartFile chunkFile = new MockMultipartFile(
                "chunkFile",
                "chunk.part",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test chunk data".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload/chunks")
                        .file(chunkFile)
                        .param("sessionId", invalidSessionId)
                        .param("chunkNumber", "0")
                        .param("totalChunks", "1")
                        .param("chunkHash", "test-hash")
                        .param("originalFilename", "test.txt")
                        .param("totalFileSize", "100")
                        .param("contentType", MediaType.TEXT_PLAIN_VALUE)
                        .param("fileHash", "file-hash"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Upload session not found"));
    }

    @Test
    void testChunkUpload_DuplicateFile() throws Exception {
        // Create existing file first
        String filename = "existing-file.txt";
        String content = "This file already exists";
        String fileHash = calculateSHA256(content.getBytes());
        
        // Save existing file metadata
        FileMetadata existingFile = new FileMetadata();
        existingFile.setDocumentId("existing-doc-id");
        existingFile.setOriginalFilename(filename);
        existingFile.setFileSize((long) content.length());
        existingFile.setContentType(MediaType.TEXT_PLAIN_VALUE);
        existingFile.setFileHash(fileHash);
        existingFile.setFilePath("test/path");
        existingFile.setEncryptionKey("test-key");
        existingFile.setUploadDate(LocalDateTime.now());
        fileMetadataRepository.save(existingFile);

        // Try to initialize chunk upload for duplicate file
        mockMvc.perform(post("/api/files/upload/chunks/init")
                        .param("originalFilename", filename)
                        .param("totalSize", String.valueOf(content.length()))
                        .param("totalChunks", "1")
                        .param("contentType", MediaType.TEXT_PLAIN_VALUE)
                        .param("fileHash", fileHash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.documentId").value("existing-doc-id"))
                .andExpect(jsonPath("$.message").value("File already exists"));
    }

    @Test
    void testChunkUpload_InvalidParameters() throws Exception {
        // Test missing parameters
        mockMvc.perform(post("/api/files/upload/chunks/init"))
                .andExpect(status().isBadRequest());

        // Test invalid chunk number
        MockMultipartFile chunkFile = new MockMultipartFile(
                "chunkFile",
                "chunk.part",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test data".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload/chunks")
                        .file(chunkFile)
                        .param("sessionId", "test-session")
                        .param("chunkNumber", "-1") // Invalid negative chunk number
                        .param("totalChunks", "1")
                        .param("chunkHash", "test-hash")
                        .param("originalFilename", "test.txt")
                        .param("totalFileSize", "100")
                        .param("contentType", MediaType.TEXT_PLAIN_VALUE)
                        .param("fileHash", "file-hash"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChunkUpload_OutOfOrderChunks() throws Exception {
        // Initialize session
        String originalFilename = "out-of-order-test.txt";
        String contentType = MediaType.TEXT_PLAIN_VALUE;
        String fileHash = "test-hash-out-of-order";
        int totalChunks = 3;
        long totalSize = 300L;

        MvcResult initResult = mockMvc.perform(post("/api/files/upload/chunks/init")
                        .param("originalFilename", originalFilename)
                        .param("totalSize", String.valueOf(totalSize))
                        .param("totalChunks", String.valueOf(totalChunks))
                        .param("contentType", contentType)
                        .param("fileHash", fileHash))
                .andExpect(status().isOk())
                .andReturn();

        ChunkUploadResponse initResponse = objectMapper.readValue(
                initResult.getResponse().getContentAsString(), ChunkUploadResponse.class);
        String sessionId = initResponse.getSessionId();

        // Upload chunks out of order: 2, 0, 1
        int[] chunkOrder = {2, 0, 1};
        
        for (int i = 0; i < chunkOrder.length; i++) {
            int chunkNumber = chunkOrder[i];
            String chunkData = "Chunk " + chunkNumber + " data";
            String chunkHash = calculateSHA256(chunkData.getBytes());
            
            MockMultipartFile chunkFile = new MockMultipartFile(
                    "chunkFile",
                    "chunk_" + chunkNumber + ".part",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    chunkData.getBytes()
            );

            MvcResult chunkResult = mockMvc.perform(multipart("/api/files/upload/chunks")
                            .file(chunkFile)
                            .param("sessionId", sessionId)
                            .param("chunkNumber", String.valueOf(chunkNumber))
                            .param("totalChunks", String.valueOf(totalChunks))
                            .param("chunkHash", chunkHash)
                            .param("originalFilename", originalFilename)
                            .param("totalFileSize", String.valueOf(totalSize))
                            .param("contentType", contentType)
                            .param("fileHash", fileHash))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            ChunkUploadResponse chunkResponse = objectMapper.readValue(
                    chunkResult.getResponse().getContentAsString(), ChunkUploadResponse.class);

            // Should complete only after all chunks are uploaded
            if (i == chunkOrder.length - 1) {
                assertThat(chunkResponse.isCompleted()).isTrue();
                assertThat(chunkResponse.getDocumentId()).isNotNull();
            } else {
                assertThat(chunkResponse.isCompleted()).isFalse();
            }
        }
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