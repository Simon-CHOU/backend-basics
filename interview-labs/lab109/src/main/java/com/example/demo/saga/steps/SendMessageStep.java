package com.example.demo.saga.steps;

import com.example.demo.saga.SagaData;
import com.example.demo.saga.SagaStep;
import com.example.demo.saga.SagaStepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 发送消息步骤
 */
@Component
public class SendMessageStep implements SagaStep {
    
    private static final Logger logger = LoggerFactory.getLogger(SendMessageStep.class);
    
    private final RabbitTemplate rabbitTemplate;
    
    @Autowired
    public SendMessageStep(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    @Override
    public String getStepName() {
        return "SEND_MESSAGE";
    }
    
    @Override
    public SagaStepResult execute(SagaData sagaData) throws Exception {
        logger.info("执行步骤: {}", getStepName());
        
        try {
            Long orderId = sagaData.getLong("orderId");
            String customerName = sagaData.getString("customerName");
            String productName = sagaData.getString("productName");
            String amount = sagaData.getString("amount");
            
            // 检查是否需要模拟失败
            Boolean shouldFail = sagaData.get("shouldFailMessage", Boolean.class);
            if (Boolean.TRUE.equals(shouldFail)) {
                logger.error("模拟消息发送失败");
                throw new RuntimeException("模拟消息发送失败");
            }
            
            Map<String, Object> message = new HashMap<>();
            message.put("orderId", orderId);
            message.put("customerName", customerName);
            message.put("productName", productName);
            message.put("amount", amount);
            message.put("action", "ORDER_CONFIRMED");
            message.put("timestamp", System.currentTimeMillis());
            
            logger.info("发送订单确认消息: {}", message);
            
            rabbitTemplate.convertAndSend("order.exchange", "order.confirmed", message);
            
            // 记录消息ID用于补偿（实际场景中可能需要消息队列支持消息撤回）
            String messageId = "msg_" + orderId + "_" + System.currentTimeMillis();
            sagaData.put("messageId", messageId);
            
            logger.info("订单确认消息发送成功: messageId={}", messageId);
            
            return SagaStepResult.success("消息发送成功", sagaData);
            
        } catch (Exception e) {
            logger.error("发送消息失败: {}", e.getMessage(), e);
            return SagaStepResult.failure("发送消息失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public SagaStepResult compensate(SagaData sagaData) throws Exception {
        logger.info("补偿步骤: {}", getStepName());
        
        try {
            Long orderId = sagaData.getLong("orderId");
            String messageId = sagaData.getString("messageId");
            String customerName = sagaData.getString("customerName");
            
            if (messageId == null) {
                logger.warn("消息ID为空，无需补偿");
                return SagaStepResult.success("消息ID为空，无需补偿");
            }
            
            // 发送补偿消息（取消订单消息）
            Map<String, Object> compensationMessage = new HashMap<>();
            compensationMessage.put("orderId", orderId);
            compensationMessage.put("customerName", customerName);
            compensationMessage.put("action", "ORDER_CANCELLED");
            compensationMessage.put("reason", "Saga补偿操作");
            compensationMessage.put("originalMessageId", messageId);
            compensationMessage.put("timestamp", System.currentTimeMillis());
            
            logger.info("发送订单取消补偿消息: {}", compensationMessage);
            
            rabbitTemplate.convertAndSend("order.exchange", "order.cancelled", compensationMessage);
            
            logger.info("补偿消息发送成功: orderId={}, messageId={}", orderId, messageId);
            
            return SagaStepResult.success("补偿消息发送成功");
            
        } catch (Exception e) {
            logger.error("发送补偿消息失败: {}", e.getMessage(), e);
            return SagaStepResult.failure("发送补偿消息失败: " + e.getMessage(), e);
        }
    }
}