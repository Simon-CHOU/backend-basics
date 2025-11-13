package com.simon.case_study_statistics_by_month;

import com.simon.case_study_statistics_by_month.domain.StatisticsResult;
import com.simon.case_study_statistics_by_month.domain.enums.CustomerType;
import com.simon.case_study_statistics_by_month.domain.enums.ProjectType;
import com.simon.case_study_statistics_by_month.mapper.StatisticsMapper;
import com.simon.case_study_statistics_by_month.service.StatisticsTaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
public class StatisticsE2EVerifyTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StatisticsTaskService statisticsTaskService;

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Test
    void verifyEndToEnd() {
        truncateTables();
        generateData(LocalDate.of(2024, 11, 1), LocalDate.of(2025, 5, 31));

        CompletableFuture<Void> f = statisticsTaskService.triggerManualStatistics();
        f.join();

        Map<String, Aggregated> expected = computeExpected();
        Map<String, Aggregated> actual = computeActual();

        Assertions.assertEquals(expected.keySet(), actual.keySet());
        for (String k : expected.keySet()) {
            Aggregated e = expected.get(k);
            Aggregated a = actual.get(k);
            Assertions.assertEquals(e.proactiveCustomerCount, a.proactiveCustomerCount);
            Assertions.assertEquals(e.cooperativeCustomerCount, a.cooperativeCustomerCount);
            Assertions.assertEquals(e.unbindingCount, a.unbindingCount);
            Assertions.assertEquals(e.totalProjectCount, a.totalProjectCount);
            Assertions.assertEquals(e.activeProjectCount, a.activeProjectCount);
            Assertions.assertEquals(e.bfoProjectCount, a.bfoProjectCount);
        }

        int taskKinds = 6;
        int months = (int) expected.values().stream().map(x -> x.yyyyMm).distinct().count();
        int retailers = (int) expected.values().stream().map(x -> x.retailerId).distinct().count();
        int expectedRows = months * retailers * taskKinds;
        Integer actualRows = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM bip_dashboard_statistics", Integer.class);
        Assertions.assertEquals(expectedRows, actualRows.intValue());

        System.out.println("E2E verify passed");
        System.out.println("months=" + months + ", retailers=" + retailers + ", rows=" + actualRows);
    }

    private void truncateTables() {
        jdbcTemplate.execute("TRUNCATE TABLE bip_dashboard_statistics");
        jdbcTemplate.execute("TRUNCATE TABLE bip_binding");
        jdbcTemplate.execute("TRUNCATE TABLE bip_project");
        jdbcTemplate.execute("TRUNCATE TABLE bip_customer");
    }

    private void generateData(LocalDate startDate, LocalDate endDate) {
        List<CustomerRow> customers = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusMonths(1)) {
            for (int i = 0; i < 5; i++) {
                CustomerType ct = (i % 2 == 0) ? CustomerType.PROACTIVE : CustomerType.COOPERATIVE;
                customers.add(new CustomerRow(UUID.randomUUID().toString().substring(0, 30),
                        "Customer-" + d.getYear() + "-" + d.getMonthValue() + "-" + i,
                        "CUST" + d.getYear() + d.getMonthValue() + i,
                        ct.getCode(),
                        "0",
                        "mimic-data-generator",
                        d.atStartOfDay(),
                        "mimic-data-generator",
                        d.atStartOfDay()));
            }
        }
        batchInsertCustomers(customers);

        List<ProjectRow> projects = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusMonths(1)) {
            for (int i = 0; i < 3; i++) {
                ProjectType pt = switch (i % 3) {
                    case 0 -> ProjectType.BFO;
                    case 1 -> ProjectType.STANDARD;
                    default -> ProjectType.CUSTOM;
                };
                projects.add(new ProjectRow(UUID.randomUUID().toString().substring(0, 30),
                        "Project-" + d.getYear() + "-" + d.getMonthValue() + "-" + i,
                        "PROJ" + d.getYear() + d.getMonthValue() + i,
                        pt.getCode(),
                        "0",
                        "mimic-data-generator",
                        d.atStartOfDay(),
                        "mimic-data-generator",
                        d.atStartOfDay()));
            }
        }
        batchInsertProjects(projects);

        List<BindingRow> bindings = new ArrayList<>();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusMonths(1)) {
            YearMonth ym = YearMonth.from(d);
            for (int i = 0; i < 10; i++) {
                CustomerRow cr = customers.get(rnd.nextInt(customers.size()));
                ProjectRow pr = projects.get(rnd.nextInt(projects.size()));
                int day = rnd.nextInt(1, ym.lengthOfMonth() + 1);
                LocalDateTime t = d.withDayOfMonth(day).atTime(rnd.nextInt(24), rnd.nextInt(60));
                bindings.add(new BindingRow(UUID.randomUUID().toString().substring(0, 30),
                        cr.id,
                        pr.id,
                        (i % 4 == 0) ? "1" : "0",
                        "mimic-data-generator",
                        t,
                        "mimic-data-generator",
                        t));
            }
        }
        batchInsertBindings(bindings);
    }

    private void batchInsertCustomers(List<CustomerRow> customers) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_customer (id, customer_name, customer_code, customer_type, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", customers, 100,
                (ps, c) -> {
                    ps.setString(1, c.id);
                    ps.setString(2, c.customerName);
                    ps.setString(3, c.customerCode);
                    ps.setString(4, c.customerType);
                    ps.setString(5, c.delFlag);
                    ps.setString(6, c.createdBy);
                    ps.setObject(7, c.createTime);
                    ps.setString(8, c.updateBy);
                    ps.setObject(9, c.updateTime);
                });
    }

    private void batchInsertProjects(List<ProjectRow> projects) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_project (id, project_name, project_code, project_type, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", projects, 100,
                (ps, p) -> {
                    ps.setString(1, p.id);
                    ps.setString(2, p.projectName);
                    ps.setString(3, p.projectCode);
                    ps.setString(4, p.projectType);
                    ps.setString(5, p.delFlag);
                    ps.setString(6, p.createdBy);
                    ps.setObject(7, p.createTime);
                    ps.setString(8, p.updateBy);
                    ps.setObject(9, p.updateTime);
                });
    }

    private void batchInsertBindings(List<BindingRow> bindings) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_binding (id, customer_id, project_id, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", bindings, 100,
                (ps, b) -> {
                    ps.setString(1, b.id);
                    ps.setString(2, b.customerId);
                    ps.setString(3, b.projectId);
                    ps.setString(4, b.delFlag);
                    ps.setString(5, b.createdBy);
                    ps.setObject(6, b.createTime);
                    ps.setString(7, b.updateBy);
                    ps.setObject(8, b.updateTime);
                });
    }

    private Map<String, Aggregated> computeExpected() {
        List<String> retailerIds = jdbcTemplate.query("SELECT id FROM bip_customer", (rs, rowNum) -> rs.getString(1));
        List<String> months = buildMonths(LocalDate.of(2024, 11, 1), LocalDate.of(2025, 5, 31));
        Map<String, Aggregated> map = new LinkedHashMap<>();
        for (String rid : retailerIds) {
            int pc = 0, cc = 0, uc = 0, tp = 0, ap = 0, bp = 0;
            for (String ym : months) {
                YearMonth m = YearMonth.parse(ym);
                LocalDateTime start = m.atDay(1).atStartOfDay();
                LocalDateTime end = m.plusMonths(1).atDay(1).atStartOfDay();

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

                pc += newPc;
                cc += newCc;
                uc += newUc;
                tp += newTp;
                ap += newAp;
                bp += newBp;

                String key = ym + "|" + rid;
                map.put(key, new Aggregated(ym, rid, pc, cc, uc, tp, ap, bp));
            }
        }
        return map;
    }

    private Map<String, Aggregated> computeActual() {
        String sql = "SELECT \"year\", \"month\", retailer_id, " +
                "SUM(CASE WHEN sub_task='主动型客户数量' THEN CAST(result AS INT) END) AS proactive_customer_count, " +
                "SUM(CASE WHEN sub_task='配合行客户数量' THEN CAST(result AS INT) END) AS cooperative_customer_count, " +
                "SUM(CASE WHEN sub_task='解绑数量' THEN CAST(result AS INT) END) AS unbinding_count, " +
                "SUM(CASE WHEN sub_task='项目信息累计数量' THEN CAST(result AS INT) END) AS total_project_count, " +
                "SUM(CASE WHEN sub_task='有效项目累计数量' THEN CAST(result AS INT) END) AS active_project_count, " +
                "SUM(CASE WHEN sub_task='重要BFO ID项目累计数量' THEN CAST(result AS INT) END) AS bfo_project_count " +
                "FROM bip_dashboard_statistics GROUP BY \"year\", \"month\", retailer_id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<String, Aggregated> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String year = Objects.toString(row.get("year"));
            String month = Objects.toString(row.get("month"));
            String retailerId = Objects.toString(row.get("retailer_id"));
            String yyyyMm = year + "-" + month;
            String key = yyyyMm + "|" + retailerId;
            Aggregated a = new Aggregated(
                    yyyyMm,
                    retailerId,
                    toInt(row.get("proactive_customer_count")),
                    toInt(row.get("cooperative_customer_count")),
                    toInt(row.get("unbinding_count")),
                    toInt(row.get("total_project_count")),
                    toInt(row.get("active_project_count")),
                    toInt(row.get("bfo_project_count"))
            );
            map.put(key, a);
        }
        return map;
    }

    private List<String> buildMonths(LocalDate startDate, LocalDate endDate) {
        List<String> list = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusMonths(1)) {
            YearMonth ym = YearMonth.from(d);
            list.add(String.format("%04d-%02d", ym.getYear(), ym.getMonthValue()));
        }
        return list;
    }

    private int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(Objects.toString(o));
    }

    static class Aggregated {
        final String yyyyMm;
        final String retailerId;
        final int proactiveCustomerCount;
        final int cooperativeCustomerCount;
        final int unbindingCount;
        final int totalProjectCount;
        final int activeProjectCount;
        final int bfoProjectCount;
        Aggregated(String yyyyMm, String retailerId, int pc, int cc, int uc, int tp, int ap, int bp) {
            this.yyyyMm = yyyyMm;
            this.retailerId = retailerId;
            this.proactiveCustomerCount = pc;
            this.cooperativeCustomerCount = cc;
            this.unbindingCount = uc;
            this.totalProjectCount = tp;
            this.activeProjectCount = ap;
            this.bfoProjectCount = bp;
        }
    }

    static class CustomerRow {
        final String id;
        final String customerName;
        final String customerCode;
        final String customerType;
        final String delFlag;
        final String createdBy;
        final LocalDateTime createTime;
        final String updateBy;
        final LocalDateTime updateTime;
        CustomerRow(String id, String customerName, String customerCode, String customerType, String delFlag, String createdBy, LocalDateTime createTime, String updateBy, LocalDateTime updateTime) {
            this.id = id;
            this.customerName = customerName;
            this.customerCode = customerCode;
            this.customerType = customerType;
            this.delFlag = delFlag;
            this.createdBy = createdBy;
            this.createTime = createTime;
            this.updateBy = updateBy;
            this.updateTime = updateTime;
        }
    }

    static class ProjectRow {
        final String id;
        final String projectName;
        final String projectCode;
        final String projectType;
        final String delFlag;
        final String createdBy;
        final LocalDateTime createTime;
        final String updateBy;
        final LocalDateTime updateTime;
        ProjectRow(String id, String projectName, String projectCode, String projectType, String delFlag, String createdBy, LocalDateTime createTime, String updateBy, LocalDateTime updateTime) {
            this.id = id;
            this.projectName = projectName;
            this.projectCode = projectCode;
            this.projectType = projectType;
            this.delFlag = delFlag;
            this.createdBy = createdBy;
            this.createTime = createTime;
            this.updateBy = updateBy;
            this.updateTime = updateTime;
        }
    }

    static class BindingRow {
        final String id;
        final String customerId;
        final String projectId;
        final String delFlag;
        final String createdBy;
        final LocalDateTime createTime;
        final String updateBy;
        final LocalDateTime updateTime;
        BindingRow(String id, String customerId, String projectId, String delFlag, String createdBy, LocalDateTime createTime, String updateBy, LocalDateTime updateTime) {
            this.id = id;
            this.customerId = customerId;
            this.projectId = projectId;
            this.delFlag = delFlag;
            this.createdBy = createdBy;
            this.createTime = createTime;
            this.updateBy = updateBy;
            this.updateTime = updateTime;
        }
    }
}
