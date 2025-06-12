package org.example.jmm;

public class ShareObjects {
    public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();
        Thread thread1 = new Thread(runnable, "Thread 1");
        Thread thread2= new Thread(runnable, "Thread 2");
        thread1.start();
        thread2.start();
        //org.example.jmm.MyObject@30845ed7
        //org.example.jmm.MyObject@2c212e9b // 说明两个线程中的myObject对象是同一个对象，即便共享同一个runnable对象，但是两个线程中的myObject对象是同一个对象
        //Thread 2:1336148
        //Thread 1:1336148
    }
}
