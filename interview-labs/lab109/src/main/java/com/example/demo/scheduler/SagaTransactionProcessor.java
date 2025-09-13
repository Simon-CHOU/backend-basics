package com.example.demo.scheduler;

import com.example.demo.entity.SagaStatus;
import com.example.demo.entity.SagaTransaction;
import com.example.demo.repository.SagaTransactionRepository;
import com.example.demo.saga.SagaOrchestrator;
import com.example.demo.saga.SagaStep;
import com.example.demo.saga.steps.CreateOrderStep;
import com.example.demo.saga.steps.SendMessageStep;
import com.example.demo.saga.steps.UpdateOrderStatusStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Saga事务处理器
 * 定时处理超时和失败的Saga事务
 */
@Component
public class SagaTransactionProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaTransactionProcessor.class);
    
    private final SagaTransactionRepository sagaRepository;
    private final SagaOrchestrator sagaOrchestrator;
    private final CreateOrderStep createOrderStep;
    private final SendMessageStep sendMessageStep;
    private final UpdateOrderStatusStep updateOrderStatusStep;
    
    @Autowired
    public SagaTransactionProcessor(
            SagaTransactionRepository sagaRepository,
            SagaOrchestrator sagaOrchestrator,
            CreateOrderStep createOrderStep,
            SendMessageStep sendMessageStep,
            UpdateOrderStatusStep updateOrderStatusStep) {
        this.sagaRepository = sagaRepository;
        this.sagaOrchestrator = sagaOrchestrator;
        this.createOrderStep = createOrderStep;
        this.sendMessageStep = sendMessageStep;
        this.updateOrderStatusStep = updateOrderStatusStep;
    }
    
    /**
     * 每30秒检查并恢复超时的Saga事务
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void processStaleTransactions() {
        logger.debug("开始检查超时的Saga事务");
        
        try {
            // 查找超过5分钟未更新的执行中或补偿中的事务
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
            List<SagaStatus> staleStatuses = Arrays.asList(
                    SagaStatus.EXECUTING,
                    SagaStatus.COMPENSATING
            );
            
            List<SagaTransaction> staleTransactions = sagaRepository.findStaleTransactions(
                    staleStatuses, timeoutThreshold);
            
            if (!staleTransactions.isEmpty()) {
                logger.info("发现 {} 个超时的Saga事务", staleTransactions.size());
                
                for (SagaTransaction saga : staleTransactions) {
                    try {
                        logger.info("恢复超时的Saga事务: sagaId={}, status={}, lastUpdate={}", 
                                saga.getId(), saga.getStatus(), saga.getUpdatedAt());
                        
                        List<SagaStep> steps = getStepsForSaga(saga);
                        sagaOrchestrator.resumeSaga(saga.getId(), steps);
                        
                        logger.info("超时Saga事务恢复完成: sagaId={}", saga.getId());
                        
                    } catch (Exception e) {
                        logger.error("恢复超时Saga事务失败: sagaId={}, 错误: {}", 
                                saga.getId(), e.getMessage(), e);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("处理超时Saga事务异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每小时清理已完成的Saga事务（保留7天）
     */
    @Scheduled(cron = "0 0 * * * *") // 每小时执行
    @Transactional
    public void cleanupCompletedTransactions() {
        logger.debug("开始清理已完成的Saga事务");
        
        try {
            LocalDateTime cleanupThreshold = LocalDateTime.now().minusDays(7);
            List<SagaStatus> completedStatuses = Arrays.asList(
                    SagaStatus.COMPLETED,
                    SagaStatus.COMPENSATED
            );
            
            long deletedCount = 0;
            
            // 分批删除，避免一次性删除过多数据
            List<SagaTransaction> transactionsToDelete = sagaRepository
                    .findAll()
                    .stream()
                    .filter(saga -> completedStatuses.contains(saga.getStatus()) && 
                            saga.getCreatedAt().isBefore(cleanupThreshold))
                    .limit(1000) // 每次最多删除1000条
                    .toList();
            
            if (!transactionsToDelete.isEmpty()) {
                for (SagaTransaction saga : transactionsToDelete) {
                    sagaRepository.delete(saga);
                    deletedCount++;
                }
                
                logger.info("清理了 {} 个已完成的Saga事务", deletedCount);
            }
            
        } catch (Exception e) {
            logger.error("清理已完成Saga事务异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每天凌晨2点生成Saga事务统计报告
     */
    @Scheduled(cron = "0 0 2 * * *") // 每天凌晨2点执行
    public void generateSagaStatistics() {
        logger.info("开始生成Saga事务统计报告");
        
        try {
            List<Object[]> statusCounts = sagaRepository.countByStatus();
            
            logger.info("=== Saga事务统计报告 ===");
            
            long totalCount = 0;
            for (Object[] statusCount : statusCounts) {
                SagaStatus status = (SagaStatus) statusCount[0];
                Long count = (Long) statusCount[1];
                totalCount += count;
                
                logger.info("状态: {} - 数量: {}", status, count);
            }
            
            logger.info("总计: {} 个Saga事务", totalCount);
            logger.info("=== 统计报告结束 ===");
            
        } catch (Exception e) {
            logger.error("生成Saga事务统计报告异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 根据Saga类型获取对应的步骤列表
     */
    private List<SagaStep> getStepsForSaga(SagaTransaction saga) {
        // 根据Saga类型返回对应的步骤
        // 这里简化处理，实际项目中可能需要更复杂的步骤管理
        return Arrays.asList(
                createOrderStep,
                sendMessageStep,
                updateOrderStatusStep
        );
    }
}