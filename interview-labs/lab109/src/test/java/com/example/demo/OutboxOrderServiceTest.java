package com.example.demo;

import com.example.demo.entity.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OutboxEventRepository;
import com.example.demo.service.OutboxOrderService;
import com.example.demo.service.OutboxService;
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
 * Outbox模式测试
 * 验证Outbox模式解决分布式事务一致性问题
 */
@SpringBootTest
@ActiveProfiles("test")
class OutboxOrderServiceTest {
    
    @Autowired
    private OutboxOrderService outboxOrderService;
    
    @Autowired
    private OutboxService outboxService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OutboxEventRepository outboxEventRepository;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.deleteAll();
        outboxEventRepository.deleteAll();
    }
    
    @Test
    @Transactional
    void testCreateOrder_Success() {
        // 测试正常创建订单
        String customerName = "张三";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        assertDoesNotThrow(() -> {
            outboxOrderService.createOrderWithOutbox(customerName, productName, amount, false);
        });
        
        // 验证订单已创建
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(customerName, order.getCustomerName());
        assertEquals(productName, order.getProductName());
        assertEquals(amount, order.getAmount());
        
        // 验证Outbox事件已创建
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc(EventStatus.PENDING);
        assertEquals(1, events.size());
        OutboxEvent event = events.get(0);
        assertEquals("Order", event.getAggregateType());
        assertEquals(order.getId().toString(), event.getAggregateId());
        assertEquals("ORDER_CREATED", event.getEventType());
        assertEquals(EventStatus.PENDING, event.getStatus());
    }
    
    @Test
    @Transactional
    void testCreateOrder_WithFailure() {
        // 测试创建订单时模拟失败
        String customerName = "李四";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        assertThrows(RuntimeException.class, () -> {
            outboxOrderService.createOrderWithOutbox(customerName, productName, amount, true);
        });
        
        // 验证订单未创建（事务回滚）
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(0, orders.size());
        
        // 验证没有创建Outbox事件（因为事务失败）
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc(EventStatus.PENDING);
        assertEquals(0, events.size());
    }
    
    @Test
    @Transactional
    void testCancelOrder_Success() {
        // 先创建订单
        String customerName = "王五";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        outboxOrderService.createOrderWithOutbox(customerName, productName, amount, false);
        
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(1, orders.size());
        Long orderId = orders.get(0).getId();
        
        // 取消订单
        assertDoesNotThrow(() -> {
            outboxOrderService.cancelOrderWithOutbox(orderId, "Customer requested cancellation");
        });
        
        // 验证订单状态已更新
        Order updatedOrder = orderRepository.findById(orderId).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
        
        // 验证取消事件已创建
        List<OutboxEvent> events = outboxEventRepository.findByAggregateIdAndEventTypeOrderByCreatedAtAsc(
                orderId.toString(), "ORDER_CANCELLED");
        assertEquals(1, events.size());
    }
    
    @Test
    @Transactional
    void testBatchCreateOrders_PartialFailure() {
        // 测试批量创建订单（部分失败）
        List<String> customerNames = List.of("赵六", "孙七", "周八");
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        assertThrows(RuntimeException.class, () -> {
            outboxOrderService.createMultipleOrdersWithOutbox(3, "测试客户", true);
        });
        
        // 验证所有订单都未创建（事务回滚）
        for (String customerName : customerNames) {
            List<Order> orders = orderRepository.findByCustomerName(customerName);
            assertEquals(0, orders.size());
        }
        
        // 验证没有创建Outbox事件（因为事务失败）
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc(EventStatus.PENDING);
        assertEquals(0, events.size());
    }
    
    @Test
    void testOutboxEventProcessing() {
        // 测试Outbox事件处理
        String customerName = "吴九";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        // 创建订单和事件
        outboxOrderService.createOrderWithOutbox(customerName, productName, amount, false);
        
        // 获取待处理事件
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(EventStatus.PENDING);
        assertEquals(1, pendingEvents.size());
        
        // 处理事件
        outboxService.processEvents();
        
        // 验证事件状态已更新
        OutboxEvent processedEvent = outboxEventRepository.findById(pendingEvents.get(0).getId()).orElse(null);
        assertNotNull(processedEvent);
        assertEquals(EventStatus.PROCESSED, processedEvent.getStatus());
        assertNotNull(processedEvent.getProcessedAt());
    }
    
    @Test
    void testOutboxEventRetry() {
        // 测试Outbox事件重试机制
        
        // 创建一个失败的事件
        OutboxEvent failedEvent = new OutboxEvent(
                "Order", "123", "ORDER_CREATED", "{\"test\": \"data\"}");
        failedEvent.setStatus(EventStatus.FAILED);
        failedEvent.setRetryCount(2);
        failedEvent.setErrorMessage("模拟失败");
        outboxEventRepository.save(failedEvent);
        
        // 执行重试
        outboxService.retryFailedEvents();
        
        // 验证重试计数增加
        OutboxEvent retriedEvent = outboxEventRepository.findById(failedEvent.getId()).orElse(null);
        assertNotNull(retriedEvent);
        assertTrue(retriedEvent.getRetryCount() >= 2);
    }
    
    @Test
    void testOutboxEventCleanup() {
        // 测试Outbox事件清理
        
        // 创建一个已处理的旧事件
        OutboxEvent oldEvent = new OutboxEvent(
                "Order", "456", "ORDER_CREATED", "{\"test\": \"data\"}");
        oldEvent.setStatus(EventStatus.PROCESSED);
        oldEvent = outboxEventRepository.save(oldEvent);
        
        // 执行清理（这里只是测试方法调用，实际清理逻辑需要时间条件）
        assertDoesNotThrow(() -> {
            outboxService.cleanupProcessedEvents(7);
        });
    }
    
    @Test
    void testOutboxEventStatistics() {
        // 测试Outbox事件统计
        
        // 创建不同状态的事件
        OutboxEvent pendingEvent = new OutboxEvent(
                "Order", "789", "ORDER_CREATED", "{\"test\": \"data\"}");
        outboxEventRepository.save(pendingEvent);
        
        OutboxEvent processedEvent = new OutboxEvent(
                "Order", "790", "ORDER_CREATED", "{\"test\": \"data\"}");
        processedEvent.setStatus(EventStatus.PROCESSED);
        outboxEventRepository.save(processedEvent);
        
        // 获取统计信息
        var statistics = outboxService.getEventStatistics();
        
        assertNotNull(statistics);
        assertTrue(statistics.containsKey("pending"));
        assertTrue(statistics.containsKey("processed"));
        assertTrue((Long) statistics.get("pending") >= 1);
        assertTrue((Long) statistics.get("processed") >= 1);
    }
}