package com.simon.integration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * FP概念集成测试 - 验证所有实验室概念的协同工作
 */
public class FpIntegrationTest {

    @Test
    void testPureFunctionWithLambdaAndStream() {
        // 纯函数 + Lambda + Stream 集成
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // 纯函数：平方计算
        Function<Integer, Integer> square = x -> x * x;
        
        // 纯函数：偶数判断
        Predicate<Integer> isEven = x -> x % 2 == 0;
        
        // Stream处理链
        List<Integer> result = numbers.stream()
            .filter(isEven)           // 函数式接口
            .map(square)              // Lambda表达式
            .collect(Collectors.toList());
        
        assertEquals(Arrays.asList(4, 16, 36, 64, 100), result);
        
        // 验证纯函数特性：相同输入总是产生相同输出
        assertEquals(16, square.apply(4));
        assertEquals(16, square.apply(4)); // 再次调用相同输入
        
        // 验证无副作用
        assertEquals(10, numbers.size()); // 原始集合未被修改
    }

    @Test
    void testMethodReferenceWithCustomFunctionalInterface() {
        // 方法引用 + 自定义函数式接口集成
        
        // 自定义函数式接口
        @FunctionalInterface
        interface StringTransformer {
            String transform(String input);
            
            default StringTransformer andThen(StringTransformer after) {
                return input -> after.transform(this.transform(input));
            }
        }
        
        // 使用方法引用
        StringTransformer toUpperCase = String::toUpperCase;
        StringTransformer addExclamation = s -> s + "!";
        
        // 函数组合
        StringTransformer transformPipeline = toUpperCase.andThen(addExclamation);
        
        String result = transformPipeline.transform("hello");
        assertEquals("HELLO!", result);
    }

    @Test
    void testParallelStreamWithPureFunctions() {
        // 并行流 + 纯函数集成（线程安全）
        List<Integer> numbers = IntStream.rangeClosed(1, 1000)
            .boxed()
            .collect(Collectors.toList());
        
        // 纯函数：安全用于并行处理
        Function<Integer, Integer> processNumber = x -> {
            // 模拟一些计算
            return x * x + 2 * x + 1;
        };
        
        List<Integer> sequentialResult = numbers.stream()
            .map(processNumber)
            .collect(Collectors.toList());
        
        List<Integer> parallelResult = numbers.parallelStream()
            .map(processNumber)
            .collect(Collectors.toList());
        
        // 由于是纯函数，顺序和并行结果应该一致
        assertEquals(sequentialResult, parallelResult);
        
        // 验证结果正确性
        assertEquals(processNumber.apply(10), sequentialResult.get(9));
    }

    @Test
    void testLazyEvaluationWithFunctionComposition() {
        // 惰性求值 + 函数组合集成
        List<String> words = Arrays.asList("hello", "world", "java", "lambda", "stream");
        
        AtomicInteger mapCount = new AtomicInteger(0);
        AtomicInteger filterCount = new AtomicInteger(0);
        
        // 函数组合
        Function<String, String> toUpper = s -> {
            mapCount.incrementAndGet();
            return s.toUpperCase();
        };
        
        Predicate<String> longerThan4 = s -> {
            filterCount.incrementAndGet();
            return s.length() > 4;
        };
        
        Stream<String> lazyStream = words.stream()
            .map(toUpper)
            .filter(longerThan4);
        
        // 验证惰性求值：中间操作未执行
        assertEquals(0, mapCount.get());
        assertEquals(0, filterCount.get());
        
        // 触发执行
        List<String> result = lazyStream.collect(Collectors.toList());
        
        // 验证结果和执行次数
        assertEquals(Arrays.asList("HELLO", "WORLD", "LAMBDA", "STREAM"), result);
        assertTrue(mapCount.get() > 0);
        assertTrue(filterCount.get() > 0);
    }

    @Test
    void testMemoizationWithFunctionalInterfaces() {
        // 记忆化 + 函数式接口集成
        
        // 昂贵的计算函数
        Function<Integer, Integer> expensiveCalculation = x -> {
            try {
                Thread.sleep(10); // 模拟耗时操作
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return x * x;
        };
        
        // 记忆化包装器
        Function<Integer, Integer> memoized = memoize(expensiveCalculation);
        
        long startTime = System.currentTimeMillis();
        Integer result1 = memoized.apply(5);
        long firstCallTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        Integer result2 = memoized.apply(5); // 相同输入
        long secondCallTime = System.currentTimeMillis() - startTime;
        
        assertEquals(25, result1);
        assertEquals(25, result2);
        
        // 第二次调用应该更快（从缓存获取）
        assertTrue(secondCallTime < firstCallTime / 2);
    }

    @Test
    void testCompleteFpPipeline() {
        // 完整的FP管道集成测试
        List<String> data = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");
        
        // 1. 纯函数转换
        Function<String, String> processString = s -> s.toUpperCase().replace("A", "@");
        
        // 2. 函数式接口过滤
        Predicate<String> lengthFilter = s -> s.length() >= 5;
        
        // 3. Stream处理
        Map<Integer, List<String>> groupedResult = data.stream()
            .map(processString)        // Lambda表达式
            .filter(lengthFilter)      // 函数式接口
            .collect(Collectors.groupingBy(
                String::length,       // 方法引用
                Collectors.toList()
            ));
        
        // 验证结果
        assertEquals(3, groupedResult.size());
        assertTrue(groupedResult.containsKey(5));
        assertTrue(groupedResult.containsKey(6));
        assertTrue(groupedResult.containsKey(10));
        assertEquals(Arrays.asList("@PPLE"), groupedResult.get(5));
        assertEquals(Arrays.asList("B@N@N@", "CHERRY"), groupedResult.get(6));
        assertEquals(Arrays.asList("ELDERBERRY"), groupedResult.get(10));
    }

    // 记忆化工具函数
    private static <T, R> Function<T, R> memoize(Function<T, R> function) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return input -> cache.computeIfAbsent(input, function);
    }
}