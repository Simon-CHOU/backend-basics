package com.simon.callicoder.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock is a mutually exclusive lock 互斥锁
 * 效果和 synchronized 一样的
 */
public class ReentrantLockCounter {
    private final ReentrantLock lock = new ReentrantLock();

    private int count = 0;

    // Thread Safe Increment
    public void increment() {
        lock.lock();
        try {
            count = count + 1;
        } finally { //finally块确保即使发生异常也会释放锁
            lock.unlock();
        }
    }
}
