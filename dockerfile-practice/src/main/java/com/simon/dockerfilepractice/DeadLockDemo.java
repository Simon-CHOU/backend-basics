package com.simon.dockerfilepractice;

public class DeadLockDemo {
    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (DeadLockDemo.class) {
                System.out.println("Thread 1 get lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (DeadLockDemo.class) {
                    System.out.println("Thread 2 get lock");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (DeadLockDemo.class) {
                System.out.println("Thread 3 get lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (DeadLockDemo.class) {
                    System.out.println("Thread 4 get lock");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (DeadLockDemo.class) {
                System.out.println("Thread 5 get lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (DeadLockDemo.class) {
                    System.out.println("Thread 6 get lock");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (DeadLockDemo.class) {
                System.out.println("Thread 7 get lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (DeadLockDemo.class) {
                    System.out.println("Thread 8 get lock");
                }
            }
        }).start();

        new Thread(() -> {
            synchronized (DeadLockDemo.class) {
                System.out.println("Thread 9 get lock");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (DeadLockDemo.class) {
                    System.out.println("Thread 10 get lock");
                }
            }
        }).start();
    }
}
