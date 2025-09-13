package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.service.OutboxOrderService;
import com.example.demo.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/outbox")
public class OutboxOrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OutboxOrderController.class);
    
    private final OutboxOrderService outboxOrderService;
    private final OutboxService outboxService;
    
    @Autowired
    public OutboxOrderController(OutboxOrderService outboxOrderService, OutboxService outboxService) {
        this.outboxOrderService = outboxOrderService;
        this.outboxService = outboxService;
    }
    
    /**
     * 使用Outbox模式创建订单
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam String customerName,
            @RequestParam String productName,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "false") boolean simulateFailure) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("开始执行Outbox模式创建订单");
            logger.info("参数: customerName={}, productName={}, amount={}, simulateFailure={}", 
                       customerName, productName, amount, simulateFailure);
            
            Order order = outboxOrderService.createOrderWithOutbox(customerName, productName, amount, simulateFailure);
            
            response.put("success", true);
            response.put("message", "订单创建成功，消息将异步发送");
            response.put("order", order);
            response.put("pattern", "Outbox模式");
            response.put("explanation", "订单和outbox事件在同一事务中保存，消息通过后台任务异步发送，确保最终一致性");
            
            logger.info("Outbox模式订单创建成功");
            logger.info("=".repeat(80) + "\n");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Outbox模式订单创建失败: {}", e.getMessage());
            logger.info("=".repeat(80) + "\n");
            
            response.put("success", false);
            response.put("message", "订单创建失败，事务已回滚");
            response.put("error", e.getMessage());
            response.put("pattern", "Outbox模式");
            response.put("explanation", "由于在同一事务中，订单和outbox事件都会回滚，保证了数据一致性");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/cancel-order/{orderId}")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String reason) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("开始执行Outbox模式取消订单");
            logger.info("参数: orderId={}, reason={}", orderId, reason);
            
            Order order = outboxOrderService.cancelOrderWithOutbox(orderId, reason);
            
            response.put("success", true);
            response.put("message", "订单取消成功，取消通知将异步发送");
            response.put("order", order);
            response.put("pattern", "Outbox模式");
            
            logger.info("Outbox模式订单取消成功");
            logger.info("=".repeat(80) + "\n");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Outbox模式订单取消失败: {}", e.getMessage());
            logger.info("=".repeat(80) + "\n");
            
            response.put("success", false);
            response.put("message", "订单取消失败");
            response.put("error", e.getMessage());
            response.put("pattern", "Outbox模式");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 批量创建订单
     */
    @PostMapping("/batch-create")
    public ResponseEntity<Map<String, Object>> batchCreateOrders(
            @RequestParam int count,
            @RequestParam String customerName,
            @RequestParam(defaultValue = "false") boolean simulateFailure) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("开始执行Outbox模式批量创建订单");
            logger.info("参数: count={}, customerName={}, simulateFailure={}", count, customerName, simulateFailure);
            
            outboxOrderService.createMultipleOrdersWithOutbox(count, customerName, simulateFailure);
            
            response.put("success", true);
            response.put("message", String.format("成功创建 %d 个订单", count));
            response.put("pattern", "Outbox模式");
            response.put("explanation", "所有订单和outbox事件在同一事务中创建，确保原子性");
            
            logger.info("Outbox模式批量订单创建成功");
            logger.info("=".repeat(80) + "\n");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Outbox模式批量订单创建失败: {}", e.getMessage());
            logger.info("=".repeat(80) + "\n");
            
            response.put("success", false);
            response.put("message", "批量创建订单失败，所有操作已回滚");
            response.put("error", e.getMessage());
            response.put("pattern", "Outbox模式");
            response.put("explanation", "事务回滚确保了数据一致性，没有部分成功的情况");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 手动触发事件处理
     */
    @PostMapping("/process-events")
    public ResponseEntity<Map<String, Object>> processEvents() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("手动触发outbox事件处理");
            outboxService.processEvents();
            
            response.put("success", true);
            response.put("message", "事件处理完成");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("手动处理事件失败: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", "事件处理失败");
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取事件统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Long> stats = outboxService.getEventStatistics();
            
            response.put("success", true);
            response.put("statistics", stats);
            response.put("message", "统计信息获取成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取统计信息失败: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", "获取统计信息失败");
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}