package com.simon.practice.exercises.lambda;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Function;

/**
 * Lambda表达式基础练习
 *
 * 练习目标：
 * 1. 掌握Lambda表达式的基本语法
 * 2. 理解函数式接口的使用
 * 3. 学会创建和使用Lambda表达式
 *
 * 请完成以下TODO标记的代码：
 */
public class LambdaBasicsExercise {

    /**
     * 练习1: 创建一个简单的Consumer Lambda
     * 目标：创建一个Lambda，接受一个字符串并打印到控制台
     */
    public static Consumer<String> createStringPrinter() {
        // TODO: 创建一个Consumer Lambda，打印传入的字符串
        return null;
    }

    /**
     * 练习2: 创建一个字符串长度检查的Predicate
     * 目标：创建一个Lambda，检查字符串长度是否大于指定值
     */
    public static Predicate<String> createLengthChecker(int minLength) {
        // TODO: 创建一个Predicate Lambda，检查字符串长度是否大于minLength
        return null;
    }

    /**
     * 练习3: 创建一个字符串转换Function
     * 目标：创建一个Lambda，将字符串转换为大写
     */
    public static Function<String, String> createToUpperFunction() {
        // TODO: 创建一个Function Lambda，将字符串转换为大写
        return null;
    }

    /**
     * 练习4: 创建一个字符串重复Function
     * 目标：创建一个Lambda，将字符串重复n次
     */
    public static Function<String, String> createRepeaterFunction(int count) {
        // TODO: 创建一个Function Lambda，将输入字符串重复count次
        return null;
    }

    /**
     * 练习5: 创建一个复合检查Predicate
     * 目标：组合多个条件检查字符串
     */
    public static Predicate<String> createComplexChecker() {
        // TODO: 创建一个Predicate，检查字符串是否：
        // 1. 长度大于3
        // 2. 包含字母"a"
        // 3. 不等于"test"
        return null;
    }

    /**
     * 练习6: 创建一个自定义操作的Consumer
     * 目标：创建一个Lambda，对整数列表进行操作
     */
    public static java.util.function.Consumer<java.util.List<Integer>> createListOperation() {
        // TODO: 创建一个Consumer Lambda，接收整数列表并：
        // 1. 移除所有小于5的数字
        // 2. 将每个数字乘以2
        // 3. 打印处理后的列表
        return null;
    }

    /**
     * 练习7: 创建一个级联转换Function
     * 目标：使用Function的compose和andThen方法
     */
    public static Function<String, Integer> createCascadedTransform() {
        // TODO: 创建一个转换链：
        // 1. 字符串 -> 转大写
        // 2. 转大写 -> 取长度
        // 3. 长度 -> 乘以10
        return null;
    }
}