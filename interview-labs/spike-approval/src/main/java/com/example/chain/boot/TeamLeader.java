package com.example.chain.boot;

import org.springframework.stereotype.Component;

@Component
public class TeamLeader extends ApprovalHandler {
    @Override
    protected boolean canHandle(ApprovalRequest request) {
        return request.getAmount() < 1000;
    }

    @Override
    protected ApprovalResponse approve(ApprovalRequest request) {
        return new ApprovalResponse("Team Leader", ApprovalStatus.APPROVED);
    }
}
