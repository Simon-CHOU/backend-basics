package com.simon.callicoder.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 常用的Atomic类
 * AtomicInteger
 * AtomicBoolean
 * AtomicLong
 * AtomicReference
 * ...
 */
class AtomicCounter {
    private AtomicInteger count = new AtomicInteger();

    public int incrementAndGet() {
        return count.incrementAndGet(); // 线程安全的
    }

    public int getCount() {
        return count.get();
    }
}

/**
 * 您应该尽可能使用这些原子类而不是同步关键字和锁，因为它们更快、更易于使用、可读性和可扩展性。
 */
public class AtomicIntegerExample {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        AtomicCounter atomicCounter = new AtomicCounter();

        for (int i = 0; i < 1000; i++) {
            executorService.submit(() -> {
                atomicCounter.incrementAndGet();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println("Final Count is : " + atomicCounter.getCount());
    }
}
