package com.example.demo.service;

import com.example.demo.saga.SagaData;
import com.example.demo.saga.SagaOrchestrator;
import com.example.demo.saga.SagaStep;
import com.example.demo.saga.steps.CreateOrderStep;
import com.example.demo.saga.steps.SendMessageStep;
import com.example.demo.saga.steps.UpdateOrderStatusStep;
import com.example.demo.entity.SagaTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 使用Saga模式的订单服务
 * 演示分布式事务的一致性保证
 */
@Service
public class SagaOrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaOrderService.class);
    
    private final SagaOrchestrator sagaOrchestrator;
    private final CreateOrderStep createOrderStep;
    private final SendMessageStep sendMessageStep;
    private final UpdateOrderStatusStep updateOrderStatusStep;
    
    @Autowired
    public SagaOrderService(
            SagaOrchestrator sagaOrchestrator,
            CreateOrderStep createOrderStep,
            SendMessageStep sendMessageStep,
            UpdateOrderStatusStep updateOrderStatusStep) {
        this.sagaOrchestrator = sagaOrchestrator;
        this.createOrderStep = createOrderStep;
        this.sendMessageStep = sendMessageStep;
        this.updateOrderStatusStep = updateOrderStatusStep;
    }
    
    /**
     * 创建订单（正常流程）
     */
    public SagaTransaction createOrder(String customerName, String productName, BigDecimal amount) {
        logger.info("开始创建订单Saga: customer={}, product={}, amount={}", 
                customerName, productName, amount);
        
        // 准备Saga数据
        SagaData sagaData = new SagaData();
        sagaData.put("customerName", customerName);
        sagaData.put("productName", productName);
        sagaData.put("amount", amount.toString());
        
        // 定义Saga步骤
        List<SagaStep> steps = Arrays.asList(
                createOrderStep,
                sendMessageStep,
                updateOrderStatusStep
        );
        
        String businessId = "order_" + System.currentTimeMillis();
        
        return sagaOrchestrator.startSaga("CREATE_ORDER", businessId, sagaData, steps);
    }
    
    /**
     * 创建订单（消息发送失败场景）
     */
    public SagaTransaction createOrderWithMessageFailure(String customerName, String productName, BigDecimal amount) {
        logger.info("开始创建订单Saga（消息发送失败）: customer={}, product={}, amount={}", 
                customerName, productName, amount);
        
        // 准备Saga数据
        SagaData sagaData = new SagaData();
        sagaData.put("customerName", customerName);
        sagaData.put("productName", productName);
        sagaData.put("amount", amount.toString());
        sagaData.put("shouldFailMessage", true); // 模拟消息发送失败
        
        // 定义Saga步骤
        List<SagaStep> steps = Arrays.asList(
                createOrderStep,
                sendMessageStep,
                updateOrderStatusStep
        );
        
        String businessId = "order_msg_fail_" + System.currentTimeMillis();
        
        return sagaOrchestrator.startSaga("CREATE_ORDER_MSG_FAIL", businessId, sagaData, steps);
    }
    
    /**
     * 创建订单（状态更新失败场景）
     */
    public SagaTransaction createOrderWithStatusUpdateFailure(String customerName, String productName, BigDecimal amount) {
        logger.info("开始创建订单Saga（状态更新失败）: customer={}, product={}, amount={}", 
                customerName, productName, amount);
        
        // 准备Saga数据
        SagaData sagaData = new SagaData();
        sagaData.put("customerName", customerName);
        sagaData.put("productName", productName);
        sagaData.put("amount", amount.toString());
        sagaData.put("shouldFailUpdate", true); // 模拟状态更新失败
        
        // 定义Saga步骤
        List<SagaStep> steps = Arrays.asList(
                createOrderStep,
                sendMessageStep,
                updateOrderStatusStep
        );
        
        String businessId = "order_update_fail_" + System.currentTimeMillis();
        
        return sagaOrchestrator.startSaga("CREATE_ORDER_UPDATE_FAIL", businessId, sagaData, steps);
    }
    
    /**
     * 创建订单（统一接口）
     */
    public SagaTransaction createOrderWithSaga(String customerName, String productName, BigDecimal amount, boolean shouldFail) {
        if (shouldFail) {
            return createOrderWithMessageFailure(customerName, productName, amount);
        } else {
            return createOrder(customerName, productName, amount);
        }
    }
    
    /**
     * 恢复Saga事务
     */
    public SagaTransaction resumeSaga(String sagaId) {
        logger.info("恢复Saga事务: sagaId={}", sagaId);
        
        // 定义Saga步骤
        List<SagaStep> steps = Arrays.asList(
                createOrderStep,
                sendMessageStep,
                updateOrderStatusStep
        );
        
        return sagaOrchestrator.resumeSaga(sagaId, steps);
    }
}