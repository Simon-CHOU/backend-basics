package com.simon.case_study_statistics_by_month.domain;

import java.time.LocalDateTime;

/**
 * 统计查询结果DTO
 */
public class StatisticsResult {
    private String yyyyMm;
    private Integer proactiveCustomerCount;
    private Integer cooperativeCustomerCount;
    private Integer unbindingCount;
    private Integer totalProjectCount;
    private Integer activeProjectCount;
    private Integer bfoProjectCount;
    private LocalDateTime createTime;

    // 构造函数
    public StatisticsResult() {}

    public StatisticsResult(String yyyyMm, Integer proactiveCustomerCount, Integer cooperativeCustomerCount, 
                          Integer unbindingCount, Integer totalProjectCount, Integer activeProjectCount, 
                          Integer bfoProjectCount) {
        this.yyyyMm = yyyyMm;
        this.proactiveCustomerCount = proactiveCustomerCount;
        this.cooperativeCustomerCount = cooperativeCustomerCount;
        this.unbindingCount = unbindingCount;
        this.totalProjectCount = totalProjectCount;
        this.activeProjectCount = activeProjectCount;
        this.bfoProjectCount = bfoProjectCount;
        this.createTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public String getYyyyMm() { return yyyyMm; }
    public void setYyyyMm(String yyyyMm) { this.yyyyMm = yyyyMm; }

    public Integer getProactiveCustomerCount() { return proactiveCustomerCount; }
    public void setProactiveCustomerCount(Integer proactiveCustomerCount) { this.proactiveCustomerCount = proactiveCustomerCount; }

    public Integer getCooperativeCustomerCount() { return cooperativeCustomerCount; }
    public void setCooperativeCustomerCount(Integer cooperativeCustomerCount) { this.cooperativeCustomerCount = cooperativeCustomerCount; }

    public Integer getUnbindingCount() { return unbindingCount; }
    public void setUnbindingCount(Integer unbindingCount) { this.unbindingCount = unbindingCount; }

    public Integer getTotalProjectCount() { return totalProjectCount; }
    public void setTotalProjectCount(Integer totalProjectCount) { this.totalProjectCount = totalProjectCount; }

    public Integer getActiveProjectCount() { return activeProjectCount; }
    public void setActiveProjectCount(Integer activeProjectCount) { this.activeProjectCount = activeProjectCount; }

    public Integer getBfoProjectCount() { return bfoProjectCount; }
    public void setBfoProjectCount(Integer bfoProjectCount) { this.bfoProjectCount = bfoProjectCount; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "StatisticsResult{" +
                "yyyyMm='" + yyyyMm + '\'' +
                ", proactiveCustomerCount=" + proactiveCustomerCount +
                ", cooperativeCustomerCount=" + cooperativeCustomerCount +
                ", unbindingCount=" + unbindingCount +
                ", totalProjectCount=" + totalProjectCount +
                ", activeProjectCount=" + activeProjectCount +
                ", bfoProjectCount=" + bfoProjectCount +
                ", createTime=" + createTime +
                '}';
    }
}