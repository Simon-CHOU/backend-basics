package com.example.demo.controller;

import com.example.demo.entity.SagaTransaction;
import com.example.demo.service.SagaOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Saga模式订单控制器
 * 提供REST接口演示Saga分布式事务
 */
@RestController
@RequestMapping("/api/saga")
public class SagaOrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaOrderController.class);
    
    private final SagaOrderService sagaOrderService;
    
    @Autowired
    public SagaOrderController(SagaOrderService sagaOrderService) {
        this.sagaOrderService = sagaOrderService;
    }
    
    /**
     * 使用Saga模式创建订单（正常流程）
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam String customerName,
            @RequestParam String productName,
            @RequestParam BigDecimal amount) {
        
        logger.info("收到Saga创建订单请求: customer={}, product={}, amount={}", 
                customerName, productName, amount);
        
        try {
            SagaTransaction saga = sagaOrderService.createOrder(customerName, productName, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Saga订单创建流程已启动");
            response.put("sagaId", saga.getId());
            response.put("sagaType", saga.getSagaType());
            response.put("businessId", saga.getBusinessId());
            response.put("status", saga.getStatus());
            response.put("currentStep", saga.getCurrentStep());
            response.put("steps", saga.getSteps());
            
            logger.info("Saga订单创建响应: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Saga创建订单失败: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Saga订单创建失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 使用Saga模式创建订单（消息发送失败场景）
     */
    @PostMapping("/orders/message-failure")
    public ResponseEntity<Map<String, Object>> createOrderWithMessageFailure(
            @RequestParam String customerName,
            @RequestParam String productName,
            @RequestParam BigDecimal amount) {
        
        logger.info("收到Saga创建订单请求（消息失败）: customer={}, product={}, amount={}", 
                customerName, productName, amount);
        
        try {
            SagaTransaction saga = sagaOrderService.createOrderWithMessageFailure(customerName, productName, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Saga订单创建流程已启动（消息发送将失败）");
            response.put("sagaId", saga.getId());
            response.put("sagaType", saga.getSagaType());
            response.put("businessId", saga.getBusinessId());
            response.put("status", saga.getStatus());
            response.put("currentStep", saga.getCurrentStep());
            response.put("steps", saga.getSteps());
            response.put("errorMessage", saga.getErrorMessage());
            
            logger.info("Saga订单创建响应（消息失败）: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Saga创建订单失败（消息失败）: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Saga订单创建失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 使用Saga模式创建订单（状态更新失败场景）
     */
    @PostMapping("/orders/status-failure")
    public ResponseEntity<Map<String, Object>> createOrderWithStatusFailure(
            @RequestParam String customerName,
            @RequestParam String productName,
            @RequestParam BigDecimal amount) {
        
        logger.info("收到Saga创建订单请求（状态更新失败）: customer={}, product={}, amount={}", 
                customerName, productName, amount);
        
        try {
            SagaTransaction saga = sagaOrderService.createOrderWithStatusUpdateFailure(customerName, productName, amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Saga订单创建流程已启动（状态更新将失败）");
            response.put("sagaId", saga.getId());
            response.put("sagaType", saga.getSagaType());
            response.put("businessId", saga.getBusinessId());
            response.put("status", saga.getStatus());
            response.put("currentStep", saga.getCurrentStep());
            response.put("steps", saga.getSteps());
            response.put("errorMessage", saga.getErrorMessage());
            
            logger.info("Saga订单创建响应（状态更新失败）: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Saga创建订单失败（状态更新失败）: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Saga订单创建失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 恢复Saga事务
     */
    @PostMapping("/resume/{sagaId}")
    public ResponseEntity<Map<String, Object>> resumeSaga(@PathVariable String sagaId) {
        
        logger.info("收到恢复Saga事务请求: sagaId={}", sagaId);
        
        try {
            SagaTransaction saga = sagaOrderService.resumeSaga(sagaId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Saga事务恢复完成");
            response.put("sagaId", saga.getId());
            response.put("sagaType", saga.getSagaType());
            response.put("businessId", saga.getBusinessId());
            response.put("status", saga.getStatus());
            response.put("currentStep", saga.getCurrentStep());
            response.put("steps", saga.getSteps());
            response.put("errorMessage", saga.getErrorMessage());
            
            logger.info("Saga事务恢复响应: {}", response);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("恢复Saga事务失败: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "恢复Saga事务失败: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}