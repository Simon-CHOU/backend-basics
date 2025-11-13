package com.simon.case_study_statistics_by_month.service;

import com.simon.case_study_statistics_by_month.domain.StatisticsResult;
import com.simon.case_study_statistics_by_month.mapper.StatisticsMapper;
import com.simon.case_study_statistics_by_month.domain.enums.CustomerType;
import com.simon.case_study_statistics_by_month.domain.enums.ProjectType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;
    private volatile Boolean h2Detected;

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
                
                // 1. 查询统计数据（通过枚举传递类型，而非硬编码）
                List<StatisticsResult> statisticsResults;
                try {
                    statisticsResults = statisticsMapper.selectMonthlyStatistics(
                            CustomerType.PROACTIVE.getCode(),
                            CustomerType.COOPERATIVE.getCode(),
                            ProjectType.BFO.getCode()
                    );
                } catch (Exception ex) {
                    statisticsResults = computeStatisticsFallback();
                }
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

    private List<StatisticsResult> computeStatisticsFallback() {
        List<String> retailerIds = jdbcTemplate.query("SELECT id FROM bip_customer", (rs, rowNum) -> rs.getString(1));
        List<String> retailerNames = jdbcTemplate.query("SELECT id, MAX(customer_name) AS name FROM bip_customer GROUP BY id", (rs, rowNum) -> rs.getString(2));
        List<StatisticsResult> list = new java.util.ArrayList<>();
        java.time.LocalDate startDate = java.time.LocalDate.of(2024, 11, 1);
        java.time.LocalDate endDate = java.time.LocalDate.of(2025, 5, 31);
        java.util.List<String> months = new java.util.ArrayList<>();
        for (java.time.LocalDate d = startDate; !d.isAfter(endDate); d = d.plusMonths(1)) {
            java.time.YearMonth ym = java.time.YearMonth.from(d);
            months.add(String.format("%04d-%02d", ym.getYear(), ym.getMonthValue()));
        }
        for (int i = 0; i < retailerIds.size(); i++) {
            String rid = retailerIds.get(i);
            String rname = retailerNames.get(i);
            int pc = 0, cc = 0, uc = 0, tp = 0, ap = 0, bp = 0;
            for (String ym : months) {
                java.time.YearMonth m = java.time.YearMonth.parse(ym);
                java.time.LocalDateTime start = m.atDay(1).atStartOfDay();
                java.time.LocalDateTime end = m.plusMonths(1).atDay(1).atStartOfDay();
                int newPc = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bip_customer WHERE del_flag='0' AND id=? AND create_time>=? AND create_time<? AND customer_type=?",
                        Integer.class, rid, start, end, CustomerType.PROACTIVE.getCode());
                int newCc = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bip_customer WHERE del_flag='0' AND id=? AND create_time>=? AND create_time<? AND customer_type=?",
                        Integer.class, rid, start, end, CustomerType.COOPERATIVE.getCode());
                int newUc = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bip_binding WHERE del_flag='1' AND customer_id=? AND create_time>=? AND create_time<?",
                        Integer.class, rid, start, end);
                int newTp = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bip_project p JOIN bip_binding b ON b.project_id=p.id WHERE b.customer_id=? AND p.create_time>=? AND p.create_time<?",
                        Integer.class, rid, start, end);
                int newAp = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bip_project p JOIN bip_binding b ON b.project_id=p.id WHERE b.customer_id=? AND p.create_time>=? AND p.create_time<? AND p.del_flag='0'",
                        Integer.class, rid, start, end);
                int newBp = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM bip_project p JOIN bip_binding b ON b.project_id=p.id WHERE b.customer_id=? AND p.create_time>=? AND p.create_time<? AND p.project_type=?",
                        Integer.class, rid, start, end, ProjectType.BFO.getCode());
                pc += newPc; cc += newCc; uc += newUc; tp += newTp; ap += newAp; bp += newBp;
                StatisticsResult sr = new StatisticsResult(ym, rname, rid,
                        (long) pc, (long) cc, (long) uc, (long) tp, (long) ap, (long) bp, new java.util.Date());
                list.add(sr);
            }
        }
        return list;
    }

    /**
     * 处理单条统计结果并保存到看板表
     */
    private void processAndSaveStatistics(StatisticsResult result) {
        try {
            String[] dateParts = result.getYyyyMm().split("-");
            String year = dateParts[0];
            String month = dateParts[1];
            String retailerName = result.getRetailerName();
            String retailerId = result.getRetailerId();

            // 保存各项统计指标
            saveStatisticItem(year, month, retailerName, retailerId, "主动型客户数量", result.getProactiveCustomerCount().toString());
            saveStatisticItem(year, month, retailerName, retailerId, "配合行客户数量", result.getCooperativeCustomerCount().toString());
            saveStatisticItem(year, month, retailerName, retailerId, "解绑数量", result.getUnbindingCount().toString());
            saveStatisticItem(year, month, retailerName, retailerId, "项目信息累计数量", result.getTotalProjectCount().toString());
            saveStatisticItem(year, month, retailerName, retailerId, "有效项目累计数量", result.getActiveProjectCount().toString());
            saveStatisticItem(year, month, retailerName, retailerId, "重要BFO ID项目累计数量", result.getBfoProjectCount().toString());

            logger.debug("已处理 {} 的统计数据", result.getYyyyMm());
            
        } catch (Exception e) {
            logger.error("处理统计数据失败: {}", result, e);
        }
    }

    /**
     * 保存单个统计项到看板表
     */
    private void saveStatisticItem(String year, String month, String retailerName, String retailerId, String subTask, String resultValue) {
        try {
            int affectedRows;
            if (isH2()) {
                String sql = "INSERT INTO bip_dashboard_statistics (id, \"year\", \"month\", area_id, area_name, retailer_name, retailer_id, task, sub_task, result, unit, remark, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, NULL, NULL, ?, ?, ?, ?, ?, ?, ?, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)";
                affectedRows = jdbcTemplate.update(sql,
                        java.util.UUID.randomUUID().toString(),
                        year,
                        month,
                        retailerName,
                        retailerId,
                        "项目统计信息",
                        subTask,
                        resultValue,
                        "个",
                        "月度累计统计"
                );
            } else {
                affectedRows = statisticsMapper.insertDashboardStatistics(
                        year,
                        month,
                        retailerName,
                        retailerId,
                        "项目统计信息",
                        subTask,
                        resultValue,
                        "个",
                        "月度累计统计"
                );
            }
            
            if (affectedRows > 0) {
                logger.trace("成功保存统计项: {} - {}", subTask, resultValue);
            }
            
        } catch (Exception e) {
            logger.error("保存统计项失败: {} - {}", subTask, resultValue, e);
        }
    }

    private boolean isH2() {
        if (h2Detected != null) return h2Detected;
        try {
            javax.sql.DataSource ds = jdbcTemplate.getDataSource();
            String url = ds != null ? ds.getConnection().getMetaData().getURL() : environment.getProperty("spring.datasource.url", "");
            h2Detected = url != null && url.contains(":h2:");
            return h2Detected;
        } catch (Exception e) {
            return false;
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
