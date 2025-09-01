package com.simon.lab020.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.lab020.dto.FileUploadResponse;
import com.simon.lab020.entity.FileMetadata;
import com.simon.lab020.repository.FileMetadataRepository;
import com.simon.lab020.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FileUploadController
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class FileUploadControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testUploadFile_Success() throws Exception {
        // Prepare test file
        String filename = "test-file.txt";
        String content = "This is a test file content for upload testing.";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes()
        );

        // Perform upload
        MvcResult result = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentId").exists())
                .andExpect(jsonPath("$.originalFilename").value(filename))
                .andExpect(jsonPath("$.fileSize").value(content.length()))
                .andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$.fileHash").exists())
                .andExpect(jsonPath("$.uploadDate").exists())
                .andExpect(jsonPath("$.message").value("File uploaded successfully"))
                .andReturn();

        // Parse response
        String responseContent = result.getResponse().getContentAsString();
        FileUploadResponse response = objectMapper.readValue(responseContent, FileUploadResponse.class);

        // Verify database record
        Optional<FileMetadata> savedFile = fileMetadataRepository.findByDocumentId(response.getDocumentId());
        assertThat(savedFile).isPresent();
        assertThat(savedFile.get().getOriginalFilename()).isEqualTo(filename);
        assertThat(savedFile.get().getFileSize()).isEqualTo(content.length());
        assertThat(savedFile.get().getContentType()).isEqualTo(MediaType.TEXT_PLAIN_VALUE);
        assertThat(savedFile.get().getEncryptionKey()).isNotNull();
        assertThat(savedFile.get().getFileHash()).isNotNull();
        assertThat(savedFile.get().getFilePath()).isNotNull();
    }

    @Test
    void testUploadFile_DuplicateFile() throws Exception {
        // Prepare test file
        String filename = "duplicate-test.txt";
        String content = "This is a duplicate test file.";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                MediaType.TEXT_PLAIN_VALUE,
                content.getBytes()
        );

        // First upload
        MvcResult firstResult = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String firstResponseContent = firstResult.getResponse().getContentAsString();
        FileUploadResponse firstResponse = objectMapper.readValue(firstResponseContent, FileUploadResponse.class);

        // Second upload (duplicate)
        MvcResult secondResult = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.documentId").value(firstResponse.getDocumentId()))
                .andExpect(jsonPath("$.message").value("File already exists"))
                .andReturn();

        // Verify only one record exists in database
        long count = fileMetadataRepository.count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void testUploadFile_EmptyFile() throws Exception {
        // Prepare empty file
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        // Perform upload
        mockMvc.perform(multipart("/api/files/upload")
                        .file(emptyFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testUploadFile_LargeFile() throws Exception {
        // Prepare large file (over 100MB limit)
        byte[] largeContent = new byte[101 * 1024 * 1024]; // 101MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-file.bin",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                largeContent
        );

        // Perform upload
        mockMvc.perform(multipart("/api/files/upload")
                        .file(largeFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("exceeds maximum limit")));
    }

    @Test
    void testUploadFile_InvalidFilename() throws Exception {
        // Prepare file with invalid filename
        MockMultipartFile fileWithInvalidName = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                MediaType.TEXT_PLAIN_VALUE,
                "malicious content".getBytes()
        );

        // Perform upload
        mockMvc.perform(multipart("/api/files/upload")
                        .file(fileWithInvalidName)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("invalid path sequence")));
    }

    @Test
    void testUploadFile_MissingFile() throws Exception {
        // Perform upload without file parameter
        mockMvc.perform(multipart("/api/files/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadFile_MultipleFiles() throws Exception {
        // Prepare multiple test files
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "file1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Content of file 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "file2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Content of file 2".getBytes()
        );

        // Upload first file
        MvcResult result1 = mockMvc.perform(multipart("/api/files/upload")
                        .file(file1)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        // Upload second file
        MvcResult result2 = mockMvc.perform(multipart("/api/files/upload")
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        // Parse responses
        FileUploadResponse response1 = objectMapper.readValue(
                result1.getResponse().getContentAsString(), FileUploadResponse.class);
        FileUploadResponse response2 = objectMapper.readValue(
                result2.getResponse().getContentAsString(), FileUploadResponse.class);

        // Verify different document IDs
        assertThat(response1.getDocumentId()).isNotEqualTo(response2.getDocumentId());

        // Verify both files are saved
        assertThat(fileMetadataRepository.count()).isEqualTo(2);
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/files/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("File upload service is healthy"));
    }

    @Test
    void testUploadFile_VariousContentTypes() throws Exception {
        // Test different content types
        String[] contentTypes = {
                MediaType.TEXT_PLAIN_VALUE,
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_PDF_VALUE,
                MediaType.IMAGE_JPEG_VALUE,
                MediaType.APPLICATION_OCTET_STREAM_VALUE
        };

        String[] extensions = {".txt", ".json", ".pdf", ".jpg", ".bin"};

        for (int i = 0; i < contentTypes.length; i++) {
            String filename = "test-file" + extensions[i];
            String content = "Test content for " + contentTypes[i];
            
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    filename,
                    contentTypes[i],
                    content.getBytes()
            );

            mockMvc.perform(multipart("/api/files/upload")
                            .file(file)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.contentType").value(contentTypes[i]));
        }

        // Verify all files are saved
        assertThat(fileMetadataRepository.count()).isEqualTo(contentTypes.length);
    }
}