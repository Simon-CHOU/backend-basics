package com.simon.practice.exercises.functional;

import java.util.function.*;
import java.util.List;
import java.util.ArrayList;

/**
 * 函数式接口练习
 *
 * 练习目标：
 * 1. 掌握常用函数式接口的使用
 * 2. 理解函数组合的概念
 * 3. 学会使用方法引用
 * 4. 创建自定义函数式接口
 *
 * 请完成以下TODO标记的代码：
 */
public class FunctionalInterfaceExercise {

    /**
     * 自定义三参数函数式接口
     */
    @FunctionalInterface
    interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);

        // TODO: 添加一个默认方法，用于打印使用日志
        default void logUsage(String message) {
            // 请实现一个简单的日志打印功能
        }
    }

    /**
     * 练习1: 使用Consumer接口处理列表
     * 目标：创建一个Consumer，处理字符串列表
     */
    public static Consumer<List<String>> createStringListProcessor() {
        // TODO: 创建一个Consumer，接收字符串列表并：
        // 1. 移除所有空字符串
        // 2. 将每个字符串转为大写
        // 3. 打印处理后的列表
        return null;
    }

    /**
     * 练习2: 使用Supplier生成数据
     * 目标：创建一个Supplier，生成随机数列表
     */
    public static Supplier<List<Integer>> createRandomListSupplier(int size, int bound) {
        // TODO: 创建一个Supplier，生成包含size个随机整数的列表
        // 随机数范围应该是[0, bound)
        return null;
    }

    /**
     * 练习3: 使用Function进行字符串转换
     * 目标：创建复合函数转换字符串
     */
    public static Function<String, String> createStringTransformer() {
        // TODO: 创建一个复合Function：
        // 1. 移除首尾空格
        // 2. 转换为大写
        // 3. 添加前缀"PROCESSED:"
        return null;
    }

    /**
     * 练习4: 使用Predicate进行复杂条件判断
     * 目标：创建复合谓词检查字符串
     */
    public static Predicate<String> createStringValidator() {
        // TODO: 创建一个复合Predicate，检查字符串是否：
        // 1. 不为空
        // 2. 长度在3-20之间
        // 3. 包含至少一个数字
        // 4. 不包含特殊字符（只允许字母、数字、空格）
        return null;
    }

    /**
     * 练习5: 使用自定义TriFunction
     * 目标：实现三参数函数计算
     */
    public static TriFunction<Integer, Integer, Integer, Integer> createSumAndMultiply() {
        // TODO: 创建一个TriFunction，计算 (a + b) * c
        return null;
    }

    /**
     * 练习6: 使用BiFunction处理两个参数
     * 目标：创建字符串合并处理函数
     */
    public static BiFunction<String, String, Integer> createCombinedAnalyzer() {
        // TODO: 创建一个BiFunction，接收两个字符串并返回：
        // 两个字符串长度的乘积
        return null;
    }

    /**
     * 练习7: 使用UnaryOperator和BinaryOperator
     * 目标：创建数值操作器
     */
    public static UnaryOperator<Integer> createIncrementer() {
        // TODO: 创建一个UnaryOperator，将输入数字加10
        return null;
    }

    public static BinaryOperator<String> createSmartConcat() {
        // TODO: 创建一个BinaryOperator，智能连接两个字符串：
        // - 如果其中一个字符串为空，返回另一个
        // - 如果都不为空，用逗号连接
        // - 如果都为空，返回"empty"
        return null;
    }

    /**
     * 练习8: 使用方法引用
     * 目标：将现有的方法转换为函数式接口
     */
    public static Function<String, Integer> createLengthCalculator() {
        // TODO: 使用方法引用创建字符串长度计算器
        return null;
    }

    public static Predicate<String> createEmptyChecker() {
        // TODO: 使用方法引用创建空字符串检查器
        return null;
    }

    public static Supplier<List<String>> createArrayListSupplier() {
        // TODO: 使用构造方法引用创建ArrayList供应商
        return null;
    }

    /**
     * 练习9: 创建高阶函数
     * 目标：创建返回函数的函数
     */
    public static Function<Integer, Predicate<String>> createLengthPredicateFactory() {
        // TODO: 创建一个工厂函数，接收最小长度参数，
        // 返回一个检查字符串长度的Predicate
        return null;
    }

    /**
     * 辅助方法：检查字符串是否只包含字母、数字和空格
     */
    private static boolean isAlphanumericWithSpace(String s) {
        return s.matches("[a-zA-Z0-9\\s]*");
    }

    /**
     * 辅助方法：检查字符串是否包含数字
     */
    private static boolean containsDigit(String s) {
        return s.matches(".*\\d.*");
    }
}