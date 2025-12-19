package com.example.chain.boot;

import org.springframework.stereotype.Component;

@Component
public class DepartmentManager extends ApprovalHandler {
    @Override
    protected boolean canHandle(ApprovalRequest request) {
        return request.getAmount() < 5000;
    }

    @Override
    protected ApprovalResponse approve(ApprovalRequest request) {
        return new ApprovalResponse("Department Manager", ApprovalStatus.APPROVED);
    }
}
