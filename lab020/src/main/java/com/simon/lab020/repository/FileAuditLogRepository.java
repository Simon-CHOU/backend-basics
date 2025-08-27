package com.simon.lab020.repository;

import com.simon.lab020.entity.FileAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for FileAuditLog entity
 */
@Repository
public interface FileAuditLogRepository extends JpaRepository<FileAuditLog, Long> {

    /**
     * Find audit logs by document ID
     *
     * @param documentId the document ID
     * @return list of audit logs for the document
     */
    List<FileAuditLog> findByDocumentIdOrderByOperationDateDesc(String documentId);

    /**
     * Find audit logs by operation type
     *
     * @param operation the operation type
     * @return list of audit logs for the operation
     */
    List<FileAuditLog> findByOperationOrderByOperationDateDesc(FileAuditLog.Operation operation);

    /**
     * Find audit logs by document ID and operation type
     *
     * @param documentId the document ID
     * @param operation the operation type
     * @return list of audit logs for the document and operation
     */
    List<FileAuditLog> findByDocumentIdAndOperation(String documentId, FileAuditLog.Operation operation);

    /**
     * Find audit logs within a date range
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of audit logs within the date range
     */
    @Query("SELECT fal FROM FileAuditLog fal WHERE fal.operationDate BETWEEN :startDate AND :endDate ORDER BY fal.operationDate DESC")
    List<FileAuditLog> findByOperationDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by user IP
     *
     * @param userIp the user IP address
     * @return list of audit logs for the user IP
     */
    List<FileAuditLog> findByUserIpOrderByOperationDateDesc(String userIp);

    /**
     * Count operations by type within a date range
     *
     * @param operation the operation type
     * @param startDate start date
     * @param endDate end date
     * @return count of operations
     */
    @Query("SELECT COUNT(fal) FROM FileAuditLog fal WHERE fal.operation = :operation AND fal.operationDate BETWEEN :startDate AND :endDate")
    Long countOperationsByTypeAndDateRange(@Param("operation") FileAuditLog.Operation operation,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find most active IPs by operation count
     *
     * @param limit the maximum number of results
     * @return list of IP addresses with operation counts
     */
    @Query(value = "SELECT user_ip, COUNT(*) as operation_count FROM file_audit_log WHERE user_ip IS NOT NULL GROUP BY user_ip ORDER BY operation_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostActiveIPs(@Param("limit") int limit);

    /**
     * Find recent operations for a document
     *
     * @param documentId the document ID
     * @param limit the maximum number of results
     * @return list of recent audit logs for the document
     */
    @Query("SELECT fal FROM FileAuditLog fal WHERE fal.documentId = :documentId ORDER BY fal.operationDate DESC")
    List<FileAuditLog> findRecentOperationsForDocument(@Param("documentId") String documentId, 
                                                        @Param("limit") int limit);



    /**
     * Count total operations
     *
     * @return total count of operations
     */
    @Query("SELECT COUNT(fal) FROM FileAuditLog fal")
    Long countTotalOperations();

    /**
     * Get operation statistics for a date range
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of operation statistics [operation, count]
     */
    @Query("SELECT fal.operation, COUNT(fal) FROM FileAuditLog fal WHERE fal.operationDate BETWEEN :startDate AND :endDate GROUP BY fal.operation")
    List<Object[]> getOperationStatistics(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by operation and date range
     *
     * @param operation the operation type
     * @param startDate start date
     * @param endDate end date
     * @return list of audit logs
     */
    List<FileAuditLog> findByOperationAndOperationDateBetween(FileAuditLog.Operation operation,
                                                              LocalDateTime startDate,
                                                              LocalDateTime endDate);

    /**
     * Find active IP addresses within a date range
     *
     * @param startDate start date
     * @param endDate end date
     * @return list of active IP addresses
     */
    @Query("SELECT DISTINCT fal.userIp FROM FileAuditLog fal WHERE fal.operationDate BETWEEN :startDate AND :endDate AND fal.userIp IS NOT NULL")
    List<String> findActiveIpAddresses(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Delete audit logs before specified date
     *
     * @param cutoffDate the cutoff date
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM FileAuditLog fal WHERE fal.operationDate < :cutoffDate")
    int deleteByOperationDateBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}