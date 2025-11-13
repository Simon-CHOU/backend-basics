package com.simon.case_study_statistics_by_month.service;

import com.simon.case_study_statistics_by_month.domain.Binding;
import com.simon.case_study_statistics_by_month.domain.Customer;
import com.simon.case_study_statistics_by_month.domain.Project;
import com.simon.case_study_statistics_by_month.domain.StatisticsResult;
import com.simon.case_study_statistics_by_month.domain.enums.CustomerType;
import com.simon.case_study_statistics_by_month.domain.enums.ProjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RefactoredStatisticsTaskService {

    private static final Logger logger = LoggerFactory.getLogger(RefactoredStatisticsTaskService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    @Autowired
    private MonthlyStatisticsCalculator calculator;

    private volatile Boolean h2Detected;

    @Scheduled(cron = "0 45 23 * * ?")
    public void scheduledStatisticsTaskV2() {
        logger.info("[V2] 开始执行定时统计任务...");
        executeStatisticsTaskV2();
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> executeStatisticsTaskV2() {
        return CompletableFuture.runAsync(() -> {
            try {
                long start = System.currentTimeMillis();
                List<Customer> customers = jdbcTemplate.query("SELECT id, customer_name, customer_code, customer_type, del_flag, created_by, create_time, update_by, update_time FROM bip_customer",
                        (rs, rn) -> new Customer()
                                .setId(rs.getString("id"))
                                .setCustomerName(rs.getString("customer_name"))
                                .setCustomerCode(rs.getString("customer_code"))
                                .setCustomerType(safeCustomerType(rs.getString("customer_type")))
                                .setDelFlag(rs.getString("del_flag"))
                                .setCreatedBy(rs.getString("created_by"))
                                .setCreateTime(toLocal(rs.getTimestamp("create_time")))
                                .setUpdateBy(rs.getString("update_by"))
                                .setUpdateTime(toLocal(rs.getTimestamp("update_time"))));

                List<Project> projects = jdbcTemplate.query("SELECT id, project_name, project_code, project_type, del_flag, created_by, create_time, update_by, update_time FROM bip_project",
                        (rs, rn) -> new Project()
                                .setId(rs.getString("id"))
                                .setProjectName(rs.getString("project_name"))
                                .setProjectCode(rs.getString("project_code"))
                                .setProjectType(safeProjectType(rs.getString("project_type")))
                                .setDelFlag(rs.getString("del_flag"))
                                .setCreatedBy(rs.getString("created_by"))
                                .setCreateTime(toLocal(rs.getTimestamp("create_time")))
                                .setUpdateBy(rs.getString("update_by"))
                                .setUpdateTime(toLocal(rs.getTimestamp("update_time"))));

                List<Binding> bindings = jdbcTemplate.query("SELECT id, customer_id, project_id, del_flag, created_by, create_time, update_by, update_time FROM bip_binding",
                        (rs, rn) -> new Binding()
                                .setId(rs.getString("id"))
                                .setCustomerId(rs.getString("customer_id"))
                                .setProjectId(rs.getString("project_id"))
                                .setDelFlag(rs.getString("del_flag"))
                                .setCreatedBy(rs.getString("created_by"))
                                .setCreateTime(toLocal(rs.getTimestamp("create_time")))
                                .setUpdateBy(rs.getString("update_by"))
                                .setUpdateTime(toLocal(rs.getTimestamp("update_time"))));

                List<StatisticsResult> list = calculator.calculate(customers, projects, bindings,
                        CustomerType.PROACTIVE.getCode(), CustomerType.COOPERATIVE.getCode(), ProjectType.BFO.getCode());

                for (StatisticsResult r : list) {
                    saveStatisticItem(r);
                }
                long end = System.currentTimeMillis();
                logger.info("[V2] 统计任务完成，写入 {} 个月，共耗时 {} ms", list.size(), (end - start));
            } catch (Exception e) {
                logger.error("[V2] 统计执行失败", e);
            }
        });
    }

    private void saveStatisticItem(StatisticsResult result) {
        String[] parts = result.getYyyyMm().split("-");
        String year = parts[0];
        String month = parts[1];
        String retailerName = result.getRetailerName();
        String retailerId = result.getRetailerId();
        insert(year, month, retailerName, retailerId, "项目统计信息", "主动型客户数量", String.valueOf(result.getProactiveCustomerCount()));
        insert(year, month, retailerName, retailerId, "项目统计信息", "配合行客户数量", String.valueOf(result.getCooperativeCustomerCount()));
        insert(year, month, retailerName, retailerId, "项目统计信息", "解绑数量", String.valueOf(result.getUnbindingCount()));
        insert(year, month, retailerName, retailerId, "项目统计信息", "项目信息累计数量", String.valueOf(result.getTotalProjectCount()));
        insert(year, month, retailerName, retailerId, "项目统计信息", "有效项目累计数量", String.valueOf(result.getActiveProjectCount()));
        insert(year, month, retailerName, retailerId, "项目统计信息", "重要BFO ID项目累计数量", String.valueOf(result.getBfoProjectCount()));
    }

    private void insert(String year, String month, String retailerName, String retailerId, String task, String subTask, String result) {
        if (isH2()) {
            String sql = "INSERT INTO bip_dashboard_statistics (id, \"year\", \"month\", area_id, area_name, retailer_name, retailer_id, task, sub_task, result, unit, remark, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, NULL, NULL, ?, ?, ?, ?, ?, ?, '0', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)";
            jdbcTemplate.update(sql, java.util.UUID.randomUUID().toString(), year, month, retailerName, retailerId, task, subTask, result, "个");
        } else {
            String sql = "INSERT INTO bip_dashboard_statistics (id, year, month, area_id, area_name, retailer_name, retailer_id, task, sub_task, result, unit, remark, del_flag, created_by, create_time, update_by, update_time) VALUES (UUID(), ?, ?, NULL, NULL, ?, ?, ?, ?, ?, ?, '0', 'system', NOW(), 'system', NOW())";
            jdbcTemplate.update(sql, year, month, retailerName, retailerId, task, subTask, result, "个");
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

    private java.time.LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private CustomerType safeCustomerType(String code) {
        try { return code == null ? null : CustomerType.fromCode(code); } catch (Exception e) { return null; }
    }

    private ProjectType safeProjectType(String code) {
        try { return code == null ? null : ProjectType.fromCode(code); } catch (Exception e) { return null; }
    }
}

