package com.simon.lab020.repository;

import com.simon.lab020.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FileMetadata entity
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * Find file metadata by document ID
     *
     * @param documentId the document ID
     * @return Optional containing the file metadata if found
     */
    Optional<FileMetadata> findByDocumentId(String documentId);

    /**
     * Check if a file with the given document ID exists
     *
     * @param documentId the document ID
     * @return true if exists, false otherwise
     */
    boolean existsByDocumentId(String documentId);

    /**
     * Find file by file hash (for duplicate detection)
     *
     * @param fileHash the file hash
     * @return Optional containing the first file with the same hash
     */
    Optional<FileMetadata> findByFileHash(String fileHash);

    /**
     * Find files uploaded within a date range
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of files uploaded within the date range
     */
    @Query("SELECT fm FROM FileMetadata fm WHERE fm.uploadDate BETWEEN :startDate AND :endDate ORDER BY fm.uploadDate DESC")
    List<FileMetadata> findByUploadDateBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Find files by content type
     *
     * @param contentType the content type
     * @return list of files with the specified content type
     */
    List<FileMetadata> findByContentType(String contentType);

    /**
     * Find files larger than specified size
     *
     * @param size the minimum file size
     * @return list of files larger than the specified size
     */
    @Query("SELECT fm FROM FileMetadata fm WHERE fm.fileSize > :size ORDER BY fm.fileSize DESC")
    List<FileMetadata> findByFileSizeGreaterThan(@Param("size") Long size);

    /**
     * Count total number of files
     *
     * @return total count of files
     */
    @Query("SELECT COUNT(fm) FROM FileMetadata fm")
    Long countTotalFiles();

    /**
     * Calculate total storage used
     *
     * @return total storage used in bytes
     */
    @Query("SELECT COALESCE(SUM(fm.fileSize), 0) FROM FileMetadata fm")
    Long calculateTotalStorageUsed();
}