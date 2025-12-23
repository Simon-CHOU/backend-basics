package com.simon.practice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 成就模型
 */
public class Achievement {
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("icon")
    private String icon;

    @JsonProperty("category")
    private AchievementCategory category;

    @JsonProperty("earnedAt")
    private LocalDateTime earnedAt;

    @JsonProperty("points")
    private int points;

    // 构造器
    public Achievement() {
        this.earnedAt = LocalDateTime.now();
    }

    public Achievement(String id, String title, String description, String icon,
                      AchievementCategory category, int points) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.category = category;
        this.points = points;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public AchievementCategory getCategory() { return category; }
    public void setCategory(AchievementCategory category) { this.category = category; }

    public LocalDateTime getEarnedAt() { return earnedAt; }
    public void setEarnedAt(LocalDateTime earnedAt) { this.earnedAt = earnedAt; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    @Override
    public String toString() {
        return String.format("%s %s - %s (%d分)", icon, title, description, points);
    }

    /**
     * 成就分类
     */
    public enum AchievementCategory {
        COMPLETION("完成类", "🏆"),
        STREAK("连续类", "🔥"),
        SPEED("速度类", "⚡"),
        MASTERY("精通类", "🎓"),
        EXPLORATION("探索类", "🔍");

        private final String displayName;
        private final String icon;

        AchievementCategory(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
}