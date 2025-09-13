package com.example.demo.repository;

import com.example.demo.entity.EventStatus;
import com.example.demo.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    
    /**
     * 查找待处理的事件
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(EventStatus status);
    
    /**
     * 查找指定状态的事件，按创建时间排序
     */
    List<OutboxEvent> findByStatusInOrderByCreatedAtAsc(List<EventStatus> statuses);
    
    /**
     * 查找需要重试的失败事件（重试次数小于最大值且创建时间在指定时间之前）
     */
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status AND e.retryCount < :maxRetries AND e.createdAt < :beforeTime")
    List<OutboxEvent> findRetryableEvents(
            @Param("status") EventStatus status,
            @Param("maxRetries") Integer maxRetries,
            @Param("beforeTime") LocalDateTime beforeTime
    );
    
    /**
     * 查找特定聚合的事件
     */
    List<OutboxEvent> findByAggregateTypeAndAggregateIdOrderByCreatedAtAsc(String aggregateType, String aggregateId);
    
    /**
     * 查找特定事件类型的事件
     */
    List<OutboxEvent> findByEventTypeOrderByCreatedAtAsc(String eventType);
    
    /**
     * 删除已处理且创建时间早于指定时间的事件（用于清理历史数据）
     */
    void deleteByStatusAndCreatedAtBefore(EventStatus status, LocalDateTime beforeTime);
    
    /**
     * 统计各状态的事件数量
     */
    @Query("SELECT e.status, COUNT(e) FROM OutboxEvent e GROUP BY e.status")
    List<Object[]> countByStatus();
    
    /**
     * 查找特定聚合和事件类型的事件
     */
    List<OutboxEvent> findByAggregateIdAndEventTypeOrderByCreatedAtAsc(String aggregateId, String eventType);
}