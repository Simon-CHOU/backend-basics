package com.simon.callicoder.thead;

/**
 * Thread vs Runnable
 * 结论：该用Runnable
 * Java 单继承，继承了Thread不能继承别的
 * 继承：扩展父类的功能。但是如果继承Thread，我只是想用run()方法。继承这种用法就不合理。
 * 综上，用 Runnable（而且Runnable Lambda创建可以让代码更紧凑）
 *
 * ref: https://www.callicoder.com/java-multithreading-thread-and-runnable-tutorial/
 */
public class ThreadExample extends Thread {

    @Override
    public void run() {
        System.out.println("Inside :" + Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        System.out.println("Inside :" + Thread.currentThread().getName());

        System.out.println("Create thread...");
        ThreadExample thread = new ThreadExample();

        System.out.println("Starting thread...");
        thread.start();
    }
}
