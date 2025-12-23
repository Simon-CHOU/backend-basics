package com.simon.practice.model;

/**
 * 练习难度等级
 */
public enum Difficulty {
    BEGINNER("初学者", "🟢", 1),
    INTERMEDIATE("中级", "🟡", 2),
    ADVANCED("高级", "🔴", 3),
    EXPERT("专家", "🟣", 4);

    private final String displayName;
    private final String icon;
    private final int level;

    Difficulty(String displayName, String icon, int level) {
        this.displayName = displayName;
        this.icon = icon;
        this.level = level;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public int getLevel() { return level; }

    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}