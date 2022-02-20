package com.simon.callicoder.lock;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReadWriteLock 包含读锁，写锁
 * 当写锁没有线程持有时，读锁可以被多个线程持有
 * 读多写少的场景下有更好的性能
 *
 * ... multiple threads can execute the getCount() method as long as no thread calls incrementAndGetCount().
 * If any thread calls incrementAndGetCount() method and acquires the write-lock,
 * then all the reader threads will pause their execution and wait for the writer thread to return.
 */
public class ReadWriteCounter {
    ReadWriteLock lock = new ReentrantReadWriteLock();

    private int count = 0;

    public int incrementAndGetCount() {
        lock.writeLock().lock();

        try {
            count++;
            return count;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getCount() {
        lock.readLock().lock();
        try {
            return count;
        } finally {
            lock.readLock().unlock();
        }
    }

}
