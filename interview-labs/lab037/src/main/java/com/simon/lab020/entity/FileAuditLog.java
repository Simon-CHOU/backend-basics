package com.simon.lab020.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing audit log for file operations
 */
@Entity
@Table(name = "file_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false, length = 36)
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 20)
    private Operation operation;

    @Column(name = "user_ip", length = 45)
    private String userIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "operation_date", nullable = false, updatable = false)
    private LocalDateTime operationDate;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Enum for file operations
     */
    public enum Operation {
        UPLOAD,
        DOWNLOAD,
        DELETE
    }
}