package com.simon.callicoder.exec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executor  execute()方法，可执行Runnable
 * ExecutorService Executor的子类，有submit()方法，可以执行 Callable（有返回值的Runnable）
 * ScheduledExecutorService 增加了schedule功能的ExecutorService
 *
 * Executors 包含创建ExecutorService的工厂方法
 *
 * https://www.callicoder.com/java-executor-service-and-thread-pool-tutorial/
 */
public class ExecutorsExample {
    public static void main(String[] args) {
        System.out.println("Inside : " + Thread.currentThread().getName());

        System.out.println("Creating Executor Service...");
        ExecutorService executorService = Executors.newSingleThreadExecutor();//只有1个worker线程，执行1个task，下一个排队等

        System.out.println("Creating a Runnable...");
        Runnable runnable = ()->{
            System.out.println("Inside : " + Thread.currentThread().getName());
        };

        System.out.println("Submit the task specified by the runnable to the executor service");
        executorService.submit(runnable);

        System.out.println("Shutting down the executor");
        executorService.shutdown(); // .shutdownNow() 立刻关闭
    }
}
