package com.simon.case_study_statistics_by_month.controller;

import com.simon.case_study_statistics_by_month.service.RefactoredStatisticsTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/statistics/v2")
public class RefactoredStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(RefactoredStatisticsController.class);

    @Autowired
    private RefactoredStatisticsTaskService taskService;

    @GetMapping("/trigger")
    public ResponseEntity<String> trigger() {
        logger.info("[V2] 接收到手动触发统计任务请求");
        CompletableFuture<Void> f = taskService.executeStatisticsTaskV2();
        f.thenRun(() -> logger.info("[V2] 手动统计任务执行完成"));
        return ResponseEntity.ok("[V2] 统计任务已开始执行，请查看日志");
    }
}

