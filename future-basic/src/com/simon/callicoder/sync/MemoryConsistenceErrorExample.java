package com.simon.callicoder.sync;

/**
 *  第一个线程不知道主线程对sayHello变量所做的更改。
 *  The first thread is unaware of the changes done by the main thread to the sayHello variable.
 *  您可以使用volatile关键字来避免内存一致性错误
 *  You can use volatile keyword to avoid memory consistency errors.
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
