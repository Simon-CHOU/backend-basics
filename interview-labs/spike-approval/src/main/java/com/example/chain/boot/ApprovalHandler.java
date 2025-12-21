package com.example.chain.boot;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ApprovalHandler {
    protected static final Logger log = LoggerFactory.getLogger(ApprovalHandler.class);
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
            log.warn("Request rejected: Amount {} too large, no one can approve", request.getAmount());
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
        String approvalId = UUID.randomUUID().toString();
        log.info("Request approved by {} (ID: {})", approver, approvalId);
        return ApprovalResponse.builder()
                .success(true)
                .approvalId(approvalId)
                .status("approved")
                .message("Approved by " + approver)
                .approver(approver)
                .build();
    }
}
