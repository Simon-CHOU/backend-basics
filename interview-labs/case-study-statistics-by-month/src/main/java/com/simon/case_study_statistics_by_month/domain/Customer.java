package com.simon.case_study_statistics_by_month.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import com.simon.case_study_statistics_by_month.domain.enums.CustomerType;

@Data
@Accessors(chain = true)
public class Customer {
    private String id;
    private String customerName;
    private String customerCode;
    private CustomerType customerType;
    private String delFlag;
    private String createdBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
