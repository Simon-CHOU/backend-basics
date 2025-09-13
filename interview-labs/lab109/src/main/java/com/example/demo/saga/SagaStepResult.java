package com.example.demo.saga;

/**
 * Saga步骤执行结果
 */
public class SagaStepResult {
    
    private final boolean success;
    private final String message;
    private final Exception exception;
    private final SagaData updatedData;
    
    private SagaStepResult(boolean success, String message, Exception exception, SagaData updatedData) {
        this.success = success;
        this.message = message;
        this.exception = exception;
        this.updatedData = updatedData;
    }
    
    public static SagaStepResult success(String message) {
        return new SagaStepResult(true, message, null, null);
    }
    
    public static SagaStepResult success(String message, SagaData updatedData) {
        return new SagaStepResult(true, message, null, updatedData);
    }
    
    public static SagaStepResult failure(String message) {
        return new SagaStepResult(false, message, null, null);
    }
    
    public static SagaStepResult failure(String message, Exception exception) {
        return new SagaStepResult(false, message, exception, null);
    }
    
    public static SagaStepResult failure(Exception exception) {
        return new SagaStepResult(false, exception.getMessage(), exception, null);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public SagaData getUpdatedData() {
        return updatedData;
    }
    
    @Override
    public String toString() {
        return "SagaStepResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", exception=" + (exception != null ? exception.getMessage() : "null") +
                '}';
    }
}