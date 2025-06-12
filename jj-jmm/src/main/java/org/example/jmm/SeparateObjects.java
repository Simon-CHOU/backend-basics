package org.example.jmm;

public class SeparateObjects {
    public static void main(String[] args) {
        Runnable runnable1 = new MyRunnable();
        Runnable runnable2 = new MyRunnable();
        Thread thread1 = new Thread(runnable1, "Thread 1");
        Thread thread2= new Thread(runnable2, "Thread 2");
        thread1.start();
        thread2.start();
        //org.example.jmm.MyObject@69cff4eb
        //org.example.jmm.MyObject@3650e4f3 //两个不同的runnable，生成了2个不同的myObject对象示例
        //Thread 2:1000000
        //Thread 1:1000000
    }
}
