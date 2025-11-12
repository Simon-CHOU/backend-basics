package com.simon.case_study_statistics_by_month.domain;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 统计查询结果DTO
 */
public class StatisticsResult {

    private String yyyyMm;
    private String retailerName;
    private String retailerId;
    private Long proactiveCustomerCount;
    private Long cooperativeCustomerCount;
    private Long unbindingCount;
    private Long totalProjectCount;
    private Long activeProjectCount;
    private Long bfoProjectCount;
    private Date createTime;

    // 构造函数
    public StatisticsResult() {}

    public StatisticsResult(String yyyyMm, String retailerName, String retailerId, Long proactiveCustomerCount, Long cooperativeCustomerCount, Long unbindingCount, Long totalProjectCount, Long activeProjectCount, Long bfoProjectCount, Date createTime) {
        this.yyyyMm = yyyyMm;
        this.retailerName = retailerName;
        this.retailerId = retailerId;
        this.proactiveCustomerCount = proactiveCustomerCount;
        this.cooperativeCustomerCount = cooperativeCustomerCount;
        this.unbindingCount = unbindingCount;
        this.totalProjectCount = totalProjectCount;
        this.activeProjectCount = activeProjectCount;
        this.bfoProjectCount = bfoProjectCount;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    public String getYyyyMm() {
        return yyyyMm;
    }

    public void setYyyyMm(String yyyyMm) {
        this.yyyyMm = yyyyMm;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public Long getProactiveCustomerCount() {
        return proactiveCustomerCount;
    }
    public void setProactiveCustomerCount(Long proactiveCustomerCount) { this.proactiveCustomerCount = proactiveCustomerCount; }

    public Long getCooperativeCustomerCount() { return cooperativeCustomerCount; }
    public void setCooperativeCustomerCount(Long cooperativeCustomerCount) { this.cooperativeCustomerCount = cooperativeCustomerCount; }

    public Long getUnbindingCount() { return unbindingCount; }
    public void setUnbindingCount(Long unbindingCount) { this.unbindingCount = unbindingCount; }

    public Long getTotalProjectCount() { return totalProjectCount; }
    public void setTotalProjectCount(Long totalProjectCount) { this.totalProjectCount = totalProjectCount; }

    public Long getActiveProjectCount() { return activeProjectCount; }
    public void setActiveProjectCount(Long activeProjectCount) { this.activeProjectCount = activeProjectCount; }

    public Long getBfoProjectCount() { return bfoProjectCount; }
    public void setBfoProjectCount(Long bfoProjectCount) { this.bfoProjectCount = bfoProjectCount; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "StatisticsResult{" +
                "yyyyMm='" + yyyyMm + '\'' +
                ", retailerName='" + retailerName + '\'' +
                ", retailerId='" + retailerId + '\'' +
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