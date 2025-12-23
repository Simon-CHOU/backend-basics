package com.simon.practice.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO检测工具类
 * 用于检测Java源文件中的TODO标记
 */
public class TodoDetector {

    private static final Pattern TODO_PATTERN = Pattern.compile(
        "//\\s*TODO|/\\*\\s*TODO\\s*\\*/|/\\*\\s*TODO.*?\\*/",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /**
     * 检测文件中是否包含TODO标记
     *
     * @param filePath 文件路径
     * @return Todo检测结果
     */
    public static TodoResult detectTodos(Path filePath) {
        try {
            String content = Files.readString(filePath);
            return detectTodos(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
        }
    }

    /**
     * 检测字符串内容中的TODO标记
     *
     * @param content 文件内容
     * @return Todo检测结果
     */
    public static TodoResult detectTodos(String content) {
        Matcher matcher = TODO_PATTERN.matcher(content);

        int todoCount = 0;
        int lineCount = 0;
        String[] lines = content.split("\n");

        // 检查每一行
        for (int i = 0; i < lines.length; i++) {
            lineCount++;
            String line = lines[i].trim();

            // 检查单行TODO
            if (line.matches("(?i).*//\\s*TODO.*")) {
                todoCount++;
            }
        }

        // 检查多行TODO注释
        matcher.reset();
        while (matcher.find()) {
            // 多行TODO可能已经在单行检查中计算过，这里只计算新增的
            String matched = matcher.group();
            if (!matched.startsWith("//")) {
                todoCount++;
            }
        }

        boolean hasTodos = todoCount > 0;
        boolean isEmptyFile = content.trim().isEmpty() || content.trim().equals("// TODO: 实现这个方法");

        return new TodoResult(hasTodos, todoCount, lineCount, isEmptyFile);
    }

    /**
     * TODO检测结果
     */
    public static class TodoResult {
        private final boolean hasTodos;
        private final int todoCount;
        private final int totalLines;
        private final boolean isEmptyFile;

        public TodoResult(boolean hasTodos, int todoCount, int totalLines, boolean isEmptyFile) {
            this.hasTodos = hasTodos;
            this.todoCount = todoCount;
            this.totalLines = totalLines;
            this.isEmptyFile = isEmptyFile;
        }

        public boolean hasTodos() { return hasTodos; }
        public int getTodoCount() { return todoCount; }
        public int getTotalLines() { return totalLines; }
        public boolean isEmptyFile() { return isEmptyFile; }

        public boolean isCompleted() {
            return !hasTodos && !isEmptyFile && totalLines > 5; // 至少有一些实际代码
        }

        @Override
        public String toString() {
            return String.format("TodoResult{hasTodos=%s, todoCount=%d, totalLines=%d, isEmptyFile=%s, completed=%s}",
                    hasTodos, todoCount, totalLines, isEmptyFile, isCompleted());
        }
    }
}