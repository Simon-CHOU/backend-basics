package com.simon.stream;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stream API 测试
 * 采用TDD方式验证Stream API的各种特性
 */
class StreamApiTest {

    private List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private List<String> words = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");

    @Test
    void testBasicMapOperation() {
        // AC1: 验证map操作
        List<Integer> squares = numbers.stream()
            .map(x -> x * x)
            .collect(Collectors.toList());
        
        assertEquals(Arrays.asList(1, 4, 9, 16, 25, 36, 49, 64, 81, 100), squares);
    }

    @Test
    void testBasicFilterOperation() {
        // AC1: 验证filter操作
        List<Integer> evens = numbers.stream()
            .filter(x -> x % 2 == 0)
            .collect(Collectors.toList());
        
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), evens);
    }

    @Test
    void testBasicReduceOperation() {
        // AC1: 验证reduce操作
        int sum = numbers.stream()
            .reduce(0, Integer::sum);
        
        assertEquals(55, sum);
        
        // 使用identity值的reduce
        int product = numbers.stream()
            .reduce(1, (a, b) -> a * b);
        
        assertEquals(3628800, product); // 10!
    }

    @Test
    void testLazyEvaluation() {
        // AC1: 验证惰性求值
        AtomicInteger mapCount = new AtomicInteger(0);
        AtomicInteger filterCount = new AtomicInteger(0);
        
        Stream<Integer> lazyStream = numbers.stream()
            .map(x -> {
                mapCount.incrementAndGet();
                return x * 2;
            })
            .filter(x -> {
                filterCount.incrementAndGet();
                return x > 5;
            });
        
        // 中间操作不会立即执行
        assertEquals(0, mapCount.get());
        assertEquals(0, filterCount.get());
        
        // 只有终止操作才会触发执行
        List<Integer> result = lazyStream.collect(Collectors.toList());
        
        assertEquals(Arrays.asList(6, 8, 10, 12, 14, 16, 18, 20), result);
        assertTrue(mapCount.get() > 0);
        assertTrue(filterCount.get() > 0);
    }

    @Test
    void testOperationOrderMatters() {
        // AC1: 验证操作顺序的重要性
        
        // 先filter后map
        List<Integer> result1 = numbers.stream()
            .filter(x -> x % 2 == 0)
            .map(x -> x * x)
            .collect(Collectors.toList());
        
        // 先map后filter
        List<Integer> result2 = numbers.stream()
            .map(x -> x * x)
            .filter(x -> x % 2 == 0)
            .collect(Collectors.toList());
        
        assertEquals(Arrays.asList(4, 16, 36, 64, 100), result1);
        assertEquals(Arrays.asList(4, 16, 36, 64, 100), result2);
        
        // 虽然结果相同，但性能可能不同
        // filter先执行可以减少map操作次数
    }

    @Test
    void testCollectorsToSet() {
        // AC2: 验证Collectors.toSet()
        Set<Integer> set = numbers.stream()
            .collect(Collectors.toSet());
        
        assertEquals(10, set.size());
        assertTrue(set.containsAll(numbers));
        
        // 去重测试
        List<Integer> duplicates = Arrays.asList(1, 2, 2, 3, 3, 3);
        Set<Integer> unique = duplicates.stream()
            .collect(Collectors.toSet());
        
        assertEquals(3, unique.size());
    }

    @Test
    void testCollectorsGroupingBy() {
        // AC2: 验证分组收集器
        Map<Integer, List<String>> byLength = words.stream()
            .collect(Collectors.groupingBy(String::length));
        
        assertEquals(3, byLength.size());
        assertTrue(byLength.containsKey(5)); // apple
        assertTrue(byLength.containsKey(6)); // banana, cherry
        assertTrue(byLength.containsKey(10)); // elderberry
        
        assertEquals(1, byLength.get(5).size());
        assertEquals(2, byLength.get(6).size());
    }

    @Test
    void testCollectorsJoining() {
        // AC2: 验证字符串连接收集器
        String result = words.stream()
            .collect(Collectors.joining(", "));
        
        assertEquals("apple, banana, cherry, date, elderberry", result);
        
        // 带前缀后缀
        String withDecorators = words.stream()
            .collect(Collectors.joining(", ", "[", "]"));
        
        assertEquals("[apple, banana, cherry, date, elderberry]", withDecorators);
    }

    @Test
    void testParallelStreamThreadSafety() {
        // AC3: 验证并行流的线程安全性要求
        
        // 非线程安全的操作
        List<Integer> unsafeResult = Collections.synchronizedList(new ArrayList<>());
        assertDoesNotThrow(() -> 
            numbers.parallelStream()
                .forEach(unsafeResult::add)
        );
        
        // 结果可能乱序，但应包含所有元素
        assertEquals(10, unsafeResult.size());
        assertTrue(unsafeResult.containsAll(numbers));
        
        // 使用线程安全的收集器
        List<Integer> safeResult = numbers.parallelStream()
            .collect(Collectors.toList());
        
        assertEquals(numbers, safeResult);
    }

    @Test
    void testInfiniteStream() {
        // AC4: 验证无限流
        List<Integer> firstFive = Stream.iterate(0, n -> n + 1)
            .limit(5)
            .collect(Collectors.toList());
        
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), firstFive);
        
        // 生成随机数的无限流
        List<Double> randoms = Stream.generate(Math::random)
            .limit(3)
            .collect(Collectors.toList());
        
        assertEquals(3, randoms.size());
        randoms.forEach(r -> {
            assertTrue(r >= 0.0);
            assertTrue(r < 1.0);
        });
    }

    @Test
    void testPrimitiveStreams() {
        // AC4: 验证原始类型流
        int sum = IntStream.rangeClosed(1, 5)
            .sum();
        
        assertEquals(15, sum);
        
        double average = IntStream.of(1, 2, 3, 4, 5)
            .average()
            .orElse(0);
        
        assertEquals(3.0, average, 0.001);
        
        // 避免装箱开销
        int[] array = IntStream.range(0, 5)
            .toArray();
        
        assertArrayEquals(new int[]{0, 1, 2, 3, 4}, array);
    }

    @Test
    void testFlatMapOperation() {
        // AC4: 验证flatMap操作
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2),
            Arrays.asList(3, 4),
            Arrays.asList(5, 6)
        );
        
        List<Integer> flat = nested.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), flat);
    }

    @Test
    void testShortCircuitOperations() {
        // AC4: 验证短路操作
        AtomicInteger count = new AtomicInteger(0);
        
        Optional<Integer> first = numbers.stream()
            .peek(x -> count.incrementAndGet())
            .filter(x -> x > 5)
            .findFirst();
        
        assertEquals(6, first.orElse(-1));
        // 由于短路，count应该小于10
        assertTrue(count.get() < 10);
        
        // anyMatch也是短路操作
        count.set(0);
        boolean hasEven = numbers.stream()
            .peek(x -> count.incrementAndGet())
            .anyMatch(x -> x % 2 == 0);
        
        assertTrue(hasEven);
        assertTrue(count.get() < 10); // 遇到第一个偶数就停止
    }

    @Test
    void testCustomCollector() {
        // AC4: 验证自定义收集器（概念验证）
        
        // 使用预定义收集器实现自定义逻辑
        String result = words.stream()
            .collect(Collectors.collectingAndThen(
                Collectors.joining("|"),
                String::toUpperCase
            ));
        
        assertEquals("APPLE|BANANA|CHERRY|DATE|ELDERBERRY", result);
    }

    @Test
    void testEmptyStreamHandling() {
        // 验证空流处理
        List<Integer> empty = Collections.emptyList();
        
        int sum = empty.stream()
            .mapToInt(Integer::intValue)
            .sum();
        
        assertEquals(0, sum);
        
        Optional<Integer> max = empty.stream()
            .max(Integer::compare);
        
        assertFalse(max.isPresent());
    }

    @Test
    void testStreamReusePrevention() {
        // 验证流不能被重复使用
        Stream<Integer> stream = numbers.stream();
        
        // 第一次使用
        long count = stream.count();
        assertEquals(10, count);
        
        // 第二次使用应该抛出异常
        assertThrows(IllegalStateException.class, () -> 
            stream.count()
        );
    }

    @Test
    void testPeekOperation() {
        // 验证peek操作（主要用于调试）
        List<Integer> peeked = new ArrayList<>();
        
        List<Integer> result = numbers.stream()
            .peek(peeked::add)
            .filter(x -> x % 2 == 0)
            .collect(Collectors.toList());
        
        // peek会看到所有元素
        assertEquals(numbers, peeked);
        
        // 但结果只包含偶数
        assertEquals(Arrays.asList(2, 4, 6, 8, 10), result);
    }
}