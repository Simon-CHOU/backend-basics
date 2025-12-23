package com.simon.practice.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.simon.practice.model.*;
import com.simon.practice.utils.TodoDetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 练习引擎核心类
 * 负责管理练习、运行测试、跟踪进度等
 */
public class ExerciseEngine {

    private static final String PROGRESS_FILE = "progress.json";
    private static final String EXERCISES_CONFIG_FILE = "config/exercises.json";

    private final ObjectMapper objectMapper;
    private final Map<String, Exercise> exercises;
    private Progress progress;
    private final ExecutorService testExecutor;

    public ExerciseEngine() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.exercises = new HashMap<>();
        this.testExecutor = Executors.newSingleThreadExecutor();

        initializeExercises();
        loadProgress();
    }

    /**
     * 初始化练习题配置
     */
    private void initializeExercises() {
        // Lambda基础练习
        Exercise lambdaBasics = new Exercise(
            "lambda-basics",
            "Lambda表达式基础",
            "学习Lambda表达式的基本语法和常用函数式接口的使用",
            "lambda",
            Difficulty.BEGINNER,
            100
        );
        lambdaBasics.setHints(Arrays.asList(
            "Lambda表达式的基本语法: (参数) -> { 表达式 }",
            "Consumer<T>: 接受一个参数但不返回结果",
            "Function<T,R>: 接受一个参数并返回结果"
        ));
        lambdaBasics.setTemplateFile("src/practice/java/com/simon/practice/exercises/lambda/LambdaBasicsExercise.java");
        lambdaBasics.setTestFile("src/test/java/com/simon/practice/validator/LambdaBasicsValidator.java");
        exercises.put("lambda-basics", lambdaBasics);

        // 函数式接口练习
        Exercise functionalInterface = new Exercise(
            "functional-interface",
            "函数式接口进阶",
            "深入理解函数式接口和高级函数组合技术",
            "functional",
            Difficulty.INTERMEDIATE,
            150
        );
        functionalInterface.setHints(Arrays.asList(
            "自定义函数式接口需要添加@FunctionalInterface注解",
            "函数组合使用andThen和compose方法",
            "方法引用简化Lambda表达式: Class::method"
        ));
        functionalInterface.setTemplateFile("src/practice/java/com/simon/practice/exercises/functional/FunctionalInterfaceExercise.java");
        functionalInterface.setTestFile("src/test/java/com/simon/practice/validator/FunctionalInterfaceValidator.java");
        exercises.put("functional-interface", functionalInterface);

        // Stream API练习
        Exercise streamApi = new Exercise(
            "stream-api",
            "Stream API实战",
            "掌握Stream API的各种操作和并行处理",
            "stream",
            Difficulty.INTERMEDIATE,
            200
        );
        streamApi.setHints(Arrays.asList(
            "Stream操作分为中间操作和终端操作",
            "使用Collectors进行复杂的数据收集",
            "并行流使用parallelStream()提升性能"
        ));
        streamApi.setTemplateFile("src/practice/java/com/simon/practice/exercises/stream/StreamApiExercise.java");
        streamApi.setTestFile("src/test/java/com/simon/practice/validator/StreamApiValidator.java");
        exercises.put("stream-api", streamApi);

        // 计算总分
        int maxTotalScore = exercises.values().stream()
            .mapToInt(Exercise::getMaxScore)
            .sum();

        // 更新进度中的总分
        if (progress == null) {
            progress = new Progress("学生", exercises.size());
        }
        progress.setMaxTotalScore(maxTotalScore);
    }

    /**
     * 加载学习进度
     */
    private void loadProgress() {
        File progressFile = new File(PROGRESS_FILE);
        if (progressFile.exists()) {
            try {
                this.progress = objectMapper.readValue(progressFile, Progress.class);
            } catch (IOException e) {
                System.err.println("警告：无法读取进度文件，使用默认进度");
                this.progress = new Progress("学生", exercises.size());
            }
        } else {
            this.progress = new Progress("学生", exercises.size());
        }
    }

    /**
     * 保存学习进度
     */
    public void saveProgress() {
        try {
            objectMapper.writeValue(new File(PROGRESS_FILE), progress);
        } catch (IOException e) {
            System.err.println("警告：无法保存进度文件");
        }
    }

    /**
     * 获取所有练习题
     */
    public List<Exercise> getExercises() {
        return new ArrayList<>(exercises.values());
    }

    /**
     * 按类别和难度筛选练习题
     */
    public List<Exercise> getExercises(String category, String difficulty) {
        return exercises.values().stream()
            .filter(e -> category == null || category.equalsIgnoreCase(e.getCategory()))
            .filter(e -> difficulty == null ||
                     difficulty.equalsIgnoreCase(e.getDifficulty().name()))
            .collect(Collectors.toList());
    }

    /**
     * 获取指定ID的练习题
     */
    public Exercise getExercise(String exerciseId) {
        return exercises.get(exerciseId);
    }

    /**
     * 运行指定练习题
     */
    public TestResult runExercise(String exerciseId, boolean verbose) {
        Exercise exercise = exercises.get(exerciseId);
        if (exercise == null) {
            return new TestResult(false, 0, 0, "练习题不存在: " + exerciseId);
        }

        progress.updateLastActivity();
        exercise.incrementAttempts();

        try {
            // 检查TODO完成情况
            TodoDetector.TodoResult todoResult = checkTodos(exercise);

            if (todoResult.hasTodos()) {
                String message = String.format("还有 %d 个TODO标记需要完成", todoResult.getTodoCount());
                return new TestResult(false, 0, exercise.getMaxScore(), message);
            }

            if (todoResult.isEmptyFile()) {
                return new TestResult(false, 0, exercise.getMaxScore(), "请开始编写练习代码");
            }

            // 运行测试
            TestExecutionResult testResult = runTests(exercise, verbose);

            if (testResult.isSuccess()) {
                int score = calculateScore(exercise, testResult);
                exercise.markAsCompleted(score);

                // 更新进度
                Progress.ExerciseProgress exerciseProgress = new Progress.ExerciseProgress();
                exerciseProgress.setCompleted(true);
                exerciseProgress.setScore(score);
                exerciseProgress.setMaxScore(exercise.getMaxScore());
                exerciseProgress.setAttempts(exercise.getAttempts());
                exerciseProgress.setCompletedAt(exercise.getCompletedAt());

                progress.addExerciseProgress(exerciseId, exerciseProgress);

                checkAchievements();
                saveProgress();

                return new TestResult(true, score, exercise.getMaxScore(), null);
            } else {
                // 测试失败
                StringBuilder errorMessage = new StringBuilder("测试未通过");
                if (testResult.hasCompileErrors()) {
                    errorMessage.append("\n编译错误:\n").append(testResult.getCompileErrors());
                }
                if (testResult.hasTestFailures()) {
                    errorMessage.append("\n测试失败:\n").append(testResult.getTestFailures());
                }

                return new TestResult(false, 0, exercise.getMaxScore(), errorMessage.toString());
            }

        } catch (Exception e) {
            return new TestResult(false, 0, exercise.getMaxScore(),
                "运行练习时出错: " + e.getMessage());
        }
    }

    /**
     * 运行所有练习题测试
     */
    public TestAllResult runAllExercises(boolean failFast, boolean verbose) {
        List<Exercise> allExercises = new ArrayList<>(exercises.values());
        List<Exercise> failedExercises = new ArrayList<>();
        int totalScore = 0;
        int maxTotalScore = 0;

        for (Exercise exercise : allExercises) {
            TestResult result = runExercise(exercise.getId(), verbose);

            maxTotalScore += exercise.getMaxScore();
            totalScore += result.getScore();

            if (!result.isSuccess()) {
                failedExercises.add(exercise);
                if (failFast) {
                    break;
                }
            }
        }

        return new TestAllResult(
            allExercises.size(),
            allExercises.size() - failedExercises.size(),
            failedExercises.size(),
            totalScore,
            maxTotalScore,
            failedExercises
        );
    }

    /**
     * 检查TODO完成情况
     */
    private TodoDetector.TodoResult checkTodos(Exercise exercise) {
        try {
            Path templatePath = Paths.get(exercise.getTemplateFile());
            if (!Files.exists(templatePath)) {
                return new TodoDetector.TodoResult(false, 0, 0, false);
            }
            return TodoDetector.detectTodos(templatePath);
        } catch (Exception e) {
            return new TodoDetector.TodoResult(false, 0, 0, false);
        }
    }

    /**
     * 运行测试（简化版本）
     */
    private TestExecutionResult runTests(Exercise exercise, boolean verbose) {
        // 这里应该使用JUnit或其他测试框架来实际运行测试
        // 为了简化，我们返回一个模拟的成功结果

        // 在实际实现中，这里应该：
        // 1. 编译源代码
        // 2. 运行对应的JUnit测试
        // 3. 收集测试结果和编译错误

        return new TestExecutionResult(true, null, null);
    }

    /**
     * 计算得分
     */
    private int calculateScore(Exercise exercise, TestExecutionResult testResult) {
        // 基础分数：根据尝试次数计算
        int attempts = exercise.getAttempts();
        int baseScore = Math.max(exercise.getMaxScore() - (attempts - 1) * 5, exercise.getMaxScore() / 2);

        // 实际实现中可以根据测试通过率调整分数
        return baseScore;
    }

    /**
     * 检查并授予成就
     */
    public void checkAchievements() {
        double completionPercentage = progress.getOverallCompletionPercentage();

        // 完成度成就
        if (completionPercentage >= 25 && !progress.hasAchievement("first-quarter")) {
            Achievement achievement = new Achievement(
                "first-quarter",
                "初学者",
                "完成25%的练习",
                "🌱",
                Achievement.AchievementCategory.COMPLETION,
                10
            );
            progress.addAchievement(achievement);
        }

        if (completionPercentage >= 50 && !progress.hasAchievement("halfway")) {
            Achievement achievement = new Achievement(
                "halfway",
                "进阶者",
                "完成50%的练习",
                "🚀",
                Achievement.AchievementCategory.COMPLETION,
                25
            );
            progress.addAchievement(achievement);
        }

        if (completionPercentage >= 100 && !progress.hasAchievement("master")) {
            Achievement achievement = new Achievement(
                "master",
                "函数式编程大师",
                "完成所有练习",
                "👑",
                Achievement.AchievementCategory.MASTERY,
                100
            );
            progress.addAchievement(achievement);
        }

        // 难度成就
        boolean hasCompletedBeginner = progress.getExerciseProgress().values().stream()
            .anyMatch(ep -> ep.isCompleted());

        if (hasCompletedBeginner && !progress.hasAchievement("first-steps")) {
            Achievement achievement = new Achievement(
                "first-steps",
                "迈出第一步",
                "完成第一道练习题",
                "👣",
                Achievement.AchievementCategory.EXPLORATION,
                5
            );
            progress.addAchievement(achievement);
        }
    }

    /**
     * 获取学习进度
     */
    public Progress getProgress() {
        return progress;
    }

    /**
     * 重置学习进度
     */
    public void resetProgress() {
        this.progress = new Progress("学生", exercises.size());

        // 重置所有练习的完成状态
        exercises.values().forEach(exercise -> {
            exercise.setCompleted(false);
            exercise.setScore(0);
            exercise.setAttempts(0);
            exercise.setCompletedAt(null);
        });

        saveProgress();
    }

    /**
     * 测试结果
     */
    public static class TestResult {
        private final boolean success;
        private final int score;
        private final int maxScore;
        private final String errorMessage;
        private final String compileErrors;
        private final String testFailures;

        public TestResult(boolean success, int score, int maxScore, String errorMessage) {
            this.success = success;
            this.score = score;
            this.maxScore = maxScore;
            this.errorMessage = errorMessage;
            this.compileErrors = null;
            this.testFailures = null;
        }

        public boolean isSuccess() { return success; }
        public int getScore() { return score; }
        public int getMaxScore() { return maxScore; }
        public String getErrorMessage() { return errorMessage; }
        public boolean hasCompileErrors() { return compileErrors != null; }
        public String getCompileErrors() { return compileErrors; }
        public boolean hasTestFailures() { return testFailures != null; }
        public String getTestFailures() { return testFailures; }
    }

    /**
     * 所有练习测试结果
     */
    public static class TestAllResult {
        private final int totalCount;
        private final int passedCount;
        private final int failedCount;
        private final int totalScore;
        private final int maxTotalScore;
        private final List<Exercise> failedExercises;

        public TestAllResult(int totalCount, int passedCount, int failedCount,
                           int totalScore, int maxTotalScore, List<Exercise> failedExercises) {
            this.totalCount = totalCount;
            this.passedCount = passedCount;
            this.failedCount = failedCount;
            this.totalScore = totalScore;
            this.maxTotalScore = maxTotalScore;
            this.failedExercises = failedExercises;
        }

        public int getTotalCount() { return totalCount; }
        public int getPassedCount() { return passedCount; }
        public int getFailedCount() { return failedCount; }
        public int getTotalScore() { return totalScore; }
        public int getMaxTotalScore() { return maxTotalScore; }
        public List<Exercise> getFailedExercises() { return failedExercises; }
        public boolean hasFailures() { return failedCount > 0; }
    }

    /**
     * 测试执行结果
     */
    private static class TestExecutionResult {
        private final boolean success;
        private final String compileErrors;
        private final String testFailures;

        public TestExecutionResult(boolean success, String compileErrors, String testFailures) {
            this.success = success;
            this.compileErrors = compileErrors;
            this.testFailures = testFailures;
        }

        public boolean isSuccess() { return success; }
        public boolean hasCompileErrors() { return compileErrors != null; }
        public String getCompileErrors() { return compileErrors; }
        public boolean hasTestFailures() { return testFailures != null; }
        public String getTestFailures() { return testFailures; }
    }
}