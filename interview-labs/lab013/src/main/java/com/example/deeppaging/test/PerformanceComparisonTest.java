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
 * 深分页性能对比测试
 * 综合比较传统分页与各种优化方案的性能差异
 */
public class PerformanceComparisonTest {
    private static final int PAGE_SIZE = 20;
    private static final int WARMUP_ROUNDS = 3; // 预热轮数
    private static final int TEST_ROUNDS = 5;    // 测试轮数
    
    public static void main(String[] args) {
        System.out.println("=== 深分页性能对比测试 ===");
        System.out.println("预热轮数: " + WARMUP_ROUNDS + ", 测试轮数: " + TEST_ROUNDS);
        
        // 测试不同深度的分页场景
        int[] offsets = {0, 10000, 50000, 100000, 200000, 500000};
        
        for (int offset : offsets) {
            System.out.printf("%n=== 分页深度: %d (第 %d 页) ===%n", offset, offset / PAGE_SIZE + 1);
            compareAllMethods(offset, PAGE_SIZE);
        }
        
        // 生成性能报告
        generatePerformanceReport();
    }
    
    /**
     * 比较所有分页方法的性能
     */
    private static void compareAllMethods(int offset, int pageSize) {
        System.out.printf("%-20s | %-12s | %-12s | %-12s | %-8s%n", 
            "方法", "平均时间(ms)", "最小时间(ms)", "最大时间(ms)", "记录数");
        System.out.println("-".repeat(80));
        
        // 1. 传统分页
        PerformanceResult traditionalResult = testTraditionalPagingPerformance(offset, pageSize);
        printResult("传统分页", traditionalResult);
        
        // 2. 基于ID分页（使用offset对应的ID）
        long startId = getIdByOffset(offset);
        if (startId > 0) {
            PerformanceResult idBasedResult = testIdBasedPagingPerformance(startId, pageSize);
            printResult("基于ID分页", idBasedResult);
        }
        
        // 3. 子查询优化
        PerformanceResult subqueryResult = testSubqueryOptimizationPerformance(offset, pageSize);
        printResult("子查询优化", subqueryResult);
        
        // 4. 覆盖索引优化
        PerformanceResult coveringResult = testCoveringIndexOptimizationPerformance(offset, pageSize);
        printResult("覆盖索引优化", coveringResult);
    }
    
    /**
     * 测试传统分页性能
     */
    private static PerformanceResult testTraditionalPagingPerformance(int offset, int pageSize) {
        String sql = "SELECT id, user_id, username, email, age, city, created_at FROM test_data ORDER BY id LIMIT ?, ?";
        return executePerformanceTest(sql, offset, pageSize);
    }
    
    /**
     * 测试基于ID分页性能
     */
    private static PerformanceResult testIdBasedPagingPerformance(long startId, int pageSize) {
        String sql = "SELECT id, user_id, username, email, age, city, created_at FROM test_data WHERE id > ? ORDER BY id LIMIT ?";
        return executePerformanceTest(sql, startId, pageSize);
    }
    
    /**
     * 测试子查询优化性能
     */
    private static PerformanceResult testSubqueryOptimizationPerformance(int offset, int pageSize) {
        String sql = """SELECT t.id, t.user_id, t.username, t.email, t.age, t.city, t.created_at 
                       FROM test_data t 
                       INNER JOIN (
                           SELECT id FROM test_data ORDER BY id LIMIT ?, ?
                       ) tmp ON t.id = tmp.id 
                       ORDER BY t.id""";
        return executePerformanceTest(sql, offset, pageSize);
    }
    
    /**
     * 测试覆盖索引优化性能
     */
    private static PerformanceResult testCoveringIndexOptimizationPerformance(int offset, int pageSize) {
        String sql = "SELECT id, username FROM test_data WHERE status = 1 ORDER BY created_at, id LIMIT ?, ?";
        return executePerformanceTest(sql, offset, pageSize);
    }
    
    /**
     * 执行性能测试
     */
    private static PerformanceResult executePerformanceTest(String sql, Object param1, Object param2) {
        List<Double> executionTimes = new ArrayList<>();
        int recordCount = 0;
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 预热
            for (int i = 0; i < WARMUP_ROUNDS; i++) {
                executeQuery(conn, sql, param1, param2);
            }
            
            // 正式测试
            for (int i = 0; i < TEST_ROUNDS; i++) {
                long startTime = System.nanoTime();
                recordCount = executeQuery(conn, sql, param1, param2);
                long endTime = System.nanoTime();
                
                double executionTime = (endTime - startTime) / 1_000_000.0; // 转换为毫秒
                executionTimes.add(executionTime);
            }
            
        } catch (SQLException e) {
            System.err.println("性能测试失败: " + e.getMessage());
            return new PerformanceResult(0, 0, 0, 0);
        }
        
        // 计算统计数据
        double avgTime = executionTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double minTime = executionTimes.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxTime = executionTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        
        return new PerformanceResult(avgTime, minTime, maxTime, recordCount);
    }
    
    /**
     * 执行查询并返回记录数
     */
    private static int executeQuery(Connection conn, String sql, Object param1, Object param2) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (param1 instanceof Long) {
                pstmt.setLong(1, (Long) param1);
            } else {
                pstmt.setInt(1, (Integer) param1);
            }
            pstmt.setInt(2, (Integer) param2);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                return count;
            }
        }
    }
    
    /**
     * 根据offset获取对应的ID
     */
    private static long getIdByOffset(int offset) {
        if (offset == 0) return 0;
        
        String sql = "SELECT id FROM test_data ORDER BY id LIMIT ?, 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取ID失败: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * 打印性能结果
     */
    private static void printResult(String method, PerformanceResult result) {
        System.out.printf("%-20s | %12.2f | %12.2f | %12.2f | %8d%n",
            method, result.avgTime(), result.minTime(), result.maxTime(), result.recordCount());
    }
    
    /**
     * 生成性能报告
     */
    private static void generatePerformanceReport() {
        System.out.println("\n=== 性能分析报告 ===");
        System.out.println("""
        深分页性能测试结论：
        
        1. 传统分页 (LIMIT offset, size):
           - 随着offset增大，查询时间呈线性增长
           - 深分页时性能急剧下降，需要扫描大量不需要的记录
           - 适用场景：浅分页（前几页）
        
        2. 基于ID分页 (WHERE id > last_id):
           - 查询时间基本恒定，不受分页深度影响
           - 利用主键索引，性能最优
           - 限制：只能顺序翻页，不能跳页
        
        3. 子查询优化:
           - 先查询ID再关联，减少数据传输
           - 性能介于传统分页和ID分页之间
           - 适用场景：需要跳页但要优化深分页性能
        
        4. 覆盖索引优化:
           - 避免回表查询，减少I/O操作
           - 查询字段必须包含在索引中
           - 适用场景：查询字段较少且有合适的覆盖索引
        
        推荐方案选择：
        - 顺序浏览：使用基于ID的分页
        - 需要跳页：使用子查询优化
        - 字段较少：考虑覆盖索引优化
        - 浅分页：传统分页即可
        """);
    }
    
    /**
     * 性能测试结果记录
     */
    public record PerformanceResult(
        double avgTime,
        double minTime,
        double maxTime,
        int recordCount
    ) {}
}