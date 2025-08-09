package com.simon.jcip;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 并发性能测试：原子整型 vs 同步整型
 * 
 * 测试目标：
 * 1. 比较AtomicInteger和synchronized int在不同竞争强度下的性能
 * 2. 分析线程数对性能的影响
 * 3. 生成CSV数据用于可视化分析
 * 
 * 基于Clean Code和TDD原则设计
 */
public class ConcurrencyPerformanceTest {
    
    // 测试配置常量
    private static final int[] THREAD_COUNTS = {1, 2, 4, 6, 8, 10, 12};
    private static final int[] CONTENTION_LEVELS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final int BASE_OPERATIONS = 100_000;
    private static final int WARMUP_ROUNDS = 3;
    private static final int TEST_ROUNDS = 5;
    
    // 测试结果数据结构
    private static class TestResult {
        final int threadCount;
        final int contentionLevel;
        final long atomicTime;
        final long synchronizedTime;
        final double speedupRatio;
        final String winner;
        
        TestResult(int threadCount, int contentionLevel, long atomicTime, long synchronizedTime) {
            this.threadCount = threadCount;
            this.contentionLevel = contentionLevel;
            this.atomicTime = atomicTime;
            this.synchronizedTime = synchronizedTime;
            this.speedupRatio = (double) synchronizedTime / atomicTime;
            this.winner = atomicTime < synchronizedTime ? "Atomic" : "Synchronized";
        }
        
        String toCsvRow() {
            return String.format("%d,%d,%d,%d,%.2f,%s", 
                threadCount, contentionLevel, atomicTime, synchronizedTime, speedupRatio, winner);
        }
    }
    
    // 原子整型测试器
    private static class AtomicCounter {
        private final AtomicInteger counter = new AtomicInteger(0);
        
        void increment() {
            counter.incrementAndGet();
        }
        
        int getValue() {
            return counter.get();
        }
        
        void reset() {
            counter.set(0);
        }
    }
    
    // 同步整型测试器
    private static class SynchronizedCounter {
        private int counter = 0;
        private final ReentrantLock lock = new ReentrantLock();
        
        void increment() {
            lock.lock();
            try {
                counter++;
            } finally {
                lock.unlock();
            }
        }
        
        int getValue() {
            lock.lock();
            try {
                return counter;
            } finally {
                lock.unlock();
            }
        }
        
        void reset() {
            lock.lock();
            try {
                counter = 0;
            } finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * 执行原子整型性能测试
     */
    private static long benchmarkAtomic(int threadCount, int operationsPerThread) throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        // 创建工作线程
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待统一开始信号
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        // 开始计时并启动所有线程
        long startTime = System.nanoTime();
        startLatch.countDown();
        endLatch.await();
        long endTime = System.nanoTime();
        
        // 验证结果正确性
        int expectedValue = threadCount * operationsPerThread;
        int actualValue = counter.getValue();
        if (actualValue != expectedValue) {
            throw new RuntimeException(String.format(
                "Atomic test failed: expected %d, got %d", expectedValue, actualValue));
        }
        
        return endTime - startTime;
    }
    
    /**
     * 执行同步整型性能测试
     */
    private static long benchmarkSynchronized(int threadCount, int operationsPerThread) throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        // 创建工作线程
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 等待统一开始信号
                    for (int j = 0; j < operationsPerThread; j++) {
                        counter.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        // 开始计时并启动所有线程
        long startTime = System.nanoTime();
        startLatch.countDown();
        endLatch.await();
        long endTime = System.nanoTime();
        
        // 验证结果正确性
        int expectedValue = threadCount * operationsPerThread;
        int actualValue = counter.getValue();
        if (actualValue != expectedValue) {
            throw new RuntimeException(String.format(
                "Synchronized test failed: expected %d, got %d", expectedValue, actualValue));
        }
        
        return endTime - startTime;
    }
    
    /**
     * 执行单轮性能测试
     */
    private static TestResult runSingleTest(int threadCount, int contentionLevel) throws InterruptedException {
        int operationsPerThread = BASE_OPERATIONS / contentionLevel;
        
        // 预热阶段
        for (int i = 0; i < WARMUP_ROUNDS; i++) {
            benchmarkAtomic(threadCount, operationsPerThread / 10);
            benchmarkSynchronized(threadCount, operationsPerThread / 10);
        }
        
        // 正式测试 - 多轮取平均值
        long totalAtomicTime = 0;
        long totalSynchronizedTime = 0;
        
        for (int round = 0; round < TEST_ROUNDS; round++) {
            totalAtomicTime += benchmarkAtomic(threadCount, operationsPerThread);
            totalSynchronizedTime += benchmarkSynchronized(threadCount, operationsPerThread);
        }
        
        long avgAtomicTime = totalAtomicTime / TEST_ROUNDS;
        long avgSynchronizedTime = totalSynchronizedTime / TEST_ROUNDS;
        
        return new TestResult(threadCount, contentionLevel, avgAtomicTime, avgSynchronizedTime);
    }
    
    /**
     * 将测试结果写入CSV文件
     */
    private static void writeResultsToCsv(TestResult[] results) throws IOException {
        String homeDir = System.getProperty("user.home");
        String csvPath = Paths.get(homeDir, "AvS.csv").toString();
        
        try (FileWriter writer = new FileWriter(csvPath)) {
            // 写入CSV头部
            writer.write("ThreadCount,ContentionLevel,AtomicTime(ns),SynchronizedTime(ns),SpeedupRatio,Winner\n");
            
            // 写入测试数据
            for (TestResult result : results) {
                writer.write(result.toCsvRow() + "\n");
            }
        }
        
        System.out.println("测试结果已保存到: " + csvPath);
    }
    
    /**
     * 打印测试结果摘要
     */
    private static void printSummary(TestResult[] results) {
        System.out.println("\n=== 并发性能测试结果摘要 ===");
        System.out.println("线程数 | 竞争度 | 原子时间(ms) | 同步时间(ms) | 倍速比 | 胜出者");
        System.out.println("-------|--------|-------------|-------------|--------|--------");
        
        for (TestResult result : results) {
            System.out.printf("%6d | %6d | %11.2f | %11.2f | %6.2fx | %s%n",
                result.threadCount,
                result.contentionLevel,
                result.atomicTime / 1_000_000.0,
                result.synchronizedTime / 1_000_000.0,
                result.speedupRatio,
                result.winner
            );
        }
        
        // 分析结果
        analyzeResults(results);
    }
    
    /**
     * 分析测试结果并给出结论
     */
    private static void analyzeResults(TestResult[] results) {
        System.out.println("\n=== 性能分析结论 ===");
        
        // 统计胜出情况
        int atomicWins = 0;
        int synchronizedWins = 0;
        
        for (TestResult result : results) {
            if ("Atomic".equals(result.winner)) {
                atomicWins++;
            } else {
                synchronizedWins++;
            }
        }
        
        System.out.printf("原子整型胜出: %d次, 同步整型胜出: %d次%n", atomicWins, synchronizedWins);
        
        // 分析不同场景下的性能特点
        System.out.println("\n场景分析:");
        System.out.println("1. 无竞争场景 (线程数=1): ");
        for (TestResult result : results) {
            if (result.threadCount == 1) {
                System.out.printf("   竞争度%d: %s胜出 (%.2fx)%n", 
                    result.contentionLevel, result.winner, result.speedupRatio);
                break;
            }
        }
        
        System.out.println("2. 高竞争场景 (线程数=12): ");
        for (TestResult result : results) {
            if (result.threadCount == 12) {
                System.out.printf("   竞争度%d: %s胜出 (%.2fx)%n", 
                    result.contentionLevel, result.winner, result.speedupRatio);
                break;
            }
        }
    }
    
    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        System.out.println("开始并发性能测试...");
        System.out.println("测试配置:");
        System.out.printf("- 线程数档位: %s%n", java.util.Arrays.toString(THREAD_COUNTS));
        System.out.printf("- 竞争强度档位: %s%n", java.util.Arrays.toString(CONTENTION_LEVELS));
        System.out.printf("- 基础操作数: %,d%n", BASE_OPERATIONS);
        System.out.printf("- 预热轮数: %d, 测试轮数: %d%n", WARMUP_ROUNDS, TEST_ROUNDS);
        
        try {
            // 执行所有测试组合
            TestResult[] results = new TestResult[THREAD_COUNTS.length * CONTENTION_LEVELS.length];
            int resultIndex = 0;
            
            for (int threadCount : THREAD_COUNTS) {
                for (int contentionLevel : CONTENTION_LEVELS) {
                    System.out.printf("正在测试: 线程数=%d, 竞争度=%d...%n", threadCount, contentionLevel);
                    
                    TestResult result = runSingleTest(threadCount, contentionLevel);
                    results[resultIndex++] = result;
                    
                    System.out.printf("  结果: %s胜出 (%.2fx)%n", result.winner, result.speedupRatio);
                }
            }
            
            // 输出结果
            printSummary(results);
            writeResultsToCsv(results);
            
            System.out.println("\n测试完成！请查看生成的CSV文件和可视化页面。");
            
        } catch (Exception e) {
            System.err.println("测试执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}