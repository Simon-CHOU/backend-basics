package com.simon.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.simon.mybatis.mapper")
public class MybatisStuffApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybatisStuffApplication.class, args);
    }
}

