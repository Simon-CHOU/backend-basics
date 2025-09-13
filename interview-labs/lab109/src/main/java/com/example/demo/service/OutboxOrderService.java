package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.OutboxEvent;
import com.example.demo.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用Outbox模式的订单服务
 * 解决分布式事务中消息发送与数据库操作的一致性问题
 */
@Service
public class OutboxOrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutboxOrderService.class);
    
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    
    @Autowired
    public OutboxOrderService(OrderRepository orderRepository, OutboxService outboxService) {
        this.orderRepository = orderRepository;
        this.outboxService = outboxService;
    }
    
    /**
     * 使用Outbox模式创建订单
     * 在同一个事务中保存订单和outbox事件，确保原子性
     */
    @Transactional
    public Order createOrderWithOutbox(String customerName, String productName, BigDecimal amount, boolean simulateFailure) {
        logger.info("=== 使用Outbox模式创建订单 ===");
        logger.info("创建订单: customer={}, product={}, amount={}, simulateFailure={}", 
                   customerName, productName, amount, simulateFailure);
        
        try {
            // 1. 保存订单到数据库
            logger.info("步骤1: 保存订单到数据库");
            Order order = new Order(customerName, productName, amount);
            order = orderRepository.save(order);
            logger.info("订单保存成功: {}", order);
            
            // 2. 创建outbox事件（在同一个事务中）
            logger.info("步骤2: 创建outbox事件");
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", order.getId());
            eventData.put("customerName", customerName);
            eventData.put("message", "订单已创建，等待确认");
            
            OutboxEvent outboxEvent = outboxService.createEvent(
                    "Order", 
                    order.getId().toString(), 
                    "ORDER_CONFIRMED", 
                    eventData
            );
            logger.info("Outbox事件创建成功: {}", outboxEvent.getId());
            
            // 3. 更新订单状态
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            
            // 4. 模拟数据库操作失败
            if (simulateFailure) {
                logger.error("模拟数据库操作失败！");
                throw new RuntimeException("模拟的数据库操作失败");
            }
            
            logger.info("订单创建成功: {}", order);
            logger.info("注意: 消息将通过后台任务异步发送，确保最终一致性");
            
            return order;
            
        } catch (Exception e) {
            logger.error("订单创建失败，事务将回滚，outbox事件也会被回滚: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 取消订单并发送取消消息
     */
    @Transactional
    public Order cancelOrderWithOutbox(Long orderId, String reason) {
        logger.info("=== 使用Outbox模式取消订单 ===");
        logger.info("取消订单: orderId={}, reason={}", orderId, reason);
        
        try {
            // 1. 查找并更新订单状态
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("订单不存在: " + orderId));
            
            logger.info("找到订单: {}", order);
            
            order.setStatus(OrderStatus.CANCELLED);
            order = orderRepository.save(order);
            logger.info("订单状态已更新为取消");
            
            // 2. 创建取消事件
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("orderId", orderId);
            eventData.put("customerName", order.getCustomerName());
            eventData.put("reason", reason);
            
            OutboxEvent outboxEvent = outboxService.createEvent(
                    "Order", 
                    orderId.toString(), 
                    "ORDER_CANCELLED", 
                    eventData
            );
            logger.info("订单取消事件创建成功: {}", outboxEvent.getId());
            
            return order;
            
        } catch (Exception e) {
            logger.error("取消订单失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 批量创建订单（演示批量操作的原子性）
     */
    @Transactional
    public void createMultipleOrdersWithOutbox(int count, String customerName, boolean simulateFailure) {
        logger.info("=== 批量创建订单 ===");
        logger.info("创建 {} 个订单，客户: {}, 模拟失败: {}", count, customerName, simulateFailure);
        
        try {
            for (int i = 1; i <= count; i++) {
                String productName = "Product-" + i;
                BigDecimal amount = new BigDecimal("100.00").multiply(new BigDecimal(i));
                
                // 在最后一个订单时模拟失败
                boolean shouldFail = simulateFailure && (i == count);
                
                createOrderWithOutbox(customerName, productName, amount, shouldFail);
                
                logger.info("第 {} 个订单创建完成", i);
            }
            
            logger.info("所有订单创建成功");
            
        } catch (Exception e) {
            logger.error("批量创建订单失败，所有操作将回滚: {}", e.getMessage());
            throw e;
        }
    }
}