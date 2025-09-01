package com.simon.lab020.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing file chunks for sharded uploads
 */
@Entity
@Table(name = "file_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chunk_id", nullable = false, unique = true, length = 36)
    private String chunkId;

    @Column(name = "document_id", nullable = false, length = 36)
    private String documentId;

    @Column(name = "chunk_number", nullable = false)
    private Integer chunkNumber;

    @Column(name = "total_chunks", nullable = false)
    private Integer totalChunks;

    @Column(name = "chunk_size", nullable = false)
    private Long chunkSize;

    @Column(name = "chunk_path", nullable = false, length = 500)
    private String chunkPath;

    @Column(name = "chunk_hash", nullable = false, length = 64)
    private String chunkHash;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "is_assembled", nullable = false)
    private Boolean isAssembled = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}