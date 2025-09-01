package com.simon.lab020.repository;

import com.simon.lab020.entity.UploadSession;
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
 * Repository interface for UploadSession entity
 */
@Repository
public interface UploadSessionRepository extends JpaRepository<UploadSession, Long> {

    /**
     * Find upload session by session ID
     *
     * @param sessionId the session ID
     * @return Optional containing the upload session if found
     */
    Optional<UploadSession> findBySessionId(String sessionId);

    /**
     * Find sessions by status
     *
     * @param status the upload status
     * @return list of sessions with the specified status
     */
    List<UploadSession> findByStatus(UploadSession.UploadStatus status);

    /**
     * Find expired sessions
     *
     * @param currentTime the current time
     * @return list of expired sessions
     */
    @Query("SELECT us FROM UploadSession us WHERE us.expiresAt < :currentTime AND us.status = 'IN_PROGRESS'")
    List<UploadSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find sessions that need cleanup (completed or failed sessions older than specified time)
     *
     * @param cutoffDate the cutoff date
     * @return list of sessions that need cleanup
     */
    @Query("SELECT us FROM UploadSession us WHERE us.updatedAt < :cutoffDate AND us.status IN ('COMPLETED', 'FAILED', 'EXPIRED')")
    List<UploadSession> findSessionsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update session status
     *
     * @param sessionId the session ID
     * @param status the new status
     */
    @Modifying
    @Transactional
    @Query("UPDATE UploadSession us SET us.status = :status, us.updatedAt = CURRENT_TIMESTAMP WHERE us.sessionId = :sessionId")
    void updateSessionStatus(@Param("sessionId") String sessionId, @Param("status") UploadSession.UploadStatus status);

    /**
     * Increment chunks uploaded count
     *
     * @param sessionId the session ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE UploadSession us SET us.chunksUploaded = us.chunksUploaded + 1, us.updatedAt = CURRENT_TIMESTAMP WHERE us.sessionId = :sessionId")
    void incrementChunksUploaded(@Param("sessionId") String sessionId);

    /**
     * Mark expired sessions
     *
     * @param currentTime the current time
     */
    @Modifying
    @Transactional
    @Query("UPDATE UploadSession us SET us.status = 'EXPIRED', us.updatedAt = CURRENT_TIMESTAMP WHERE us.expiresAt < :currentTime AND us.status = 'IN_PROGRESS'")
    void markExpiredSessions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Delete sessions by IDs
     *
     * @param sessionIds list of session IDs to delete
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UploadSession us WHERE us.sessionId IN :sessionIds")
    void deleteBySessionIds(@Param("sessionIds") List<String> sessionIds);

    /**
     * Check if session exists and is active
     *
     * @param sessionId the session ID
     * @return true if session exists and is in progress
     */
    @Query("SELECT COUNT(us) > 0 FROM UploadSession us WHERE us.sessionId = :sessionId AND us.status = 'IN_PROGRESS' AND us.expiresAt > CURRENT_TIMESTAMP")
    boolean existsActiveSession(@Param("sessionId") String sessionId);
}