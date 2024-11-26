package com.simon.callicoder.chm;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 编写一个Java例程，证明 ConcurrentHashMap 不能保证强一致性？
 */
public class CHMapProof {
    private static final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 1000;

    public static void main(String[] args) throws InterruptedException {
        // 创建多个线程同时进行读写操作
        Thread[] threads = new Thread[THREAD_COUNT];
        
        // 初始值设为0
        map.put("counter", 0);
        
        // 创建多个线程，每个线程都进行递增操作
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    // 读取当前值
                    Integer currentValue = map.get("counter");
                    // 尝试更新为新值
                    map.replace("counter", currentValue, currentValue + 1);
                    
                    // 模拟其他操作的耗时
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 理论上的最终结果应该是 THREAD_COUNT * OPERATIONS_PER_THREAD
        System.out.println("预期结果: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println("实际结果: " + map.get("counter"));
    }
}    