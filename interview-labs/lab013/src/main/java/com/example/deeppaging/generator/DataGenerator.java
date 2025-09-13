package com.example.deeppaging.generator;

import com.example.deeppaging.config.DatabaseConfig;
import com.github.javafaker.Faker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Random;

/**
 * 测试数据生成器
 * 生成100万条测试数据用于深分页实验
 */
public class DataGenerator {
    private static final int TOTAL_RECORDS = 1_000_000;
    private static final int BATCH_SIZE = 10_000;
    private static final Faker faker = new Faker(Locale.ENGLISH);
    private static final Random random = new Random();
    
    private static final String[] CITIES = {
        "北京", "上海", "广州", "深圳", "杭州", "南京", "武汉", "成都", 
        "西安", "重庆", "天津", "苏州", "长沙", "郑州", "青岛", "大连"
    };
    
    public static void main(String[] args) {
        System.out.println("开始生成测试数据...");
        long startTime = System.currentTimeMillis();
        
        try {
            generateTestData();
            long endTime = System.currentTimeMillis();
            System.out.printf("数据生成完成！总计: %d 条记录，耗时: %.2f 秒%n", 
                TOTAL_RECORDS, (endTime - startTime) / 1000.0);
        } catch (SQLException e) {
            System.err.println("数据生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 生成测试数据
     */
    private static void generateTestData() throws SQLException {
        String sql = """
                INSERT INTO test_data 
                (user_id, username, email, age, city, status, score, description) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 关闭自动提交以提高批量插入性能
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int batchCount = 0;
                
                for (int i = 1; i <= TOTAL_RECORDS; i++) {
                    pstmt.setInt(1, random.nextInt(100000) + 1); // user_id
                    pstmt.setString(2, faker.name().username()); // username
                    pstmt.setString(3, faker.internet().emailAddress()); // email
                    pstmt.setInt(4, random.nextInt(60) + 18); // age: 18-77
                    pstmt.setString(5, CITIES[random.nextInt(CITIES.length)]); // city
                    pstmt.setInt(6, random.nextInt(3) + 1); // status: 1-3
                    pstmt.setDouble(7, random.nextDouble() * 1000); // score: 0-1000
                    pstmt.setString(8, faker.lorem().sentence(10)); // description
                    
                    pstmt.addBatch();
                    batchCount++;
                    
                    // 每BATCH_SIZE条记录执行一次批量插入
                    if (batchCount == BATCH_SIZE) {
                        pstmt.executeBatch();
                        conn.commit();
                        batchCount = 0;
                        
                        // 显示进度
                        if (i % (BATCH_SIZE * 10) == 0) {
                            System.out.printf("已生成 %d 条记录 (%.1f%%)%n", 
                                i, (double) i / TOTAL_RECORDS * 100);
                        }
                    }
                }
                
                // 处理剩余的记录
                if (batchCount > 0) {
                    pstmt.executeBatch();
                    conn.commit();
                }
            }
        }
    }
}