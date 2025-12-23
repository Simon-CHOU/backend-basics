package com.simon.practice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 练习题模型
 */
public class Exercise {
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("difficulty")
    private Difficulty difficulty;

    @JsonProperty("hints")
    private List<String> hints;

    @JsonProperty("templateFile")
    private String templateFile;

    @JsonProperty("testFile")
    private String testFile;

    @JsonProperty("completed")
    private boolean completed;

    @JsonProperty("completedAt")
    private LocalDateTime completedAt;

    @JsonProperty("attempts")
    private int attempts;

    @JsonProperty("score")
    private int score;

    @JsonProperty("maxScore")
    private int maxScore;

    // 构造器
    public Exercise() {}

    public Exercise(String id, String title, String description, String category,
                   Difficulty difficulty, int maxScore) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.maxScore = maxScore;
        this.score = 0;
        this.completed = false;
        this.attempts = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public List<String> getHints() { return hints; }
    public void setHints(List<String> hints) { this.hints = hints; }

    public String getTemplateFile() { return templateFile; }
    public void setTemplateFile(String templateFile) { this.templateFile = templateFile; }

    public String getTestFile() { return testFile; }
    public void setTestFile(String testFile) { this.testFile = testFile; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void markAsCompleted(int score) {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
        this.score = Math.min(score, this.maxScore);
    }

    public double getCompletionPercentage() {
        return maxScore > 0 ? (double) score / maxScore * 100 : 0;
    }

    @Override
    public String toString() {
        return String.format("Exercise{id='%s', title='%s', category='%s', completed=%s, score=%d/%d}",
                id, title, category, completed, score, maxScore);
    }
}