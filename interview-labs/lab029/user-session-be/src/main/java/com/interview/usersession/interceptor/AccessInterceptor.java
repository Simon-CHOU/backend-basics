package com.interview.usersession.interceptor;

import com.interview.usersession.model.UserInfo;
import com.interview.usersession.service.SessionService;
import com.interview.usersession.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 访问拦截器
 * 
 * 核心功能：
 * 1. 从请求头Cookie中提取token
 * 2. 从Redis获取用户会话信息
 * 3. 将用户信息存储到ThreadLocal中
 * 4. 请求结束后清理ThreadLocal
 * 
 * 这是实现全局用户信息获取的关键组件
 * 
 * @author Interview Lab
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessInterceptor implements HandlerInterceptor {
    
    private final SessionService sessionService;
    
    /**
     * Token的Header名称
     */
    private static final String TOKEN_HEADER = "Cookie";
    private static final String TOKEN_PREFIX = "token=";
    
    /**
     * 请求前置处理
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @return 是否继续处理请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("拦截器处理请求: {} {}", method, requestURI);
        
        // 跳过登录接口和健康检查接口
        if (isExcludedPath(requestURI)) {
            log.debug("跳过拦截器处理: {}", requestURI);
            return true;
        }
        
        // 从请求头获取token
        String token = extractTokenFromRequest(request);
        if (token == null || token.trim().isEmpty()) {
            log.warn("请求缺少token: {} {}", method, requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // 从Redis获取用户信息
        UserInfo userInfo = sessionService.getUserInfo(token);
        if (userInfo == null) {
            log.warn("无效的token或会话已过期: token={}, uri={}", token, requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // 更新最后访问时间
        userInfo.updateLastAccessTime();
        
        // 存储到ThreadLocal
        UserUtil.setCurrentUser(userInfo);
        
        // 延长会话过期时间
        sessionService.extendSession(token);
        
        log.debug("用户认证成功: userId={}, username={}, uri={}", 
                userInfo.getUserId(), userInfo.getUsername(), requestURI);
        
        return true;
    }
    
    /**
     * 请求完成后处理
     * 
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器
     * @param ex 异常信息
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                              Object handler, Exception ex) {
        // 清理ThreadLocal，防止内存泄漏
        UserUtil.clearContext();
        
        if (ex != null) {
            log.error("请求处理异常: {} {}", request.getMethod(), request.getRequestURI(), ex);
        }
        
        log.debug("拦截器完成处理: {} {}", request.getMethod(), request.getRequestURI());
    }
    
    /**
     * 从请求中提取token
     * 
     * 支持多种方式：
     * 1. Cookie: token=xxx
     * 2. Header: Authorization: Bearer xxx
     * 3. Header: token: xxx
     * 
     * @param request 请求对象
     * @return token值，如果未找到则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 方式1：从Cookie中获取
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            String token = extractTokenFromCookie(cookieHeader);
            if (token != null) {
                return token;
            }
        }
        
        // 方式2：从Authorization Header获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 方式3：从token Header获取
        String tokenHeader = request.getHeader("token");
        if (tokenHeader != null && !tokenHeader.trim().isEmpty()) {
            return tokenHeader.trim();
        }
        
        return null;
    }
    
    /**
     * 从Cookie字符串中提取token
     * 
     * @param cookieHeader Cookie头字符串
     * @return token值
     */
    private String extractTokenFromCookie(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }
        
        // Cookie格式: "token=uuid-value; other=value"
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            cookie = cookie.trim();
            if (cookie.startsWith(TOKEN_PREFIX)) {
                return cookie.substring(TOKEN_PREFIX.length());
            }
        }
        
        return null;
    }
    
    /**
     * 判断是否为排除路径
     * 
     * @param requestURI 请求URI
     * @return 是否排除
     */
    private boolean isExcludedPath(String requestURI) {
        // 排除的路径列表
        String[] excludedPaths = {
                "/api/auth/login",
                "/api/auth/logout", 
                "/api/health",
                "/actuator",
                "/swagger",
                "/v3/api-docs",
                "/favicon.ico"
        };
        
        for (String excludedPath : excludedPaths) {
            if (requestURI.startsWith(excludedPath)) {
                return true;
            }
        }
        
        return false;
    }
}