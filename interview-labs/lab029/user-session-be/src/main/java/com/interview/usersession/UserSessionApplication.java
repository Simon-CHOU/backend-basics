package com.interview.usersession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Session Management Application
 * 
 * 演示全局用户信息获取设计模式的Spring Boot应用
 * 核心功能：
 * 1. 基于Redis的会话管理
 * 2. ThreadLocal用户上下文
 * 3. 拦截器自动用户信息注入
 * 4. 全局UserUtil工具类
 * 
 * @author Interview Lab
 * @version 1.0
 */
@SpringBootApplication
public class UserSessionApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserSessionApplication.class, args);
    }

}