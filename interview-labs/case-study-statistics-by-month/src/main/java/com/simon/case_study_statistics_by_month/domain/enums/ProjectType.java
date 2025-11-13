package com.simon.case_study_statistics_by_month.domain.enums;

/**
 * 项目类型枚举
 * 与数据库中的 project_type 字段值保持一致（例如：Type0、Type1、Type2）。
 */
public enum ProjectType {
    BFO("Type0", "重要BFO"),
    STANDARD("Type1", "标准项目"),
    CUSTOM("Type2", "定制项目");

    private final String code;
    private final String label;

    ProjectType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ProjectType fromCode(String code) {
        for (ProjectType pt : values()) {
            if (pt.code.equals(code)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("Unknown ProjectType code: " + code);
    }
}