# 统计逻辑重构（绞杀式 + TDD）

**目标与策略（Key Concepts Up Front）**
- 目标：用可维护的 Java 实现替换巨无霸 SQL（`selectMonthlyStatistics`），保留原路径作为对照与回退。
- 方法：绞杀式重构（Strangler Fig Pattern）+ TDD。并行引入 V2 路径，逐步迁移调用，待稳定后再考虑下线旧路径。
- 范围：新增纯 Java 统计计算器 + 新的 Service/Controller/定时任务；首先保证新的 Java 逻辑的单元测试 100% 行/分支/条件覆盖。

---

## 设计总览

- 新核心类：`MonthlyStatisticsCalculator`（纯 Java）
  - 输入：`List<Customer> / List<Project> / List<Binding>` + 口径枚举代码（`Type0/Type1`、`BFO`）。
  - 输出：`List<StatisticsResult>`，包含 `(yyyyMm, retailerName, retailerId)` 与 6 项累计指标。
  - 语义：严格复刻原 SQL 的口径与窗口累计：
    - 月份集合 = 客户创建月 ∪ 绑定创建月 ∪（有绑定关系的）项目创建月。
    - 当月新增：
      - 主动/配合客户：按客户创建时间与类型、`del_flag='0'`。
      - 解绑：按绑定创建时间且 `del_flag='1'`。
      - 项目总数/有效/BFO：按项目创建时间、项目与客户存在绑定、分别计算总数/`del_flag='0'`/`project_type=BFO`。
    - 累计：按 `retailer_id` 分组、月份升序进行逐月累加。

- 新服务类：`RefactoredStatisticsTaskService`（V2）
  - 读取底表：用 `JdbcTemplate` 查询客户/项目/绑定并映射为领域对象（枚举安全解析）。
  - 计算：调用 `MonthlyStatisticsCalculator`，得到逐月累计。
  - 写入：透视为 6 行看板记录并写入 `bip_dashboard_statistics`；对 H2/MySQL 做“方言分支”处理（引号列名 vs 原生 SQL）。
  - 定时：`@Scheduled("0 45 23 * * ?")` 与 `@Async("taskExecutor")`。

- 新控制器：`RefactoredStatisticsController`（V2 手动触发）
  - 路由：`GET /api/statistics/v2/trigger`。
  - 与原 `/api/statistics/trigger` 并存。

---

## TDD 实施过程

1. 编写单元测试（先行）
   - `MonthlyStatisticsCalculatorTest`
     - `emptyInputReturnsEmpty`：空输入返回空结果，覆盖排序与边界。
     - `minimalSampleAggregationAndCumulative`：复刻 `wiki/HowToVerifyResults.md` 的最小样例，逐月累计断言六项值（覆盖主要路径）。
     - `filtersAndNullsAndUnionCoverage`：覆盖 `del_flag` 过滤、缺失类型、缺失项目、`null create_time` 的守卫、月份并集行为、BFO/有效项目的条件分支。
   - 目标：新的 Java 统计逻辑达到 100% 行/分支/条件覆盖。

2. 编写实现（围绕测试通过）
   - 实现 `MonthlyStatisticsCalculator.calculate(...)`：
     - 建立 `retailerId → YearMonth 集合` 的月份并集；
     - 对每月计算“当月新增”，随后累加到累计；
     - 排序规则与原 SQL 对齐：`retailer_name` → `retailer_id` → `yyyyMm`。
   - 实现 `RefactoredStatisticsTaskService`：数据装配、调用计算器、写入看板。
   - 增加 `RefactoredStatisticsController`：V2 触发入口。

3. 验证与并行运行
   - 保留原 `StatisticsTaskService` 与 `StatisticsMapper.xml` 不动；V1 与 V2 并行存在，便于对照与回退。
   - 在验证阶段可以同时运行两条路径，比较透视后的结果是否一致（建议留到集成验证）。

---

## 代码锚点

- 新计算器：`src/main/java/com/simon/case_study_statistics_by_month/service/MonthlyStatisticsCalculator.java`
- 新服务：`src/main/java/com/simon/case_study_statistics_by_month/service/RefactoredStatisticsTaskService.java`
- 新控制器：`src/main/java/com/simon/case_study_statistics_by_month/controller/RefactoredStatisticsController.java`
- 测试：`src/test/java/com/simon/case_study_statistics_by_month/MonthlyStatisticsCalculatorTest.java`
- 旧 SQL：`src/main/resources/mapper/StatisticsMapper.xml:20-115`

---

## 与“巨无霸 SQL”的语义一致性说明

- 月份来源：等价于原 SQL 的 `all_customer_months` UNION 三路来源（客户/绑定/项目创建月）。
- 客户指标：`del_flag='0'` 且类型匹配（Type0/Type1）；时间分桶为客户创建月。
- 解绑指标：绑定 `del_flag='1'`，时间分桶为绑定创建月。
- 项目指标：项目创建月 + 存在客户绑定关系；有效项目依据 `p.del_flag='0'`，BFO 依据 `p.project_type=Type0`。
- 累计：等价于窗口函数 `SUM(...) OVER (PARTITION BY retailer_id ORDER BY yyyy_mm)`。

---

## 观点研判与建议

- “重构过后，不删除原本的代码，方便对比。其实就是重写。”
  - 研判：在绞杀式策略下，短期保留旧路径是正确的（对照与回退），但本质是“演进替换”，不是“仅仅重写”。重写只是手段，关键在于并行运行、逐步切换流量、建立信任后再下线旧路径。
  - 建议：保留旧 SQL 与旧 Service/Controller，新增 V2 路径，先在预生产进行一致性与性能验证，达标后切换并最终下线旧路径。

- “重写新的 controller - service - 定时任务，和原来的逻辑相对照，类文件也创建新的。”
  - 研判：正确。当前已新增 `RefactoredStatisticsController`、`RefactoredStatisticsTaskService` 与新的计算器；与原逻辑并存，命名区分清晰，便于开关与对照。
  - 建议：通过配置或开关控制 V1/V2 的触发入口，逐步切换；保留审计与回滚路径。

---

## 后续工作（可选）

- 集成比对测试：在同一数据集上同时跑 V1/V2，透视后比对各维度的 6 项结果；记录差异并修正。
- 性能优化：对 `MonthlyStatisticsCalculator` 做数据预索引（如 `customerId→bindings→projects` 的层级映射）以降低循环复杂度；仅在数据规模增大时需要。
- 可观测性：统一日志前缀（`StatisticsTask-*` 与 `V2` 标签）、统计耗时与分段计数，便于压测与调优。
