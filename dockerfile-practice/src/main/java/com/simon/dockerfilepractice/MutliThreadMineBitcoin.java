package com.simon.dockerfilepractice;

public class MutliThreadMineBitcoin {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 100000000; j++) {
                        System.out.println("Thread " + Thread.currentThread().getName() + " is running");
                    }
                }
            }, "Thread-" + i).start();
        }
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 100000000; j++) {
                        System.out.println("Thread " + Thread.currentThread().getName() + " is running");
                    }
                }
            }, "Thread-" + i).start();
        }
    }




}
