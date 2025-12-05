package com.simon.purefunc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * 纯函数特性测试
 * 采用TDD方式验证纯函数的核心特性
 */
class PureFunctionTest {

    @Test
    void testPureFunctionNoSideEffects() {
        // AC1: 验证纯函数无副作用
        int a = 5, b = 3;
        
        // 纯函数不应该修改输入参数
        int result = PureFunctionDemo.add(a, b);
        assertEquals(8, result);
        assertEquals(5, a); // a未被修改
        assertEquals(3, b); // b未被修改
        
        // 纯函数不应该有外部可见的副作用
        String s1 = "Hello", s2 = "World";
        String concatResult = PureFunctionDemo.concatenate(s1, s2);
        assertEquals("HelloWorld", concatResult);
        assertEquals("Hello", s1); // s1未被修改
        assertEquals("World", s2); // s2未被修改
    }

    @Test
    void testReferentialTransparency() {
        // AC2: 验证引用透明性
        
        // 相同输入总是产生相同输出
        int result1 = PureFunctionDemo.add(2, 3);
        int result2 = PureFunctionDemo.add(2, 3);
        assertEquals(result1, result2);
        
        // 函数调用可以被返回值替换
        int directResult = 2 + 3;
        assertEquals(directResult, result1);
        
        // 支持等式推理
        int squared = PureFunctionDemo.square(5);
        assertEquals(25, squared);
        assertEquals(PureFunctionDemo.square(5), 25);
    }

    @Test
    void testPureFunctionDeterminism() {
        // AC2: 验证确定性行为
        
        // 纯函数在不同时间调用应该返回相同结果
        int result1 = PureFunctionDemo.add(10, 20);
        
        // 模拟时间流逝
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        
        int result2 = PureFunctionDemo.add(10, 20);
        assertEquals(result1, result2);
        
        // 多次调用结果一致
        for (int i = 0; i < 100; i++) {
            assertEquals(30, PureFunctionDemo.add(10, 20));
        }
    }

    @Test
    void testImpureFunctionSideEffects() {
        // AC1: 验证非纯函数有副作用
        PureFunctionDemo demo = new PureFunctionDemo();
        
        // 测试非纯函数increment()的副作用
        // 由于counter是私有字段，我们通过多次调用来验证副作用
        int firstCall = demo.increment();
        int secondCall = demo.increment();
        
        // 非纯函数产生不同结果（由于副作用）
        assertNotEquals(firstCall, secondCall);
        assertEquals(0, firstCall); // 第一次调用返回0（counter++的返回值）
        assertEquals(1, secondCall); // 第二次调用返回1
    }

    @Test
    void testImpureFunctionNonDeterminism() {
        // AC1: 验证非纯函数的非确定性
        PureFunctionDemo demo = new PureFunctionDemo();
        
        LocalDateTime time1 = demo.getCurrentTime();
        
        // 时间流逝
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        
        LocalDateTime time2 = demo.getCurrentTime();
        
        // 非纯函数在不同时间返回不同结果
        assertNotEquals(time1, time2);
    }

    @Test
    void testPureFunctionThreadSafety() throws InterruptedException {
        // AC3: 验证纯函数的线程安全性
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        List<Future<Integer>> results = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int value = i;
            Future<Integer> future = executor.submit(() -> {
                latch.countDown();
                latch.await();
                return PureFunctionDemo.add(value, value);
            });
            results.add(future);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        
        // 验证所有线程都得到正确结果
        for (int i = 0; i < threadCount; i++) {
            try {
                assertEquals(i * 2, results.get(i).get());
            } catch (Exception e) {
                fail("线程执行异常: " + e.getMessage());
            }
        }
    }

    @Test
    void testMemoizationPotential() {
        // AC3: 验证纯函数可缓存性
        
        // 简单模拟记忆化
        java.util.Map<String, Integer> cache = new ConcurrentHashMap<>();
        
        Function<String, Integer> expensiveOperation = input -> {
            // 模拟昂贵计算
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            return input.length() * 2;
        };
        
        Function<String, Integer> memoized = input -> 
            cache.computeIfAbsent(input, expensiveOperation);
        
        // 第一次调用（计算）
        long startTime = System.nanoTime();
        int result1 = memoized.apply("test");
        long time1 = System.nanoTime() - startTime;
        
        // 第二次调用（缓存）
        startTime = System.nanoTime();
        int result2 = memoized.apply("test");
        long time2 = System.nanoTime() - startTime;
        
        assertEquals(result1, result2);
        assertTrue(time2 < time1, "缓存访问应该更快");
    }

    @Test
    void testPureFunctionComposition() {
        // 验证纯函数的组合性
        
        // 组合纯函数: square(add(x, y))
        int result = PureFunctionDemo.square(PureFunctionDemo.add(3, 2));
        assertEquals(25, result);
        
        // 等式推理: square(add(3,2)) = square(5) = 25
        assertEquals(PureFunctionDemo.square(5), result);
        
        // 另一种组合: add(square(3), square(2))
        int result2 = PureFunctionDemo.add(
            PureFunctionDemo.square(3), 
            PureFunctionDemo.square(2)
        );
        assertEquals(13, result2);
    }

    @Test
    void testPureFunctionTestability() {
        // AC3: 验证纯函数的可测试性
        
        // 无需mock或setup，直接测试
        assertEquals(5, PureFunctionDemo.add(2, 3));
        assertEquals("HelloWorld", PureFunctionDemo.concatenate("Hello", "World"));
        assertEquals(16, PureFunctionDemo.square(4));
        
        // 边界条件测试
        assertEquals(0, PureFunctionDemo.add(0, 0));
        assertEquals("", PureFunctionDemo.concatenate("", ""));
        assertEquals(0, PureFunctionDemo.square(0));
        
        // 负数测试
        assertEquals(-1, PureFunctionDemo.add(2, -3));
        assertEquals(9, PureFunctionDemo.square(-3));
    }
}