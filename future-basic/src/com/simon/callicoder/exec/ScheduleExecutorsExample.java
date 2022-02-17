package com.simon.callicoder.exec;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 线程池：线程创建开销大，重用线程可以减少开销
 * 任务通过阻塞队列（Blocking Queue）提交到线程池
 * 如果任务数大于线程数，则任务在blocking queue排队
 * 如果blocking queue满了，则新提交的任务会被reject
 *
 * submit tasks -> blocking queue -> thread pool
 */
public class ScheduleExecutorsExample {

    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            System.out.println("Executing Task At " + System.nanoTime());
        };

        System.out.println("Submitting task at " + System.nanoTime() + " to be executed after 5 seconds.");
        scheduledExecutorService.schedule(task , 5, TimeUnit.SECONDS); //延迟执行

        scheduledExecutorService.shutdown();
    }
}
