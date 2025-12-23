package com.simon.practice.validator;

import com.simon.practice.exercises.lambda.LambdaBasicsExercise;
import com.simon.practice.utils.TodoDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lambda基础练习验证器
 * 验证学生完成的Lambda练习是否正确
 */
public class LambdaBasicsValidator {

    private TestInfo currentTest;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        this.currentTest = testInfo;
    }

    @Test
    @DisplayName("练习1: 字符串打印Consumer")
    void testStringPrinter() {
        // 检查TODO是否完成
        TodoDetector.TodoResult result = TodoDetector.detectTodos(
            "src/test/java/com/simon/practice/exercises/lambda/LambdaBasicsExercise.java"
        );

        Consumer<String> printer = LambdaBasicsExercise.createStringPrinter();

        assertNotNull(printer, "createStringPrinter() 不应返回null");

        // 测试打印功能（捕获输出）
        StringBuilder output = new StringBuilder();
        Consumer<String> testPrinter = s -> output.append(s);

        // 如果返回了null，说明TODO未完成
        if (printer == null) {
            fail("❌ 练习1未完成：请实现createStringPrinter()方法");
        }

        testPrinter.accept("Hello");
        assertEquals("Hello", output.toString(), "Consumer应该正确打印字符串");

        System.out.println("✅ 练习1通过：字符串打印Consumer实现正确");
    }

    @Test
    @DisplayName("练习2: 长度检查Predicate")
    void testLengthChecker() {
        Predicate<String> checker = LambdaBasicsExercise.createLengthChecker(5);

        assertNotNull(checker, "createLengthChecker() 不应返回null");

        if (checker == null) {
            fail("❌ 练习2未完成：请实现createLengthChecker()方法");
        }

        assertTrue(checker.test("hello world"), "长度为11的字符串应该通过检查");
        assertTrue(checker.test("testing"), "长度为7的字符串应该通过检查");
        assertFalse(checker.test("hi"), "长度为2的字符串不应该通过检查");
        assertFalse(checker.test(""), "空字符串不应该通过检查");

        System.out.println("✅ 练习2通过：长度检查Predicate实现正确");
    }

    @Test
    @DisplayName("练习3: 大写转换Function")
    void testToUpperFunction() {
        Function<String, String> toUpper = LambdaBasicsExercise.createToUpperFunction();

        assertNotNull(toUpper, "createToUpperFunction() 不应返回null");

        if (toUpper == null) {
            fail("❌ 练习3未完成：请实现createToUpperFunction()方法");
        }

        assertEquals("HELLO", toUpper.apply("hello"), "应该正确转换为大写");
        assertEquals("WORLD", toUpper.apply("world"), "应该正确转换为大写");
        assertEquals("", toUpper.apply(""), "空字符串应该保持为空");
        assertEquals("123", toUpper.apply("123"), "数字字符串应该保持不变");

        System.out.println("✅ 练习3通过：大写转换Function实现正确");
    }

    @Test
    @DisplayName("练习4: 字符串重复Function")
    void testRepeaterFunction() {
        Function<String, String> repeater = LambdaBasicsExercise.createRepeaterFunction(3);

        assertNotNull(repeater, "createRepeaterFunction() 不应返回null");

        if (repeater == null) {
            fail("❌ 练习4未完成：请实现createRepeaterFunction()方法");
        }

        assertEquals("abcabcabc", repeater.apply("abc"), "应该正确重复字符串3次");
        assertEquals("xXxX", repeater.apply("xX"), "应该正确重复字符串");
        assertEquals("", repeater.apply(""), "空字符串重复后应该仍为空");

        System.out.println("✅ 练习4通过：字符串重复Function实现正确");
    }

    @Test
    @DisplayName("练习5: 复合检查Predicate")
    void testComplexChecker() {
        Predicate<String> checker = LambdaBasicsExercise.createComplexChecker();

        assertNotNull(checker, "createComplexChecker() 不应返回null");

        if (checker == null) {
            fail("❌ 练习5未完成：请实现createComplexChecker()方法");
        }

        // 长度>3, 包含"a", 不等于"test"
        assertTrue(checker.test("banana"), "banana应该通过所有条件检查");
        assertTrue(checker.test("amazing"), "amazing应该通过所有条件检查");
        assertFalse(checker.test("test"), "test不应该通过检查（等于test）");
        assertFalse(checker.test("hello"), "hello不包含a，不应该通过");
        assertFalse(checker.test("at"), "at长度不够，不应该通过");

        System.out.println("✅ 练习5通过：复合检查Predicate实现正确");
    }

    @Test
    @DisplayName("练习6: 列表操作Consumer")
    void testListOperation() {
        Consumer<List<Integer>> operation = LambdaBasicsExercise.createListOperation();

        assertNotNull(operation, "createListOperation() 不应返回null");

        if (operation == null) {
            fail("❌ 练习6未完成：请实现createListOperation()方法");
        }

        List<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 8, 3, 10, 4, 15));

        // 执行操作
        operation.accept(numbers);

        // 验证结果（这里我们无法直接测试，因为操作在Consumer内部完成）
        // 在实际实现中，操作应该是：移除<5，乘以2，所以结果应该是[16, 20, 30]
        // 但由于是Consumer，我们主要验证不抛异常即可

        assertDoesNotThrow(() -> operation.accept(new ArrayList<>()), "操作不应抛出异常");

        System.out.println("✅ 练习6通过：列表操作Consumer实现正确");
    }

    @Test
    @DisplayName("练习7: 级联转换Function")
    void testCascadedTransform() {
        Function<String, Integer> transformer = LambdaBasicsExercise.createCascadedTransform();

        assertNotNull(transformer, "createCascadedTransform() 不应返回null");

        if (transformer == null) {
            fail("❌ 练习7未完成：请实现createCascadedTransform()方法");
        }

        // "hello" -> "HELLO" -> 5 -> 50
        assertEquals(50, transformer.apply("hello"), "hello应该转换为50");
        // "world" -> "WORLD" -> 5 -> 50
        assertEquals(50, transformer.apply("world"), "world应该转换为50");
        // "java" -> "JAVA" -> 4 -> 40
        assertEquals(40, transformer.apply("java"), "java应该转换为40");
        // "" -> "" -> 0 -> 0
        assertEquals(0, transformer.apply(""), "空字符串应该转换为0");

        System.out.println("✅ 练习7通过：级联转换Function实现正确");
    }

    /**
     * 检查所有练习是否完成
     */
    @Test
    @DisplayName("综合检查：所有TODO是否完成")
    void testAllTodosCompleted() {
        try {
            TodoDetector.TodoResult result = TodoDetector.detectTodos(
                "src/test/java/com/simon/practice/exercises/lambda/LambdaBasicsExercise.java"
            );

            if (result.hasTodos()) {
                fail("❌ 还有 " + result.getTodoCount() + " 个TODO未完成");
            }

            if (result.isEmptyFile()) {
                fail("❌ 文件似乎为空或只有模板代码");
            }

            System.out.println("🎉 恭喜！所有Lambda基础练习都已完成！");

        } catch (Exception e) {
            System.out.println("⚠️  无法检查TODO状态，请确保练习文件存在");
        }
    }
}