package com.simon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.simon.mapper")
public class MybatisTraditionalDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MybatisTraditionalDemoApplication.class, args);
    }
}
