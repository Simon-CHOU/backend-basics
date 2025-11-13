package com.simon.case_study_statistics_by_month.domain.enums;

/**
 * 客户类型枚举
 * 与数据库中的 customer_type 字段值保持一致（例如：Type0、Type1）。
 */
public enum CustomerType {
    PROACTIVE("Type0", "主动型"),
    COOPERATIVE("Type1", "配合型");

    private final String code;
    private final String label;

    CustomerType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static CustomerType fromCode(String code) {
        for (CustomerType ct : values()) {
            if (ct.code.equals(code)) {
                return ct;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerType code: " + code);
    }
}