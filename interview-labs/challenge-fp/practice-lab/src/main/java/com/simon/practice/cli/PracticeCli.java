package com.simon.practice.cli;

import com.simon.practice.core.ExerciseEngine;
import com.simon.practice.model.Exercise;
import com.simon.practice.model.Progress;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 函数式编程练习CLI工具
 * 提供交互式练习环境和进度跟踪
 */
@Command(
    name = "fp-practice",
    description = "Java函数式编程交互式练习系统",
    version = "1.0.0",
    mixinStandardHelpOptions = true,
    headerHeading = "Java函数式编程练习系统%n%n",
    commandListHeading = "%n可用命令：%n",
    parameterListHeading = "%n参数：%n",
    optionListHeading = "%n选项：%n"
)
public class PracticeCli implements Callable<Integer> {

    private final ExerciseEngine engine;

    public PracticeCli() {
        this.engine = new ExerciseEngine();
        AnsiConsole.systemInstall(); // 启用彩色输出
    }

    @Option(names = {"-v", "--verbose"}, description = "显示详细输出")
    private boolean verbose;

    @Option(names = {"-c", "--color"}, description = "启用彩色输出（默认开启）")
    private boolean colorEnabled = true;

    @Override
    public Integer call() {
        printBanner();
        printStatus();
        return 0;
    }

    @Command(name = "list", description = "列出所有练习题")
    public int list(
        @Option(names = {"-c", "--category"}, description = "按类别筛选") String category,
        @Option(names = {"-d", "--difficulty"}, description = "按难度筛选") String difficulty
    ) {
        List<Exercise> exercises = engine.getExercises(category, difficulty);

        if (exercises.isEmpty()) {
            printWarning("没有找到匹配的练习题");
            return 1;
        }

        printSuccess("找到 " + exercises.size() + " 道练习题：");
        System.out.println();

        for (Exercise exercise : exercises) {
            printExerciseSummary(exercise);
        }

        return 0;
    }

    @Command(name = "run", description = "运行指定的练习题")
    public int run(
        @Parameters(description = "练习题ID（如: lambda-basics, functional-interface, stream-api）") String exerciseId,
        @Option(names = {"-v", "--verbose"}, description = "显示详细输出") boolean verbose
    ) {
        printInfo("正在运行练习: " + exerciseId);
        System.out.println();

        Exercise exercise = engine.getExercise(exerciseId);
        if (exercise == null) {
            printError("练习题不存在: " + exerciseId);
            return 1;
        }

        ExerciseEngine.TestResult result = engine.runExercise(exerciseId, verbose);

        if (result.isSuccess()) {
            printSuccess("🎉 恭喜！练习 '" + exercise.getTitle() + "' 完成成功！");
            System.out.println();

            if (result.getScore() >= result.getMaxScore()) {
                printPerfectScore("完美！获得满分 " + result.getMaxScore() + " 分！");
            } else {
                printScore("得分: " + result.getScore() + "/" + result.getMaxScore());
            }

            // 显示进度
            Progress progress = engine.getProgress();
            printProgress(progress);

            // 检查成就
            checkAchievements(progress);

        } else {
            printError("❌ 练习未完成，请继续努力！");
            System.out.println();

            if (result.getErrorMessage() != null) {
                printError("错误信息: " + result.getErrorMessage());
            }

            if (result.hasCompileErrors()) {
                printWarning("编译错误信息:");
                System.out.println(result.getCompileErrors());
            }

            if (result.hasTestFailures()) {
                printWarning("测试失败信息:");
                System.out.println(result.getTestFailures());
            }

            // 显示提示
            showHints(exercise);
        }

        return result.isSuccess() ? 0 : 1;
    }

    @Command(name = "test-all", description = "运行所有练习题测试")
    public int testAll(
        @Option(names = {"-f", "--fail-fast"}, description = "遇到失败立即停止") boolean failFast,
        @Option(names = {"-v", "--verbose"}, description = "显示详细输出") boolean verbose
    ) {
        printInfo("开始运行所有练习题测试...");
        System.out.println();

        ExerciseEngine.TestAllResult allResult = engine.runAllExercises(failFast, verbose);

        printTestResults(allResult);

        if (allResult.hasFailures()) {
            return 1;
        }

        printSuccess("🎊 太棒了！所有练习都已完成！");
        printAchievements(engine.getProgress());

        return 0;
    }

    @Command(name = "progress", description = "显示学习进度")
    public int progress() {
        Progress progress = engine.getProgress();
        printProgress(progress);
        printAchievements(progress);
        return 0;
    }

    @Command(name = "reset", description = "重置学习进度")
    public int reset(
        @Option(names = {"-y", "--yes"}, description = "确认重置") boolean confirmed
    ) {
        if (!confirmed) {
            printWarning("这将重置你的所有学习进度！使用 -y 选项确认。");
            return 1;
        }

        engine.resetProgress();
        printSuccess("学习进度已重置");
        return 0;
    }

    @Command(name = "hint", description = "获取练习提示")
    public int hint(
        @Parameters(description = "练习题ID") String exerciseId,
        @Option(names = {"-l", "--level"}, description = "提示级别 (1-3)") Integer level
    ) {
        Exercise exercise = engine.getExercise(exerciseId);
        if (exercise == null) {
            printError("练习题不存在: " + exerciseId);
            return 1;
        }

        showHints(exercise, level != null ? level : 1);
        return 0;
    }

    // 私有辅助方法
    private void printBanner() {
        System.out.println(
            Ansi.ansi()
                .fg(Ansi.Color.CYAN)
                .a("╔══════════════════════════════════════════════════════════════╗\n")
                .a("║                    Java函数式编程练习系统                      ║\n")
                .a("║                      FP Practice Lab                         ║\n")
                .a("╚══════════════════════════════════════════════════════════════╝")
                .reset()
        );
        System.out.println();
    }

    private void printStatus() {
        Progress progress = engine.getProgress();

        System.out.println(
            colorize("📊 学习进度: ", Ansi.Color.YELLOW) +
            colorize(String.format("%.1f%%", progress.getOverallCompletionPercentage()),
                    progress.getOverallCompletionPercentage() >= 100 ? Ansi.Color.GREEN : Ansi.Color.CYAN) +
            colorize(" (" + progress.getTotalScore() + "/" + progress.getMaxTotalScore() + " 分)", Ansi.Color.WHITE)
        );

        System.out.println(
            colorize("✅ 已完成: ", Ansi.Color.GREEN) +
            colorize(progress.getCompletedExercises() + "/" + progress.getTotalExercises() + " 道题", Ansi.Color.WHITE)
        );

        System.out.println();
        printInfo("使用 'fp-practice --help' 查看可用命令");
        System.out.println();
    }

    private void printExerciseSummary(Exercise exercise) {
        String status = exercise.isCompleted()
            ? colorize("✅ 已完成", Ansi.Color.GREEN)
            : colorize("❌ 未完成", Ansi.Color.RED);

        String score = exercise.isCompleted()
            ? colorize("(" + exercise.getScore() + "/" + exercise.getMaxScore() + ")", Ansi.Color.YELLOW)
            : colorize("(未评分)", Ansi.Color.WHITE);

        System.out.println(
            String.format("%s %s - %s %s %s",
                exercise.getDifficulty().getIcon(),
                colorize(exercise.getId(), Ansi.Color.CYAN),
                colorize(exercise.getTitle(), Ansi.Color.WHITE),
                status,
                score
            )
        );

        if (verbose) {
            System.out.println("   " + colorize(exercise.getDescription(), Ansi.Color.WHITE));
        }
        System.out.println();
    }

    private void printTestResults(ExerciseEngine.TestAllResult allResult) {
        printSuccess("测试完成！");
        System.out.println();

        System.out.println(
            colorize("通过: ", Ansi.Color.GREEN) +
            colorize(allResult.getPassedCount() + "/" + allResult.getTotalCount(), Ansi.Color.WHITE)
        );

        System.out.println(
            colorize("失败: ", Ansi.Color.RED) +
            colorize(allResult.getFailedCount() + "/" + allResult.getTotalCount(), Ansi.Color.WHITE)
        );

        System.out.println(
            colorize("总分: ", Ansi.Color.YELLOW) +
            colorize(allResult.getTotalScore() + "/" + allResult.getMaxTotalScore(), Ansi.Color.WHITE)
        );

        System.out.println();

        if (allResult.hasFailures()) {
            printWarning("失败的练习题:");
            allResult.getFailedExercises().forEach(ex ->
                System.out.println("  ❌ " + ex.getId() + " - " + ex.getTitle())
            );
            System.out.println();
        }
    }

    private void printProgress(Progress progress) {
        double percentage = progress.getOverallCompletionPercentage();
        Ansi.Color color = percentage >= 80 ? Ansi.Color.GREEN :
                          percentage >= 50 ? Ansi.Color.YELLOW : Ansi.Color.RED;

        System.out.println(
            colorize("📈 总体进度: ", Ansi.Color.CYAN) +
            colorize(String.format("%.1f%%", percentage), color) +
            colorize(" (" + progress.getTotalScore() + "/" + progress.getMaxTotalScore() + " 分)", Ansi.Color.WHITE)
        );

        System.out.println(
            colorize("✅ 已完成练习: ", Ansi.Color.GREEN) +
            colorize(progress.getCompletedExercises() + "/" + progress.getTotalExercises(), Ansi.Color.WHITE)
        );

        System.out.println();
    }

    private void printAchievements(Progress progress) {
        if (progress.getAchievements().isEmpty()) {
            printInfo("还没有获得成就，继续努力！");
            return;
        }

        printSuccess("🏆 获得成就:");
        progress.getAchievements().values().forEach(achievement ->
            System.out.println("  " + achievement.getIcon() + " " + achievement.getTitle())
        );
        System.out.println();
    }

    private void showHints(Exercise exercise) {
        showHints(exercise, 1);
    }

    private void showHints(Exercise exercise, int level) {
        if (exercise.getHints() == null || exercise.getHints().isEmpty()) {
            printInfo("暂无提示可用");
            return;
        }

        int hintLevel = Math.min(level, exercise.getHints().size());
        printInfo("提示 (级别 " + hintLevel + "):");
        System.out.println("💡 " + exercise.getHints().get(hintLevel - 1));
        System.out.println();
    }

    private void checkAchievements(Progress progress) {
        // 这里可以添加成就检查逻辑
        engine.checkAchievements();
    }

    // 颜色输出辅助方法
    private String colorize(String text, Ansi.Color color) {
        if (!colorEnabled) return text;
        return Ansi.ansi().fg(color).a(text).reset().toString();
    }

    private void printSuccess(String message) {
        System.out.println(colorize("✅ " + message, Ansi.Color.GREEN));
    }

    private void printError(String message) {
        System.out.println(colorize("❌ " + message, Ansi.Color.RED));
    }

    private void printWarning(String message) {
        System.out.println(colorize("⚠️  " + message, Ansi.Color.YELLOW));
    }

    private void printInfo(String message) {
        System.out.println(colorize("ℹ️  " + message, Ansi.Color.CYAN));
    }

    private void printScore(String message) {
        System.out.println(colorize("🏆 " + message, Ansi.Color.YELLOW));
    }

    private void printPerfectScore(String message) {
        System.out.println(colorize("🌟 " + message, Ansi.Color.MAGENTA));
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PracticeCli()).execute(args);
        System.exit(exitCode);
    }
}