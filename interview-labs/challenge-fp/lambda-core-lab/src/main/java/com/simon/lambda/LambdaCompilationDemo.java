package com.simon.lambda;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lambda表达式编译机制演示
 * 验证Lambda使用invokedynamic而非匿名内部类
 */
public class LambdaCompilationDemo {
    
    // Lambda表达式示例
    public static final Consumer<String> LAMBDA_PRINTER = s -> System.out.println(s);
    
    // 等效的匿名内部类（对比用）
    public static final Consumer<String> ANONYMOUS_PRINTER = new Consumer<String>() {
        @Override
        public void accept(String s) {
            System.out.println(s);
        }
    };
    
    /**
     * 演示Lambda表达式的使用
     */
    public void demonstrateLambda() {
        List<String> words = Arrays.asList("hello", "world", "lambda");
        
        System.out.println("=== 使用Lambda表达式 ===");
        words.forEach(LAMBDA_PRINTER);
        
        System.out.println("=== 使用匿名内部类 ===");
        words.forEach(ANONYMOUS_PRINTER);
        
        System.out.println("=== 直接内联Lambda ===");
        words.forEach(s -> System.out.println(s.toUpperCase()));
    }
    
    /**
     * 方法引用示例
     */
    public void demonstrateMethodReference() {
        List<String> words = Arrays.asList("hello", "world", "method", "reference");
        
        System.out.println("=== 方法引用 ===");
        words.forEach(System.out::println);
    }
    
    public static void main(String[] args) {
        LambdaCompilationDemo demo = new LambdaCompilationDemo();
        demo.demonstrateLambda();
        demo.demonstrateMethodReference();
        
        System.out.println("\n查看字节码指令:");
        System.out.println("javap -c -p LambdaCompilationDemo.class");
        System.out.println("注意查找invokedynamic指令");
    }
}