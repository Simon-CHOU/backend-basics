package org.example.jmm.cos;

import org.example.jmm.MyObject;

public class MyRunnable1 implements Runnable {
    private int count = 0;
    MyObject myObject = null;

    public MyRunnable1() {
    }

    public MyRunnable1(MyObject myObject) {
        this.myObject = myObject;
    }

    @Override
    public void run() {
//        MyObject myObject = new MyObject();
        System.out.println(myObject);
        for (int i = 0; i < 1_000_000; i++) {
            this.count++;
        }
        System.out.println(Thread.currentThread().getName() + ":" + this.count);
    }
}
