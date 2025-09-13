-- 深分页实验数据库初始化脚本
USE deep_paging_test;

-- 创建测试表
CREATE TABLE IF NOT EXISTS test_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    city VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1,
    score DECIMAL(10,2) DEFAULT 0.00,
    description TEXT
);

-- 创建索引用于后续优化测试
CREATE INDEX idx_user_id ON test_data(user_id);
CREATE INDEX idx_created_at ON test_data(created_at);
CREATE INDEX idx_status ON test_data(status);

-- 创建覆盖索引用于优化方案3
CREATE INDEX idx_covering ON test_data(status, created_at, id, username);

-- 显示表结构
DESCRIBE test_data;