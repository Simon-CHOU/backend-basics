package com.simon.practice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 学习进度模型
 */
public class Progress {
    @JsonProperty("studentName")
    private String studentName;

    @JsonProperty("startedAt")
    private LocalDateTime startedAt;

    @JsonProperty("lastActivityAt")
    private LocalDateTime lastActivityAt;

    @JsonProperty("totalScore")
    private int totalScore;

    @JsonProperty("maxTotalScore")
    private int maxTotalScore;

    @JsonProperty("completedExercises")
    private int completedExercises;

    @JsonProperty("totalExercises")
    private int totalExercises;

    @JsonProperty("exerciseProgress")
    private Map<String, ExerciseProgress> exerciseProgress;

    @JsonProperty("achievements")
    private Map<String, Achievement> achievements;

    // 构造器
    public Progress() {
        this.startedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.exerciseProgress = new HashMap<>();
        this.achievements = new HashMap<>();
    }

    public Progress(String studentName, int totalExercises) {
        this();
        this.studentName = studentName;
        this.totalExercises = totalExercises;
        this.completedExercises = 0;
        this.totalScore = 0;
        this.maxTotalScore = 0;
    }

    // Getters and Setters
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public int getMaxTotalScore() { return maxTotalScore; }
    public void setMaxTotalScore(int maxTotalScore) { this.maxTotalScore = maxTotalScore; }

    public int getCompletedExercises() { return completedExercises; }
    public void setCompletedExercises(int completedExercises) { this.completedExercises = completedExercises; }

    public int getTotalExercises() { return totalExercises; }
    public void setTotalExercises(int totalExercises) { this.totalExercises = totalExercises; }

    public Map<String, ExerciseProgress> getExerciseProgress() { return exerciseProgress; }
    public void setExerciseProgress(Map<String, ExerciseProgress> exerciseProgress) { this.exerciseProgress = exerciseProgress; }

    public Map<String, Achievement> getAchievements() { return achievements; }
    public void setAchievements(Map<String, Achievement> achievements) { this.achievements = achievements; }

    // 业务方法
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    public void addExerciseProgress(String exerciseId, ExerciseProgress progress) {
        this.exerciseProgress.put(exerciseId, progress);
        updateSummary();
    }

    public ExerciseProgress getExerciseProgress(String exerciseId) {
        return exerciseProgress.getOrDefault(exerciseId, new ExerciseProgress());
    }

    public void addAchievement(Achievement achievement) {
        this.achievements.put(achievement.getId(), achievement);
    }

    public boolean hasAchievement(String achievementId) {
        return achievements.containsKey(achievementId);
    }

    public double getOverallCompletionPercentage() {
        if (maxTotalScore == 0) return 0;
        return (double) totalScore / maxTotalScore * 100;
    }

    private void updateSummary() {
        this.completedExercises = (int) exerciseProgress.values().stream()
                .mapToLong(ep -> ep.isCompleted() ? 1 : 0)
                .sum();

        this.totalScore = exerciseProgress.values().stream()
                .mapToInt(ExerciseProgress::getScore)
                .sum();
    }

    /**
     * 练习进度详情
     */
    public static class ExerciseProgress {
        private boolean completed;
        private int score;
        private int maxScore;
        private int attempts;
        private LocalDateTime completedAt;
        private LocalDateTime lastAttemptAt;

        public ExerciseProgress() {
            this.completed = false;
            this.score = 0;
            this.maxScore = 0;
            this.attempts = 0;
        }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public int getMaxScore() { return maxScore; }
        public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }

        public LocalDateTime getCompletedAt() { return completedAt; }
        public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

        public LocalDateTime getLastAttemptAt() { return lastAttemptAt; }
        public void setLastAttemptAt(LocalDateTime lastAttemptAt) { this.lastAttemptAt = lastAttemptAt; }
    }
}