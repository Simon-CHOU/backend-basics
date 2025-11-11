package com.simon.case_study_statistics_by_month;

import com.simon.case_study_statistics_by_month.domain.Binding;
import com.simon.case_study_statistics_by_month.domain.Customer;
import com.simon.case_study_statistics_by_month.domain.Project;
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
        // 定义时间范围
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);

        // 生成客户和项目数据
        List<Customer> customers = generateCustomers(startDate, endDate);
        List<Project> projects = generateProjects(startDate, endDate);

        // 批量插入客户和项目
        batchInsertCustomers(customers);
        batchInsertProjects(projects);

        // 生成绑定和解绑数据
        List<Binding> bindings = generateBindings(customers, projects, startDate, endDate);

        // 批量插入绑定关系
        batchInsertBindings(bindings);
    }

    private List<Customer> generateCustomers(LocalDate startDate, LocalDate endDate) {
        List<Customer> customers = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusMonths(1)) {
            for (int i = 0; i < 5; i++) { // 每个月生成5个客户
                customers.add(new Customer()
                        .setId(UUID.randomUUID().toString().substring(0, 30))
                        .setCustomerName("Customer-" + date.getYear() + "-" + date.getMonthValue() + "-" + i)
                        .setCustomerCode("CUST" + date.getYear() + date.getMonthValue() + i)
                        .setCustomerType("Type" + (i % 2))
                        .setDelFlag("0")
                        .setCreateTime(date.atStartOfDay())
                        .setUpdateTime(date.atStartOfDay())
                        .setCreatedBy("mimic-data-generator")
                        .setUpdateBy("mimic-data-generator")
                );
            }
        }
        return customers;
    }

    private List<Project> generateProjects(LocalDate startDate, LocalDate endDate) {
        List<Project> projects = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusMonths(1)) {
            for (int i = 0; i < 3; i++) { // 每个月生成3个项目
                projects.add(new Project()
                        .setId(UUID.randomUUID().toString().substring(0, 30))
                        .setProjectName("Project-" + date.getYear() + "-" + date.getMonthValue() + "-" + i)
                        .setProjectCode("PROJ" + date.getYear() + date.getMonthValue() + i)
                        .setProjectType("Type" + (i % 3))
                        .setDelFlag("0")
                        .setCreateTime(date.atStartOfDay())
                        .setUpdateTime(date.atStartOfDay())
                        .setCreatedBy("mimic-data-generator")
                        .setUpdateBy("mimic-data-generator")
                );
            }
        }
        return projects;
    }

    private List<Binding> generateBindings(List<Customer> customers, List<Project> projects, LocalDate startDate, LocalDate endDate) {
        List<Binding> bindings = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusMonths(1)) {
            YearMonth yearMonth = YearMonth.from(date);
            for (int i = 0; i < 10; i++) { // 每个月生成10个绑定/解绑事件
                Customer randomCustomer = customers.get(random.nextInt(customers.size()));
                Project randomProject = projects.get(random.nextInt(projects.size()));
                
                // 确保创建时间在当月内
                int dayOfMonth = random.nextInt(1, yearMonth.lengthOfMonth() + 1);
                LocalDateTime eventTime = date.withDayOfMonth(dayOfMonth).atTime(random.nextInt(24), random.nextInt(60));

                bindings.add(new Binding()
                        .setId(UUID.randomUUID().toString().substring(0, 30))
                        .setCustomerId(randomCustomer.getId())
                        .setProjectId(randomProject.getId())
                        .setDelFlag(i % 4 == 0 ? "1" : "0") // 大约1/4是解绑
                        .setCreateTime(eventTime)
                        .setUpdateTime(eventTime)
                        .setCreatedBy("mimic-data-generator")
                        .setUpdateBy("mimic-data-generator")
                );
            }
        }
        return bindings;
    }


    private void batchInsertCustomers(List<Customer> customers) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_customer (id, customer_name, customer_code, customer_type, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                customers,
                100,
                (ps, customer) -> {
                    ps.setString(1, customer.getId());
                    ps.setString(2, customer.getCustomerName());
                    ps.setString(3, customer.getCustomerCode());
                    ps.setString(4, customer.getCustomerType());
                    ps.setString(5, customer.getDelFlag());
                    ps.setString(6, customer.getCreatedBy());
                    ps.setObject(7, customer.getCreateTime());
                    ps.setString(8, customer.getUpdateBy());
                    ps.setObject(9, customer.getUpdateTime());
                });
    }

    private void batchInsertProjects(List<Project> projects) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_project (id, project_name, project_code, project_type, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                projects,
                100,
                (ps, project) -> {
                    ps.setString(1, project.getId());
                    ps.setString(2, project.getProjectName());
                    ps.setString(3, project.getProjectCode());
                    ps.setString(4, project.getProjectType());
                    ps.setString(5, project.getDelFlag());
                    ps.setString(6, project.getCreatedBy());
                    ps.setObject(7, project.getCreateTime());
                    ps.setString(8, project.getUpdateBy());
                    ps.setObject(9, project.getUpdateTime());
                });
    }

    private void batchInsertBindings(List<Binding> bindings) {
        jdbcTemplate.batchUpdate("INSERT INTO bip_binding (id, customer_id, project_id, del_flag, created_by, create_time, update_by, update_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                bindings,
                100,
                (ps, binding) -> {
                    ps.setString(1, binding.getId());
                    ps.setString(2, binding.getCustomerId());
                    ps.setString(3, binding.getProjectId());
                    ps.setString(4, binding.getDelFlag());
                    ps.setString(5, binding.getCreatedBy());
                    ps.setObject(6, binding.getCreateTime());
                    ps.setString(7, binding.getUpdateBy());
                    ps.setObject(8, binding.getUpdateTime());
                });
    }
}
