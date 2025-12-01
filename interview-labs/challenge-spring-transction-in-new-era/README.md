# challenge spring transaction in new era
          
**目标与成果**
- 将备忘录拆解为可运行的 labs，用最小依赖（H2、JPA/JDBC）在当前 Spring Boot 项目内，以TDD验证核心事务理论。
- 已实现并通过所有测试；覆盖 REQUIRED、REQUIRES_NEW、NESTED、Self-Invocation、Checked 异常回滚、ReadOnly、不跨线程共享事务等关键点。

**如何运行**
- 在项目目录执行：`mvn test`（使用你本机 Maven）。所有 lab 测试会自动跑完。
- H2 内存库与JPA自动建表，未引入任何外部中间件。

**Labs总览（知识点→实验→验证）**
- REQUIRED 原子性
  - 服务实现：`src/main/java/.../labs/required/RequiredService.java`
  - 断言测试：`src/test/java/.../labs/RequiredLabTests.java`
  - 验证点：正常提交保存两条记录；抛出运行时异常时两条都回滚
  - 参考：`RequiredService.ok` 与 `RequiredService.fail` 在 `labs/required/RequiredService.java:13,18`

- REQUIRES_NEW 独立事务
  - 服务实现：`.../labs/requiresnew/PaymentService.java` 调用 `.../labs/requiresnew/AuditService.java`
  - 断言测试：`.../labs/RequiresNewLabTests.java`
  - 验证点：外层失败回滚，但内层审计独立提交成功
  - 参考：`PaymentService.payAndFail` 在 `labs/requiresnew/PaymentService.java:17`；`AuditService.record` 在 `labs/requiresnew/AuditService.java:16`

- NESTED 保存点
  - 服务实现：`.../labs/nested/BatchService.java` + `.../labs/nested/NestedItemService.java`
  - 断言测试：`.../labs/NestedLabTests.java`
  - 验证点：内层失败仅回滚本次保存点，外层整体继续并提交；若外层最终失败则全部回滚
  - 关键实现：选择 JDBC 事务管理器执行 `Propagation.NESTED`（JPA不支持保存点）
  - 参考：`NestedItemService.importOne` 在 `labs/nested/NestedItemService.java:13`；`BatchService.importAll` 在 `labs/nested/BatchService.java:17`

- Self-Invocation 失效与修复
  - 服务实现：失效示例 `.../labs/selfcall/SelfCallService.java`；修复示例 `.../labs/selfcall/CallerService.java` + `.../labs/selfcall/SeparateCalleeService.java`
  - 断言测试：`.../labs/SelfCallLabTests.java`
  - 验证点：同类内自调用未经过代理，`REQUIRES_NEW`不生效；拆分到不同Bean后，`REQUIRES_NEW`生效、内层独立提交
  - 参考：`SelfCallService.outerAndFail` 在 `labs/selfcall/SelfCallService.java:12`；修复版 `CallerService.outerAndFail` 在 `labs/selfcall/CallerService.java:21`

- Checked 异常回滚规则
  - 服务实现：`.../labs/rollback/RollbackService.java`
  - 断言测试：`.../labs/RollbackLabTests.java`
  - 验证点：默认受检异常不回滚；显式 `rollbackFor=Exception.class` 才回滚
  - 参考：`RollbackService.checkedDefault` 在 `labs/rollback/RollbackService.java:12`；`checkedRollback` 在 `labs/rollback/RollbackService.java:17`

- ReadOnly 意图而非强约束
  - 服务实现：`.../labs/readonly/ReadOnlyService.java`
  - 断言测试：`.../labs/ReadOnlyLabTests.java`
  - 验证点：`readOnly=true`不阻止写入（用于意图与优化），写操作照样成功
  - 参考：`ReadOnlyService.writeInsideReadOnly` 在 `labs/readonly/ReadOnlyService.java:12`

- 虚拟线程与事务边界
  - 服务实现：`.../labs/virtualthread/VirtualThreadService.java`
  - 断言测试：`.../labs/VirtualThreadLabTests.java`
  - 验证点：跨线程的保存与外层事务隔离，外层回滚不影响新线程的提交（ThreadLocal无法跨线程传播）
  - 参考：`VirtualThreadService.crossThreadAndFail` 在 `labs/virtualthread/VirtualThreadService.java:14`

**项目结构与关键配置**
- 事务管理器装配
  - JPA TM（默认）：`src/main/java/.../config/TxManagersConfig.java:13`
  - JDBC TM（保存点/NESTED）：`src/main/java/.../config/TxManagersConfig.java:9`
  - 在服务上明确选择TM，避免二选一时的歧义
- 通用实体与仓库
  - `Entry`：`.../labs/common/Entry.java`，字段：`type/payload/createdAt`
  - `EntryRepository`：`.../labs/common/EntryRepository.java`，提供计数与删除（删除方法带事务）
- 数据源与JPA
  - `src/main/resources/application.properties` 使用内存H2、`ddl-auto=create-drop`、SQL日志开启

**实践要点与边界定义**
- REQUIRED 与回滚触发
  - 运行时异常触发回滚；受检异常默认不回滚，需显式配置
- REQUIRES_NEW 与审计
  - 外层失败不影响内层独立提交；适合审计、日志、外呼等必须成功的支线
- NESTED 与保存点
  - 仅在 JDBC `DataSourceTransactionManager` 下生效；JPA 不支持保存点
- 自调用失效
  - `@Transactional`必须经过代理；同类内直接调用不会走拦截链，传播策略不生效
- ReadOnly 的负向定义
  - 不等于数据库只读；是意图提示与flush优化，写操作仍然可能落库
- 跨线程边界
  - 事务上下文基于ThreadLocal，无法跨线程；新线程的写入独立于外层事务

**TDD设计（用例命名与验收标准）**
- 命名遵循“行为+预期”：
  - `okCommitsBoth`、`failRollsBackBoth`、`innerCommitsOuterRollsBack`、`innerFailuresIsolatedBySavepoint`、`splitBeansEnableRequiresNew`、`checkedDefaultDoesNotRollback`、`checkedRollbackRollsBack`、`readOnlyDoesNotPreventWrite`、`innerInNewThreadCommitsOuterRollsBack`
- 每个测试前清理状态（仓库删除按事务方法执行）并通过计数断言最终效果

**扩展与建议**
- 如需演示隔离级别（RR/RC等）和锁行为，建议额外添加基于`JdbcTemplate`的并发测试用例，模拟两个事务T1/T2交错操作；当前实现侧重事务边界与传播。
- 如需R2DBC/Reactive演示，新增依赖与实验模块，但按你规则“非必要不引中间件”，现阶段已避免。

**你可以从这些入口开始读代码**
- `labs/required/RequiredService.java:13`
- `labs/requiresnew/PaymentService.java:17`
- `labs/requiresnew/AuditService.java:16`
- `labs/nested/NestedItemService.java:13`
- `labs/selfcall/SelfCallService.java:12`
- `labs/selfcall/CallerService.java:21`
- `labs/rollback/RollbackService.java:12`
- `labs/readonly/ReadOnlyService.java:12`
- `labs/virtualthread/VirtualThreadService.java:14`
- `config/TxManagersConfig.java:9`

若你希望把每个 lab 的控制台日志简化为“业务流程-事务开始/提交/回滚”的可读格式，我可以继续加上简单的`logger`输出与独立`Runner`，并保持不引入外部中间件。