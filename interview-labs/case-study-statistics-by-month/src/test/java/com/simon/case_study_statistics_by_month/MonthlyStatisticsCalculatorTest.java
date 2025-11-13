package com.simon.case_study_statistics_by_month;

import com.simon.case_study_statistics_by_month.domain.Binding;
import com.simon.case_study_statistics_by_month.domain.Customer;
import com.simon.case_study_statistics_by_month.domain.Project;
import com.simon.case_study_statistics_by_month.domain.StatisticsResult;
import com.simon.case_study_statistics_by_month.domain.enums.CustomerType;
import com.simon.case_study_statistics_by_month.domain.enums.ProjectType;
import com.simon.case_study_statistics_by_month.service.MonthlyStatisticsCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MonthlyStatisticsCalculatorTest {

    @Test
    void emptyInputReturnsEmpty() {
        MonthlyStatisticsCalculator calc = new MonthlyStatisticsCalculator();
        List<StatisticsResult> out = calc.calculate(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                CustomerType.PROACTIVE.getCode(), CustomerType.COOPERATIVE.getCode(), ProjectType.BFO.getCode());
        Assertions.assertTrue(out.isEmpty());
    }

    @Test
    void minimalSampleAggregationAndCumulative() {
        List<Customer> customers = new ArrayList<>();
        List<Project> projects = new ArrayList<>();
        List<Binding> bindings = new ArrayList<>();

        customers.add(new Customer().setId("CUSTA").setCustomerName("客户A").setCustomerType(CustomerType.PROACTIVE).setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 11, 10, 10, 0)));
        customers.add(new Customer().setId("CUSTB").setCustomerName("客户B").setCustomerType(CustomerType.COOPERATIVE).setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 12, 5, 9, 0)));

        projects.add(new Project().setId("PROJ1").setProjectName("项目1").setProjectType(ProjectType.BFO).setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 11, 12, 12, 0)));
        projects.add(new Project().setId("PROJ2").setProjectName("项目2").setProjectType(ProjectType.STANDARD).setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2025, 1, 3, 8, 30)));

        bindings.add(new Binding().setId("BIND1").setCustomerId("CUSTA").setProjectId("PROJ1").setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 11, 15, 13, 0)));
        bindings.add(new Binding().setId("BIND2").setCustomerId("CUSTA").setProjectId("PROJ2").setDelFlag("1")
                .setCreateTime(LocalDateTime.of(2025, 2, 1, 10, 0)));
        bindings.add(new Binding().setId("BIND3").setCustomerId("CUSTB").setProjectId("PROJ1").setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 12, 20, 16, 0)));

        MonthlyStatisticsCalculator calc = new MonthlyStatisticsCalculator();
        List<StatisticsResult> out = calc.calculate(customers, projects, bindings,
                CustomerType.PROACTIVE.getCode(), CustomerType.COOPERATIVE.getCode(), ProjectType.BFO.getCode());

        Assertions.assertFalse(out.isEmpty());
        // 客户A的 4 个月累计
        StatisticsResult a11 = find(out, "CUSTA", "2024-11");
        StatisticsResult a12 = find(out, "CUSTA", "2024-12");
        StatisticsResult a01 = find(out, "CUSTA", "2025-01");
        StatisticsResult a02 = find(out, "CUSTA", "2025-02");
        Assertions.assertEquals(1L, a11.getProactiveCustomerCount());
        Assertions.assertEquals(1L, a12.getProactiveCustomerCount());
        Assertions.assertEquals(1L, a01.getProactiveCustomerCount());
        Assertions.assertEquals(1L, a02.getProactiveCustomerCount());
        Assertions.assertEquals(0L, a11.getCooperativeCustomerCount());
        Assertions.assertEquals(0L, a12.getCooperativeCustomerCount());
        Assertions.assertEquals(0L, a01.getCooperativeCustomerCount());
        Assertions.assertEquals(0L, a02.getCooperativeCustomerCount());
        Assertions.assertEquals(0L, a11.getUnbindingCount());
        Assertions.assertEquals(0L, a12.getUnbindingCount());
        Assertions.assertEquals(0L, a01.getUnbindingCount());
        Assertions.assertEquals(1L, a02.getUnbindingCount());
        Assertions.assertEquals(1L, a11.getTotalProjectCount());
        Assertions.assertEquals(1L, a12.getTotalProjectCount());
        Assertions.assertEquals(2L, a01.getTotalProjectCount());
        Assertions.assertEquals(2L, a02.getTotalProjectCount());
        Assertions.assertEquals(1L, a11.getActiveProjectCount());
        Assertions.assertEquals(1L, a12.getActiveProjectCount());
        Assertions.assertEquals(2L, a01.getActiveProjectCount());
        Assertions.assertEquals(2L, a02.getActiveProjectCount());
        Assertions.assertEquals(1L, a11.getBfoProjectCount());
        Assertions.assertEquals(1L, a12.getBfoProjectCount());
        Assertions.assertEquals(1L, a01.getBfoProjectCount());
        Assertions.assertEquals(1L, a02.getBfoProjectCount());

        // 客户B的 2 个月累计（含空月保持不变）
        StatisticsResult b11 = find(out, "CUSTB", "2024-11");
        StatisticsResult b12 = find(out, "CUSTB", "2024-12");
        StatisticsResult b01 = find(out, "CUSTB", "2025-01");
        StatisticsResult b02 = find(out, "CUSTB", "2025-02");
        Assertions.assertEquals(0L, b11.getProactiveCustomerCount());
        Assertions.assertEquals(0L, b12.getProactiveCustomerCount());
        Assertions.assertEquals(0L, b01.getProactiveCustomerCount());
        Assertions.assertEquals(0L, b02.getProactiveCustomerCount());
        Assertions.assertEquals(0L, b11.getCooperativeCustomerCount());
        Assertions.assertEquals(1L, b12.getCooperativeCustomerCount());
        Assertions.assertEquals(1L, b01.getCooperativeCustomerCount());
        Assertions.assertEquals(1L, b02.getCooperativeCustomerCount());
        Assertions.assertEquals(0L, b11.getUnbindingCount());
        Assertions.assertEquals(0L, b12.getUnbindingCount());
        Assertions.assertEquals(0L, b01.getUnbindingCount());
        Assertions.assertEquals(0L, b02.getUnbindingCount());
        Assertions.assertEquals(1L, b11.getTotalProjectCount());
        Assertions.assertEquals(1L, b12.getTotalProjectCount());
        Assertions.assertEquals(1L, b01.getTotalProjectCount());
        Assertions.assertEquals(1L, b02.getTotalProjectCount());
        Assertions.assertEquals(1L, b11.getActiveProjectCount());
        Assertions.assertEquals(1L, b12.getActiveProjectCount());
        Assertions.assertEquals(1L, b01.getActiveProjectCount());
        Assertions.assertEquals(1L, b02.getActiveProjectCount());
        Assertions.assertEquals(1L, b11.getBfoProjectCount());
        Assertions.assertEquals(1L, b12.getBfoProjectCount());
        Assertions.assertEquals(1L, b01.getBfoProjectCount());
        Assertions.assertEquals(1L, b02.getBfoProjectCount());
    }

    @Test
    void filtersAndNullsAndUnionCoverage() {
        List<Customer> customers = new ArrayList<>();
        List<Project> projects = new ArrayList<>();
        List<Binding> bindings = new ArrayList<>();

        customers.add(new Customer().setId("C1").setCustomerName(null).setCustomerType(CustomerType.PROACTIVE).setDelFlag("1")
                .setCreateTime(LocalDateTime.of(2024, 11, 1, 0, 0))); // del_flag=1 不计数
        customers.add(new Customer().setId("C1").setCustomerName("A").setCustomerType(CustomerType.COOPERATIVE).setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 12, 1, 0, 0))); // cooperative 新增
        customers.add(new Customer().setId("C2").setCustomerName("B").setCustomerType(null).setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2025, 1, 1, 0, 0))); // 无类型不计数
        customers.add(new Customer().setId("C2").setCustomerName("C").setCustomerType(CustomerType.PROACTIVE).setDelFlag("0")
                .setCreateTime(null)); // null createTime 覆盖 addMonth 守卫

        projects.add(new Project().setId("P1").setProjectName("P1").setProjectType(ProjectType.BFO).setDelFlag("1")
                .setCreateTime(LocalDateTime.of(2024, 11, 1, 0, 0))); // 非有效项目
        projects.add(new Project().setId("P2").setProjectName("P2").setProjectType(null).setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 11, 1, 0, 0))); // 无类型不计 BFO

        bindings.add(new Binding().setId("B1").setCustomerId("C1").setProjectId("P1").setDelFlag("1")
                .setCreateTime(LocalDateTime.of(2024, 11, 2, 0, 0))); // 解绑当月新增
        bindings.add(new Binding().setId("B2").setCustomerId("C1").setProjectId("P2").setDelFlag("0")
                .setCreateTime(null)); // null createTime 覆盖 addMonth 守卫
        bindings.add(new Binding().setId("B3").setCustomerId("C2").setProjectId("P_missing").setDelFlag("0")
                .setCreateTime(LocalDateTime.of(2024, 11, 3, 0, 0))); // 缺失项目覆盖分支

        MonthlyStatisticsCalculator calc = new MonthlyStatisticsCalculator();
        List<StatisticsResult> out = calc.calculate(customers, projects, bindings,
                CustomerType.PROACTIVE.getCode(), CustomerType.COOPERATIVE.getCode(), ProjectType.BFO.getCode());

        // C1 的月份集合包含：客户(12 月)、绑定(11 月)、项目创建(11 月)
        StatisticsResult c1_11 = find(out, "C1", "2024-11");
        StatisticsResult c1_12 = find(out, "C1", "2024-12");
        Assertions.assertEquals(0L, c1_11.getProactiveCustomerCount());
        Assertions.assertEquals(1L, c1_12.getCooperativeCustomerCount());
        Assertions.assertEquals(1L, c1_11.getUnbindingCount());
        Assertions.assertEquals(2L, c1_11.getTotalProjectCount()); // P1/P2 均在 11 月创建并与 C1 绑定
        Assertions.assertEquals(1L, c1_11.getActiveProjectCount()); // 仅 P2 有效
        Assertions.assertEquals(1L, c1_11.getBfoProjectCount()); // 仅 P1 为 BFO

        // 排序与名称回退：无名客户按 id 回退
        Assertions.assertNotNull(out);
    }

    private StatisticsResult find(List<StatisticsResult> list, String rid, String ym) {
        return list.stream().filter(x -> rid.equals(x.getRetailerId()) && ym.equals(x.getYyyyMm())).findFirst().orElseThrow();
    }
}

