package com.simon.functional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;
import java.util.List;
import java.util.ArrayList;

/**
 * 函数式接口测试
 * 采用TDD方式验证函数式接口特性
 */
class FunctionalInterfaceTest {

    @Test
    void testConsumerInterface() {
        // AC2: 验证Consumer接口
        List<String> consumed = new ArrayList<>();
        
        Consumer<String> consumer = consumed::add;
        consumer.accept("test1");
        consumer.accept("test2");
        
        assertEquals(2, consumed.size());
        assertEquals("test1", consumed.get(0));
        assertEquals("test2", consumed.get(1));
        
        // 测试andThen组合
        List<String> secondList = new ArrayList<>();
        Consumer<String> combined = consumer.andThen(secondList::add);
        
        combined.accept("combined");
        
        assertEquals(3, consumed.size());
        assertEquals(1, secondList.size());
        assertEquals("combined", secondList.get(0));
    }

    @Test
    void testSupplierInterface() {
        // AC2: 验证Supplier接口
        Supplier<String> constantSupplier = () -> "constant";
        assertEquals("constant", constantSupplier.get());
        
        // 测试延迟计算
        Supplier<Double> randomSupplier = Math::random;
        double first = randomSupplier.get();
        double second = randomSupplier.get();
        
        assertNotEquals(first, second); // 大概率不同
        assertTrue(first >= 0 && first < 1);
        assertTrue(second >= 0 && second < 1);
    }

    @Test
    void testFunctionInterface() {
        // AC2: 验证Function接口
        Function<String, Integer> lengthFunction = String::length;
        assertEquals(5, lengthFunction.apply("Hello"));
        assertEquals(0, lengthFunction.apply(""));
        
        // 测试compose和andThen
        Function<Integer, Integer> doubleFunction = x -> x * 2;
        Function<String, Integer> composed = lengthFunction.andThen(doubleFunction);
        assertEquals(10, composed.apply("Hello"));
        
        Function<Integer, String> toString = Object::toString;
        Function<String, String> composed2 = lengthFunction.andThen(toString);
        assertEquals("5", composed2.apply("Hello"));
    }

    @Test
    void testPredicateInterface() {
        // AC2: 验证Predicate接口
        Predicate<String> isLong = s -> s.length() > 3;
        assertTrue(isLong.test("long"));
        assertFalse(isLong.test("big"));
        
        // 测试组合操作
        Predicate<String> containsA = s -> s.contains("a");
        Predicate<String> longAndContainsA = isLong.and(containsA);
        
        assertTrue(longAndContainsA.test("banana"));
        assertFalse(longAndContainsA.test("hello")); // hello长度足够但不包含a
        assertFalse(longAndContainsA.test("abc"));   // 长度不足
        
        // 测试negate
        Predicate<String> notLong = isLong.negate();
        assertTrue(notLong.test("abc"));
        assertFalse(notLong.test("abcd"));
    }

    @Test
    void testCustomFunctionalInterface() {
        // AC3: 验证自定义函数式接口
        TriFunction<Integer, Integer, Integer, Integer> sumThree = 
            (a, b, c) -> a + b + c;
        
        assertEquals(6, sumThree.apply(1, 2, 3));
        assertEquals(0, sumThree.apply(0, 0, 0));
        assertEquals(-3, sumThree.apply(-1, -1, -1));
        
        // 测试默认方法
        assertDoesNotThrow(sumThree::logUsage);
    }

    @Test
    void testMethodReferences() {
        // 验证方法引用
        
        // 静态方法引用
        Function<String, Integer> parseInt = Integer::parseInt;
        assertEquals(123, parseInt.apply("123"));
        
        // 实例方法引用
        String testString = "Hello";
        Supplier<Integer> lengthSupplier = testString::length;
        assertEquals(5, lengthSupplier.get());
        
        // 构造方法引用
        Supplier<List<String>> listSupplier = ArrayList::new;
        List<String> list = listSupplier.get();
        assertNotNull(list);
        assertTrue(list.isEmpty());
        
        list.add("test");
        assertEquals(1, list.size());
    }

    @Test
    void testUnaryOperator() {
        // 验证UnaryOperator
        UnaryOperator<String> toUpper = String::toUpperCase;
        assertEquals("HELLO", toUpper.apply("hello"));
        assertEquals("", toUpper.apply(""));
        
        UnaryOperator<Integer> increment = x -> x + 1;
        assertEquals(6, increment.apply(5));
    }

    @Test
    void testBinaryOperator() {
        // 验证BinaryOperator
        BinaryOperator<Integer> multiply = (a, b) -> a * b;
        assertEquals(15, multiply.apply(3, 5));
        assertEquals(0, multiply.apply(0, 5));
        
        BinaryOperator<String> concat = String::concat;
        assertEquals("HelloWorld", concat.apply("Hello", "World"));
    }

    @Test
    void testBiFunction() {
        // 验证BiFunction
        BiFunction<String, String, Integer> totalLength = 
            (s1, s2) -> s1.length() + s2.length();
        
        assertEquals(10, totalLength.apply("Hello", "World"));
        assertEquals(0, totalLength.apply("", ""));
    }

    @Test
    void testFunctionalInterfaceAnnotation() {
        // AC1: 验证@FunctionalInterface注解约束
        
        // 正确的函数式接口
        @FunctionalInterface
        interface ValidFunctional {
            void execute();
            default void helper() {}
            static void utility() {}
        }
        
        ValidFunctional valid = () -> {};
        assertDoesNotThrow(valid::execute);
        
        // 测试注解的编译时检查（通过编译错误来验证）
        // 以下代码如果取消注释应该导致编译错误
        /*
        @FunctionalInterface
        interface InvalidFunctional {
            void execute();
            void anotherMethod(); // 多个抽象方法 - 应该编译错误
        }
        */
    }

    @Test
    void testInterfaceComposition() {
        // 验证接口组合
        Function<String, String> upperCase = String::toUpperCase;
        Function<String, String> addExclamation = s -> s + "!";
        
        Function<String, String> excited = upperCase.andThen(addExclamation);
        assertEquals("HELLO!", excited.apply("hello"));
        
        Function<String, String> reverseOrder = addExclamation.compose(upperCase);
        assertEquals("HELLO!", reverseOrder.apply("hello"));
    }

    @Test
    void testPredicateComposition() {
        // 验证Predicate组合
        Predicate<Integer> isEven = x -> x % 2 == 0;
        Predicate<Integer> isPositive = x -> x > 0;
        
        Predicate<Integer> isPositiveEven = isEven.and(isPositive);
        assertTrue(isPositiveEven.test(4));
        assertFalse(isPositiveEven.test(-2));
        assertFalse(isPositiveEven.test(3));
        
        Predicate<Integer> isOdd = isEven.negate();
        assertTrue(isOdd.test(3));
        assertFalse(isOdd.test(4));
    }

    @Test
    void testCurryingWithCustomInterface() {
        // 使用自定义接口实现柯里化
        TriFunction<Integer, Integer, Integer, Integer> curriedAdd = 
            (a, b, c) -> a + b + c;
        
        // 部分应用
        Function<Integer, Function<Integer, Function<Integer, Integer>>> 
            curried = a -> b -> c -> a + b + c;
        
        assertEquals(6, curried.apply(1).apply(2).apply(3));
        assertEquals(6, curriedAdd.apply(1, 2, 3));
    }
}