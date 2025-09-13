package com.interview.usersession.service;

import com.interview.usersession.dto.LoginRequest;
import com.interview.usersession.dto.LoginResponse;
import com.interview.usersession.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户认证服务
 * 
 * 处理用户登录、注销等认证相关操作
 * 为了演示目的，使用内存中的模拟用户数据
 * 
 * @author Interview Lab
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final SessionService sessionService;
    
    /**
     * 模拟用户数据库
     * 实际项目中应该从数据库查询
     */
    private static final Map<String, MockUser> MOCK_USERS = new HashMap<>();
    
    static {
        // 初始化模拟用户数据
        MOCK_USERS.put("admin", new MockUser(
                "1", "admin", "password123", "系统管理员", 
                "admin@example.com", "技术部",
                Arrays.asList("ADMIN", "USER"),
                Arrays.asList("READ", "WRITE", "DELETE", "ADMIN")
        ));
        
        MOCK_USERS.put("john", new MockUser(
                "2", "john", "password456", "约翰·史密斯",
                "john@example.com", "业务部",
                Arrays.asList("USER"),
                Arrays.asList("READ", "WRITE")
        ));
        
        MOCK_USERS.put("alice", new MockUser(
                "3", "alice", "password789", "爱丽丝·王",
                "alice@example.com", "产品部",
                Arrays.asList("USER", "MANAGER"),
                Arrays.asList("READ", "WRITE", "MANAGE")
        ));
    }
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        log.info("用户尝试登录: username={}", username);
        
        // 验证用户凭据
        MockUser mockUser = MOCK_USERS.get(username);
        if (mockUser == null || !mockUser.getPassword().equals(password)) {
            log.warn("登录失败，用户名或密码错误: username={}", username);
            return LoginResponse.failure("用户名或密码错误");
        }
        
        // 构建用户信息
        UserInfo userInfo = UserInfo.builder()
                .userId(mockUser.getUserId())
                .username(mockUser.getUsername())
                .displayName(mockUser.getDisplayName())
                .email(mockUser.getEmail())
                .department(mockUser.getDepartment())
                .roles(mockUser.getRoles())
                .permissions(mockUser.getPermissions())
                .loginTime(LocalDateTime.now())
                .lastAccessTime(LocalDateTime.now())
                .build();
        
        // 创建会话
        String token = sessionService.createSession(userInfo);
        
        // 构建响应
        LoginResponse.UserBasicInfo basicInfo = LoginResponse.UserBasicInfo.builder()
                .userId(userInfo.getUserId())
                .username(userInfo.getUsername())
                .displayName(userInfo.getDisplayName())
                .email(userInfo.getEmail())
                .build();
        
        log.info("用户登录成功: userId={}, username={}, token={}", 
                userInfo.getUserId(), username, token);
        
        return LoginResponse.success(token, basicInfo);
    }
    
    /**
     * 用户注销
     * 
     * @param token 会话token
     * @return 是否注销成功
     */
    public boolean logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("注销失败，token为空");
            return false;
        }
        
        // 获取用户信息用于日志
        UserInfo userInfo = sessionService.getUserInfo(token);
        
        // 删除会话
        boolean success = sessionService.deleteSession(token);
        
        if (success && userInfo != null) {
            log.info("用户注销成功: userId={}, username={}", 
                    userInfo.getUserId(), userInfo.getUsername());
        }
        
        return success;
    }
    
    /**
     * 验证token有效性
     * 
     * @param token 会话token
     * @return 用户信息，如果token无效则返回null
     */
    public UserInfo validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        
        return sessionService.getUserInfo(token);
    }
    
    /**
     * 刷新会话（延长过期时间）
     * 
     * @param token 会话token
     * @return 是否刷新成功
     */
    public boolean refreshSession(String token) {
        return sessionService.extendSession(token);
    }
    
    /**
     * 模拟用户数据类
     */
    private static class MockUser {
        private final String userId;
        private final String username;
        private final String password;
        private final String displayName;
        private final String email;
        private final String department;
        private final List<String> roles;
        private final List<String> permissions;
        
        public MockUser(String userId, String username, String password, String displayName,
                       String email, String department, List<String> roles, List<String> permissions) {
            this.userId = userId;
            this.username = username;
            this.password = password;
            this.displayName = displayName;
            this.email = email;
            this.department = department;
            this.roles = roles;
            this.permissions = permissions;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
        public String getDepartment() { return department; }
        public List<String> getRoles() { return roles; }
        public List<String> getPermissions() { return permissions; }
    }
}