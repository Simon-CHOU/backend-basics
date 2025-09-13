package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.service.ProblematicOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/problematic")
public class ProblematicOrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProblematicOrderController.class);
    
    private final ProblematicOrderService orderService;
    
    @Autowired
    public ProblematicOrderController(ProblematicOrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * 演示场景1: 消息发送在事务之前的问题
     */
    @PostMapping("/scenario1")
    public ResponseEntity<Map<String, Object>> scenario1(
            @RequestParam String customerName,
            @RequestParam String productName,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "false") boolean simulateDbFailure) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("开始执行场景1: 消息发送在事务之前");
            logger.info("参数: customerName={}, productName={}, amount={}, simulateDbFailure={}", 
                       customerName, productName, amount, simulateDbFailure);
            
            Order order = orderService.createOrderMessageFirst(customerName, productName, amount, simulateDbFailure);
            
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("order", order);
            response.put("scenario", "消息发送在事务之前");
            
            logger.info("场景1执行成功");
            logger.info("=".repeat(80) + "\n");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("场景1执行失败: {}", e.getMessage());
            logger.info("=".repeat(80) + "\n");
            
            response.put("success", false);
            response.put("message", "订单创建失败，但消息可能已经发送！");
            response.put("error", e.getMessage());
            response.put("scenario", "消息发送在事务之前");
            response.put("problem", "消息已发送但数据库事务回滚，导致数据不一致");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 演示场景2: 消息发送在事务之后的问题
     */
    @PostMapping("/scenario2")
    public ResponseEntity<Map<String, Object>> scenario2(
            @RequestParam String customerName,
            @RequestParam String productName,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "false") boolean simulateMessageFailure) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("开始执行场景2: 消息发送在事务之后");
            logger.info("参数: customerName={}, productName={}, amount={}, simulateMessageFailure={}", 
                       customerName, productName, amount, simulateMessageFailure);
            
            Order order = orderService.createOrderMessageAfter(customerName, productName, amount, simulateMessageFailure);
            
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("order", order);
            response.put("scenario", "消息发送在事务之后");
            
            logger.info("场景2执行成功");
            logger.info("=".repeat(80) + "\n");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("场景2执行失败: {}", e.getMessage());
            logger.info("=".repeat(80) + "\n");
            
            response.put("success", false);
            response.put("message", "消息发送失败，但数据库已提交！");
            response.put("error", e.getMessage());
            response.put("scenario", "消息发送在事务之后");
            response.put("problem", "数据库已提交但消息发送失败，导致数据不一致");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 演示场景3: 消息发送在事务内部的性能问题
     */
    @PostMapping("/scenario3")
    public ResponseEntity<Map<String, Object>> scenario3(
            @RequestParam String customerName,
            @RequestParam String productName,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "false") boolean simulateSlowMessage) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("\n" + "=".repeat(80));
            logger.info("开始执行场景3: 消息发送在事务内部");
            logger.info("参数: customerName={}, productName={}, amount={}, simulateSlowMessage={}", 
                       customerName, productName, amount, simulateSlowMessage);
            
            long startTime = System.currentTimeMillis();
            Order order = orderService.createOrderMessageInTransaction(customerName, productName, amount, simulateSlowMessage);
            long endTime = System.currentTimeMillis();
            
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("order", order);
            response.put("scenario", "消息发送在事务内部");
            response.put("executionTime", endTime - startTime);
            response.put("problem", simulateSlowMessage ? "事务持续时间过长，影响系统吞吐量" : "正常执行");
            
            logger.info("场景3执行成功，总耗时: {}ms", endTime - startTime);
            logger.info("=".repeat(80) + "\n");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("场景3执行失败: {}", e.getMessage());
            logger.info("=".repeat(80) + "\n");
            
            response.put("success", false);
            response.put("message", "订单创建失败");
            response.put("error", e.getMessage());
            response.put("scenario", "消息发送在事务内部");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}