package com.example.demo.repository;

import com.example.demo.entity.SagaStatus;
import com.example.demo.entity.SagaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SagaTransactionRepository extends JpaRepository<SagaTransaction, String> {
    
    /**
     * 根据业务ID查找Saga事务
     */
    Optional<SagaTransaction> findByBusinessId(String businessId);
    
    /**
     * 根据状态查找Saga事务
     */
    List<SagaTransaction> findByStatusOrderByCreatedAtAsc(SagaStatus status);
    
    /**
     * 根据Saga类型和状态查找事务
     */
    List<SagaTransaction> findBySagaTypeAndStatusOrderByCreatedAtAsc(String sagaType, SagaStatus status);
    
    /**
     * 查找需要补偿的事务（执行中但长时间未更新的事务）
     */
    @Query("SELECT s FROM SagaTransaction s WHERE s.status IN :statuses AND s.updatedAt < :beforeTime")
    List<SagaTransaction> findStaleTransactions(
            @Param("statuses") List<SagaStatus> statuses,
            @Param("beforeTime") LocalDateTime beforeTime
    );
    
    /**
     * 统计各状态的事务数量
     */
    @Query("SELECT s.status, COUNT(s) FROM SagaTransaction s GROUP BY s.status")
    List<Object[]> countByStatus();
    
    /**
     * 删除已完成且创建时间早于指定时间的事务（用于清理历史数据）
     */
    void deleteByStatusInAndCreatedAtBefore(List<SagaStatus> statuses, LocalDateTime beforeTime);
    
    /**
     * 根据业务ID和Saga类型查找事务
     */
    List<SagaTransaction> findByBusinessIdAndSagaTypeOrderByCreatedAtAsc(String businessId, String sagaType);
}