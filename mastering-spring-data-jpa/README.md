# 学会Spring Data JPA | ORM

主要内容包含：
- 什么是Spring Data JPA及ORM
- 利用lombok注解实现builder, getter/setter，构造器等方法
- 如何使用类映射表，基本注解使用(@GeneratedValue)
- 如何自定义类型映射converter
- 在repository中通过方法名简单查询
- 在repository中通过原生sql或者jqpl实现查询
- 利用Specification实现复杂动态查询以及排序，分页
- 使用Flyway进行数据库版本管理和迁移

## 数据库迁移管理 - Flyway

本项目使用Flyway进行数据库版本管理，确保数据库结构的一致性和可追溯性。

### Flyway配置

项目已集成Flyway，相关配置在`application-dev.properties`中：

```properties
# Flyway配置
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.out-of-order=false
```

### 迁移脚本规范

迁移脚本位于`src/main/resources/db/migration/`目录下，命名规范：

- **版本化迁移**：`V{版本号}__{描述}.sql`
  - 示例：`V1__Create_student_table.sql`
  - 示例：`V2__Insert_student_data.sql`

- **可重复迁移**：`R__{描述}.sql`
  - 用于视图、存储过程等可重复执行的脚本

### 迁移脚本开发指南

1. **创建新的迁移脚本**
   ```bash
   # 在 src/main/resources/db/migration/ 目录下创建
   V3__Add_user_table.sql
   ```

2. **脚本内容要求**
   - 每个脚本应该是幂等的
   - 使用`IF NOT EXISTS`等条件语句
   - 添加适当的注释说明

3. **示例脚本结构**
   ```sql
   -- V3__Add_user_table.sql
   -- 创建用户表
   CREATE TABLE IF NOT EXISTS users (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(50) NOT NULL UNIQUE,
       email VARCHAR(100) NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
   ```

### 常用Flyway命令

虽然Spring Boot会自动执行迁移，但了解Flyway命令有助于调试：

```bash
# 查看迁移状态
mvn flyway:info

# 手动执行迁移
mvn flyway:migrate

# 验证迁移
mvn flyway:validate

# 清理数据库（仅开发环境）
mvn flyway:clean
```

### 注意事项

1. **生产环境**：
   - 禁用`flyway:clean`命令
   - 谨慎使用`baseline-on-migrate`
   - 建议先在测试环境验证迁移脚本

2. **开发环境**：
   - 可以使用`spring.jpa.hibernate.ddl-auto=validate`确保实体与数据库结构一致
   - 迁移脚本一旦提交，不应再修改

3. **团队协作**：
   - 迁移脚本版本号要连续
   - 避免并行开发时的版本号冲突
   - 及时同步和执行最新的迁移脚本

ref:
BV17m421M7CY
