package com.simon.lab020.repository;

import com.simon.lab020.entity.FileChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FileChunk entity
 */
@Repository
public interface FileChunkRepository extends JpaRepository<FileChunk, Long> {

    /**
     * Find file chunk by chunk ID
     *
     * @param chunkId the chunk ID
     * @return Optional containing the file chunk if found
     */
    Optional<FileChunk> findByChunkId(String chunkId);

    /**
     * Find all chunks for a specific document
     *
     * @param documentId the document ID
     * @return list of chunks for the document
     */
    List<FileChunk> findByDocumentIdOrderByChunkNumber(String documentId);

    /**
     * Find a specific chunk by document ID and chunk number
     *
     * @param documentId the document ID
     * @param chunkNumber the chunk number
     * @return Optional containing the file chunk if found
     */
    Optional<FileChunk> findByDocumentIdAndChunkNumber(String documentId, Integer chunkNumber);

    /**
     * Count uploaded chunks for a document
     *
     * @param documentId the document ID
     * @return number of uploaded chunks
     */
    @Query("SELECT COUNT(fc) FROM FileChunk fc WHERE fc.documentId = :documentId")
    Integer countByDocumentId(@Param("documentId") String documentId);

    /**
     * Check if all chunks are uploaded for a document
     *
     * @param documentId the document ID
     * @param totalChunks the total number of chunks expected
     * @return true if all chunks are uploaded
     */
    @Query("SELECT COUNT(fc) = :totalChunks FROM FileChunk fc WHERE fc.documentId = :documentId")
    boolean areAllChunksUploaded(@Param("documentId") String documentId, @Param("totalChunks") Integer totalChunks);

    /**
     * Find unassembled chunks older than specified date
     *
     * @param cutoffDate the cutoff date
     * @return list of unassembled chunks older than cutoff date
     */
    @Query("SELECT fc FROM FileChunk fc WHERE fc.isAssembled = false AND fc.uploadDate < :cutoffDate")
    List<FileChunk> findUnassembledChunksOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Mark chunks as assembled for a document
     *
     * @param documentId the document ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE FileChunk fc SET fc.isAssembled = true WHERE fc.documentId = :documentId")
    void markChunksAsAssembled(@Param("documentId") String documentId);

    /**
     * Delete chunks by document ID
     *
     * @param documentId the document ID
     */
    @Modifying
    @Transactional
    void deleteByDocumentId(String documentId);

    /**
     * Delete unassembled chunks older than specified date
     *
     * @param cutoffDate the cutoff date
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM FileChunk fc WHERE fc.isAssembled = false AND fc.uploadDate < :cutoffDate")
    void deleteUnassembledChunksOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if a chunk exists for document and chunk number
     *
     * @param documentId the document ID
     * @param chunkNumber the chunk number
     * @return true if chunk exists
     */
    boolean existsByDocumentIdAndChunkNumber(String documentId, Integer chunkNumber);

    /**
     * Find uploaded chunk numbers for a document
     *
     * @param documentId the document ID
     * @return list of uploaded chunk numbers
     */
    @Query("SELECT fc.chunkNumber FROM FileChunk fc WHERE fc.documentId = :documentId ORDER BY fc.chunkNumber")
    List<Integer> findUploadedChunkNumbers(@Param("documentId") String documentId);
}