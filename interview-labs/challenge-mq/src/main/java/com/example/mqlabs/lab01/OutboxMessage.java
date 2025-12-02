package com.example.mqlabs.lab01;

public record OutboxMessage(Long id, String aggregateType, String aggregateId, String type, String payload, java.time.Instant createdAt, String status) {}
