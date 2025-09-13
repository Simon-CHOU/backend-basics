package com.example.demo.entity;

public enum EventStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    PROCESSED("已处理"),
    FAILED("失败"),
    DEAD_LETTER("死信");
    
    private final String description;
    
    EventStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}