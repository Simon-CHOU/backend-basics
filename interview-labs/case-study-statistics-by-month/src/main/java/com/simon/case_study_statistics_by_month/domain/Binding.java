package com.simon.case_study_statistics_by_month.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Binding {
    private String id;
    private String customerId;
    private String projectId;
    private String delFlag;
    private String createdBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}