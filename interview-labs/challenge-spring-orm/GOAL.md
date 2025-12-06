# 总体目标

本项目旨在通过一系列精心设计的实验（Lab），深入对比和理解两大主流Java持久化框架——JPA (Java Persistence API) 与 MyBatis 的核心特性、设计哲学和最佳实践。项目分为 `jpa-stuff` 和 `mybatis-stuff` 两个模块，分别聚焦于不同框架下的常见场景。

所有实验都遵循测试驱动开发（TDD）的原则，通过单元测试和端到端测试来验证功能的正确性。

# JPA 模块 (`jpa-stuff`)

JPA 模块的核心是探索其自动化、约定优于配置的特性，特别是围绕持久化上下文（Persistence Context）展开的各种行为。

## Lab 1: 持久化上下文与脏检查 (Persistence Context & Dirty Checking)

*   **目标**：验证在 `@Transactional` 管理的事务中，对持久态（Managed）实体所做的任何修改，都会在事务提交时被JPA自动检测（脏检查）并同步到数据库，无需显式调用 `save` 或 `update` 方法。
*   **方法**：
    1.  在 `@Transactional` 注解的测试方法中，通过 `findById` 加载一个实体。
    2.  修改该实体的一个或多个属性。
    3.  不调用任何 `repository.save()` 方法。
    4.  事务方法结束后，开启一个新的事务（或使用一个新的查询），重新从数据库加载该实体。
    5.  断言该实体的属性值已更新为修改后的值。

## Lab 2: 抓取策略与 N+1 问题 (Fetching Strategies & N+1 Problem)

*   **目标**：演示经典的 N+1 查询问题，并利用 `JOIN FETCH` 关键字在一次查询中抓取关联实体，从而解决该问题。
*   **方法**：
    1.  **重现问题**：定义两个实体，如 `Order` 和 `OrderItem`，`Order` 对 `OrderItem` 为一对多关系，且默认抓取策略为 `LAZY`。查询 N 个 `Order` 实体，然后遍历每个 `Order` 的 `OrderItem` 集合。通过日志或性能分析工具，观察到执行了 1 次查询 `Order` 的 SQL 和 N 次查询 `OrderItem` 的 SQL。
    2.  **解决问题**：在 JPQL 查询语句中使用 `LEFT JOIN FETCH` 关键字，将 `Order` 与其关联的 `OrderItem` 一并查询出来。
    3.  **验证**：再次执行查询，观察 SQL 日志，确认只执行了 1 次 `JOIN` 查询，有效解决了 N+1 问题。

## Lab 3: 乐观锁 (Optimistic Locking)

*   **目标**：验证通过在实体中添加 `@Version` 注解，JPA可以实现乐观锁机制，防止并发更新导致的数据丢失。
*   **方法**：
    1.  在实体中添加一个带有 `@Version` 注解的字段（如 `private Integer version;`）。
    2.  在测试中，模拟两个并发的事务：
        a. 事务A加载一个实体。
        b. 事务B也加载同一个实体，并修改、提交，此时数据库中该记录的 `version` 字段会自增。
        c. 事务A随后也对它持有的（过时的）实体进行修改并尝试提交。
    3.  断言事务A的提交会失败，并抛出 `OptimisticLockException` (或其子类)。

## Lab 4: 查询与分页 (Queries & Pagination)

*   **目标**：掌握并验证 Spring Data JPA 提供的多种查询方式，包括方法名派生查询、使用 `@Query` 注解的 JPQL 查询以及 `Specification` 动态查询，并结合 `Pageable` 实现分页。
*   **方法**：
    1.  **派生查询**：在 Repository 接口中定义一个遵循命名约定的方法，如 `findByStatus(String status, Pageable pageable)`，直接调用并验证分页结果。
    2.  **JPQL查询**：使用 `@Query` 注解编写 JPQL 语句，并传入 `Pageable` 参数，验证返回的 `Page` 对象中的内容、总页数、总元素数是否正确。
    3.  **Specification查询**：构建一个 `Specification` 实现，根据动态条件（如 `if (name != null) { ... }`）构造查询谓词（Predicate），调用 `repository.findAll(specification, pageable)` 并验证结果。

## Lab 5: 级联与孤儿删除 (Cascading & Orphan Removal)

*   **目标**：验证 `orphanRemoval = true` 属性的正确行为。当一个子实体从父实体的集合中被移除时，它会自动从数据库中被删除。
*   **方法**：
    1.  在父实体（如 `Order`）对子实体集合（如 `List<OrderItem>`）的一对多关系注解中，设置 `orphanRemoval = true` 和 `cascade = CascadeType.ALL`。
    2.  加载一个包含多个子实体的父实体。
    3.  在事务中，从父实体的子实体集合中移除一个或多个子实体。
    4.  提交事务。
    5.  开启新事务，验证被移除的子实体在数据库中已不存在。

## Lab 6: Record 投影 (Record Projection)

*   **目标**：使用 JPQL 的构造函数表达式（Constructor Expression）将查询结果直接映射到一个 Java `Record` 对象，实现一个只读的数据传输对象（DTO），避免加载完整的实体。
*   **方法**：
    1.  定义一个 `Record`，其构造函数参数列表与 JPQL 查询的 `SELECT` 子句字段匹配。
    2.  在 `@Query` 注解中，使用 `SELECT new com.simon.dto.MyRecord(e.field1, e.field2) FROM Entity e` 语法。
    3.  执行该查询，并断言返回的 `Record` 列表内容正确。

## Lab 7: 原生查询 (Native Query)

*   **目标**：验证使用原生 SQL 查询的能力，特别是当需要利用数据库特有功能或执行复杂查询时。
*   **方法**：
    1.  在 Repository 方法上使用 `@Query` 注解，并设置 `nativeQuery = true`。
    2.  编写标准的 SQL 语句，例如 `SELECT count(*) FROM users`。
    3.  执行该方法，并将其结果与通过 JPA 标准方法（如 `repository.count()`）得到的结果进行对比，断言两者一致。

# MyBatis 模块 (`mybatis-stuff`)

MyBatis 模块的核心是探索其对 SQL 的完全控制能力、灵活性以及显式操作的哲学。

## 关键修复与配置

在进行 MyBatis 实验之前，对项目进行了关键的修复和配置，以确保测试环境的正确性和稳定性：

1.  **Mapper 扫描**：在 `MybatisStuffApplication` 主类上添加 `@MapperScan("com.simon.mybatis.mapper")` 注解，以解决 `NoSuchBeanDefinitionException`，确保 MyBatis 的 Mapper 接口能被 Spring 容器扫描并注册为 Bean。
2.  **显式配置 MyBatis**：创建 `MyBatisConfig` 配置类，手动定义 `SqlSessionFactory` 和 `SqlSessionTemplate` 的 Bean。这增强了配置的明确性，并解决了在某些 Spring Boot 版本下自动配置可能出现的问题。
3.  **测试隔离**：
    *   为所有测试类添加 `@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)` 注解。此举确保每个测试方法执行完毕后，Spring 的 `ApplicationContext` 都会被销毁并重建，从而清空所有缓存和状态，避免测试间的数据污染。
    *   移除了 H2 数据库连接字符串中的 `DB_CLOSE_DELAY=-1`。这使得当 `ApplicationContext` 关闭时，内存数据库连接会断开，数据库被清空。结合 `@DirtiesContext`，实现了完美的测试隔离。
4.  **数据库初始化**：在 `application.yaml` 中配置 `spring.sql.init.mode=always` 和 `continue-on-error=true`，保证了每次应用启动时都会尝试执行 `schema.sql`，并且在表已存在时不会中断程序，这对于测试环境的搭建至关重要。

## Lab A: 显式更新 (CRUD Explicitness)

*   **目标**：证明与 JPA 的自动脏检查不同，MyBatis 必须显式调用在 Mapper XML 中定义的 `update` 语句，才能将对象的变更持久化到数据库。
*   **方法**：
    1.  通过 Mapper 接口的 `select` 方法加载一条数据到实体对象。
    2.  修改该对象的属性。
    3.  **不调用**任何 `update` 方法。
    4.  重新从数据库查询该条数据。
    5.  断言数据的状态与修改前一致，证明了不显式调用更新操作，数据不会改变。

## Lab B: 动态 SQL 与分页 (Dynamic SQL & Pagination)

*   **目标**：利用 MyBatis 强大的动态 SQL 功能（如 `<where>`, `<if>`），根据不同条件构建灵活的查询语句，并实现手动分页。
*   **方法**：
    1.  在 Mapper XML 的 `<select>` 标签中，使用 `<where>` 和 `<if test="...">` 标签来动态拼接 `AND` 条件。
    2.  查询方法接受一个包含可选查询参数的 DTO 或 `Map`。
    3.  在 SQL 语句的末尾添加 `LIMIT #{limit} OFFSET #{offset}` 来实现分页。
    4.  编写多个测试用例，分别传入不同组合的查询参数（包括 `null` 值），验证返回的结果集符合动态生成的查询条件和分页规则。

## Lab C: 连接查询与集合映射 (Join Query & Collection Mapping)

*   **目标**：执行一个 `LEFT JOIN` 查询，并将查询结果（一个扁平的结果集）通过 `<resultMap>` 映射到一个嵌套的对象结构中，特别是将 "多" 的一方映射为一个集合（Collection）。
*   **方法**：
    1.  定义一个复杂的 `<resultMap>`，使用 `<association>` 映射一对一关系，使用 `<collection>` 映射一对多关系。
    2.  在 `<collection>` 标签中，通过 `ofType` 指定集合中元素的类型，并定义其属性映射。
    3.  编写 `LEFT JOIN` SQL 语句查询主表和关联表的数据。
    4.  执行查询，并断言返回的主对象中，其集合属性被正确填充，且集合中对象的数量和内容都符合预期。

## Lab D: 原生计数 (Native Count)

*   **目标**：验证 MyBatis 执行原生 `COUNT(*)` SQL 语句的正确性。
*   **方法**：
    1.  在 Mapper XML 中定义一个返回类型为 `int` 或 `long` 的 `select` 语句，内容为 `SELECT COUNT(*) FROM ...`。
    2.  在测试中，首先向数据库中插入确定数量的记录。
    3.  调用该 `count` 方法。
    4.  断言返回的计数值与插入的记录数完全相等。
