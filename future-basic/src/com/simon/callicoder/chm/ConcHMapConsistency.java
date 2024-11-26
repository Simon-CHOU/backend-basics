package com.simon.callicoder.chm;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 如果要让 ConcurrentHashMap 保证最终一致性，有哪些方法可以实现？请给我一个例程
 */
public class ConcHMapConsistency {
    private static final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 1000;

    public static void main(String[] args) throws InterruptedException {
        testUnsafeUpdate(); // 添加不安全的测试
        // 测试不同的实现方法
        testAtomicUpdate();
        testCompareAndSet();
        testSynchronizedBlock();
    }
    // 不保证一致性的方法
    private static void testUnsafeUpdate() throws InterruptedException {
        map.clear();
        map.put("counter", 0);
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // 不安全的更新方式
                    Integer currentValue = map.get("counter");
                    map.put("counter", currentValue + 1);

                    // 增加一些延迟使问题更明显
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("不安全方式 - 预期结果: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("不安全方式 - 实际结果: " + map.get("counter"));
        System.out.println("不安全方式 - 丢失的更新次数: " +
                (THREAD_COUNT * OPERATIONS_PER_THREAD - map.get("counter")));
    }

    // 方法1：使用atomic操作
    private static void testAtomicUpdate() throws InterruptedException {
        map.clear();
        map.put("counter", 0);
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // 使用atomic操作保证一致性
                    map.compute("counter", (k, v) -> v + 1);
                }
            });
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Atomic方式 - 预期结果: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("Atomic方式 - 实际结果: " + map.get("counter"));
    }

    // 方法2：使用CAS循环
    private static void testCompareAndSet() throws InterruptedException {
        map.clear();
        map.put("counter", 0);
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // 使用CAS循环直到成功
                    boolean updated = false;
                    while (!updated) {
                        Integer currentValue = map.get("counter");
                        updated = map.replace("counter", currentValue, currentValue + 1);
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("CAS方式 - 预期结果: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("CAS方式 - 实际结果: " + map.get("counter"));
    }

    // 方法3：使用synchronized
    private static void testSynchronizedBlock() throws InterruptedException {
        map.clear();
        map.put("counter", 0);
        Thread[] threads = new Thread[THREAD_COUNT];
        Object lock = new Object(); // 同步锁

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // 使用同步块保证一致性
                    synchronized (lock) {
                        Integer currentValue = map.get("counter");
                        map.put("counter", currentValue + 1);
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Synchronized方式 - 预期结果: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("Synchronized方式 - 实际结果: " + map.get("counter"));
    }
}
