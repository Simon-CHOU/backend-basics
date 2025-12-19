package com.example.chain.standard;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class SupportHandler {
    protected SupportHandler nextHandler;

    public String handleRequest(SupportRequest request) {
        if (canHandle(request)) {
            return process(request);
        } else if (nextHandler != null) {
            return nextHandler.handleRequest(request);
        } else {
            return "Request cannot be handled";
        }
    }

    protected abstract boolean canHandle(SupportRequest request);
    protected abstract String process(SupportRequest request);
}
