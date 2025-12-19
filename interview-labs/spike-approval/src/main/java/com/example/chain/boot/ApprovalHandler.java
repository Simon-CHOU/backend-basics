package com.example.chain.boot;

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
            return new ApprovalResponse("None", ApprovalStatus.REJECTED);
        }
    }

    protected abstract boolean canHandle(ApprovalRequest request);
    protected abstract ApprovalResponse approve(ApprovalRequest request);
}
