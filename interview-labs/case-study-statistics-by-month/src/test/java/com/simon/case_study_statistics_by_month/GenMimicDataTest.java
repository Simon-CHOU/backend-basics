package com.simon.case_study_statistics_by_month;

import com.simon.case_study_statistics_by_month.domain.enums.CustomerType;
import com.simon.case_study_statistics_by_month.domain.enums.ProjectType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
public class GenMimicDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void gen() {
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);

        List<CustomerRow> customers = generateCustomers(startDate, endDate);
        List<ProjectRow> projects = generateProjects(startDate, endDate);

        batchInsertCustomers(customers);
        batchInsertProjects(projects);

        List<BindingRow> bindings = generateBindings(customers, projects, startDate, endDate);
        batchInsertBindings(bindings);
    }

    private List<CustomerRow> generateCustomers(LocalDate startDate, LocalDate endDate) {
        List<CustomerRow> customers = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusMonths(1)) {
            for (int i = 0; i < 5; i++) {
                CustomerType ct = (i % 2 == 0) ? CustomerType.PROACTIVE : CustomerType.COOPERATIVE;
                customers.add(new CustomerRow(
                        UUID.randomUUID().toString().substring(0, 30),
                        "Customer-" + date.getYear() + "-" + date.getMonthValue() + "-" + i,
                        "CUST" + date.getYear() + date.getMonthValue() + i,
                        ct.getCode(),
                        "0",
                        "mimic-data-generator",
                        date.atStartOfDay(),
                        "mimic-data-generator",
                        date.atStartOfDay()
                ));
            }
        }
        return customers;
    }

    private List<ProjectRow> generateProjects(LocalDate startDate, LocalDate endDate) {
        List<ProjectRow> projects = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusMonths(1)) {
            for (int i = 0; i < 3; i++) {
                ProjectType pt = switch (i % 3) {
                    case 0 -> ProjectType.BFO;
                    case 1 -> ProjectType.STANDARD;
                    default -> ProjectType.CUSTOM;
                };
                projects.add(new ProjectRow(
                        UUID.randomUUID().toString().substring(0, 30),
                        "Project-" + date.getYear() + "-" + date.getMonthValue() + "-" + i,
                        "PROJ" + date.getYear() + date.getMonthValue() + i,
                        pt.getCode(),
                        "0",
                        "mimic-data-generator",
                        date.atStartOfDay(),
                        "mimic-data-generator",
                        date.atStartOfDay()
                ));
            }
        }
        return projects;
    }

    private List<BindingRow> generateBindings(List<CustomerRow> customers, List<ProjectRow> projects, LocalDate startDate, LocalDate endDate) {
        List<BindingRow> bindings = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusMonths(1)) {
            YearMonth yearMonth = YearMonth.from(date);
            for (int i = 0; i < 10; i++) {
                CustomerRow randomCustomer = customers.get(random.nextInt(customers.size()));
                ProjectRow randomProject = projects.get(random.nextInt(projects.size()));
                int dayOfMonth = random.nextInt(1, yearMonth.lengthOfMonth() + 1);
                LocalDateTime eventTime = date.withDayOfMonth(dayOfMonth).atTime(random.nextInt(24), random.nextInt(60));
                bindings.add(new BindingRow(
                        UUID.randomUUID().toString().substring(0, 30),
                        randomCustomer.id,
                        randomProject.id,
                        i % 4 == 0 ? "1" : "0",
                        "mimic-data-generator",
                        eventTime,
                        "mimic-data-generator",
                        eventTime
                ));
            }
        }
        return bindings;
    }

    private void batchInsertCustomers(List<CustomerRow> customers) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_customer (id, customer_name, customer_code, customer_type, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                customers,
                100,
                (ps, customer) -> {
                    ps.setString(1, customer.id);
                    ps.setString(2, customer.customerName);
                    ps.setString(3, customer.customerCode);
                    ps.setString(4, customer.customerType);
                    ps.setString(5, customer.delFlag);
                    ps.setString(6, customer.createdBy);
                    ps.setObject(7, customer.createTime);
                    ps.setString(8, customer.updateBy);
                    ps.setObject(9, customer.updateTime);
                });
    }

    private void batchInsertProjects(List<ProjectRow> projects) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_project (id, project_name, project_code, project_type, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                projects,
                100,
                (ps, project) -> {
                    ps.setString(1, project.id);
                    ps.setString(2, project.projectName);
                    ps.setString(3, project.projectCode);
                    ps.setString(4, project.projectType);
                    ps.setString(5, project.delFlag);
                    ps.setString(6, project.createdBy);
                    ps.setObject(7, project.createTime);
                    ps.setString(8, project.updateBy);
                    ps.setObject(9, project.updateTime);
                });
    }

    private void batchInsertBindings(List<BindingRow> bindings) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_binding (id, customer_id, project_id, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                bindings,
                100,
                (ps, binding) -> {
                    ps.setString(1, binding.id);
                    ps.setString(2, binding.customerId);
                    ps.setString(3, binding.projectId);
                    ps.setString(4, binding.delFlag);
                    ps.setString(5, binding.createdBy);
                    ps.setObject(6, binding.createTime);
                    ps.setString(7, binding.updateBy);
                    ps.setObject(8, binding.updateTime);
                });
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
