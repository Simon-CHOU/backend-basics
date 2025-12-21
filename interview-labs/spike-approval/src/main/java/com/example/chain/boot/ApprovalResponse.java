package com.example.chain.boot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalResponse {
    private boolean success;
    private String approvalId;
    private String status; // "approved" or "rejected"
    private String message;
    private String approver;
}
