package com.simon.callicoder.sync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In case of static methods, synchronization is associated with the Class object.
 * 对于静态方法，同步与Class对象相关联。
 */
class SynchronizedCounter {
    private int count = 0;
    /**
     * synchronized关键字确保在同一时间只有一个线程可以输入increment()方法。
     * The synchronized keyword makes sure that only one thread can enter the increment() method at one time.
     */
    public synchronized void increment() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
public class SynchronizedMethodExample {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();

        for(int i = 0; i< 1000; i++) {
            executorService.submit(()->{ synchronizedCounter.increment();});
        }

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println("Final count is : " + synchronizedCounter.getCount());

        // 1000
    }
}
