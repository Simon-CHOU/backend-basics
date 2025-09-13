package com.example.demo.entity;

public enum SagaStatus {
    STARTED("已开始"),
    EXECUTING("执行中"),
    COMPLETED("已完成"),
    COMPENSATING("补偿中"),
    COMPENSATED("已补偿"),
    FAILED("失败");
    
    private final String description;
    
    SagaStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}