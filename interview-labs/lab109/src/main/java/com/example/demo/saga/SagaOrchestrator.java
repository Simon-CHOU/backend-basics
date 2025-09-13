package com.example.demo.saga;

import com.example.demo.entity.SagaStatus;
import com.example.demo.entity.SagaTransaction;
import com.example.demo.repository.SagaTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Saga编排器
 * 负责管理Saga事务的执行和补偿流程
 */
@Service
public class SagaOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaOrchestrator.class);
    
    private final SagaTransactionRepository sagaRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public SagaOrchestrator(SagaTransactionRepository sagaRepository, ObjectMapper objectMapper) {
        this.sagaRepository = sagaRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 开始Saga事务
     */
    @Transactional
    public SagaTransaction startSaga(String sagaType, String businessId, SagaData sagaData, List<SagaStep> steps) {
        logger.info("开始Saga事务: type={}, businessId={}", sagaType, businessId);
        
        try {
            String sagaId = UUID.randomUUID().toString();
            
            // 构建步骤信息
            List<String> stepNames = new ArrayList<>();
            for (SagaStep step : steps) {
                stepNames.add(step.getStepName());
            }
            
            SagaTransaction saga = new SagaTransaction();
            saga.setId(sagaId);
            saga.setSagaType(sagaType);
            saga.setBusinessId(businessId);
            saga.setStatus(SagaStatus.STARTED);
            saga.setSteps(stepNames);
            saga.setCurrentStep(0);
            saga.setData(objectMapper.writeValueAsString(sagaData.getAllData()));
            
            saga = sagaRepository.save(saga);
            
            logger.info("Saga事务创建成功: sagaId={}", sagaId);
            
            // 开始执行步骤
            return executeSaga(saga, steps);
            
        } catch (Exception e) {
            logger.error("开始Saga事务失败: {}", e.getMessage(), e);
            throw new RuntimeException("开始Saga事务失败", e);
        }
    }
    
    /**
     * 执行Saga事务
     */
    @Transactional
    public SagaTransaction executeSaga(SagaTransaction saga, List<SagaStep> steps) {
        logger.info("执行Saga事务: sagaId={}, currentStep={}", saga.getId(), saga.getCurrentStep());
        
        try {
            saga.setStatus(SagaStatus.EXECUTING);
            saga = sagaRepository.save(saga);
            
            SagaData sagaData = loadSagaData(saga);
            
            // 从当前步骤开始执行
            for (int i = saga.getCurrentStepAsInt(); i < steps.size(); i++) {
                SagaStep step = steps.get(i);
                
                logger.info("执行步骤 {}/{}: {}", i + 1, steps.size(), step.getStepName());
                
                SagaStepResult result = step.execute(sagaData);
                
                if (result.isSuccess()) {
                    // 步骤执行成功，更新Saga状态
                    saga.setCurrentStep(i + 1);
                    if (result.getUpdatedData() != null) {
                        sagaData = result.getUpdatedData();
                    }
                    saga.setData(objectMapper.writeValueAsString(sagaData.getAllData()));
                    saga.setUpdatedAt(LocalDateTime.now());
                    saga = sagaRepository.save(saga);
                    
                    logger.info("步骤执行成功: {}", step.getStepName());
                } else {
                    // 步骤执行失败，开始补偿
                    logger.error("步骤执行失败: {}, 原因: {}", step.getStepName(), result.getMessage());
                    
                    saga.setStatus(SagaStatus.COMPENSATING);
                    saga.setErrorMessage(result.getMessage());
                    saga = sagaRepository.save(saga);
                    
                    return compensateSaga(saga, steps, i - 1); // 从前一个步骤开始补偿
                }
            }
            
            // 所有步骤执行成功
            saga.setStatus(SagaStatus.COMPLETED);
            saga.setCompletedAt(LocalDateTime.now());
            saga = sagaRepository.save(saga);
            
            logger.info("Saga事务执行成功: sagaId={}", saga.getId());
            
            return saga;
            
        } catch (Exception e) {
            logger.error("执行Saga事务异常: sagaId={}, 错误: {}", saga.getId(), e.getMessage(), e);
            
            saga.setStatus(SagaStatus.FAILED);
            saga.setErrorMessage(e.getMessage());
            saga = sagaRepository.save(saga);
            
            // 开始补偿
            return compensateSaga(saga, steps, saga.getCurrentStepAsInt() - 1);
        }
    }
    
    /**
     * 补偿Saga事务
     */
    @Transactional
    public SagaTransaction compensateSaga(SagaTransaction saga, List<SagaStep> steps, int fromStep) {
        logger.info("开始补偿Saga事务: sagaId={}, fromStep={}", saga.getId(), fromStep);
        
        try {
            saga.setStatus(SagaStatus.COMPENSATING);
            saga = sagaRepository.save(saga);
            
            SagaData sagaData = loadSagaData(saga);
            
            // 从指定步骤开始逆序补偿
            for (int i = fromStep; i >= 0; i--) {
                SagaStep step = steps.get(i);
                
                if (!step.isCompensable()) {
                    logger.info("步骤不支持补偿，跳过: {}", step.getStepName());
                    continue;
                }
                
                logger.info("补偿步骤 {}: {}", i + 1, step.getStepName());
                
                try {
                    SagaStepResult result = step.compensate(sagaData);
                    
                    if (result.isSuccess()) {
                        logger.info("步骤补偿成功: {}", step.getStepName());
                    } else {
                        logger.error("步骤补偿失败: {}, 原因: {}", step.getStepName(), result.getMessage());
                        // 补偿失败，但继续补偿其他步骤
                    }
                } catch (Exception e) {
                    logger.error("步骤补偿异常: {}, 错误: {}", step.getStepName(), e.getMessage(), e);
                    // 补偿异常，但继续补偿其他步骤
                }
            }
            
            saga.setStatus(SagaStatus.COMPENSATED);
            saga.setCompletedAt(LocalDateTime.now());
            saga = sagaRepository.save(saga);
            
            logger.info("Saga事务补偿完成: sagaId={}", saga.getId());
            
            return saga;
            
        } catch (Exception e) {
            logger.error("补偿Saga事务异常: sagaId={}, 错误: {}", saga.getId(), e.getMessage(), e);
            
            saga.setStatus(SagaStatus.FAILED);
            saga.setErrorMessage("补偿失败: " + e.getMessage());
            saga = sagaRepository.save(saga);
            
            return saga;
        }
    }
    
    /**
     * 恢复Saga事务（用于处理中断的事务）
     */
    @Transactional
    public SagaTransaction resumeSaga(String sagaId, List<SagaStep> steps) {
        logger.info("恢复Saga事务: sagaId={}", sagaId);
        
        Optional<SagaTransaction> sagaOpt = sagaRepository.findById(sagaId);
        if (sagaOpt.isEmpty()) {
            throw new RuntimeException("Saga事务不存在: " + sagaId);
        }
        
        SagaTransaction saga = sagaOpt.get();
        
        if (saga.getStatus() == SagaStatus.EXECUTING) {
            return executeSaga(saga, steps);
        } else if (saga.getStatus() == SagaStatus.COMPENSATING) {
            return compensateSaga(saga, steps, saga.getCurrentStepAsInt() - 1);
        } else {
            logger.warn("Saga事务状态不支持恢复: sagaId={}, status={}", sagaId, saga.getStatus());
            return saga;
        }
    }
    
    /**
     * 加载Saga数据
     */
    private SagaData loadSagaData(SagaTransaction saga) {
        try {
            if (saga.getSagaData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = objectMapper.readValue(saga.getSagaData(), new TypeReference<Map<String, Object>>() {});
                return new SagaData(dataMap);
            }
            return new SagaData();
        } catch (Exception e) {
            logger.error("加载Saga数据失败: {}", e.getMessage(), e);
            return new SagaData();
        }
    }
}