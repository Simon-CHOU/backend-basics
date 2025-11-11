package com.simon.case_study_statistics_by_month.service;

import com.simon.case_study_statistics_by_month.domain.StatisticsResult;
import com.simon.case_study_statistics_by_month.mapper.StatisticsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 统计任务服务类
 */
@Service
public class StatisticsTaskService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsTaskService.class);

    @Autowired
    private StatisticsMapper statisticsMapper;

    /**
     * 定时执行统计任务 - 每晚23:40执行
     */
    @Scheduled(cron = "0 40 23 * * ?")
    public void scheduledStatisticsTask() {
        logger.info("开始执行定时统计任务...");
        executeStatisticsTask();
    }

    /**
     * 执行统计任务
     * 使用CompletableFuture实现多线程处理
     */
    @Async
    public CompletableFuture<Void> executeStatisticsTask() {
        return CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                // 1. 查询统计数据
                List<StatisticsResult> statisticsResults = statisticsMapper.selectMonthlyStatistics();
                logger.info("查询到 {} 个月的统计数据", statisticsResults.size());

                // 2. 处理并写入每条统计结果
                for (StatisticsResult result : statisticsResults) {
                    processAndSaveStatistics(result);
                }

                long endTime = System.currentTimeMillis();
                logger.info("统计任务执行完成，耗时: {} ms", (endTime - startTime));
                
            } catch (Exception e) {
                logger.error("统计任务执行失败", e);
            }
        });
    }

    /**
     * 处理单条统计结果并保存到看板表
     */
    private void processAndSaveStatistics(StatisticsResult result) {
        try {
            String[] dateParts = result.getYyyyMm().split("-");
            String year = dateParts[0];
            String month = dateParts[1];

            // 保存各项统计指标
            saveStatisticItem(year, month, "主动型客户数量", result.getProactiveCustomerCount().toString());
            saveStatisticItem(year, month, "配合行客户数量", result.getCooperativeCustomerCount().toString());
            saveStatisticItem(year, month, "解绑数量", result.getUnbindingCount().toString());
            saveStatisticItem(year, month, "项目信息累计数量", result.getTotalProjectCount().toString());
            saveStatisticItem(year, month, "有效项目累计数量", result.getActiveProjectCount().toString());
            saveStatisticItem(year, month, "重要BFO ID项目累计数量", result.getBfoProjectCount().toString());

            logger.debug("已处理 {} 的统计数据", result.getYyyyMm());
            
        } catch (Exception e) {
            logger.error("处理统计数据失败: {}", result, e);
        }
    }

    /**
     * 保存单个统计项到看板表
     */
    private void saveStatisticItem(String year, String month, String subTask, String resultValue) {
        try {
            int affectedRows = statisticsMapper.insertDashboardStatistics(
                year, 
                month, 
                "default_retailer", // retailer_name - 可根据需要从customer表获取
                "default_retailer_id", // retailer_id - 可根据需要从customer表获取
                "项目统计信息", // task固定值
                subTask, // 统计项名称
                resultValue, // 统计结果值
                "个", // 单位
                "月度累计统计" // 备注
            );
            
            if (affectedRows > 0) {
                logger.trace("成功保存统计项: {} - {}", subTask, resultValue);
            }
            
        } catch (Exception e) {
            logger.error("保存统计项失败: {} - {}", subTask, resultValue, e);
        }
    }

    /**
     * 手动触发统计任务（供Controller调用）
     */
    public CompletableFuture<Void> triggerManualStatistics() {
        logger.info("手动触发统计任务...");
        return executeStatisticsTask();
    }
}