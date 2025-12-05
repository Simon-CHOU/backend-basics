package com.simon.functional;

import java.util.function.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * 函数式接口演示
 * 展示Java内置函数式接口和自定义接口
 */
@FunctionalInterface
interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
    
    // 默认方法 - 允许
    default void logUsage() {
        System.out.println("TriFunction被使用");
    }
    
    // 静态方法 - 允许
    static <A, B, C, R> TriFunction<A, B, C, R> of(TriFunction<A, B, C, R> func) {
        return func;
    }
}

public class FunctionalInterfaceDemo {
    
    // ========== 内置函数式接口示例 ==========
    
    /**
     * Consumer<T> - 消费型接口
     * 接受输入，无返回值
     */
    public void demonstrateConsumer() {
        System.out.println("=== Consumer示例 ===");
        
        Consumer<String> printer = System.out::println;
        printer.accept("Hello Consumer");
        
        // andThen 组合
        Consumer<String> logger = s -> System.out.println("LOG: " + s);
        Consumer<String> combined = printer.andThen(logger);
        combined.accept("组合消费者");
    }
    
    /**
     * Supplier<T> - 供给型接口
     * 无输入，提供返回值
     */
    public void demonstrateSupplier() {
        System.out.println("=== Supplier示例 ===");
        
        Supplier<String> messageSupplier = () -> "Hello from Supplier";
        System.out.println(messageSupplier.get());
        
        // 延迟计算示例
        Supplier<Double> randomSupplier = Math::random;
        System.out.println("随机数: " + randomSupplier.get());
        System.out.println("另一个随机数: " + randomSupplier.get());
    }
    
    /**
     * Function<T, R> - 函数型接口
     * 接受输入，返回转换结果
     */
    public void demonstrateFunction() {
        System.out.println("=== Function示例 ===");
        
        Function<String, Integer> lengthFunction = String::length;
        System.out.println("长度: " + lengthFunction.apply("Hello"));
        
        // compose 和 andThen 组合
        Function<Integer, Integer> doubleFunction = x -> x * 2;
        Function<String, Integer> composed = lengthFunction.andThen(doubleFunction);
        System.out.println("双倍长度: " + composed.apply("Hello"));
    }
    
    /**
     * Predicate<T> - 断言型接口
     * 接受输入，返回布尔值
     */
    public void demonstratePredicate() {
        System.out.println("=== Predicate示例 ===");
        
        Predicate<String> isLong = s -> s.length() > 5;
        System.out.println("长字符串: " + isLong.test("Hello"));
        System.out.println("长字符串: " + isLong.test("Hello World"));
        
        // 组合断言
        Predicate<String> containsA = s -> s.contains("a");
        Predicate<String> longAndContainsA = isLong.and(containsA);
        
        List<String> words = Arrays.asList("apple", "banana", "cat", "elephant");
        words.stream()
            .filter(longAndContainsA)
            .forEach(System.out::println);
    }
    
    // ========== 其他重要接口 ==========
    
    public void demonstrateOtherInterfaces() {
        System.out.println("=== 其他函数式接口 ===");
        
        // UnaryOperator<T> - 一元操作
        UnaryOperator<String> upperCase = String::toUpperCase;
        System.out.println(upperCase.apply("hello"));
        
        // BinaryOperator<T> - 二元操作
        BinaryOperator<Integer> adder = Integer::sum;
        System.out.println("加法: " + adder.apply(5, 3));
        
        // BiFunction<T, U, R> - 二元函数
        BiFunction<String, String, String> concat = (s1, s2) -> s1 + s2;
        System.out.println(concat.apply("Hello", "World"));
    }
    
    // ========== 自定义函数式接口 ==========
    
    public void demonstrateCustomFunctionalInterface() {
        System.out.println("=== 自定义函数式接口 ===");
        
        TriFunction<Integer, Integer, Integer, Integer> sumThree = 
            (a, b, c) -> a + b + c;
        
        int result = sumThree.apply(1, 2, 3);
        System.out.println("三数之和: " + result);
        
        sumThree.logUsage();
    }
    
    // ========== 方法引用演示 ==========
    
    public void demonstrateMethodReferences() {
        System.out.println("=== 方法引用 ===");
        
        // 静态方法引用
        Function<String, Integer> parseInt = Integer::parseInt;
        System.out.println(parseInt.apply("123"));
        
        // 实例方法引用
        String str = "Hello";
        Supplier<Integer> lengthSupplier = str::length;
        System.out.println(lengthSupplier.get());
        
        // 构造方法引用
        Supplier<List<String>> listSupplier = ArrayList::new;
        List<String> newList = listSupplier.get();
        newList.add("通过方法引用创建");
        System.out.println(newList);
    }
    
    public static void main(String[] args) {
        FunctionalInterfaceDemo demo = new FunctionalInterfaceDemo();
        
        demo.demonstrateConsumer();
        demo.demonstrateSupplier();
        demo.demonstrateFunction();
        demo.demonstratePredicate();
        demo.demonstrateOtherInterfaces();
        demo.demonstrateCustomFunctionalInterface();
        demo.demonstrateMethodReferences();
        
        System.out.println("\n=== 函数式接口特性总结 ===");
        System.out.println("1. 单抽象方法(SAM)接口");
        System.out.println("2. 支持@FunctionalInterface注解");
        System.out.println("3. 允许默认方法和静态方法");
        System.out.println("4. 支持Lambda表达式和方法引用");
        System.out.println("5. 提供组合操作(andThen, compose等)");
    }
}