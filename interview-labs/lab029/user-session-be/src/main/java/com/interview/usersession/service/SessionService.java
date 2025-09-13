package com.interview.usersession.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.usersession.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 会话管理服务
 * 
 * 负责用户会话在Redis中的存储、获取、更新和删除操作
 * 
 * @author Interview Lab
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 会话key前缀
     */
    private static final String SESSION_KEY_PREFIX = "session:";
    
    /**
     * 默认会话过期时间（2小时）
     */
    private static final Duration DEFAULT_SESSION_TIMEOUT = Duration.ofHours(2);
    
    /**
     * 创建用户会话
     * 
     * @param userInfo 用户信息
     * @return 会话token
     */
    public String createSession(UserInfo userInfo) {
        try {
            // 生成UUID作为session token
            String token = UUID.randomUUID().toString();
            
            // 设置token到用户信息中
            userInfo.setSessionToken(token);
            
            // 序列化用户信息为JSON
            String userInfoJson = objectMapper.writeValueAsString(userInfo);
            
            // 存储到Redis
            String sessionKey = getSessionKey(token);
            redisTemplate.opsForValue().set(sessionKey, userInfoJson, DEFAULT_SESSION_TIMEOUT);
            
            log.info("创建用户会话成功: userId={}, username={}, token={}", 
                    userInfo.getUserId(), userInfo.getUsername(), token);
            
            return token;
        } catch (JsonProcessingException e) {
            log.error("序列化用户信息失败", e);
            throw new RuntimeException("创建会话失败", e);
        }
    }
    
    /**
     * 根据token获取用户信息
     * 
     * @param token 会话token
     * @return 用户信息，如果会话不存在或已过期则返回null
     */
    public UserInfo getUserInfo(String token) {
        try {
            String sessionKey = getSessionKey(token);
            String userInfoJson = redisTemplate.opsForValue().get(sessionKey);
            
            if (userInfoJson == null) {
                log.debug("会话不存在或已过期: token={}", token);
                return null;
            }
            
            // 反序列化用户信息
            UserInfo userInfo = objectMapper.readValue(userInfoJson, UserInfo.class);
            
            // 更新会话过期时间（滑动过期）
            redisTemplate.expire(sessionKey, DEFAULT_SESSION_TIMEOUT);
            
            log.debug("获取用户会话成功: userId={}, username={}", 
                     userInfo.getUserId(), userInfo.getUsername());
            
            return userInfo;
        } catch (JsonProcessingException e) {
            log.error("反序列化用户信息失败: token={}", token, e);
            return null;
        }
    }
    
    /**
     * 更新用户会话信息
     * 
     * @param token 会话token
     * @param userInfo 更新的用户信息
     * @return 是否更新成功
     */
    public boolean updateSession(String token, UserInfo userInfo) {
        try {
            String sessionKey = getSessionKey(token);
            
            // 检查会话是否存在
            if (!redisTemplate.hasKey(sessionKey)) {
                log.warn("尝试更新不存在的会话: token={}", token);
                return false;
            }
            
            // 设置token到用户信息中
            userInfo.setSessionToken(token);
            
            // 序列化并更新
            String userInfoJson = objectMapper.writeValueAsString(userInfo);
            redisTemplate.opsForValue().set(sessionKey, userInfoJson, DEFAULT_SESSION_TIMEOUT);
            
            log.info("更新用户会话成功: userId={}, username={}", 
                    userInfo.getUserId(), userInfo.getUsername());
            
            return true;
        } catch (JsonProcessingException e) {
            log.error("更新会话失败: token={}", token, e);
            return false;
        }
    }
    
    /**
     * 删除用户会话（注销）
     * 
     * @param token 会话token
     * @return 是否删除成功
     */
    public boolean deleteSession(String token) {
        String sessionKey = getSessionKey(token);
        Boolean deleted = redisTemplate.delete(sessionKey);
        
        if (Boolean.TRUE.equals(deleted)) {
            log.info("删除用户会话成功: token={}", token);
            return true;
        } else {
            log.warn("删除用户会话失败，会话可能不存在: token={}", token);
            return false;
        }
    }
    
    /**
     * 检查会话是否存在
     * 
     * @param token 会话token
     * @return 会话是否存在
     */
    public boolean sessionExists(String token) {
        String sessionKey = getSessionKey(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }
    
    /**
     * 延长会话过期时间
     * 
     * @param token 会话token
     * @return 是否延长成功
     */
    public boolean extendSession(String token) {
        String sessionKey = getSessionKey(token);
        return Boolean.TRUE.equals(redisTemplate.expire(sessionKey, DEFAULT_SESSION_TIMEOUT));
    }
    
    /**
     * 获取会话剩余过期时间
     * 
     * @param token 会话token
     * @return 剩余时间（秒），如果会话不存在则返回-1
     */
    public long getSessionTTL(String token) {
        String sessionKey = getSessionKey(token);
        return redisTemplate.getExpire(sessionKey);
    }
    
    /**
     * 构建会话key
     * 
     * @param token 会话token
     * @return Redis key
     */
    private String getSessionKey(String token) {
        return SESSION_KEY_PREFIX + token;
    }
}