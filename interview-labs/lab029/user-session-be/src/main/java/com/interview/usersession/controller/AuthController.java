package com.interview.usersession.controller;

import com.interview.usersession.dto.LoginRequest;
import com.interview.usersession.dto.LoginResponse;
import com.interview.usersession.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * 提供用户登录、注销等认证相关的REST API
 * 
 * @author Interview Lab
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @param response HTTP响应对象，用于设置Cookie
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        
        log.info("收到登录请求: username={}", loginRequest.getUsername());
        
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            
            if (loginResponse.isSuccess()) {
                // 设置Cookie
                Cookie tokenCookie = new Cookie("token", loginResponse.getToken());
                tokenCookie.setHttpOnly(true); // 防止XSS攻击
                tokenCookie.setPath("/"); // Cookie作用域
                tokenCookie.setMaxAge(24 * 60 * 60); // 24小时过期
                // tokenCookie.setSecure(true); // HTTPS环境下启用
                response.addCookie(tokenCookie);
                
                log.info("用户登录成功: username={}, token={}", 
                        loginRequest.getUsername(), loginResponse.getToken());
            }
            
            return ResponseEntity.ok(loginResponse);
            
        } catch (Exception e) {
            log.error("登录处理异常: username={}", loginRequest.getUsername(), e);
            return ResponseEntity.ok(LoginResponse.failure("系统异常，请稍后重试"));
        }
    }
    
    /**
     * 用户注销
     * 
     * @param token 会话token（从Cookie或Header获取）
     * @param response HTTP响应对象，用于清除Cookie
     * @return 注销结果
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(value = "token", required = false) String cookieToken,
            @RequestHeader(value = "token", required = false) String headerToken,
            HttpServletResponse response) {
        
        // 优先使用Header中的token，其次使用Cookie中的token
        String token = headerToken != null ? headerToken : cookieToken;
        
        log.info("收到注销请求: token={}", token);
        
        try {
            boolean success = authService.logout(token);
            
            if (success) {
                // 清除Cookie
                Cookie tokenCookie = new Cookie("token", "");
                tokenCookie.setHttpOnly(true);
                tokenCookie.setPath("/");
                tokenCookie.setMaxAge(0); // 立即过期
                response.addCookie(tokenCookie);
                
                log.info("用户注销成功: token={}", token);
                return ResponseEntity.ok("注销成功");
            } else {
                log.warn("用户注销失败: token={}", token);
                return ResponseEntity.ok("注销失败，会话可能已过期");
            }
            
        } catch (Exception e) {
            log.error("注销处理异常: token={}", token, e);
            return ResponseEntity.ok("系统异常，请稍后重试");
        }
    }
    
    /**
     * 验证token有效性
     * 
     * @param token 会话token
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(
            @CookieValue(value = "token", required = false) String cookieToken,
            @RequestHeader(value = "token", required = false) String headerToken) {
        
        String token = headerToken != null ? headerToken : cookieToken;
        
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(401).body("缺少token");
        }
        
        try {
            boolean isValid = authService.validateToken(token) != null;
            
            if (isValid) {
                return ResponseEntity.ok("token有效");
            } else {
                return ResponseEntity.status(401).body("token无效或已过期");
            }
            
        } catch (Exception e) {
            log.error("token验证异常: token={}", token, e);
            return ResponseEntity.status(500).body("系统异常");
        }
    }
}