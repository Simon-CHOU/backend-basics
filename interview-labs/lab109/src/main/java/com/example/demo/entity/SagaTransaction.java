package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Saga事务实体
 * 用于跟踪分布式事务的执行状态和补偿操作
 */
@Setter
@Getter
@Entity
@Table(name = "saga_transactions")
public class SagaTransaction {
    
    @Id
    private String id;
    
    @NotBlank
    @Column(nullable = false)
    private String sagaType;
    
    @NotBlank
    @Column(nullable = false)
    private String businessId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String currentStep;
    
    @Column(columnDefinition = "TEXT")
    private String executedSteps;
    
    @Column(columnDefinition = "TEXT")
    private String compensatedSteps;
    
    @Column(columnDefinition = "TEXT")
    private String sagaData;
    
    @Column
    private String errorMessage;
    
    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Setter
    @Column
    private LocalDateTime updatedAt;
    
    @Setter
    @Column
    private LocalDateTime completedAt;
    
    public SagaTransaction() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.status = SagaStatus.STARTED;
        this.executedSteps = "";
        this.compensatedSteps = "";
    }
    
    public SagaTransaction(String sagaType, String businessId, String sagaData) {
        this();
        this.sagaType = sagaType;
        this.businessId = businessId;
        this.sagaData = sagaData;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSagaType() {
        return sagaType;
    }
    
    public void setSagaType(String sagaType) {
        this.sagaType = sagaType;
    }
    
    public String getBusinessId() {
        return businessId;
    }
    
    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
    
    public SagaStatus getStatus() {
        return status;
    }
    
    public void setStatus(SagaStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == SagaStatus.COMPLETED || status == SagaStatus.COMPENSATED || status == SagaStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public int getCurrentStepAsInt() {
        if (currentStep == null || currentStep.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(currentStep);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setCurrentStep(int stepIndex) {
        this.currentStep = String.valueOf(stepIndex);
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getExecutedSteps() {
        return executedSteps;
    }
    
    public void setExecutedSteps(String executedSteps) {
        this.executedSteps = executedSteps;
    }
    
    public void setSteps(List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            this.executedSteps = "";
        } else {
            this.executedSteps = String.join(",", steps);
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addExecutedStep(String step) {
        if (this.executedSteps == null || this.executedSteps.isEmpty()) {
            this.executedSteps = step;
        } else {
            this.executedSteps += "," + step;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCompensatedSteps() {
        return compensatedSteps;
    }
    
    public void setCompensatedSteps(String compensatedSteps) {
        this.compensatedSteps = compensatedSteps;
    }
    
    public void addCompensatedStep(String step) {
        if (this.compensatedSteps == null || this.compensatedSteps.isEmpty()) {
            this.compensatedSteps = step;
        } else {
            this.compensatedSteps += "," + step;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getSagaData() {
        return sagaData;
    }
    
    public void setSagaData(String sagaData) {
        this.sagaData = sagaData;
    }
    
    public void setData(String data) {
        this.sagaData = data;
        this.updatedAt = LocalDateTime.now();
    }
    
    public List<String> getSteps() {
        if (executedSteps == null || executedSteps.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(executedSteps.split(","));
    }
    
    public Map<String, Object> getData() {
        if (sagaData == null || sagaData.isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(sagaData, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    @Override
    public String toString() {
        return "SagaTransaction{" +
                "id='" + id + '\'' +
                ", sagaType='" + sagaType + '\'' +
                ", businessId='" + businessId + '\'' +
                ", status=" + status +
                ", currentStep='" + currentStep + '\'' +
                ", executedSteps='" + executedSteps + '\'' +
                ", compensatedSteps='" + compensatedSteps + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}