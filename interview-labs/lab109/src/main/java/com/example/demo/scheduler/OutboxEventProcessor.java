package com.example.demo.scheduler;

import com.example.demo.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbox事件处理器
 * 定时处理待发送的outbox事件
 */
@Component
public class OutboxEventProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(OutboxEventProcessor.class);
    
    private final OutboxService outboxService;
    
    @Autowired
    public OutboxEventProcessor(OutboxService outboxService) {
        this.outboxService = outboxService;
    }
    
    /**
     * 每5秒处理一次待处理的事件
     */
    @Scheduled(fixedDelay = 5000)
    public void processEvents() {
        try {
            logger.debug("开始处理outbox事件...");
            outboxService.processEvents();
            logger.debug("Outbox事件处理完成");
        } catch (Exception e) {
            logger.error("处理outbox事件时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每分钟重试一次失败的事件
     */
    @Scheduled(fixedDelay = 60000)
    public void retryFailedEvents() {
        try {
            logger.debug("开始重试失败的outbox事件...");
            outboxService.retryFailedEvents();
            logger.debug("失败事件重试完成");
        } catch (Exception e) {
            logger.error("重试失败事件时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 每天凌晨2点清理7天前的已处理事件
     */
    @Scheduled(cron = "0 0 2 * * ?") 
    public void cleanupProcessedEvents() {
        try {
            logger.info("开始清理历史outbox事件...");
            outboxService.cleanupProcessedEvents(7);
            logger.info("历史事件清理完成");
        } catch (Exception e) {
            logger.error("清理历史事件时发生错误: {}", e.getMessage(), e);
        }
    }
}