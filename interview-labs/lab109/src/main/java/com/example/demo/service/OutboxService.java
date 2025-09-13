package com.example.demo.service;

import com.example.demo.entity.EventStatus;
import com.example.demo.entity.OutboxEvent;
import com.example.demo.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Outbox模式服务
 * 负责管理outbox事件的生命周期
 */
@Service
public class OutboxService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutboxService.class);
    private static final int MAX_RETRY_COUNT = 3;
    
    private final OutboxEventRepository outboxEventRepository;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public OutboxService(OutboxEventRepository outboxEventRepository, 
                        MessageService messageService,
                        ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 创建outbox事件（在同一个事务中）
     */
    @Transactional
    public OutboxEvent createEvent(String aggregateType, String aggregateId, 
                                  String eventType, Object eventData) {
        try {
            String payload = objectMapper.writeValueAsString(eventData);
            OutboxEvent event = new OutboxEvent(aggregateType, aggregateId, eventType, payload);
            
            OutboxEvent savedEvent = outboxEventRepository.save(event);
            logger.info("Outbox事件已创建: {}", savedEvent);
            
            return savedEvent;
            
        } catch (JsonProcessingException e) {
            logger.error("序列化事件数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建outbox事件失败", e);
        }
    }
    
    /**
     * 处理待处理的事件
     */
    @Transactional
    public void processEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(EventStatus.PENDING);
        
        logger.info("找到 {} 个待处理的outbox事件", pendingEvents.size());
        
        for (OutboxEvent event : pendingEvents) {
            processEvent(event);
        }
    }
    
    /**
     * 处理单个事件
     */
    @Transactional
    public void processEvent(OutboxEvent event) {
        logger.info("开始处理outbox事件: {}", event.getId());
        
        try {
            // 标记为处理中
            event.setStatus(EventStatus.PROCESSING);
            outboxEventRepository.save(event);
            
            // 根据事件类型处理事件
            handleEventByType(event);
            
            // 标记为已处理
            event.setStatus(EventStatus.PROCESSED);
            event.setProcessedAt(LocalDateTime.now());
            outboxEventRepository.save(event);
            
            logger.info("Outbox事件处理成功: {}", event.getId());
            
        } catch (Exception e) {
            logger.error("处理outbox事件失败: {}, 错误: {}", event.getId(), e.getMessage(), e);
            
            // 增加重试次数
            event.incrementRetryCount();
            event.setErrorMessage(e.getMessage());
            
            // 判断是否超过最大重试次数
            if (event.getRetryCount() >= MAX_RETRY_COUNT) {
                event.setStatus(EventStatus.DEAD_LETTER);
                logger.error("Outbox事件超过最大重试次数，标记为死信: {}", event.getId());
            } else {
                event.setStatus(EventStatus.FAILED);
                logger.warn("Outbox事件处理失败，等待重试: {}, 重试次数: {}", event.getId(), event.getRetryCount());
            }
            
            outboxEventRepository.save(event);
        }
    }
    
    /**
     * 根据事件类型处理事件
     */
    private void handleEventByType(OutboxEvent event) throws Exception {
        switch (event.getEventType()) {
            case "ORDER_CONFIRMED":
                handleOrderConfirmedEvent(event);
                break;
            case "ORDER_CANCELLED":
                handleOrderCancelledEvent(event);
                break;
            default:
                logger.warn("未知的事件类型: {}", event.getEventType());
                throw new IllegalArgumentException("未知的事件类型: " + event.getEventType());
        }
    }
    
    /**
     * 处理订单确认事件
     */
    private void handleOrderConfirmedEvent(OutboxEvent event) throws Exception {
        try {
            Map<String, Object> eventData = objectMapper.readValue(event.getPayload(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            String customerName = eventData.get("customerName").toString();
            String message = eventData.get("message").toString();
            
            messageService.sendOrderConfirmationMessage(orderId, customerName, message);
            
        } catch (Exception e) {
            logger.error("处理订单确认事件失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 处理订单取消事件
     */
    private void handleOrderCancelledEvent(OutboxEvent event) throws Exception {
        try {
            Map<String, Object> eventData = objectMapper.readValue(event.getPayload(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            String customerName = eventData.get("customerName").toString();
            String reason = eventData.get("reason").toString();
            
            messageService.sendOrderCancellationMessage(orderId, customerName, reason);
            
        } catch (Exception e) {
            logger.error("处理订单取消事件失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 重试失败的事件
     */
    @Transactional
    public void retryFailedEvents() {
        // 查找5分钟前创建的失败事件进行重试
        LocalDateTime retryTime = LocalDateTime.now().minusMinutes(5);
        List<OutboxEvent> retryableEvents = outboxEventRepository.findRetryableEvents(
                EventStatus.FAILED, MAX_RETRY_COUNT, retryTime);
        
        logger.info("找到 {} 个可重试的失败事件", retryableEvents.size());
        
        for (OutboxEvent event : retryableEvents) {
            logger.info("重试处理事件: {}, 当前重试次数: {}", event.getId(), event.getRetryCount());
            processEvent(event);
        }
    }
    
    /**
     * 清理已处理的历史事件
     */
    @Transactional
    public void cleanupProcessedEvents(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        
        try {
            outboxEventRepository.deleteByStatusAndCreatedAtBefore(EventStatus.PROCESSED, cutoffTime);
            logger.info("清理了 {} 天前的已处理事件", daysToKeep);
        } catch (Exception e) {
            logger.error("清理历史事件失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取事件统计信息
     */
    public Map<String, Long> getEventStatistics() {
        List<Object[]> stats = outboxEventRepository.countByStatus();
        return stats.stream()
                .collect(java.util.stream.Collectors.toMap(
                        stat -> ((EventStatus) stat[0]).name(),
                        stat -> (Long) stat[1]
                ));
    }
}