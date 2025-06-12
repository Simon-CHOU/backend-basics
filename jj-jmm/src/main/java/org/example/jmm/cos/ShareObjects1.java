package org.example.jmm.cos;

import org.example.jmm.MyObject;
import org.example.jmm.cos.MyRunnable1;

public class ShareObjects1 {
    public static void main(String[] args) {
        MyObject myObject = new MyObject();
        MyRunnable1 runnable = new MyRunnable1(myObject);
        Thread thread1 = new Thread(runnable, "Thread 1");
        Thread thread2= new Thread(runnable, "Thread 2");
        thread1.start();
        thread2.start();
        //org.example.jmm.MyObject@7f33c15b
        //org.example.jmm.MyObject@7f33c15b
        //Thread 1:1470987
        //Thread 2:1470987
    }
}
