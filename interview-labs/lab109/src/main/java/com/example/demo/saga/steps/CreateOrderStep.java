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

import java.math.BigDecimal;

/**
 * 创建订单步骤
 */
@Component
public class CreateOrderStep implements SagaStep {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateOrderStep.class);
    
    private final OrderRepository orderRepository;
    
    @Autowired
    public CreateOrderStep(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    public String getStepName() {
        return "CREATE_ORDER";
    }
    
    @Override
    @Transactional
    public SagaStepResult execute(SagaData sagaData) throws Exception {
        logger.info("执行步骤: {}", getStepName());
        
        try {
            String customerName = sagaData.getString("customerName");
            String productName = sagaData.getString("productName");
            BigDecimal amount = new BigDecimal(sagaData.getString("amount"));
            
            logger.info("创建订单: customer={}, product={}, amount={}", customerName, productName, amount);
            
            Order order = new Order(customerName, productName, amount);
            order = orderRepository.save(order);
            
            // 将订单ID保存到Saga数据中，供后续步骤使用
            sagaData.put("orderId", order.getId());
            
            logger.info("订单创建成功: {}", order);
            
            return SagaStepResult.success("订单创建成功", sagaData);
            
        } catch (Exception e) {
            logger.error("创建订单失败: {}", e.getMessage(), e);
            return SagaStepResult.failure("创建订单失败: " + e.getMessage(), e);
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
            
            logger.info("删除订单: orderId={}", orderId);
            
            orderRepository.deleteById(orderId);
            
            logger.info("订单删除成功: orderId={}", orderId);
            
            return SagaStepResult.success("订单删除成功");
            
        } catch (Exception e) {
            logger.error("删除订单失败: {}", e.getMessage(), e);
            return SagaStepResult.failure("删除订单失败: " + e.getMessage(), e);
        }
    }
}