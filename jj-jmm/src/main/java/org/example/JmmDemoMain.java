package org.example;

public class JmmDemoMain {
    
    // 简单的对象类
    static class MyObject {
        // 空类，用于演示对象创建和共享
    }
    
    // Runnable实现类1：每次run()都创建新的MyObject
    static class MyRunnable implements Runnable {
        private int count = 0;
        
        @Override
        public void run() {
            MyObject myObject = new MyObject(); // 每次运行都创建新对象
            System.out.println(Thread.currentThread().getName() + " - MyObject: " + myObject);
            for (int i = 0; i < 1_000_000; i++) {
                this.count++;
            }
            System.out.println(Thread.currentThread().getName() + " - Count: " + this.count);
        }
    }
    
    // Runnable实现类2：可以接受外部传入的MyObject
    static class MyRunnableWithSharedObject implements Runnable {
        private int count = 0;
        private MyObject myObject;
        
        public MyRunnableWithSharedObject(MyObject myObject) {
            this.myObject = myObject;
        }
        
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " - Shared MyObject: " + myObject);
            for (int i = 0; i < 1_000_000; i++) {
                this.count++;
            }
            System.out.println(Thread.currentThread().getName() + " - Count: " + this.count);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Java Memory Model 演示 ===");
        
        // 场景1：共享同一个Runnable对象，但每次run()创建新的MyObject
        System.out.println("\n场景1：共享Runnable对象，但MyObject在run()中创建");
        MyRunnable sharedRunnable = new MyRunnable();
        Thread thread1 = new Thread(sharedRunnable, "Thread-1");
        Thread thread2 = new Thread(sharedRunnable, "Thread-2");
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        System.out.println("结果：虽然共享Runnable对象，但MyObject地址不同（每次run()都创建新对象）");
        System.out.println("count值相同：因为两个线程共享同一个Runnable实例的count字段");
        
        Thread.sleep(100); // 稍作停顿
        
        // 场景2：不同的Runnable对象
        System.out.println("\n场景2：不同的Runnable对象");
        MyRunnable runnable1 = new MyRunnable();
        MyRunnable runnable2 = new MyRunnable();
        Thread thread3 = new Thread(runnable1, "Thread-3");
        Thread thread4 = new Thread(runnable2, "Thread-4");
        thread3.start();
        thread4.start();
        thread3.join();
        thread4.join();
        System.out.println("结果：不同Runnable对象，MyObject地址不同，count值都是1000000（各自独立）");
        
        Thread.sleep(100);
        
        // 场景3：共享同一个MyObject实例
        System.out.println("\n场景3：共享同一个MyObject实例");
        MyObject sharedObject = new MyObject();
        MyRunnableWithSharedObject runnableWithShared1 = new MyRunnableWithSharedObject(sharedObject);
        MyRunnableWithSharedObject runnableWithShared2 = new MyRunnableWithSharedObject(sharedObject);
        Thread thread5 = new Thread(runnableWithShared1, "Thread-5");
        Thread thread6 = new Thread(runnableWithShared2, "Thread-6");
        thread5.start();
        thread6.start();
        thread5.join();
        thread6.join();
        System.out.println("结果：MyObject地址相同（共享同一个对象实例）");
        
        Thread.sleep(100);
        
        // 场景4：共享Runnable对象和MyObject对象
        System.out.println("\n场景4：共享Runnable对象和MyObject对象");
        MyObject anotherSharedObject = new MyObject();
        MyRunnableWithSharedObject sharedRunnableWithObject = new MyRunnableWithSharedObject(anotherSharedObject);
        Thread thread7 = new Thread(sharedRunnableWithObject, "Thread-7");
        Thread thread8 = new Thread(sharedRunnableWithObject, "Thread-8");
        thread7.start();
        thread8.start();
        thread7.join();
        thread8.join();
        System.out.println("结果：MyObject地址相同，count值相同（共享Runnable实例的count字段）");
        
        System.out.println("\n=== JMM 原理总结 ===");
        System.out.println("1. 对象在堆内存中创建，线程栈中存储对象引用");
        System.out.println("2. 多个线程可以共享同一个对象实例（通过引用）");
        System.out.println("3. 每个线程有自己的栈空间，但可以访问共享的堆对象");
        System.out.println("4. 实例变量在对象中，被共享；局部变量在线程栈中，不被共享");
    }
}
