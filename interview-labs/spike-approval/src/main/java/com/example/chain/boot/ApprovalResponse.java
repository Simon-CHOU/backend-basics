package com.example.chain.boot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalResponse {
    private String approvedBy;
    private ApprovalStatus status;
}
