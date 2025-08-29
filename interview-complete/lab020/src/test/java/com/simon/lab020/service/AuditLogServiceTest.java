package com.simon.lab020.service;

import com.simon.lab020.entity.FileAuditLog;
import com.simon.lab020.repository.FileAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogService
 * Following TDD and Clean Code principles with Given-When-Then pattern
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogService Tests")
class AuditLogServiceTest {

    @Mock
    private FileAuditLogRepository auditLogRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuditLogService auditLogService;

    private static final String TEST_DOCUMENT_ID = "test-doc-123";
    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_AGENT = "Mozilla/5.0 Test Browser";
    private static final String TEST_FILENAME = "test-file.txt";
    private static final long TEST_FILE_SIZE = 1024L;

    @BeforeEach
    void setUp() {
        // Given: Mock request headers (using lenient to avoid unnecessary stubbing errors)
        lenient().when(request.getHeader("User-Agent")).thenReturn(TEST_USER_AGENT);
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("X-Real-IP")).thenReturn(null);
        lenient().when(request.getRemoteAddr()).thenReturn(TEST_IP);
    }

    @Test
    @DisplayName("Should log operation successfully with all details")
    void testLogOperation_Success() {
        // Given: Valid operation parameters
        String details = "Test operation details";
        FileAuditLog.Operation operation = FileAuditLog.Operation.UPLOAD;
        
        // When: Log operation is called
        auditLogService.logOperation(TEST_DOCUMENT_ID, operation, request, details);
        
        // Then: Audit log should be saved with correct details
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getDocumentId()).isEqualTo(TEST_DOCUMENT_ID);
        assertThat(savedLog.getOperation()).isEqualTo(operation);
        assertThat(savedLog.getUserIp()).isEqualTo(TEST_IP);
        assertThat(savedLog.getUserAgent()).isEqualTo(TEST_USER_AGENT);
        assertThat(savedLog.getDetails()).isEqualTo(details);
        assertThat(savedLog.getOperationDate()).isNotNull();
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void testLogOperation_RepositoryException() {
        // Given: Repository throws exception
        when(auditLogRepository.save(any(FileAuditLog.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        // When: Log operation is called
        // Then: Should not throw exception (graceful handling)
        auditLogService.logOperation(TEST_DOCUMENT_ID, FileAuditLog.Operation.UPLOAD, request, "details");
        
        // Verify save was attempted
        verify(auditLogRepository).save(any(FileAuditLog.class));
    }

    @Test
    @DisplayName("Should extract IP from X-Forwarded-For header")
    void testLogOperation_XForwardedForHeader() {
        // Given: X-Forwarded-For header is present
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.1");
        
        // When: Log operation is called
        auditLogService.logOperation(TEST_DOCUMENT_ID, FileAuditLog.Operation.DOWNLOAD, request, "details");
        
        // Then: Should use first IP from X-Forwarded-For
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserIp()).isEqualTo("203.0.113.1");
    }

    @Test
    @DisplayName("Should extract IP from X-Real-IP header when X-Forwarded-For is not available")
    void testLogOperation_XRealIPHeader() {
        // Given: X-Real-IP header is present
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.2");
        
        // When: Log operation is called
        auditLogService.logOperation(TEST_DOCUMENT_ID, FileAuditLog.Operation.DELETE, request, "details");
        
        // Then: Should use X-Real-IP
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserIp()).isEqualTo("203.0.113.2");
    }

    @Test
    @DisplayName("Should log upload operation with file details")
    void testLogUpload_Success() {
        // Given: Valid upload parameters
        
        // When: Log upload is called
        auditLogService.logUpload(TEST_DOCUMENT_ID, request, TEST_FILENAME, TEST_FILE_SIZE);
        
        // Then: Should save audit log with upload details
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getDocumentId()).isEqualTo(TEST_DOCUMENT_ID);
        assertThat(savedLog.getOperation()).isEqualTo(FileAuditLog.Operation.UPLOAD);
        assertThat(savedLog.getDetails()).contains(TEST_FILENAME);
        assertThat(savedLog.getDetails()).contains("1.00 KB");
    }

    @Test
    @DisplayName("Should log download operation with filename")
    void testLogDownload_Success() {
        // Given: Valid download parameters
        
        // When: Log download is called
        auditLogService.logDownload(TEST_DOCUMENT_ID, request, TEST_FILENAME);
        
        // Then: Should save audit log with download details
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getDocumentId()).isEqualTo(TEST_DOCUMENT_ID);
        assertThat(savedLog.getOperation()).isEqualTo(FileAuditLog.Operation.DOWNLOAD);
        assertThat(savedLog.getDetails()).contains(TEST_FILENAME);
        assertThat(savedLog.getDetails()).contains("downloaded");
    }

    @Test
    @DisplayName("Should log deletion operation with filename")
    void testLogDeletion_Success() {
        // Given: Valid deletion parameters
        
        // When: Log deletion is called
        auditLogService.logDeletion(TEST_DOCUMENT_ID, request, TEST_FILENAME);
        
        // Then: Should save audit log with deletion details
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getDocumentId()).isEqualTo(TEST_DOCUMENT_ID);
        assertThat(savedLog.getOperation()).isEqualTo(FileAuditLog.Operation.DELETE);
        assertThat(savedLog.getDetails()).contains(TEST_FILENAME);
        assertThat(savedLog.getDetails()).contains("deleted");
    }

    @Test
    @DisplayName("Should retrieve audit logs for document")
    void testGetAuditLogs_Success() {
        // Given: Repository returns audit logs
        List<FileAuditLog> expectedLogs = Arrays.asList(
            createTestAuditLog(FileAuditLog.Operation.UPLOAD),
            createTestAuditLog(FileAuditLog.Operation.DOWNLOAD)
        );
        when(auditLogRepository.findByDocumentIdOrderByOperationDateDesc(TEST_DOCUMENT_ID))
            .thenReturn(expectedLogs);
        
        // When: Get audit logs is called
        List<FileAuditLog> actualLogs = auditLogService.getAuditLogs(TEST_DOCUMENT_ID);
        
        // Then: Should return expected logs
        assertThat(actualLogs).isEqualTo(expectedLogs);
        verify(auditLogRepository).findByDocumentIdOrderByOperationDateDesc(TEST_DOCUMENT_ID);
    }

    @Test
    @DisplayName("Should retrieve audit logs by operation and date range")
    void testGetAuditLogsByOperation_Success() {
        // Given: Valid operation and date range
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        FileAuditLog.Operation operation = FileAuditLog.Operation.UPLOAD;
        
        List<FileAuditLog> expectedLogs = Arrays.asList(
            createTestAuditLog(operation)
        );
        when(auditLogRepository.findByOperationAndOperationDateBetween(operation, startDate, endDate))
            .thenReturn(expectedLogs);
        
        // When: Get audit logs by operation is called
        List<FileAuditLog> actualLogs = auditLogService.getAuditLogsByOperation(operation, startDate, endDate);
        
        // Then: Should return expected logs
        assertThat(actualLogs).isEqualTo(expectedLogs);
        verify(auditLogRepository).findByOperationAndOperationDateBetween(operation, startDate, endDate);
    }

    @Test
    @DisplayName("Should retrieve operation statistics")
    void testGetOperationStatistics_Success() {
        // Given: Repository returns statistics data
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<Object[]> repositoryResults = Arrays.asList(
            new Object[]{FileAuditLog.Operation.UPLOAD, 10L},
            new Object[]{FileAuditLog.Operation.DOWNLOAD, 25L},
            new Object[]{FileAuditLog.Operation.DELETE, 5L}
        );
        when(auditLogRepository.getOperationStatistics(startDate, endDate))
            .thenReturn(repositoryResults);
        
        // When: Get operation statistics is called
        Map<FileAuditLog.Operation, Long> statistics = auditLogService.getOperationStatistics(startDate, endDate);
        
        // Then: Should return correct statistics map
        assertThat(statistics).hasSize(3);
        assertThat(statistics.get(FileAuditLog.Operation.UPLOAD)).isEqualTo(10L);
        assertThat(statistics.get(FileAuditLog.Operation.DOWNLOAD)).isEqualTo(25L);
        assertThat(statistics.get(FileAuditLog.Operation.DELETE)).isEqualTo(5L);
        verify(auditLogRepository).getOperationStatistics(startDate, endDate);
    }

    @Test
    @DisplayName("Should handle empty operation statistics")
    void testGetOperationStatistics_EmptyResults() {
        // Given: Repository returns empty results
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        
        when(auditLogRepository.getOperationStatistics(startDate, endDate))
            .thenReturn(Collections.emptyList());
        
        // When: Get operation statistics is called
        Map<FileAuditLog.Operation, Long> statistics = auditLogService.getOperationStatistics(startDate, endDate);
        
        // Then: Should return empty map
        assertThat(statistics).isEmpty();
        verify(auditLogRepository).getOperationStatistics(startDate, endDate);
    }

    @Test
    @DisplayName("Should retrieve active IP addresses")
    void testGetActiveIpAddresses_Success() {
        // Given: Repository returns IP addresses
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<String> expectedIps = Arrays.asList("192.168.1.100", "192.168.1.101", "203.0.113.1");
        when(auditLogRepository.findActiveIpAddresses(startDate, endDate))
            .thenReturn(expectedIps);
        
        // When: Get active IP addresses is called
        List<String> actualIps = auditLogService.getActiveIpAddresses(startDate, endDate);
        
        // Then: Should return expected IP addresses
        assertThat(actualIps).isEqualTo(expectedIps);
        verify(auditLogRepository).findActiveIpAddresses(startDate, endDate);
    }

    @Test
    @DisplayName("Should cleanup old logs successfully")
    void testCleanupOldLogs_Success() {
        // Given: Repository cleanup succeeds
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(90);
        when(auditLogRepository.deleteByOperationDateBefore(beforeDate))
            .thenReturn(50);
        
        // When: Cleanup old logs is called
        int result = auditLogService.cleanupOldLogs(beforeDate);
        
        // Then: Should return success indicator
        assertThat(result).isEqualTo(1);
        verify(auditLogRepository).deleteByOperationDateBefore(beforeDate);
    }

    @Test
    @DisplayName("Should handle cleanup failure gracefully")
    void testCleanupOldLogs_Failure() {
        // Given: Repository cleanup throws exception
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(90);
        when(auditLogRepository.deleteByOperationDateBefore(beforeDate))
            .thenThrow(new RuntimeException("Database error"));
        
        // When: Cleanup old logs is called
        int result = auditLogService.cleanupOldLogs(beforeDate);
        
        // Then: Should return failure indicator
        assertThat(result).isEqualTo(0);
        verify(auditLogRepository).deleteByOperationDateBefore(beforeDate);
    }

    @Test
    @DisplayName("Should handle null X-Forwarded-For header")
    void testGetClientIpAddress_NullXForwardedFor() {
        // Given: X-Forwarded-For is null
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        
        // When: Log operation is called
        auditLogService.logOperation(TEST_DOCUMENT_ID, FileAuditLog.Operation.UPLOAD, request, "details");
        
        // Then: Should use remote address
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserIp()).isEqualTo(TEST_IP);
    }

    @Test
    @DisplayName("Should handle empty X-Forwarded-For header")
    void testGetClientIpAddress_EmptyXForwardedFor() {
        // Given: X-Forwarded-For is empty
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        
        // When: Log operation is called
        auditLogService.logOperation(TEST_DOCUMENT_ID, FileAuditLog.Operation.UPLOAD, request, "details");
        
        // Then: Should use remote address
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getUserIp()).isEqualTo(TEST_IP);
    }

    @Test
    @DisplayName("Should handle large file size in upload log")
    void testLogUpload_LargeFileSize() {
        // Given: Large file size
        long largeFileSize = 1024L * 1024L * 100L; // 100 MB
        
        // When: Log upload is called
        auditLogService.logUpload(TEST_DOCUMENT_ID, request, TEST_FILENAME, largeFileSize);
        
        // Then: Should format file size correctly
        ArgumentCaptor<FileAuditLog> auditLogCaptor = ArgumentCaptor.forClass(FileAuditLog.class);
        verify(auditLogRepository).save(auditLogCaptor.capture());
        
        FileAuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getDetails()).contains("102400.00 KB");
    }

    /**
     * Helper method to create test audit log
     */
    private FileAuditLog createTestAuditLog(FileAuditLog.Operation operation) {
        return FileAuditLog.builder()
            .id(1L)
            .documentId(TEST_DOCUMENT_ID)
            .operation(operation)
            .userIp(TEST_IP)
            .userAgent(TEST_USER_AGENT)
            .operationDate(LocalDateTime.now())
            .details("Test details")
            .build();
    }
}