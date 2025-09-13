package com.interview.usersession.controller;

import com.interview.usersession.model.UserInfo;
import com.interview.usersession.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息控制器
 * 
 * 演示UserUtil工具类的使用
 * 展示如何在业务代码中获取当前登录用户信息
 * 
 * @author Interview Lab
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    /**
     * 获取当前用户信息
     * 
     * 演示UserUtil.getCurrentUser()的使用
     * 
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        log.info("获取当前用户信息请求");
        
        try {
            // 使用UserUtil获取当前用户信息
            UserInfo currentUser = UserUtil.getCurrentUser();
            
            if (currentUser == null) {
                log.warn("当前用户信息为空");
                return ResponseEntity.status(401).body(Map.of("error", "用户未登录"));
            }
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("userId", currentUser.getUserId());
            response.put("username", currentUser.getUsername());
            response.put("displayName", currentUser.getDisplayName());
            response.put("email", currentUser.getEmail());
            response.put("department", currentUser.getDepartment());
            response.put("roles", currentUser.getRoles());
            response.put("permissions", currentUser.getPermissions());
            response.put("loginTime", currentUser.getLoginTime());
            response.put("lastAccessTime", currentUser.getLastAccessTime());
            
            log.info("返回当前用户信息: userId={}, username={}", 
                    currentUser.getUserId(), currentUser.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取当前用户信息异常", e);
            return ResponseEntity.status(500).body(Map.of("error", "系统异常"));
        }
    }
    
    /**
     * 获取当前用户ID
     * 
     * 演示UserUtil.getCurrentUserId()的使用
     * 
     * @return 当前用户ID
     */
    @GetMapping("/id")
    public ResponseEntity<Map<String, Object>> getCurrentUserId() {
        log.info("获取当前用户ID请求");
        
        try {
            String userId = UserUtil.getCurrentUserId();
            
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "用户未登录"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            
            log.info("返回当前用户ID: {}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取当前用户ID异常", e);
            return ResponseEntity.status(500).body(Map.of("error", "系统异常"));
        }
    }
    
    /**
     * 检查用户权限
     * 
     * 演示UserUtil.hasPermission()的使用
     * 
     * @return 权限检查结果
     */
    @GetMapping("/permissions")
    public ResponseEntity<Map<String, Object>> checkPermissions() {
        log.info("检查用户权限请求");
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // 检查各种权限
            response.put("canRead", UserUtil.hasPermission("READ"));
            response.put("canWrite", UserUtil.hasPermission("write"));
            response.put("canDelete", UserUtil.hasPermission("delete"));
            response.put("canAdmin", UserUtil.hasPermission("admin"));
            response.put("canManage", UserUtil.hasPermission("manage"));
            
            // 检查角色
            response.put("isAdmin", UserUtil.hasRole("admin"));
            response.put("isUser", UserUtil.hasRole("user"));
            response.put("isManager", UserUtil.hasRole("manager"));
            
            // 获取所有权限和角色
            response.put("allPermissions", UserUtil.getCurrentUserPermissions());
            response.put("allRoles", UserUtil.getCurrentUserRoles());
            
            String userId = UserUtil.getCurrentUserId();
            log.info("返回用户权限信息: userId={}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检查用户权限异常", e);
            return ResponseEntity.status(500).body(Map.of("error", "系统异常"));
        }
    }
    
    /**
     * 获取用户基本信息
     * 
     * 演示UserUtil各种便捷方法的使用
     * 
     * @return 用户基本信息
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        log.info("获取用户基本信息请求");
        
        try {
            // 检查是否已登录
            if (!UserUtil.isLoggedIn()) {
                return ResponseEntity.status(401).body(Map.of("error", "用户未登录"));
            }
            
            Map<String, Object> response = new HashMap<>();
            
            // 使用各种便捷方法
            response.put("userId", UserUtil.getCurrentUserId());
            response.put("username", UserUtil.getCurrentUsername());
            response.put("displayName", UserUtil.getCurrentUserDisplayName());
            response.put("email", UserUtil.getCurrentUserEmail());
            response.put("department", UserUtil.getCurrentUserDepartment());
            response.put("isLoggedIn", UserUtil.isLoggedIn());
            
            String userId = UserUtil.getCurrentUserId();
            log.info("返回用户基本信息: userId={}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户基本信息异常", e);
            return ResponseEntity.status(500).body(Map.of("error", "系统异常"));
        }
    }
}