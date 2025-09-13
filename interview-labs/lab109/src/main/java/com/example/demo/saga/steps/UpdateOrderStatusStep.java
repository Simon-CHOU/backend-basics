package com.example.demo.saga.steps;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.saga.SagaData;
import com.example.demo.saga.SagaStep;
import com.example.demo.saga.SagaStepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 更新订单状态步骤
 */
@Component
public class UpdateOrderStatusStep implements SagaStep {
    
    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderStatusStep.class);
    
    private final OrderRepository orderRepository;
    
    @Autowired
    public UpdateOrderStatusStep(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    public String getStepName() {
        return "UPDATE_ORDER_STATUS";
    }
    
    @Override
    @Transactional
    public SagaStepResult execute(SagaData sagaData) throws Exception {
        logger.info("执行步骤: {}", getStepName());
        
        try {
            Long orderId = sagaData.getLong("orderId");
            if (orderId == null) {
                return SagaStepResult.failure("订单ID为空");
            }
            
            // 检查是否需要模拟失败
            Boolean shouldFail = sagaData.get("shouldFailUpdate", Boolean.class);
            if (Boolean.TRUE.equals(shouldFail)) {
                logger.error("模拟订单状态更新失败");
                throw new RuntimeException("模拟订单状态更新失败");
            }
            
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                return SagaStepResult.failure("订单不存在: " + orderId);
            }
            
            Order order = orderOpt.get();
            OrderStatus originalStatus = order.getStatus();
            
            logger.info("更新订单状态: orderId={}, 原状态={}, 新状态={}", 
                    orderId, originalStatus, OrderStatus.CONFIRMED);
            
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            // 保存原始状态用于补偿
            sagaData.put("originalOrderStatus", originalStatus.name());
            
            logger.info("订单状态更新成功: orderId={}", orderId);
            
            return SagaStepResult.success("订单状态更新成功", sagaData);
            
        } catch (Exception e) {
            logger.error("更新订单状态失败: {}", e.getMessage(), e);
            return SagaStepResult.failure("更新订单状态失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public SagaStepResult compensate(SagaData sagaData) throws Exception {
        logger.info("补偿步骤: {}", getStepName());
        
        try {
            Long orderId = sagaData.getLong("orderId");
            if (orderId == null) {
                logger.warn("订单ID为空，无需补偿");
                return SagaStepResult.success("订单ID为空，无需补偿");
            }
            
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                logger.warn("订单不存在，无需补偿: orderId={}", orderId);
                return SagaStepResult.success("订单不存在，无需补偿");
            }
            
            Order order = orderOpt.get();
            String originalStatusStr = sagaData.getString("originalOrderStatus");
            
            if (originalStatusStr != null) {
                OrderStatus originalStatus = OrderStatus.valueOf(originalStatusStr);
                logger.info("恢复订单状态: orderId={}, 当前状态={}, 恢复到={}", 
                        orderId, order.getStatus(), originalStatus);
                
                order.setStatus(originalStatus);
                orderRepository.save(order);
                
                logger.info("订单状态恢复成功: orderId={}", orderId);
            } else {
                // 如果没有原始状态，设置为取消状态
                logger.info("设置订单为取消状态: orderId={}", orderId);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
            }
            
            return SagaStepResult.success("订单状态补偿成功");
            
        } catch (Exception e) {
            logger.error("订单状态补偿失败: {}", e.getMessage(), e);
            return SagaStepResult.failure("订单状态补偿失败: " + e.getMessage(), e);
        }
    }
}