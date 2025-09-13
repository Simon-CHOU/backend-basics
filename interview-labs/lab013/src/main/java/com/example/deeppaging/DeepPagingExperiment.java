package com.example.deeppaging;

import com.example.deeppaging.config.DatabaseConfig;
import com.example.deeppaging.generator.DataGenerator;
import com.example.deeppaging.test.OptimizedPagingTest;
import com.example.deeppaging.test.PerformanceComparisonTest;
import com.example.deeppaging.test.TraditionalPagingTest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * 深分页问题实验主控制类
 * 按步骤执行完整的深分页性能测试实验
 */
public class DeepPagingExperiment {
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== 深分页问题实验 ===");
        System.out.println("本实验将通过实际操作深入理解深分页问题及其解决方案\n");
        
        try {
            // 检查数据库连接
            checkDatabaseConnection();
            
            // 显示实验菜单
            showExperimentMenu();
            
        } catch (Exception e) {
            System.err.println("实验执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConfig.close();
            scanner.close();
        }
    }
    
    /**
     * 检查数据库连接
     */
    private static void checkDatabaseConnection() {
        System.out.println("步骤1: 检查数据库连接...");
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("✓ 数据库连接成功");
            
            // 检查表是否存在
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM test_data")) {
                
                if (rs.next()) {
                    int recordCount = rs.getInt("count");
                    System.out.printf("✓ 测试表存在，当前记录数: %,d%n", recordCount);
                    
                    if (recordCount == 0) {
                        System.out.println("⚠ 测试表为空，需要生成测试数据");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("✗ 数据库连接失败: " + e.getMessage());
            System.err.println("请确保:");
            System.err.println("1. Docker容器已启动: docker-compose up -d");
            System.err.println("2. MySQL服务正常运行");
            System.err.println("3. 数据库配置正确");
            throw new RuntimeException("数据库连接失败", e);
        }
    }
    
    /**
     * 显示实验菜单
     */
    private static void showExperimentMenu() {
        while (true) {
            System.out.println("\n=== 实验菜单 ===");
            System.out.println("1. 生成测试数据 (100万条记录)");
            System.out.println("2. 传统分页性能测试");
            System.out.println("3. EXPLAIN分析查询计划");
            System.out.println("4. 优化方案测试");
            System.out.println("5. 优化方案EXPLAIN分析");
            System.out.println("6. 综合性能对比测试");
            System.out.println("7. 查看数据库统计信息");
            System.out.println("8. 完整实验流程");
            System.out.println("0. 退出");
            System.out.print("\n请选择操作 (0-8): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1" -> generateTestData();
                case "2" -> runTraditionalPagingTest();
                case "3" -> runExplainAnalysis();
                case "4" -> runOptimizedPagingTest();
                case "5" -> runOptimizedExplainAnalysis();
                case "6" -> runPerformanceComparison();
                case "7" -> showDatabaseStats();
                case "8" -> runCompleteExperiment();
                case "0" -> {
                    System.out.println("实验结束，感谢使用！");
                    return;
                }
                default -> System.out.println("无效选择，请重新输入");
            }
        }
    }
    
    /**
     * 生成测试数据
     */
    private static void generateTestData() {
        System.out.println("\n=== 步骤2: 生成测试数据 ===");
        System.out.print("确认生成100万条测试数据？这可能需要几分钟时间 (y/N): ");
        
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y") || confirm.equals("yes")) {
            DataGenerator.main(new String[]{});
        } else {
            System.out.println("已取消数据生成");
        }
    }
    
    /**
     * 运行传统分页测试
     */
    private static void runTraditionalPagingTest() {
        System.out.println("\n=== 步骤3: 传统分页性能测试 ===");
        TraditionalPagingTest.main(new String[]{});
    }
    
    /**
     * 运行EXPLAIN分析
     */
    private static void runExplainAnalysis() {
        System.out.println("\n=== 步骤4: EXPLAIN分析查询计划 ===");
        
        // 分析不同offset的查询计划
        int[] offsets = {0, 10000, 100000, 500000};
        
        for (int offset : offsets) {
            TraditionalPagingTest.explainQuery(offset, 20);
        }
    }
    
    /**
     * 运行优化方案测试
     */
    private static void runOptimizedPagingTest() {
        System.out.println("\n=== 步骤5: 优化方案测试 ===");
        OptimizedPagingTest.main(new String[]{});
    }
    
    /**
     * 运行优化方案EXPLAIN分析
     */
    private static void runOptimizedExplainAnalysis() {
        System.out.println("\n=== 步骤6: 优化方案EXPLAIN分析 ===");
        OptimizedPagingTest.explainOptimizedQueries();
    }
    
    /**
     * 运行性能对比测试
     */
    private static void runPerformanceComparison() {
        System.out.println("\n=== 步骤7: 综合性能对比测试 ===");
        PerformanceComparisonTest.main(new String[]{});
    }
    
    /**
     * 显示数据库统计信息
     */
    private static void showDatabaseStats() {
        System.out.println("\n=== 数据库统计信息 ===");
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 表记录数
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM test_data")) {
                if (rs.next()) {
                    System.out.printf("总记录数: %,d%n", rs.getInt("total"));
                }
            }
            
            // ID范围
            try (ResultSet rs = stmt.executeQuery("SELECT MIN(id) as min_id, MAX(id) as max_id FROM test_data")) {
                if (rs.next()) {
                    System.out.printf("ID范围: %d - %d%n", rs.getLong("min_id"), rs.getLong("max_id"));
                }
            }
            
            // 索引信息
            System.out.println("\n索引信息:");
            try (ResultSet rs = stmt.executeQuery("SHOW INDEX FROM test_data")) {
                System.out.printf("%-20s %-15s %-20s %-10s%n", "表名", "索引名", "列名", "基数");
                System.out.println("-".repeat(70));
                
                while (rs.next()) {
                    System.out.printf("%-20s %-15s %-20s %-10s%n",
                        rs.getString("Table"),
                        rs.getString("Key_name"),
                        rs.getString("Column_name"),
                        rs.getString("Cardinality"));
                }
            }
            
            // 表大小信息
            System.out.println("\n表大小信息:");
            try (ResultSet rs = stmt.executeQuery(
                "SELECT table_name, ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'DB Size in MB' " +
                "FROM information_schema.tables " +
                "WHERE table_schema = 'deep_paging_test' AND table_name = 'test_data'")) {
                
                while (rs.next()) {
                    System.out.printf("表 %s 大小: %.2f MB%n", 
                        rs.getString("table_name"), 
                        rs.getDouble("DB Size in MB"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 运行完整实验流程
     */
    private static void runCompleteExperiment() {
        System.out.println("\n=== 完整实验流程 ===");
        System.out.println("将按顺序执行所有实验步骤...");
        
        // 检查是否有测试数据
        if (!hasTestData()) {
            System.out.println("\n检测到没有测试数据，开始生成...");
            DataGenerator.main(new String[]{});
        }
        
        System.out.println("\n开始执行完整实验流程...");
        
        // 步骤3: 传统分页测试
        runTraditionalPagingTest();
        
        // 步骤4: EXPLAIN分析
        runExplainAnalysis();
        
        // 步骤5: 优化方案测试
        runOptimizedPagingTest();
        
        // 步骤6: 优化方案EXPLAIN分析
        runOptimizedExplainAnalysis();
        
        // 步骤7: 性能对比测试
        runPerformanceComparison();
        
        System.out.println("\n=== 实验完成 ===");
        System.out.println("通过本次实验，您应该已经深入理解了:");
        System.out.println("1. 深分页问题的产生原因");
        System.out.println("2. 传统LIMIT分页的性能瓶颈");
        System.out.println("3. 多种优化方案的实现原理");
        System.out.println("4. 各种方案的适用场景和优缺点");
        System.out.println("5. 如何选择合适的分页策略");
    }
    
    /**
     * 检查是否有测试数据
     */
    private static boolean hasTestData() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM test_data")) {
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("检查测试数据失败: " + e.getMessage());
        }
        
        return false;
    }
}