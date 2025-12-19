package com.example.chain.standard;

public class BasicSupportHandler extends SupportHandler {

    public BasicSupportHandler(SupportHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.getLevel() == SupportLevel.BASIC;
    }

    @Override
    protected String process(SupportRequest request) {
        return "BasicSupport: Handled " + request.getContent();
    }
}
