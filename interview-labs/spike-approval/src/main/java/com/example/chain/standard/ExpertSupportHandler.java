package com.example.chain.standard;

public class ExpertSupportHandler extends SupportHandler {

    public ExpertSupportHandler(SupportHandler nextHandler) {
        super(nextHandler);
    }

    @Override
    protected boolean canHandle(SupportRequest request) {
        return request.getLevel() == SupportLevel.EXPERT;
    }

    @Override
    protected String process(SupportRequest request) {
        return "ExpertSupport: Handled " + request.getContent();
    }
}
