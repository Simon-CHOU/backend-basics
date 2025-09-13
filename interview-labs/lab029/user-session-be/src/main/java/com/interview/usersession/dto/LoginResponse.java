package com.interview.usersession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 
 * @author Interview Lab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * 是否登录成功
     */
    private boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 会话token
     */
    private String token;
    
    /**
     * 用户基本信息
     */
    private UserBasicInfo userInfo;
    
    /**
     * 用户基本信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBasicInfo {
        private String userId;
        private String username;
        private String displayName;
        private String email;
    }
    
    /**
     * 创建成功响应
     */
    public static LoginResponse success(String token, UserBasicInfo userInfo) {
        return LoginResponse.builder()
                .success(true)
                .message("登录成功")
                .token(token)
                .userInfo(userInfo)
                .build();
    }
    
    /**
     * 创建失败响应
     */
    public static LoginResponse failure(String message) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}