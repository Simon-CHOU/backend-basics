package com.interview.usersession.util;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.interview.usersession.model.UserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 用户工具类 - 全局用户信息获取的核心实现
 * 
 * 这是整个系统的核心组件，提供了在任何业务代码中获取当前登录用户信息的能力。
 * 使用TransmittableThreadLocal确保在异步场景下也能正确传递用户上下文。
 * 
 * 设计要点：
 * 1. 使用TransmittableThreadLocal而非ThreadLocal，支持异步线程传递
 * 2. 提供便捷的静态方法，简化业务代码调用
 * 3. 包含完善的异常处理和日志记录
 * 4. 支持用户信息的生命周期管理
 * 
 * @author Interview Lab
 */
@Slf4j
public class UserUtil {
    
    /**
     * 使用TransmittableThreadLocal存储用户信息
     * 相比ThreadLocal，TTL支持异步线程间的上下文传递
     */
    private static final TransmittableThreadLocal<UserInfo> USER_CONTEXT = new TransmittableThreadLocal<>();
    
    /**
     * 设置当前线程的用户信息
     * 
     * @param userInfo 用户信息对象
     */
    public static void setUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            // 更新最后访问时间
            userInfo.updateLastAccessTime();
            USER_CONTEXT.set(userInfo);
            log.debug("设置用户上下文: userId={}, username={}", 
                     userInfo.getUserId(), userInfo.getUsername());
        } else {
            log.warn("尝试设置空的用户信息到上下文中");
        }
    }
    
    /**
     * 获取当前线程的用户信息
     * 
     * 这是最核心的方法，业务代码通过此方法获取用户信息
     * 
     * @return 当前用户信息，如果未登录则返回null
     */
    public static UserInfo getUserInfo() {
        UserInfo userInfo = USER_CONTEXT.get();
        if (userInfo == null) {
            log.debug("当前线程中没有用户上下文信息");
        }
        return userInfo;
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID，如果未登录则返回null
     */
    public static String getUserId() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名，如果未登录则返回null
     */
    public static String getUsername() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getUsername() : null;
    }
    
    /**
     * 获取当前用户显示名称
     * 
     * @return 显示名称，如果未登录则返回null
     */
    public static String getDisplayName() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getDisplayName() : null;
    }
    
    /**
     * 获取当前用户角色列表
     * 
     * @return 角色列表，如果未登录则返回null
     */
    public static List<String> getUserRoles() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getRoles() : null;
    }
    
    /**
     * 获取当前用户权限列表
     * 
     * @return 权限列表，如果未登录则返回null
     */
    public static List<String> getUserPermissions() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getPermissions() : null;
    }
    
    /**
     * 检查当前用户是否已登录
     * 
     * @return 是否已登录
     */
    public static boolean isLoggedIn() {
        return getUserInfo() != null;
    }
    
    /**
     * 检查当前用户是否具有指定角色
     * 
     * @param role 角色名称
     * @return 是否具有该角色
     */
    public static boolean hasRole(String role) {
        UserInfo userInfo = getUserInfo();
        return userInfo != null && userInfo.hasRole(role);
    }
    
    /**
     * 检查当前用户是否具有指定权限
     * 
     * @param permission 权限名称
     * @return 是否具有该权限
     */
    public static boolean hasPermission(String permission) {
        UserInfo userInfo = getUserInfo();
        return userInfo != null && userInfo.hasPermission(permission);
    }
    
    /**
     * 检查当前用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null && userInfo.isAdmin();
    }
    
    /**
     * 获取当前用户的会话token
     * 
     * @return 会话token，如果未登录则返回null
     */
    public static String getSessionToken() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getSessionToken() : null;
    }
    
    /**
     * 清除当前线程的用户信息
     * 
     * 通常在请求结束时调用，防止内存泄漏
     */
    public static void clearUserInfo() {
        UserInfo userInfo = USER_CONTEXT.get();
        if (userInfo != null) {
            log.debug("清除用户上下文: userId={}, username={}", 
                     userInfo.getUserId(), userInfo.getUsername());
        }
        USER_CONTEXT.remove();
    }
    
    /**
     * 设置当前用户信息 (别名方法)
     * 
     * 为了兼容不同的调用方式，提供setCurrentUser作为setUserInfo的别名
     * 
     * @param userInfo 用户信息对象
     */
    public static void setCurrentUser(UserInfo userInfo) {
        setUserInfo(userInfo);
    }
    
    /**
     * 清除用户上下文 (别名方法)
     * 
     * 为了兼容不同的调用方式，提供clearContext作为clearUserInfo的别名
     */
    public static void clearContext() {
        clearUserInfo();
    }
    
    /**
     * 获取当前用户ID (别名方法)
     * 
     * @return 用户ID，如果未登录返回null
     */
    public static String getCurrentUserId() {
        return getUserId();
    }
    
    /**
     * 获取当前用户名 (别名方法)
     * 
     * @return 用户名，如果未登录返回null
     */
    public static String getCurrentUsername() {
        return getUsername();
    }
    
    /**
     * 获取当前用户显示名称 (别名方法)
     * 
     * @return 显示名称，如果未登录返回null
     */
    public static String getCurrentUserDisplayName() {
        return getDisplayName();
    }
    
    /**
     * 获取当前用户邮箱
     * 
     * @return 用户邮箱，如果未登录返回null
     */
    public static String getCurrentUserEmail() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getEmail() : null;
    }
    
    /**
     * 获取当前用户部门
     * 
     * @return 用户部门，如果未登录返回null
     */
    public static String getCurrentUserDepartment() {
        UserInfo userInfo = getUserInfo();
        return userInfo != null ? userInfo.getDepartment() : null;
    }
    
    /**
     * 获取当前用户信息，如果未登录则抛出异常
     * 
     * 适用于必须要求用户登录的业务场景
     * 
     * @return 用户信息
     * @throws IllegalStateException 如果用户未登录
     */
    public static UserInfo requireUserInfo() {
        UserInfo userInfo = getUserInfo();
        if (userInfo == null) {
            throw new IllegalStateException("用户未登录，无法获取用户信息");
        }
        return userInfo;
    }
    
    /**
     * 获取当前用户ID，如果未登录则抛出异常
     * 
     * @return 用户ID
     * @throws IllegalStateException 如果用户未登录
     */
    public static String requireUserId() {
        return requireUserInfo().getUserId();
    }
    
    /**
     * 获取当前用户名，如果未登录则抛出异常
     * 
     * @return 用户名
     * @throws IllegalStateException 如果用户未登录
     */
    public static String requireUsername() {
        return requireUserInfo().getUsername();
    }
    
    /**
     * 获取当前用户信息 (别名方法)
     * 
     * 为了兼容不同的调用方式，提供getCurrentUser作为getUserInfo的别名
     * 
     * @return 用户信息，如果未登录返回null
     */
    public static UserInfo getCurrentUser() {
        return getUserInfo();
    }
    
    /**
     * 获取当前用户权限列表 (别名方法)
     * 
     * @return 用户权限列表，如果未登录返回null
     */
    public static List<String> getCurrentUserPermissions() {
        return getUserPermissions();
    }
    
    /**
     * 获取当前用户角色列表 (别名方法)
     * 
     * @return 用户角色列表，如果未登录返回null
     */
    public static List<String> getCurrentUserRoles() {
        return getUserRoles();
    }
}