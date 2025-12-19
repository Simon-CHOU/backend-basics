package com.example.chain.boot;

import java.util.UUID;

public abstract class ApprovalHandler {
    protected ApprovalHandler next;

    public void setNext(ApprovalHandler next) {
        this.next = next;
    }

    public ApprovalResponse handle(ApprovalRequest request) {
        if (canHandle(request)) {
            return approve(request);
        } else if (next != null) {
            return next.handle(request);
        } else {
            return ApprovalResponse.builder()
                    .success(false)
                    .status("rejected")
                    .message("Amount too large, no one can approve")
                    .build();
        }
    }

    protected abstract boolean canHandle(ApprovalRequest request);
    protected abstract ApprovalResponse approve(ApprovalRequest request);

    protected ApprovalResponse createApprovedResponse(String approver) {
        return ApprovalResponse.builder()
                .success(true)
                .approvalId(UUID.randomUUID().toString())
                .status("approved")
                .message("Approved by " + approver)
                .build();
    }
}
