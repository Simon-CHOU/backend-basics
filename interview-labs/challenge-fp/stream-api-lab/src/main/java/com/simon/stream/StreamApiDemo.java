package com.simon.stream;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

/**
 * Stream API 演示
 * 展示函数式数据处理管道的各种特性
 */
public class StreamApiDemo {
    
    private List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private List<String> words = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");
    
    /**
     * 基础流操作演示
     */
    public void demonstrateBasicOperations() {
        System.out.println("=== 基础流操作 ===");
        
        // map - 转换
        List<Integer> squares = numbers.stream()
            .map(x -> x * x)
            .collect(Collectors.toList());
        System.out.println("平方数: " + squares);
        
        // filter - 过滤
        List<Integer> evens = numbers.stream()
            .filter(x -> x % 2 == 0)
            .collect(Collectors.toList());
        System.out.println("偶数: " + evens);
        
        // reduce - 归约
        int sum = numbers.stream()
            .reduce(0, Integer::sum);
        System.out.println("总和: " + sum);
    }
    
    /**
     * 惰性求值演示
     */
    public void demonstrateLazyEvaluation() {
        System.out.println("=== 惰性求值演示 ===");
        
        System.out.println("中间操作不会立即执行:");
        Stream<Integer> lazyStream = numbers.stream()
            .map(x -> {
                System.out.println("映射: " + x);
                return x * 2;
            })
            .filter(x -> {
                System.out.println("过滤: " + x);
                return x > 5;
            });
        
        System.out.println("只有调用终止操作时才会执行:");
        List<Integer> result = lazyStream.collect(Collectors.toList());
        System.out.println("结果: " + result);
    }
    
    /**
     * 并行流演示
     */
    public void demonstrateParallelStream() {
        System.out.println("=== 并行流演示 ===");
        
        // 顺序流
        long sequentialTime = measureTime(() -> 
            numbers.stream()
                .map(this::expensiveOperation)
                .reduce(0, Integer::sum)
        );
        
        // 并行流
        long parallelTime = measureTime(() -> 
            numbers.parallelStream()
                .map(this::expensiveOperation)
                .reduce(0, Integer::sum)
        );
        
        System.out.printf("顺序流时间: %d ms%n", sequentialTime);
        System.out.printf("并行流时间: %d ms%n", parallelTime);
        System.out.printf("加速比: %.2fx%n", (double) sequentialTime / parallelTime);
    }
    
    /**
     * 集合收集器演示
     */
    public void demonstrateCollectors() {
        System.out.println("=== 集合收集器 ===");
        
        // 转换为集合
        Set<Integer> set = numbers.stream()
            .collect(Collectors.toSet());
        System.out.println("Set: " + set);
        
        // 分组
        Map<String, List<String>> byLength = words.stream()
            .collect(Collectors.groupingBy(String::toString));
        System.out.println("按长度分组: " + byLength);
        
        // 连接字符串
        String concatenated = words.stream()
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("连接结果: " + concatenated);
        
        // 统计信息
        IntSummaryStatistics stats = numbers.stream()
            .collect(Collectors.summarizingInt(Integer::intValue));
        System.out.println("统计: " + stats);
    }
    
    /**
     * 高级流操作
     */
    public void demonstrateAdvancedOperations() {
        System.out.println("=== 高级流操作 ===");
        
        // 无限流
        System.out.println("无限流示例:");
        Stream.iterate(0, n -> n + 1)
            .limit(5)
            .forEach(System.out::println);
        
        // 原始类型流
        int sumOfSquares = IntStream.rangeClosed(1, 5)
            .map(x -> x * x)
            .sum();
        System.out.println("1-5平方和: " + sumOfSquares);
        
        // flatMap
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2),
            Arrays.asList(3, 4),
            Arrays.asList(5, 6)
        );
        
        List<Integer> flat = nested.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        System.out.println("扁平化: " + flat);
    }
    
    /**
     * 演示短路操作
     */
    public void demonstrateShortCircuit() {
        System.out.println("=== 短路操作 ===");
        
        // findFirst - 找到第一个就停止
        Optional<Integer> firstEven = numbers.stream()
            .filter(x -> {
                System.out.println("检查: " + x);
                return x % 2 == 0;
            })
            .findFirst();
        
        firstEven.ifPresent(x -> System.out.println("第一个偶数: " + x));
        
        // anyMatch - 找到匹配就停止
        boolean hasEven = numbers.stream()
            .peek(x -> System.out.println("检查匹配: " + x))
            .anyMatch(x -> x % 2 == 0);
        System.out.println("包含偶数: " + hasEven);
    }
    
    /**
     * 模拟昂贵操作
     */
    private int expensiveOperation(int x) {
        try {
            Thread.sleep(100); // 模拟耗时操作
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return x * x;
    }
    
    /**
     * 测量执行时间
     */
    private long measureTime(Runnable operation) {
        long start = System.currentTimeMillis();
        operation.run();
        return System.currentTimeMillis() - start;
    }
    
    public static void main(String[] args) {
        StreamApiDemo demo = new StreamApiDemo();
        
        demo.demonstrateBasicOperations();
        demo.demonstrateLazyEvaluation();
        demo.demonstrateParallelStream();
        demo.demonstrateCollectors();
        demo.demonstrateAdvancedOperations();
        demo.demonstrateShortCircuit();
        
        System.out.println("\n=== Stream API 特性总结 ===");
        System.out.println("1. 惰性求值: 中间操作延迟执行");
        System.out.println("2. 函数式管道: map/filter/reduce组合");
        System.out.println("3. 并行处理: parallelStream()自动并行化");
        System.out.println("4. 短路操作: findFirst/anyMatch等");
        System.out.println("5. 丰富收集器: 提供各种集合转换");
    }
}