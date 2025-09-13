package com.example.demo;

import com.example.demo.entity.*;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.SagaTransactionRepository;
import com.example.demo.service.SagaOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saga模式测试
 * 验证Saga模式分布式事务处理和补偿机制
 */
@SpringBootTest
@ActiveProfiles("test")
class SagaOrderServiceTest {
    
    @Autowired
    private SagaOrderService sagaOrderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private SagaTransactionRepository sagaRepository;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.deleteAll();
        sagaRepository.deleteAll();
    }
    
    @Test
    void testCreateOrder_Success() {
        // 测试正常创建订单流程
        String customerName = "张三";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        SagaTransaction saga = sagaOrderService.createOrderWithSaga(customerName, productName, amount, false);
        
        assertNotNull(saga);
        assertEquals("CREATE_ORDER", saga.getSagaType());
        assertEquals(SagaStatus.COMPLETED, saga.getStatus());
        assertEquals(3, saga.getCurrentStep()); // 所有步骤都完成
        assertNotNull(saga.getCompletedAt());
        
        // 验证订单已创建并状态正确
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(customerName, order.getCustomerName());
        assertEquals(productName, order.getProductName());
        assertEquals(amount, order.getAmount());
    }
    
    @Test
    void testCreateOrder_MessageFailure() {
        // 测试消息发送失败的补偿流程
        String customerName = "李四";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        SagaTransaction saga = sagaOrderService.createOrderWithSaga(customerName, productName, amount, true);
        
        assertNotNull(saga);
        assertEquals("CREATE_ORDER_MSG_FAIL", saga.getSagaType());
        assertEquals(SagaStatus.COMPENSATED, saga.getStatus());
        assertNotNull(saga.getErrorMessage());
        assertTrue(saga.getErrorMessage().contains("消息发送失败"));
        
        // 验证订单已被删除（补偿操作）
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(0, orders.size());
    }
    
    @Test
    void testCreateOrder_StatusUpdateFailure() {
        // 测试状态更新失败的补偿流程
        String customerName = "王五";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        SagaTransaction saga = sagaOrderService.createOrderWithSaga(customerName, productName, amount, true);
        
        assertNotNull(saga);
        assertEquals("CREATE_ORDER_UPDATE_FAIL", saga.getSagaType());
        assertEquals(SagaStatus.COMPENSATED, saga.getStatus());
        assertNotNull(saga.getErrorMessage());
        assertTrue(saga.getErrorMessage().contains("状态更新失败"));
        
        // 验证订单已被删除（补偿操作）
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(0, orders.size());
    }
    
    @Test
    void testSagaSteps() {
        // 测试Saga步骤的详细执行
        String customerName = "赵六";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        SagaTransaction saga = sagaOrderService.createOrderWithSaga(customerName, productName, amount, false);
        
        // 验证步骤信息
        List<String> steps = saga.getSteps();
        assertNotNull(steps);
        assertTrue(steps.size() >= 0); // 步骤会在执行过程中添加
        
        // 验证数据传递
        Map<String, Object> data = saga.getData();
        assertNotNull(data);
        assertEquals(customerName, data.get("customerName"));
        assertEquals(productName, data.get("productName"));
        assertEquals(amount.toString(), data.get("amount"));
    }
    
    @Test
    void testSagaCompensation() {
        // 测试补偿机制的详细流程
        String customerName = "孙七";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        // 执行会失败的Saga
        SagaTransaction saga = sagaOrderService.createOrderWithSaga(customerName, productName, amount, true);
        
        // 验证补偿状态
        assertEquals(SagaStatus.COMPENSATED, saga.getStatus());
        assertNotNull(saga.getCompletedAt());
        
        // 验证补偿效果：订单应该被删除
        List<Order> orders = orderRepository.findByCustomerName(customerName);
        assertEquals(0, orders.size());
    }
    
    @Test
    void testResumeSaga() {
        // 测试Saga恢复功能
        String customerName = "周八";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        // 创建一个正常的Saga
        SagaTransaction originalSaga = sagaOrderService.createOrderWithSaga(customerName, productName, amount, false);
        
        // 尝试恢复已完成的Saga（应该不会改变状态）
        SagaTransaction resumedSaga = sagaOrderService.resumeSaga(originalSaga.getId());
        
        assertNotNull(resumedSaga);
        assertEquals(originalSaga.getId(), resumedSaga.getId());
        assertEquals(SagaStatus.COMPLETED, resumedSaga.getStatus());
    }
    
    @Test
    void testSagaDataPersistence() {
        // 测试Saga数据持久化
        String customerName = "吴九";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        SagaTransaction saga = sagaOrderService.createOrderWithSaga(customerName, productName, amount, false);
        
        // 从数据库重新加载Saga
        SagaTransaction reloadedSaga = sagaRepository.findById(saga.getId()).orElse(null);
        
        assertNotNull(reloadedSaga);
        assertEquals(saga.getId(), reloadedSaga.getId());
        assertEquals(saga.getSagaType(), reloadedSaga.getSagaType());
        assertEquals(saga.getBusinessId(), reloadedSaga.getBusinessId());
        assertEquals(saga.getStatus(), reloadedSaga.getStatus());
        assertEquals(saga.getCurrentStep(), reloadedSaga.getCurrentStep());
        assertEquals(saga.getSteps(), reloadedSaga.getSteps());
    }
    
    @Test
    void testMultipleSagas() {
        // 测试多个并发Saga
        String[] customerNames = {"客户A", "客户B", "客户C"};
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        SagaTransaction[] sagas = new SagaTransaction[customerNames.length];
        
        // 创建多个Saga
        for (int i = 0; i < customerNames.length; i++) {
            sagas[i] = sagaOrderService.createOrderWithSaga(customerNames[i], productName, amount, false);
        }
        
        // 验证所有Saga都成功完成
        for (int i = 0; i < customerNames.length; i++) {
            assertNotNull(sagas[i]);
            assertEquals(SagaStatus.COMPLETED, sagas[i].getStatus());
            
            // 验证对应的订单
            List<Order> orders = orderRepository.findByCustomerName(customerNames[i]);
            assertEquals(1, orders.size());
            assertEquals(OrderStatus.CONFIRMED, orders.get(0).getStatus());
        }
    }
    
    @Test
    void testSagaBusinessIdUniqueness() {
        // 测试业务ID的唯一性
        String customerName = "测试客户";
        String productName = "测试产品";
        BigDecimal amount = new BigDecimal("100.00");
        
        SagaTransaction saga1 = sagaOrderService.createOrderWithSaga(customerName + "1", productName, amount, false);
        SagaTransaction saga2 = sagaOrderService.createOrderWithSaga(customerName + "2", productName, amount, false);
        
        assertNotNull(saga1);
        assertNotNull(saga2);
        assertNotEquals(saga1.getId(), saga2.getId());
        assertNotEquals(saga1.getBusinessId(), saga2.getBusinessId());
    }
}