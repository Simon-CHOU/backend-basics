package com.interview.usersession.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息实体类
 * 
 * 存储在Redis中的用户会话信息，包含用户基本信息、角色权限等
 * 通过ThreadLocal在整个请求生命周期中传递
 * 
 * @author Interview Lab
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户显示名称
     */
    private String displayName;
    
    /**
     * 用户角色列表
     */
    private List<String> roles;
    
    /**
     * 用户权限列表
     */
    private List<String> permissions;
    
    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    
    /**
     * 最后访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;
    
    /**
     * 会话token
     */
    private String sessionToken;
    
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 部门信息
     */
    private String department;
    
    /**
     * 检查用户是否具有指定角色
     * 
     * @param role 角色名称
     * @return 是否具有该角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * 检查用户是否具有指定权限
     * 
     * @param permission 权限名称
     * @return 是否具有该权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * 检查用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * 更新最后访问时间
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }
}