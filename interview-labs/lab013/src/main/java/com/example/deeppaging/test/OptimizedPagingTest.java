package com.example.deeppaging.test;

import com.example.deeppaging.config.DatabaseConfig;
import com.example.deeppaging.test.TraditionalPagingTest.TestRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 优化分页查询性能测试
 * 实现三种优化方案：基于ID分页、子查询优化、覆盖索引优化
 */
public class OptimizedPagingTest {
    private static final int PAGE_SIZE = 20;
    
    public static void main(String[] args) {
        System.out.println("=== 优化分页查询性能测试 ===");
        
        // 测试不同的起始ID值（模拟深分页场景）
        long[] startIds = {1, 1000, 10000, 50000, 100000, 200000, 500000, 800000};
        
        System.out.println("\n--- 方案1: 基于主键ID的分页查询 ---");
        for (long startId : startIds) {
            testIdBasedPaging(startId, PAGE_SIZE);
        }
        
        System.out.println("\n--- 方案2: 子查询优化LIMIT ---");
        int[] offsets = {0, 1000, 10000, 50000, 100000, 200000, 500000, 800000};
        for (int offset : offsets) {
            testSubqueryOptimization(offset, PAGE_SIZE);
        }
        
        System.out.println("\n--- 方案3: 覆盖索引优化 ---");
        for (int offset : offsets) {
            testCoveringIndexOptimization(offset, PAGE_SIZE);
        }
    }
    
    /**
     * 方案1: 基于主键ID的分页查询
     * 使用 WHERE id > last_id LIMIT size 替代 LIMIT offset, size
     */
    private static void testIdBasedPaging(long lastId, int pageSize) {
        String sql = "SELECT id, user_id, username, email, age, city, created_at FROM test_data WHERE id > ? ORDER BY id LIMIT ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, lastId);
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
                double executionTime = (endTime - startTime) / 1_000_000.0;
                
                System.out.printf("LastID: %8d | 查询时间: %8.2f ms | 返回记录数: %2d%n", 
                    lastId, executionTime, records.size());
                
                if (!records.isEmpty()) {
                    System.out.printf("           | 第一条记录ID: %d%n", records.get(0).id());
                }
            }
            
        } catch (SQLException e) {
            System.err.printf("ID分页查询失败 (lastId=%d): %s%n", lastId, e.getMessage());
        }
    }
    
    /**
     * 方案2: 子查询优化LIMIT
     * 先通过子查询获取ID，再关联查询完整数据
     */
    private static void testSubqueryOptimization(int offset, int pageSize) {
        String sql = """
                SELECT t.id, t.user_id, t.username, t.email, t.age, t.city, t.created_at 
                FROM test_data t 
                INNER JOIN (
                    SELECT id FROM test_data ORDER BY id LIMIT ?, ?
                ) tmp ON t.id = tmp.id 
                ORDER BY t.id
                """;
        
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
                double executionTime = (endTime - startTime) / 1_000_000.0;
                
                System.out.printf("Offset: %8d | 查询时间: %8.2f ms | 返回记录数: %2d%n", 
                    offset, executionTime, records.size());
                
                if (!records.isEmpty()) {
                    System.out.printf("           | 第一条记录ID: %d%n", records.get(0).id());
                }
            }
            
        } catch (SQLException e) {
            System.err.printf("子查询优化失败 (offset=%d): %s%n", offset, e.getMessage());
        }
    }
    
    /**
     * 方案3: 覆盖索引优化
     * 使用覆盖索引避免回表查询
     */
    private static void testCoveringIndexOptimization(int offset, int pageSize) {
        // 使用覆盖索引 idx_covering(status, created_at, id, username)
        String sql = "SELECT id, username FROM test_data WHERE status = 1 ORDER BY created_at, id LIMIT ?, ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, offset);
            pstmt.setInt(2, pageSize);
            
            long startTime = System.nanoTime();
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<CoveringIndexRecord> records = new ArrayList<>();
                
                while (rs.next()) {
                    CoveringIndexRecord record = new CoveringIndexRecord(
                        rs.getLong("id"),
                        rs.getString("username")
                    );
                    records.add(record);
                }
                
                long endTime = System.nanoTime();
                double executionTime = (endTime - startTime) / 1_000_000.0;
                
                System.out.printf("Offset: %8d | 查询时间: %8.2f ms | 返回记录数: %2d%n", 
                    offset, executionTime, records.size());
                
                if (!records.isEmpty()) {
                    System.out.printf("           | 第一条记录ID: %d%n", records.get(0).id());
                }
            }
            
        } catch (SQLException e) {
            System.err.printf("覆盖索引优化失败 (offset=%d): %s%n", offset, e.getMessage());
        }
    }
    
    /**
     * 覆盖索引查询记录
     */
    public record CoveringIndexRecord(long id, String username) {}
    
    /**
     * 执行EXPLAIN分析优化查询的执行计划
     */
    public static void explainOptimizedQueries() {
        System.out.println("\n=== 优化方案EXPLAIN分析 ===");
        
        // 分析基于ID的分页查询
        explainIdBasedPaging(50000, PAGE_SIZE);
        
        // 分析子查询优化
        explainSubqueryOptimization(50000, PAGE_SIZE);
        
        // 分析覆盖索引优化
        explainCoveringIndexOptimization(50000, PAGE_SIZE);
    }
    
    private static void explainIdBasedPaging(long lastId, int pageSize) {
        String sql = "EXPLAIN SELECT id, user_id, username, email, age, city, created_at FROM test_data WHERE id > ? ORDER BY id LIMIT ?";
        executeExplain("基于ID分页", sql, lastId, pageSize);
    }
    
    private static void explainSubqueryOptimization(int offset, int pageSize) {
        String sql = """
                EXPLAIN SELECT t.id, t.user_id, t.username, t.email, t.age, t.city, t.created_at 
                FROM test_data t 
                INNER JOIN (
                    SELECT id FROM test_data ORDER BY id LIMIT ?, ?
                ) tmp ON t.id = tmp.id 
                ORDER BY t.id
                """;
        executeExplain("子查询优化", sql, offset, pageSize);
    }
    
    private static void explainCoveringIndexOptimization(int offset, int pageSize) {
        String sql = "EXPLAIN SELECT id, username FROM test_data WHERE status = 1 ORDER BY created_at, id LIMIT ?, ?";
        executeExplain("覆盖索引优化", sql, offset, pageSize);
    }
    
    private static void executeExplain(String method, String sql, Object param1, Object param2) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (param1 instanceof Long) {
                pstmt.setLong(1, (Long) param1);
            } else {
                pstmt.setInt(1, (Integer) param1);
            }
            pstmt.setInt(2, (Integer) param2);
            
            System.out.printf("%n--- %s EXPLAIN分析 ---%n", method);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.printf("%-5s %-15s %-10s %-15s %-10s %-10s %-15s %-30s%n",
                    "id", "select_type", "table", "type", "key", "rows", "filtered", "Extra");
                System.out.println("-".repeat(130));
                
                while (rs.next()) {
                    System.out.printf("%-5s %-15s %-10s %-15s %-10s %-10s %-15s %-30s%n",
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
            System.err.printf("%s EXPLAIN查询失败: %s%n", method, e.getMessage());
        }
    }
}