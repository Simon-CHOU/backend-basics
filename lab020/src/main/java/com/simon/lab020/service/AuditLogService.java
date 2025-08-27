package com.simon.lab020.service;

import com.simon.lab020.entity.FileAuditLog;
import com.simon.lab020.repository.FileAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing file operation audit logs
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final FileAuditLogRepository auditLogRepository;

    /**
     * Log a file operation
     *
     * @param documentId the document ID
     * @param operation the operation type
     * @param request the HTTP request
     * @param details additional operation details
     */
    @Transactional
    public void logOperation(String documentId, FileAuditLog.Operation operation, 
                           HttpServletRequest request, String details) {
        try {
            FileAuditLog auditLog = new FileAuditLog();
            auditLog.setDocumentId(documentId);
            auditLog.setOperation(operation);
            auditLog.setUserIp(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setOperationDate(LocalDateTime.now());
            auditLog.setDetails(details);

            auditLogRepository.save(auditLog);
            
            log.debug("Audit log created: documentId={}, operation={}, ip={}", 
                     documentId, operation, getClientIpAddress(request));
                     
        } catch (Exception e) {
            log.error("Failed to create audit log: documentId={}, operation={}", 
                     documentId, operation, e);
            // Don't throw exception to avoid affecting main operation
        }
    }

    /**
     * Log file upload operation
     *
     * @param documentId the document ID
     * @param request the HTTP request
     * @param filename the uploaded filename
     * @param fileSize the file size
     */
    public void logUpload(String documentId, HttpServletRequest request, String filename, long fileSize) {
        String details = String.format("File uploaded: %s (%.2f KB)", filename, fileSize / 1024.0);
        logOperation(documentId, FileAuditLog.Operation.UPLOAD, request, details);
    }

    /**
     * Log file download operation
     *
     * @param documentId the document ID
     * @param request the HTTP request
     * @param filename the downloaded filename
     */
    public void logDownload(String documentId, HttpServletRequest request, String filename) {
        String details = String.format("File downloaded: %s", filename);
        logOperation(documentId, FileAuditLog.Operation.DOWNLOAD, request, details);
    }

    /**
     * Log file deletion operation
     *
     * @param documentId the document ID
     * @param request the HTTP request
     * @param filename the deleted filename
     */
    public void logDeletion(String documentId, HttpServletRequest request, String filename) {
        String details = String.format("File deleted: %s", filename);
        logOperation(documentId, FileAuditLog.Operation.DELETE, request, details);
    }

    /**
     * Get audit logs for a document
     *
     * @param documentId the document ID
     * @return list of audit logs
     */
    @Transactional(readOnly = true)
    public List<FileAuditLog> getAuditLogs(String documentId) {
        return auditLogRepository.findByDocumentIdOrderByOperationDateDesc(documentId);
    }

    /**
     * Get audit logs by operation type
     *
     * @param operation the operation type
     * @param startDate the start date
     * @param endDate the end date
     * @return list of audit logs
     */
    @Transactional(readOnly = true)
    public List<FileAuditLog> getAuditLogsByOperation(FileAuditLog.Operation operation, 
                                                     LocalDateTime startDate, 
                                                     LocalDateTime endDate) {
        return auditLogRepository.findByOperationAndOperationDateBetween(operation, startDate, endDate);
    }

    /**
     * Get operation statistics
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return operation statistics
     */
    @Transactional(readOnly = true)
    public Map<FileAuditLog.Operation, Long> getOperationStatistics(LocalDateTime startDate, 
                                                                   LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getOperationStatistics(startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (FileAuditLog.Operation) result[0],
                    result -> (Long) result[1]
                ));
    }

    /**
     * Get active IP addresses
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of active IP addresses
     */
    @Transactional(readOnly = true)
    public List<String> getActiveIpAddresses(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findActiveIpAddresses(startDate, endDate);
    }

    /**
     * Clean up old audit logs
     *
     * @param beforeDate delete logs before this date
     * @return number of deleted logs
     */
    @Transactional
    public int cleanupOldLogs(LocalDateTime beforeDate) {
        try {
            auditLogRepository.deleteByOperationDateBefore(beforeDate);
            log.info("Cleaned up old audit logs before {}", beforeDate);
            return 1; // Return success indicator
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs", e);
            return 0;
        }
    }

    /**
     * Extract client IP address from request
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs (take the first one)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}