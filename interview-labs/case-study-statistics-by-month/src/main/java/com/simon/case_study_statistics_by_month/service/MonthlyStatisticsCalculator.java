package com.simon.case_study_statistics_by_month.service;

import com.simon.case_study_statistics_by_month.domain.Binding;
import com.simon.case_study_statistics_by_month.domain.Customer;
import com.simon.case_study_statistics_by_month.domain.Project;
import com.simon.case_study_statistics_by_month.domain.StatisticsResult;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MonthlyStatisticsCalculator {

    public List<StatisticsResult> calculate(List<Customer> customers,
                                            List<Project> projects,
                                            List<Binding> bindings,
                                            String proactiveCode,
                                            String cooperativeCode,
                                            String bfoProjectCode) {
        Map<String, String> retailerName = new HashMap<>();
        Map<String, List<String>> namesById = customers.stream().collect(Collectors.groupingBy(Customer::getId, Collectors.mapping(Customer::getCustomerName, Collectors.toList())));
        for (Map.Entry<String, List<String>> e : namesById.entrySet()) {
            String max = e.getValue().stream().filter(Objects::nonNull).max(String::compareTo).orElse(null);
            retailerName.put(e.getKey(), max);
        }

        Map<String, Project> projectById = projects.stream().collect(Collectors.toMap(Project::getId, p -> p, (a, b) -> a));

        Map<String, Set<YearMonth>> monthsByRetailer = new HashMap<>();
        for (Customer c : customers) {
            addMonth(monthsByRetailer, c.getId(), YearMonth.from(c.getCreateTime()));
        }
        for (Binding b : bindings) {
            addMonth(monthsByRetailer, b.getCustomerId(), YearMonth.from(b.getCreateTime()));
        }
        for (Binding b : bindings) {
            Project p = projectById.get(b.getProjectId());
            if (p != null) {
                addMonth(monthsByRetailer, b.getCustomerId(), YearMonth.from(p.getCreateTime()));
            }
        }

        List<String> retailerIds = new ArrayList<>();
        retailerIds.addAll(customers.stream().map(Customer::getId).collect(Collectors.toSet()));
        retailerIds.addAll(bindings.stream().map(Binding::getCustomerId).collect(Collectors.toSet()));
        retailerIds = retailerIds.stream().distinct().sorted().collect(Collectors.toList());

        List<StatisticsResult> results = new ArrayList<>();
        for (String rid : retailerIds) {
            List<YearMonth> months = monthsByRetailer.getOrDefault(rid, Collections.emptySet()).stream().sorted().collect(Collectors.toList());
            long pc = 0, cc = 0, uc = 0, tp = 0, ap = 0, bp = 0;
            for (YearMonth ym : months) {
                int newPc = 0;
                int newCc = 0;
                for (Customer c : customers) {
                    if (rid.equals(c.getId()) && "0".equals(c.getDelFlag()) && YearMonth.from(c.getCreateTime()).equals(ym)) {
                        if (c.getCustomerType() != null && proactiveCode.equals(c.getCustomerType().getCode())) newPc++;
                        if (c.getCustomerType() != null && cooperativeCode.equals(c.getCustomerType().getCode())) newCc++;
                    }
                }
                int newUc = 0;
                for (Binding b : bindings) {
                    if (rid.equals(b.getCustomerId()) && "1".equals(b.getDelFlag()) && YearMonth.from(b.getCreateTime()).equals(ym)) {
                        newUc++;
                    }
                }
                int newTp = 0, newAp = 0, newBp = 0;
                for (Binding b : bindings) {
                    if (!rid.equals(b.getCustomerId())) continue;
                    Project p = projectById.get(b.getProjectId());
                    if (p == null) continue;
                    if (!YearMonth.from(p.getCreateTime()).equals(ym)) continue;
                    newTp++;
                    if ("0".equals(p.getDelFlag())) newAp++;
                    if (p.getProjectType() != null && bfoProjectCode.equals(p.getProjectType().getCode())) newBp++;
                }
                pc += newPc; cc += newCc; uc += newUc; tp += newTp; ap += newAp; bp += newBp;
                String rname = retailerName.getOrDefault(rid, rid);
                StatisticsResult sr = new StatisticsResult(String.format("%04d-%02d", ym.getYear(), ym.getMonthValue()), rname, rid,
                        pc, cc, uc, tp, ap, bp, new Date());
                results.add(sr);
            }
        }
        results.sort(Comparator.comparing(StatisticsResult::getRetailerName, Comparator.nullsLast(String::compareTo))
                .thenComparing(StatisticsResult::getRetailerId)
                .thenComparing(StatisticsResult::getYyyyMm));
        return results;
    }

    private void addMonth(Map<String, Set<YearMonth>> map, String rid, YearMonth ym) {
        if (rid == null || ym == null) return;
        map.computeIfAbsent(rid, k -> new HashSet<>()).add(ym);
    }
}
