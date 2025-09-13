package com.interview.usersession.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * 提供系统健康状态检查接口
 * 不需要用户认证，用于监控系统状态
 * 
 * @author Interview Lab
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 基本健康检查
     * 
     * @return 系统状态
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("status", "UP");
            response.put("timestamp", LocalDateTime.now());
            response.put("service", "user-session-service");
            response.put("version", "1.0.0");
            
            // 检查Redis连接
            boolean redisHealthy = checkRedisHealth();
            response.put("redis", redisHealthy ? "UP" : "DOWN");
            
            // 如果Redis不健康，整体状态为DOWN
            if (!redisHealthy) {
                response.put("status", "DOWN");
            }
            
            log.debug("健康检查完成: status={}, redis={}", 
                    response.get("status"), response.get("redis"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("健康检查异常", e);
            
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 详细健康检查
     * 
     * @return 详细系统状态
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("timestamp", LocalDateTime.now());
            response.put("service", "user-session-service");
            response.put("version", "1.0.0");
            
            // JVM信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> jvm = new HashMap<>();
            jvm.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
            jvm.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
            jvm.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
            jvm.put("usedMemory", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
            jvm.put("processors", runtime.availableProcessors());
            response.put("jvm", jvm);
            
            // Redis健康检查
            Map<String, Object> redis = new HashMap<>();
            boolean redisHealthy = checkRedisHealth();
            redis.put("status", redisHealthy ? "UP" : "DOWN");
            
            if (redisHealthy && redisTemplate != null) {
                try {
                    // 测试Redis读写
                    String testKey = "health:test:" + System.currentTimeMillis();
                    redisTemplate.opsForValue().set(testKey, "test", 10, java.util.concurrent.TimeUnit.SECONDS);
                    String testValue = (String) redisTemplate.opsForValue().get(testKey);
                    redisTemplate.delete(testKey);
                    
                    redis.put("readWrite", "test".equals(testValue) ? "OK" : "FAILED");
                } catch (Exception e) {
                    redis.put("readWrite", "FAILED");
                    redis.put("error", e.getMessage());
                }
            }
            response.put("redis", redis);
            
            // 整体状态
            boolean overallHealthy = redisHealthy;
            response.put("status", overallHealthy ? "UP" : "DOWN");
            
            log.info("详细健康检查完成: status={}", response.get("status"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("详细健康检查异常", e);
            
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 检查Redis健康状态
     * 
     * @return Redis是否健康
     */
    private boolean checkRedisHealth() {
        if (redisTemplate == null) {
            log.warn("RedisTemplate未配置");
            return false;
        }
        
        try {
            // 执行ping命令
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis健康检查失败", e);
            return false;
        }
    }
}