package com.simon.case_study_statistics_by_month.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.simon.case_study_statistics_by_month.domain.enums.ProjectType;

@Data
@Accessors(chain = true)
public class Project {
    private String id;
    private String projectName;
    private String projectCode;
    private ProjectType projectType;
    private String delFlag;
    private String createdBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
