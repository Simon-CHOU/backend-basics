package com.simon.threadlocal.practice;

public class Process {
    private ThreadLocal<String> threadLocal = new ThreadLocal();

    public void something() {
        threadLocal.set("A thread local value");
    }
    public String display() {
        String res = threadLocal.get();
        threadLocal.remove();
        return res;
    }
}
