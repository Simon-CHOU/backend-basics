package com.example.demo;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.ProblematicOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 问题场景测试
 * 验证消息发送与事务回滚的不一致性问题
 */
@SpringBootTest
@ActiveProfiles("test")
class ProblematicOrderServiceTest {
    
    @Autowired
    private ProblematicOrderService problematicOrderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.deleteAll();
    }
    
    @Test
    void testSendMessageBeforeTransaction_Success() {
        // 测试事务前发送消息（正常情况）
        String customerName = "张三";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        assertDoesNotThrow(() -> {
            problematicOrderService.createOrderMessageFirst(customerName, productName, amount, false);
        });
        
        // 验证订单已创建
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.CONFIRMED, orders.get(0).getStatus());
    }
    
    @Test
    void testSendMessageBeforeTransaction_TransactionFails() {
        // 测试事务前发送消息（事务失败情况）
        String customerName = "李四";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        // 事务会失败，但消息已经发送
        assertThrows(RuntimeException.class, () -> {
            problematicOrderService.createOrderMessageFirst(customerName, productName, amount, true);
        });
        
        // 验证订单未创建（事务回滚）
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(0, orders.size());
        
        // 但消息已经发送，造成不一致
    }
    
    @Test
    void testSendMessageAfterTransaction_Success() {
        // 测试事务后发送消息（正常情况）
        String customerName = "王五";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        assertDoesNotThrow(() -> {
            problematicOrderService.createOrderMessageAfter(customerName, productName, amount, false);
        });
        
        // 验证订单已创建
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.CONFIRMED, orders.get(0).getStatus());
    }
    
    @Test
    void testSendMessageAfterTransaction_MessageFails() {
        // 测试事务后发送消息（消息发送失败情况）
        String customerName = "赵六";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        // 消息发送会失败，但事务已提交
        assertThrows(RuntimeException.class, () -> {
            problematicOrderService.createOrderMessageAfter(customerName, productName, amount, true);
        });
        
        // 验证订单已创建（事务已提交）
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.CONFIRMED, orders.get(0).getStatus());
        
        // 但消息发送失败，造成不一致
    }
    
    @Test
    @Transactional
    void testSendMessageInTransaction_Success() {
        // 测试事务内发送消息（正常情况）
        String customerName = "孙七";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        assertDoesNotThrow(() -> {
            problematicOrderService.createOrderMessageInTransaction(customerName, productName, amount, false);
        });
        
        // 验证订单已创建
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.CONFIRMED, orders.get(0).getStatus());
    }
    
    @Test
    @Transactional
    void testSendMessageInTransaction_MessageFails() {
        // 测试事务内发送消息（消息发送失败情况）
        String customerName = "周八";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        // 消息发送失败，整个事务回滚
        assertThrows(RuntimeException.class, () -> {
            problematicOrderService.createOrderMessageInTransaction(customerName, productName, amount, true);
        });
        
        // 验证订单未创建（事务回滚）
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(0, orders.size());
    }
    
    @Test
    @Transactional
    void testSendMessageInTransaction_DatabaseFails() {
        // 测试事务内发送消息（数据库操作失败情况）
        String customerName = "吴九";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        // 数据库操作失败，整个事务回滚
        assertThrows(RuntimeException.class, () -> {
            problematicOrderService.createOrderMessageInTransaction(customerName, productName, amount, true);
        });
        
        // 验证订单未创建（事务回滚）
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(0, orders.size());
        
        // 但消息可能已经发送，造成不一致
    }
}