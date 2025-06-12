package org.example.jmm.cos;

import org.example.jmm.MyObject;
import org.example.jmm.cos.MyRunnable1;

public class SeparateObjects1 {
    public static void main(String[] args) {
        MyObject object = new MyObject();
        Runnable runnable1 = new MyRunnable1(object);
        Runnable runnable2 = new MyRunnable1(object);
        Thread thread1 = new Thread(runnable1, "Thread 1");
        Thread thread2= new Thread(runnable2, "Thread 2");
        thread1.start();
        thread2.start();
        //org.example.jmm.MyObject@5e9e0933
        //org.example.jmm.MyObject@5e9e0933
        //Thread 2:1000000
        //Thread 1:1000000
        // 共享了
    }
}
