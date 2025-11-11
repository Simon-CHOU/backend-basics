package com.simon.case_study_statistics_by_month.controller;

import com.simon.case_study_statistics_by_month.service.StatisticsTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * 统计任务控制器
 * 提供手动触发统计任务的REST接口
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsTaskService statisticsTaskService;

    /**
     * 手动触发统计任务
     * curl -X GET http://localhost:8080/api/statistics/trigger
     */
    @GetMapping("/trigger")
    public ResponseEntity<String> triggerStatistics() {
        try {
            logger.info("接收到手动触发统计任务的请求");
            
            // 异步执行统计任务
            CompletableFuture<Void> future = statisticsTaskService.triggerManualStatistics();
            
            // 可以添加回调处理，这里简单返回成功响应
            future.thenRun(() -> 
                logger.info("手动统计任务执行完成")
            );
            
            return ResponseEntity.ok("统计任务已开始执行，请查看日志了解执行进度");
            
        } catch (Exception e) {
            logger.error("触发统计任务失败", e);
            return ResponseEntity.internalServerError()
                    .body("触发统计任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取统计任务状态
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatisticsStatus() {
        return ResponseEntity.ok("统计服务运行正常，定时任务配置为每晚23:40执行");
    }
}