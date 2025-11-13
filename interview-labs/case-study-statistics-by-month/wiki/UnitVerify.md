# 在现有架构下的单元化验证方案（不重写统计逻辑）

**结论（Key Concepts Up Front）**
- 可以在不重写现有统计 SQL 与服务架构的前提下，通过“单元/契约”测试验证关键正确性环节：
  - 写入链路的正确性（每条 `StatisticsResult` 是否被拆分为 6 项、year/month 拆分是否正确、方言分支是否选择正确）。
  - 累计逻辑的正确性（使用已有 Java 回退实现进行可控数据的单元化验证）。
- 无法通过纯单元测试完全替代 end-to-end 对 MySQL 窗口函数与真实数据行级行为的验证；这些仍需 E2E 补位。
- 通过以下测试组合，可覆盖“核心不出错”的大头风险：契约写入 + 累计校验 + 边界与错误处理，满足 TDD 精神下的极简单元化保障。

**背景与约束（Why）**
- 统计核心依赖 MySQL 的窗口函数与 `DATE_FORMAT` 等方言特性（`src/main/resources/mapper/StatisticsMapper.xml:20-115`），纯 JVM 单元测试难以真实模拟。
- 当前服务在执行阶段包含异步与方言分支（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:50-81, 140-196, 198-208`），这些环节具备可单测的业务不变式（不依赖真实 DB）。
- 代码里已存在 Java 回退实现 `computeStatisticsFallback()`（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:83-127`），可作为“累计逻辑”的可测试替身，用受控数据和 mock 验证逻辑正确性。

---

## 测试设计（What + How）

- 契约测试 A：写入链路正确性
  - 目标：给定一个 `StatisticsResult`，是否准确拆分为 6 条看板记录；`yyyyMm` 是否被正确拆解为 `year/month`；单位与任务名称是否固定；H2/MySQL 分支是否可控。
  - 作用域：`processAndSaveStatistics` 与 `saveStatisticItem`（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:129-196`）。
  - 方法：
    - 使用 Mockito/MockK 对 `JdbcTemplate` 和 `StatisticsMapper` 进行 mock；为 H2 分支构造 `DataSource` URL `:h2:`，为 MySQL 分支构造非 H2 URL。
    - 构造一个 `StatisticsResult(yyyyMm="2024-11", retailerName, retailerId, 各累计值)`，调用 `processAndSaveStatistics(result)`。
    - 断言：
      - 恰好调用 6 次写入，`sub_task` 集合为固定的 6 个名称；
      - 传入 `year="2024"`、`month="11"`；`unit="个"`、`task="项目统计信息"`；
      - H2 分支下写入 SQL 使用引用列名 `"year"/"month"`（避免关键字冲突）。

- 单元测试 B：累计逻辑正确性（使用回退实现）
  - 目标：在不接触真实 DB 的情况下，验证“当月新增 + 窗口累计”的逻辑正确性。
  - 作用域：`computeStatisticsFallback()`（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:83-127`）。
  - 方法：
    - mock `JdbcTemplate.query` 返回固定的 `retailerIds` 与 `retailerNames`；mock `queryForObject` 根据 `(retailerId, month, 指标)` 返回设定的当月新增数量；
    - 设定 2 客户 × 4 连续月份的最小样例（与 `wiki/HowToVerifyResults.md` 的样例一致），确保包含“空月份保持累计不变”的场景；
    - 调用回退方法，断言各月份的累计结果与手工计算一致（主动/配合/解绑/总项/有效/BFO）。
  - 价值：不依赖 SQL 方言与真实库，仍能在 JVM 内验证累计正确性和时间分桶边界（`[当月首日, 次月首日)`）。

- 单元测试 C：边界条件与不变式
  - 目标：月度无新增时累计不变；`yyyyMm` 解析健壮；零/负数（不应出现）防御；空 `retailerName` 的容忍度。
  - 作用域：`processAndSaveStatistics` 年月拆分（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:134-146`）与回退方法的时间窗口。
  - 方法：构造 `yyyyMm` 非法值（如 `2024-1`、`2024-13`）验证抛错或明确定义行为；构造当月新增均为 0 的月份，断言累计保持不变。

- 单元测试 D：方言检测与分支选择
  - 目标：`isH2()` 的判断是否基于 `DataSource` URL 正确选择分支；在 H2 下是否使用引号列名；在 MySQL 下是否走 Mapper。
  - 作用域：`isH2()`（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:198-208`）。
  - 方法：注入伪造的 `DataSource`（返回 `jdbc:h2:mem:...` 与 `jdbc:mysql://...` 两种 URL），断言 `saveStatisticItem` 的调用对象与 SQL 形态。

- 单元测试 E（可选）：`StatisticsResult` 映射与拆分的纯函数化校验
  - 目标：`yyyyMm` → `year/month` 的解析一致性，避免格式偏差导致数据写入错误。
  - 方法：对解析逻辑进行纯函数测试（无需 DB）。

```
@startuml
title 单元化验证视角
actor Test as "JUnit/Mockito"
participant Service as "StatisticsTaskService"
collections MockDB as "Mocked JdbcTemplate/Mapper"

Test -> Service : 构造 StatisticsResult 输入
Service -> MockDB : 写入 6 项（H2/MySQL 分支）
Test --> MockDB : 验证参数、次数、方言分支

Test -> Service : 调用 computeStatisticsFallback()
Service -> MockDB : 查询零散 counts（按月/客户/指标）
Test --> Service : 验证累计结果正确
@enduml
```

---

## 覆盖面与风险（So What）

- 能覆盖的关键风险：
  - 写入契约不变式（6 项、year/month 拆分、单位/任务固定）不会因改动而破坏；
  - 累计逻辑在 JVM 端与手工核算一致，保证“窗口累计”这一核心算法不出错；
  - 方言识别与分支行为正确，避免 H2/MySQL 在 CI 与本地的差异引发写入失败。
- 无法覆盖的部分（需保留 E2E）：
  - MySQL 窗口函数在真实数据分布下的语义与性能；
  - 真实数据库的索引/统计信息导致的执行计划差异；
  - 与真实表结构/约束/触发器（若有）的交互行为。

---

## 最小实施步骤（Next Actions）

- 为 `StatisticsTaskService` 构造可注入的依赖（已存在），使用 Mockito 编写如下测试：
  - 契约 A：构造单个 `StatisticsResult`，断言 6 次写入与参数集合；覆盖 H2 与 MySQL 两分支。
  - 累计 B：mock `JdbcTemplate` 返回固定 counts，断言 `computeStatisticsFallback()` 的累计等于手工核算表。
  - 边界 C：构造空月份/非法 `yyyyMm`，验证行为与日志。
  - 方言 D：伪造 URL，断言分支选择与 SQL 形态。
- 运行：
  - WSL/Linux/Mac：`./mvnw -q -Dtest=StatisticsUnit* test`
  - Windows PowerShell：`mvnw.cmd -q -Dtest=StatisticsUnit* test`

---

## 结论（Senior-Level Recommendation）
- 在不重写统计 SQL 的条件下，仍可通过“契约 + 回退累计 + 方言分支”的单元化测试覆盖大部分核心 correctness 风险；这符合 TDD 的“最小可测试单元”思想。
- 保留 E2E 只验证“SQL 方言与真实库行为”这类单元测试无法覆盖的部分，将验证成本压到最低，同时提高回归效率与定位清晰度。
- 后续若允许小幅重构，可将“累计逻辑”提取为纯函数（入参为当月新增列表），则单元测试的可维护性与表达力会更强，但当前方案已足够实用与经济。

---

## 术语与标准引用（“方言 / 方言分支”）

- 概念定义：
  - SQL 方言（SQL Dialect）：不同数据库厂商在 SQL 语法、函数、关键字、行为上的差异集合。例如 MySQL 的 `DATE_FORMAT` 函数与 H2 的兼容模式、关键字解析差异。
  - 方言分支：代码中根据所连接数据库的方言差异而选择不同实现路径的条件分支。例如本项目中 H2 下使用引号列名与 `JdbcTemplate` 写入，MySQL 下使用 Mapper 原生 SQL。

- 本项目中的“方言分支”出处（代码锚点）：
  - H2 检测与分支选择：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:198-208`
  - H2 写入（引号列名 `"year"/"month"`，避免保留字冲突）：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:158-175`
  - MySQL 写入（Mapper 原生 SQL）：`src/main/resources/mapper/StatisticsMapper.xml:117-126`
  - 统计查询使用 MySQL 方言能力（窗口函数与 `DATE_FORMAT`）：`src/main/resources/mapper/StatisticsMapper.xml:20-115`

- 标准引用：
  - [1] MySQL, “Date and Time Functions,” MySQL 8.0 Reference Manual, Oracle. 可用函数 `DATE_FORMAT`. 链接: `https://dev.mysql.com/doc/refman/8.0/en/date-and-time-functions.html`. 访问日期: 2025-11-13。
  - [2] H2 Database, “Compatibility Modes,” H2 Features. MySQL 兼容模式说明。链接: `https://h2database.com/html/features.html#compatibility`. 访问日期: 2025-11-13。
  - [3] H2 Database, “Identifiers and Quoted Identifiers,” H2 Grammar. 双引号标识符与大小写/关键字行为。链接: `https://h2database.com/html/grammar.html#identifiers`. 访问日期: 2025-11-13。
  - [4] H2 Database, “Keywords,” H2 Grammar. 保留关键字（包含 `YEAR`、`MONTH` 等）。链接: `https://h2database.com/html/grammar.html#keywords`. 访问日期: 2025-11-13。