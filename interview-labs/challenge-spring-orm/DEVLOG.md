End-to-End 测试演练 DEVLOG（倒叙）

2025-12-05 18:24 结果：根项目测试通过
- 操作：移除 MyBatis H2 连接串中的 `DB_CLOSE_DELAY=-1`，确保每次上下文关闭后内存数据库也关闭，避免数据泄漏。
- 验证：`mvn -q -DskipTests=false test` 退出码 0，所有 JPA/MyBatis 的单元与 E2E 均通过。

2025-12-05 18:21 问题：MyBatis 测试数据污染导致断言失败
- 现象：分页断言与计数断言异常（期望 3，实际 26）。
- 原因：同名内存库未关闭，跨测试共享数据；且上下文复用导致表数据未清理。
- 解决：
  - 在所有 MyBatis 测试类上添加 `@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)`，每个测试后重建上下文。
  - 明确 `spring.sql.init.schema-locations: classpath:/schema.sql` 且 `continue-on-error: true`，防止重复初始化导致失败。
  - 移除 `DB_CLOSE_DELAY=-1`（见上条），保证内存库生命周期与上下文一致。

2025-12-05 18:17 问题：MyBatis ApplicationContext 无法启动（缺少 Mapper Bean）
- 现象：`NoSuchBeanDefinitionException: UserMapper`。
- 原因：仅有 `@Mapper` 注解不足以被 Spring 扫描，需要显式 Mapper 扫描或自动配置。
- 解决：
  - 在 `MybatisStuffApplication` 添加 `@MapperScan("com.simon.mybatis.mapper")`。
  - 引入手工配置 `MyBatisConfig`，显式定义 `SqlSessionFactory` 与 `SqlSessionTemplate`，并设置 `mapperLocations` 为 `classpath:/mappers/*.xml`，绕过 MyBatis AutoConfiguration 在 Boot4 条件判断的兼容性差异。

2025-12-05 18:16 问题：JPA 乐观锁断言类型不匹配
- 现象：期望 `OptimisticLockingFailureException`，实际抛出 `jakarta.persistence.OptimisticLockException`。
- 原因：Hibernate/JPA 在 `merge` 场景抛原生 JPA 异常；Spring 统一异常转换在特定路径才触发。
- 解决：将断言改为 `assertThrows(OptimisticLockException.class, ...)`，匹配实现行为。

2025-12-05 18:15 问题：JPA 测试 `flush()` 报 “No EntityManager with actual transaction available”
- 原因：`@SpringBootTest` 默认非事务，直接调用 `EntityManager.flush()` 需要事务。
- 解决：为相关测试方法添加 `@Transactional`，并在 E2E 用例上同样添加，避免 `LazyInitializationException`。

2025-12-05 18:14 问题：`@DataJpaTest`、`TestEntityManager` 包不存在
- 原因：初始模块依赖与生成模板不一致，且存在旧版 JUnit3 测试样例导致编译失败，阻断依赖解析。
- 解决：
  - 移除遗留的 `com/simon/AppTest.java`（JUnit3）。
  - JPA 测试统一切换为 `@SpringBootTest` + `@PersistenceContext EntityManager`。

