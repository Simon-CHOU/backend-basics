package com.example.demo.saga;

/**
 * Saga步骤接口
 * 定义每个步骤的执行和补偿操作
 */
public interface SagaStep {
    
    /**
     * 获取步骤名称
     */
    String getStepName();
    
    /**
     * 执行步骤
     * @param sagaData Saga数据
     * @return 执行结果
     * @throws Exception 执行异常
     */
    SagaStepResult execute(SagaData sagaData) throws Exception;
    
    /**
     * 补偿步骤（回滚操作）
     * @param sagaData Saga数据
     * @return 补偿结果
     * @throws Exception 补偿异常
     */
    SagaStepResult compensate(SagaData sagaData) throws Exception;
    
    /**
     * 是否支持补偿
     */
    default boolean isCompensable() {
        return true;
    }
}