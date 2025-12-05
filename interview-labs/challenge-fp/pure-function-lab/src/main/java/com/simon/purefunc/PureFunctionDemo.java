package com.simon.purefunc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 纯函数概念演示
 * 展示纯函数与非纯函数的区别
 */
public class PureFunctionDemo {
    
    // 外部状态 - 用于演示副作用
    private int counter = 0;
    private List<String> log = new ArrayList<>();
    
    // ========== 纯函数示例 ==========
    
    /**
     * 纯函数：加法运算
     * - 无副作用
     * - 引用透明
     */
    public static int add(int a, int b) {
        return a + b;
    }
    
    /**
     * 纯函数：字符串拼接
     * - 使用不可变对象
     */
    public static String concatenate(String s1, String s2) {
        return s1 + s2;
    }
    
    /**
     * 纯函数：计算平方
     */
    public static int square(int x) {
        return x * x;
    }
    
    // ========== 非纯函数示例 ==========
    
    /**
     * 非纯函数：有副作用（修改外部状态）
     */
    public int increment() {
        return counter++; // 副作用：修改实例变量
    }
    
    /**
     * 非纯函数：有副作用（I/O操作）
     */
    public void logMessage(String message) {
        log.add(message); // 副作用：修改外部集合
        System.out.println(message); // 副作用：I/O操作
    }
    
    /**
     * 非纯函数：依赖外部状态
     */
    public double calculateWithTax(double amount) {
        double taxRate = getCurrentTaxRate(); // 依赖外部状态
        return amount * (1 + taxRate);
    }
    
    /**
     * 非纯函数：非确定性（不同时间返回不同结果）
     */
    public LocalDateTime getCurrentTime() {
        return LocalDateTime.now(); // 非确定性
    }
    
    // ========== 辅助方法 ==========
    
    private double getCurrentTaxRate() {
        // 模拟外部依赖
        return 0.1; // 固定税率，但实际可能变化
    }
    
    /**
     * 演示纯函数的优势
     */
    public void demonstratePureFunctionAdvantages() {
        System.out.println("=== 纯函数优势演示 ===");
        
        // 1. 可测试性
        int result1 = add(2, 3);
        int result2 = add(2, 3);
        assert result1 == result2 : "纯函数保证相同输入相同输出";
        
        // 2. 引用透明性
        String expression1 = concatenate("Hello", "World");
        String expression2 = "Hello" + "World";
        assert expression1.equals(expression2) : "引用透明性验证";
        
        // 3. 易于推理
        int squared = square(5);
        assert squared == 25 : "等式推理验证";
        
        System.out.println("所有纯函数验证通过！");
    }
    
    /**
     * 演示非纯函数的问题
     */
    public void demonstrateImpureFunctionIssues() {
        System.out.println("=== 非纯函数问题演示 ===");
        
        // 1. 状态修改问题
        int initial = counter;
        increment();
        assert counter != initial : "非纯函数修改了外部状态";
        
        // 2. 非确定性行为
        LocalDateTime time1 = getCurrentTime();
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        LocalDateTime time2 = getCurrentTime();
        assert !time1.equals(time2) : "非纯函数在不同时间返回不同结果";
        
        System.out.println("非纯函数问题演示完成！");
    }
    
    public static void main(String[] args) {
        PureFunctionDemo demo = new PureFunctionDemo();
        
        demo.demonstratePureFunctionAdvantages();
        demo.demonstrateImpureFunctionIssues();
        
        System.out.println("\n=== 纯函数特征总结 ===");
        System.out.println("1. 无副作用: 不修改外部状态，不进行I/O");
        System.out.println("2. 引用透明: 相同输入 ⇒ 相同输出");
        System.out.println("3. 可确定性: 结果只依赖于输入参数");
        System.out.println("4. 可测试性: 无需mock外部依赖");
        System.out.println("5. 并行安全: 无竞态条件风险");
    }
}