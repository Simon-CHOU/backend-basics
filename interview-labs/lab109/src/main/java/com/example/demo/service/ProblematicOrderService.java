package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 问题场景演示服务
 * 展示在分布式事务中消息发送与数据库操作不一致的问题
 */
@Service
public class ProblematicOrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProblematicOrderService.class);
    
    private final OrderRepository orderRepository;
    private final MessageService messageService;
    
    @Autowired
    public ProblematicOrderService(OrderRepository orderRepository, MessageService messageService) {
        this.orderRepository = orderRepository;
        this.messageService = messageService;
    }
    
    /**
     * 场景1: 消息发送在事务之前
     * 问题: 如果消息发送成功但数据库事务失败，消息无法回滚
     */
    @Transactional
    public Order createOrderMessageFirst(String customerName, String productName, BigDecimal amount, boolean simulateDbFailure) {
        logger.info("=== 场景1: 消息发送在事务之前 ===");
        logger.info("创建订单: customer={}, product={}, amount={}, simulateFailure={}", 
                   customerName, productName, amount, simulateDbFailure);
        
        Order order = new Order(customerName, productName, amount);
        
        try {
            // 1. 先发送消息（在事务外）
            logger.info("步骤1: 发送订单确认消息");
            messageService.sendOrderConfirmationMessage(999L, customerName, "订单即将创建");
            logger.info("消息发送成功！");
            
            // 2. 保存订单到数据库（在事务内）
            logger.info("步骤2: 保存订单到数据库");
            order = orderRepository.save(order);
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            
            // 3. 模拟数据库操作失败
            if (simulateDbFailure) {
                logger.error("模拟数据库操作失败！");
                throw new RuntimeException("模拟的数据库操作失败");
            }
            
            logger.info("订单创建成功: {}", order);
            return order;
            
        } catch (Exception e) {
            logger.error("订单创建失败，但消息已经发送！这就是问题所在: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 场景2: 消息发送在事务之后
     * 问题: 事务提交后发送消息，如果消息发送失败，数据库已经提交无法回滚
     */
    @Transactional
    public Order createOrderMessageAfter(String customerName, String productName, BigDecimal amount, boolean simulateMessageFailure) {
        logger.info("=== 场景2: 消息发送在事务之后 ===");
        logger.info("创建订单: customer={}, product={}, amount={}, simulateMessageFailure={}", 
                   customerName, productName, amount, simulateMessageFailure);
        
        try {
            // 1. 先保存订单到数据库（在事务内）
            logger.info("步骤1: 保存订单到数据库");
            Order order = new Order(customerName, productName, amount);
            order = orderRepository.save(order);
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            logger.info("数据库操作成功: {}", order);
            
            // 2. 发送消息（仍在事务内，但模拟事务提交后的场景）
            logger.info("步骤2: 发送订单确认消息");
            if (simulateMessageFailure) {
                logger.error("模拟消息发送失败！");
                throw new RuntimeException("模拟的消息发送失败");
            }
            
            messageService.sendOrderConfirmationMessage(order.getId(), customerName, "订单已创建");
            logger.info("消息发送成功！");
            
            return order;
            
        } catch (Exception e) {
            logger.error("操作失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 场景3: 消息发送在事务内部
     * 问题: 消息发送的IO延迟会影响事务持续时间，降低系统吞吐量
     */
    @Transactional
    public Order createOrderMessageInTransaction(String customerName, String productName, BigDecimal amount, boolean simulateSlowMessage) {
        logger.info("=== 场景3: 消息发送在事务内部 ===");
        logger.info("创建订单: customer={}, product={}, amount={}, simulateSlowMessage={}", 
                   customerName, productName, amount, simulateSlowMessage);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 保存订单到数据库
            logger.info("步骤1: 保存订单到数据库");
            Order order = new Order(customerName, productName, amount);
            order = orderRepository.save(order);
            
            // 2. 在事务内发送消息（模拟IO延迟）
            logger.info("步骤2: 在事务内发送消息");
            if (simulateSlowMessage) {
                logger.info("模拟消息发送延迟...");
                try {
                    Thread.sleep(2000); // 模拟2秒的网络延迟
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            
            messageService.sendOrderConfirmationMessage(order.getId(), customerName, "订单已创建");
            
            // 3. 更新订单状态
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            
            long endTime = System.currentTimeMillis();
            logger.info("事务完成，总耗时: {}ms", endTime - startTime);
            
            return order;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("事务失败，总耗时: {}ms, 错误: {}", endTime - startTime, e.getMessage());
            throw e;
        }
    }
}