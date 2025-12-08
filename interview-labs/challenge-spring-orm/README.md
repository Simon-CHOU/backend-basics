# Spring ORM 挑战项目

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-red)](https://maven.apache.org/)

本项目旨在通过一系列精心设计的实验，深入对比和理解两大主流Java持久化框架——JPA (Java Persistence API) 与 MyBatis 的核心特性、设计哲学和最佳实践。

## 🎯 项目目标

- **深入理解JPA和MyBatis的核心概念和设计哲学**
- **掌握两种框架在不同场景下的最佳实践**
- **通过实际实验对比性能差异**
- **为面试和实际项目开发提供扎实的技术基础**

## 📁 项目结构

```
challenge-spring-orm/
├── jpa-stuff/          # JPA模块 - 探索自动化ORM特性
├── mybatis-stuff/      # MyBatis模块 - 探索SQL控制能力
├── GOAL.md            # 详细的实验目标说明
└── README.md          # 项目说明文档
```

## 🚀 快速开始

### 环境要求

- **Java 21+**
- **Maven 3.9+**
- **IDE** (推荐 IntelliJ IDEA 或 Eclipse)

### 运行所有测试

```bash
# 克隆项目
git clone <repository-url>
cd challenge-spring-orm

# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl jpa-stuff
mvn test -pl mybatis-stuff
```

### 查看SQL执行日志

测试执行时会自动打印SQL语句，你可以通过日志观察两种框架的SQL生成差异：

```bash
# 运行测试并查看SQL日志
mvn test -Dspring.jpa.show-sql=true -Dlogging.level.org.mybatis=DEBUG
```

## 📚 实验内容

### JPA模块 (`jpa-stuff`)

| Lab | 实验主题 | 核心概念 |
|-----|----------|----------|
| Lab 1 | 持久化上下文与脏检查 | Persistence Context, Dirty Checking |
| Lab 2 | 抓取策略与N+1问题 | Fetch Strategy, JOIN FETCH |
| Lab 3 | 乐观锁 | Optimistic Locking, @Version |
| Lab 4 | 查询与分页 | Derived Queries, @Query, Specification |
| Lab 5 | 级联与孤儿删除 | CascadeType, orphanRemoval |
| Lab 6 | Record投影 | Constructor Expression, DTO |
| Lab 7 | 原生查询 | Native Query, nativeQuery=true |

### MyBatis模块 (`mybatis-stuff`)

| Lab | 实验主题 | 核心概念 |
|-----|----------|----------|
| Lab A | 显式更新 | CRUD Explicitness |
| Lab B | 动态SQL与分页 | Dynamic SQL, Pagination |
| Lab C | 连接查询与集合映射 | Join Query, Collection Mapping |
| Lab D | 原生计数 | Native Count |

## 🔍 关键特性对比

| 特性 | JPA | MyBatis |
|------|-----|---------|
| **学习曲线** | 中等（需要理解JPA概念） | 较低（基于SQL） |
| **SQL控制** | 自动生成，有限控制 | 完全控制 |
| **性能** | 优化空间大，需要理解机制 | 可预测，依赖SQL优化 |
| **缓存** | 一级、二级缓存自动管理 | 一级缓存，二级缓存需配置 |
| **关联查询** | 自动处理，N+1问题需注意 | 手动编写SQL，更灵活 |
| **移植性** | 高（数据库无关） | 中等（依赖SQL方言） |

## 📊 性能测试

项目包含性能对比测试，展示两种框架在不同场景下的表现：

```bash
# 运行性能测试
mvn test -Dtest=PerformanceComparisonTest
```

测试场景包括：
- 批量插入性能
- 复杂查询性能
- 关联查询性能
- 并发更新性能

## 🛠️ 技术栈

- **Spring Boot 4.0.0** - 应用框架
- **Spring Data JPA** - JPA实现
- **Hibernate** - JPA Provider
- **MyBatis** - SQL映射框架
- **H2 Database** - 内存数据库（测试用）
- **JUnit 5** - 测试框架
- **Java 21** - 编程语言

## 📖 学习建议

1. **先运行所有测试**：了解项目的基本功能
2. **逐个分析实验**：按照GOAL.md中的说明，理解每个实验的目的
3. **对比SQL输出**：观察两种框架生成的SQL差异
4. **修改实验代码**：尝试修改参数，观察结果变化
5. **性能测试**：通过性能测试了解实际应用中的表现

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进这个项目：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🔗 相关资源

- [Spring Data JPA 官方文档](https://spring.io/projects/spring-data-jpa)
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/)
- [Hibernate 用户指南](https://hibernate.org/orm/documentation/)
- [Spring Boot 参考指南](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交 Issue
- 发起 Discussion
- 邮件联系

---

**注意**：本项目主要用于学习和面试准备，生产环境使用请根据实际需求选择合适的框架。