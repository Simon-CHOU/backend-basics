package com.example.chain.boot;

import org.springframework.stereotype.Component;

@Component
public class CEO extends ApprovalHandler {
    @Override
    protected boolean canHandle(ApprovalRequest request) {
        return true; // CEO handles everything else
    }

    @Override
    protected ApprovalResponse approve(ApprovalRequest request) {
        return createApprovedResponse("CEO");
    }
}
