package com.simon.callicoder.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

class ReentrantLockMethodsCounter {
    private final ReentrantLock lock = new ReentrantLock();

    private int count = 0;

    public int incrementAndGet() {
        // Check if the lock is currently acquired by any thread
        System.out.println(Thread.currentThread().getName()+ " " +"IsLocked : " + lock.isLocked());

        // Check if the lock is acquired by the current thread itself.
        System.out.println(Thread.currentThread().getName()+ " " +"IsHeldByCurrentThread : " + lock.isHeldByCurrentThread());
        boolean isAcquired = lock.tryLock(); // lock.tryLock(1, TimeUnit.SECONDS); //timeout
        System.out.println(Thread.currentThread().getName()+ " " +"Lock Acquired : " + isAcquired  + "\n");

        if (isAcquired) {
            try {
                Thread.sleep(2000);
                count = count + 1;
            } catch (InterruptedException e) {
                throw new IllegalStateException();
            } finally {
                lock.unlock();
            }
        }
        return count;
    }
}

public class ReentrantLockMethodsExample {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        ReentrantLockMethodsCounter lockMethodsCounter = new ReentrantLockMethodsCounter();

        executorService.submit(()->{
            System.out.println("IncrementCount (First Thread) : " + lockMethodsCounter.incrementAndGet()+"\n");
        });
        executorService.submit(()->{
            System.out.println("IncrementCount (Second Thread) : " + lockMethodsCounter.incrementAndGet() +"\n");
        });

        executorService.shutdown();


//        pool-1-thread-1 IsLocked : false
//        pool-1-thread-2 IsLocked : false
//        pool-1-thread-2 IsHeldByCurrentThread : false
//        pool-1-thread-1 IsHeldByCurrentThread : false
//        pool-1-thread-1 Lock Acquired : false
//
//        pool-1-thread-2 Lock Acquired : true
//
//        IncrementCount (First Thread) : 0
//
//        IncrementCount (Second Thread) : 1
    }
}