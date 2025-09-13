package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    public MessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    /**
     * 发送订单确认消息
     * 模拟向外部系统（如通知服务、库存服务等）发送消息
     */
    public void sendOrderConfirmationMessage(Long orderId, String customerName, String message) {
        try {
            String routingKey = "order.confirmed";
            String messageBody = String.format(
                "订单确认通知: 订单ID=%d, 客户=%s, 消息=%s", 
                orderId, customerName, message
            );
            
            logger.info("准备发送消息: routingKey={}, message={}", routingKey, messageBody);
            
            // 模拟消息发送
            rabbitTemplate.convertAndSend("order.exchange", routingKey, messageBody);
            
            logger.info("消息发送成功: orderId={}", orderId);
            
        } catch (Exception e) {
            logger.error("消息发送失败: orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 发送订单取消消息
     */
    public void sendOrderCancellationMessage(Long orderId, String customerName, String reason) {
        try {
            String routingKey = "order.cancelled";
            String messageBody = String.format(
                "订单取消通知: 订单ID=%d, 客户=%s, 原因=%s", 
                orderId, customerName, reason
            );
            
            logger.info("准备发送取消消息: routingKey={}, message={}", routingKey, messageBody);
            
            rabbitTemplate.convertAndSend("order.exchange", routingKey, messageBody);
            
            logger.info("取消消息发送成功: orderId={}", orderId);
            
        } catch (Exception e) {
            logger.error("取消消息发送失败: orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("取消消息发送失败", e);
        }
    }
    
    /**
     * 模拟发送失败的情况
     */
    public void sendMessageWithFailure(Long orderId, String customerName) {
        logger.info("模拟消息发送失败场景: orderId={}", orderId);
        throw new RuntimeException("模拟的消息发送失败");
    }
}