package com.interview.usersession.config;

import com.interview.usersession.interceptor.AccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * 主要功能：
 * 1. 注册AccessInterceptor拦截器
 * 2. 配置CORS跨域支持
 * 3. 其他Web相关配置
 * 
 * @author Interview Lab
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final AccessInterceptor accessInterceptor;
    
    /**
     * 注册拦截器
     * 
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        // 排除登录相关接口
                        "/api/auth/login",
                        "/api/auth/logout",
                        // 排除健康检查接口
                        "/api/health",
                        // 排除静态资源
                        "/static/**",
                        "/public/**",
                        "/favicon.ico",
                        // 排除Swagger文档
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        // 排除Actuator监控端点
                        "/actuator/**"
                );
    }
    
    /**
     * 配置CORS跨域支持
     * 
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许所有域名（开发环境）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 允许携带Cookie
                .maxAge(3600); // 预检请求缓存时间
    }
}