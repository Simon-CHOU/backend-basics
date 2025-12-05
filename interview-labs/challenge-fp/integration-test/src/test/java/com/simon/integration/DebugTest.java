package com.simon.integration;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class DebugTest {
    public static void main(String[] args) {
        List<String> data = Arrays.asList("apple", "banana", "cherry", "date", "elderberry");
        
        Function<String, String> processString = s -> s.toUpperCase().replace("A", "@");
        Predicate<String> lengthFilter = s -> s.length() >= 5;
        
        Map<Integer, List<String>> groupedResult = data.stream()
            .map(processString)
            .filter(lengthFilter)
            .collect(Collectors.groupingBy(
                String::length,
                Collectors.toList()
            ));
        
        System.out.println("分组结果: " + groupedResult);
        System.out.println("分组大小: " + groupedResult.size());
        System.out.println("分组键: " + groupedResult.keySet());
        
        // 打印每个处理后的字符串及其长度
        System.out.println("\n处理后的字符串:");
        data.stream()
            .map(processString)
            .filter(lengthFilter)
            .forEach(s -> System.out.println(s + " -> 长度: " + s.length()));
    }
}