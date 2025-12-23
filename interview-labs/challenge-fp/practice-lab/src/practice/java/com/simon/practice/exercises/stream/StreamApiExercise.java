package com.simon.practice.exercises.stream;

import java.util.*;
import java.util.stream.*;
import java.util.function.Function;

/**
 * Stream API练习
 *
 * 练习目标：
 * 1. 掌握Stream的创建和基本操作
 * 2. 理解中间操作和终端操作的区别
 * 3. 学会使用收集器(Collector)
 * 4. 掌握并行流的使用
 *
 * 请完成以下TODO标记的代码：
 */
public class StreamApiExercise {

    /**
     * 练习1: 基本过滤和映射
     * 目标：从整数列表中提取偶数并计算平方
     */
    public static List<Integer> filterEvenAndSquare(List<Integer> numbers) {
        // TODO: 使用Stream过滤偶数并计算平方
        return null;
    }

    /**
     * 练习2: 字符串处理
     * 目标：处理字符串列表，找出长度大于3的单词并转为大写
     */
    public static List<String> processWords(List<String> words) {
        // TODO: 过滤长度>3的单词，转为大写，并收集到List
        return null;
    }

    /**
     * 练习3: 使用收集器
     * 目标：将字符串按长度分组
     */
    public static Map<Integer, List<String>> groupByLength(List<String> words) {
        // TODO: 使用Collectors.groupingBy按字符串长度分组
        return null;
    }

    /**
     * 练习4: 数值流操作
     * 目标：计算一些统计信息
     */
    public static IntSummaryStatistics calculateStats(List<Integer> numbers) {
        // TODO: 使用IntStream计算统计信息（最大值、最小值、平均值等）
        return null;
    }

    /**
     * 练习5: 扁平化操作
     * 目标：将嵌套的列表扁平化
     */
    public static List<String> flattenNestedLists(List<List<String>> nestedLists) {
        // TODO: 使用flatMap将嵌套的字符串列表扁平化为单一列表
        return null;
    }

    /**
     * 练习6: 查找操作
     * 目标：在字符串中查找符合条件的元素
     */
    public static Optional<String> findFirstLongWord(List<String> words, int minLength) {
        // TODO: 查找第一个长度大于minLength的单词
        return null;
    }

    /**
     * 练习7: 规约操作
     * 目标：使用reduce进行复杂的计算
     */
    public static String concatenateWithSeparator(List<String> words, String separator) {
        // TODO: 使用reduce将字符串列表用指定分隔符连接
        return null;
    }

    /**
     * 练习8: 自定义收集器
     * 目标：将结果收集到自定义数据结构中
     */
    public static Map<String, Long> countWordFrequency(List<String> words) {
        // TODO: 统计每个单词的出现频率
        return null;
    }

    /**
     * 练习9: 并行流处理
     * 目标：使用并行流处理大数据集
     */
    public static double parallelSumOfSquares(List<Integer> numbers) {
        // TODO: 使用并行流计算平方和
        return 0;
    }

    /**
     * 练习10: 复杂数据处理
     * 目标：处理包含重复数据的情况
     */
    public static List<String> removeDuplicatesAndSort(List<String> words) {
        // TODO: 移除重复元素并按字母顺序排序
        return null;
    }

    /**
     * 练习11: 条件分区
     * 目标：根据条件将元素分区
     */
    public static Map<Boolean, List<Integer>> partitionByEvenOdd(List<Integer> numbers) {
        // TODO: 使用Collectors.partitioningBy按奇偶性分区
        return null;
    }

    /**
     * 练习12: 链式处理
     * 目标：创建复杂的Stream处理链
     */
    public static List<Integer> complexProcessing(List<String> numberStrings) {
        // TODO: 创建处理链：
        // 1. 过滤非数字字符串
        // 2. 转换为整数
        // 3. 过滤负数
        // 4. 计算绝对值
        // 5. 去重
        // 6. 排序
        // 7. 限制前10个
        return null;
    }

    /**
     * 练习13: 创建Stream的不同方式
     * 目标：展示多种Stream创建方法
     */
    public static Stream<Integer> createStreamFromVariousSources() {
        // TODO: 创建包含多种来源的Stream：
        // 1. 从数组创建
        // 2. 从集合创建
        // 3. 使用Stream.generate
        // 4. 使用Stream.iterate
        // 将它们合并并返回
        return null;
    }

    /**
     * 练习14: 收集到不同类型的容器
     * 目标：将Stream结果收集到不同类型的集合
     */
    public static Set<String> collectToSet(List<String> words) {
        // TODO: 将字符串收集到Set中自动去重
        return null;
    }

    public static String[] collectToArray(List<String> words) {
        // TODO: 将字符串收集到数组中
        return null;
    }

    /**
     * 练习15: 高级收集器操作
     * 目标：使用下游收集器进行复杂收集
     */
    public static Map<Character, Long> countByFirstChar(List<String> words) {
        // TODO: 按首字母分组并统计每组的数量
        return null;
    }
}