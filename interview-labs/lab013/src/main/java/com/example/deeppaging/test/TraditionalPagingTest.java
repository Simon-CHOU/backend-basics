package com.example.deeppaging.test;

import com.example.deeppaging.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 传统分页查询性能测试
 * 使用 LIMIT offset, size 语法进行分页查询
 */
public class TraditionalPagingTest {
    private static final int PAGE_SIZE = 20;
    
    public static void main(String[] args) {
        System.out.println("=== 传统分页查询性能测试 ===");
        
        // 测试不同的offset值
        int[] offsets = {0, 1000, 10000, 50000, 100000, 200000, 500000, 800000};
        
        for (int offset : offsets) {
            testTraditionalPaging(offset, PAGE_SIZE);
        }
    }
    
    /**
     * 测试传统分页查询性能
     * @param offset 偏移量
     * @param pageSize 页面大小
     */
    private static void testTraditionalPaging(int offset, int pageSize) {
        String sql = "SELECT id, user_id, username, email, age, city, created_at FROM test_data ORDER BY id LIMIT ?, ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, offset);
            pstmt.setInt(2, pageSize);
            
            long startTime = System.nanoTime();
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<TestRecord> records = new ArrayList<>();
                
                while (rs.next()) {
                    TestRecord record = new TestRecord(
                        rs.getLong("id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getInt("age"),
                        rs.getString("city"),
                        rs.getTimestamp("created_at")
                    );
                    records.add(record);
                }
                
                long endTime = System.nanoTime();
                double executionTime = (endTime - startTime) / 1_000_000.0; // 转换为毫秒
                
                System.out.printf("Offset: %8d | 查询时间: %8.2f ms | 返回记录数: %2d%n", 
                    offset, executionTime, records.size());
                
                // 显示第一条记录的ID（用于验证查询正确性）
                if (!records.isEmpty()) {
                    System.out.printf("           | 第一条记录ID: %d%n", records.get(0).id());
                }
            }
            
        } catch (SQLException e) {
            System.err.printf("查询失败 (offset=%d): %s%n", offset, e.getMessage());
        }
    }
    
    /**
     * 测试记录数据结构
     */
    public record TestRecord(
        long id,
        int userId,
        String username,
        String email,
        int age,
        String city,
        java.sql.Timestamp createdAt
    ) {}
    
    /**
     * 执行EXPLAIN分析查询计划
     */
    public static void explainQuery(int offset, int pageSize) {
        String explainSql = "EXPLAIN SELECT id, user_id, username, email, age, city, created_at FROM test_data ORDER BY id LIMIT ?, ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(explainSql)) {
            
            pstmt.setInt(1, offset);
            pstmt.setInt(2, pageSize);
            
            System.out.printf("%n=== EXPLAIN 分析 (offset=%d) ===%n", offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // 打印表头
                System.out.printf("%-5s %-15s %-10s %-15s %-10s %-10s %-15s %-20s%n",
                    "id", "select_type", "table", "type", "key", "rows", "filtered", "Extra");
                System.out.println("-".repeat(120));
                
                while (rs.next()) {
                    System.out.printf("%-5s %-15s %-10s %-15s %-10s %-10s %-15s %-20s%n",
                        rs.getString("id"),
                        rs.getString("select_type"),
                        rs.getString("table"),
                        rs.getString("type"),
                        rs.getString("key"),
                        rs.getString("rows"),
                        rs.getString("filtered"),
                        rs.getString("Extra"));
                }
            }
            
        } catch (SQLException e) {
            System.err.printf("EXPLAIN 查询失败: %s%n", e.getMessage());
        }
    }
}