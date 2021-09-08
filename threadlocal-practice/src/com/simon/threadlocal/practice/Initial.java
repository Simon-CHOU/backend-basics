package com.simon.threadlocal.practice;

public class Initial {
    private ThreadLocal<String> myThreadLocal =
            ThreadLocal.withInitial(() -> String.valueOf(System.currentTimeMillis()));
//    private ThreadLocal<String> myThreadLocal = new ThreadLocal<String>() {
//        @Override
//        protected String initialValue() {
//            return String.valueOf(System.currentTimeMillis());
//        }
//    };
}
