package com.simon.mastering_spring_data_jpa.dto;


import java.util.Optional;

public enum Gender {
    BOY,
    GIRL;

    public String getDbValue() {
        if (this == BOY) {
            return "M";
        } else if (this == GIRL) {
            return "F";
        } else {
            return "O";
        }
    }

    public static Optional<Gender> getGenderByValue(String dbData) {
        return switch (dbData) {
            case "M" -> Optional.of(BOY);    // 匹配 "M" 时返回 BOY 的 Optional
            case "F" -> Optional.of(GIRL);   // 匹配 "F" 时返回 GIRL 的 Optional
            default -> Optional.empty();     // 无匹配时返回空 Optional（由 orElse 提供默认值）
        };
    }
}
