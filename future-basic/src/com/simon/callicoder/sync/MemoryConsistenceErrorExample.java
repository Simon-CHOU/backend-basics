package com.simon.callicoder.sync;

/**
 *  第一个线程不知道主线程对sayHello变量所做的更改。
 *  The first thread is unaware of the changes done by the main thread to the sayHello variable.
 *  您可以使用volatile关键字来避免内存一致性错误
 *  You can use volatile keyword to avoid memory consistency errors.
 *
 *  解决这个问题需要保证以下两个事情：
 *  1. 要么不同时写，操作原子化 atomic
 *  2. 要么做到修改可见性，一个线程对其他对其他线程是可见的
 *  synchronized 关键字可以一次性解决这两个问题
 */
public class MemoryConsistenceErrorExample {
    private static boolean sayHello = false;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            while (!sayHello) {
            }

            System.out.println("Hello world!");

            while (sayHello) {
            }

            System.out.println("Good Bye!");
        });

        thread.start();

        Thread.sleep(1000);
        System.out.println("Say Hello..");
        sayHello = true;

        Thread.sleep(1000);
        System.out.println("Say Bye..");
        sayHello = false;

        // 理想状态
//        Say Hello..
//        Hello World!
//        Say Bye..
//        Good Bye!

        //实际输出
//        Say Hello..
//        Say Bye..
        // 并且程序无法正常结束
    }
}
