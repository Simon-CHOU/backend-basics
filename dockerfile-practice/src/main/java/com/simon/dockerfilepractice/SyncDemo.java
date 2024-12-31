package com.simon.dockerfilepractice;

public class SyncDemo {
    //实现一个分布式锁
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    //获取锁
                    synchronized (SyncDemo.class) {
                        System.out.println(Thread.currentThread().getName() + "获取到锁");
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName() + "释放锁");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "t" + i).start();
        }
        System.out.println("主线程执行完毕");
    }
}
