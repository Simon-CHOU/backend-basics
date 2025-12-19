package com.example.chain.standard;

public class AdvancedSupportHandler extends SupportHandler {

    public AdvancedSupportHandler(SupportHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.getLevel() == SupportLevel.ADVANCED;
    }

    @Override
    protected String process(SupportRequest request) {
        return "AdvancedSupport: Handled " + request.getContent();
    }
}
