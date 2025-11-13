# STAR 项目经历 — 月度统计任务重构与验证

## 执行摘要（金字塔：结论先行）
- 用“绞杀式重构 + TDD”以纯 Java 逐步替换巨无霸 SQL，保留旧路径并行对照与回退。
- 新核心统计逻辑实现 100% 行/分支/条件覆盖，跨 H2/MySQL 方言一致产出。
- 建立可复制的端到端验证与教程，降低黑盒验证摩擦，提升可维护性与可审计性。
- 通过枚举化、异步线程池策略与方言分支隔离，消除技术债与隐性缺陷点。

---

## Situation（情境）
- 历史包袱：`StatisticsMapper.xml` 中 `selectMonthlyStatistics` 属于“巨无霸 SQL”，逻辑跨窗口统计、月份拼接与累计聚合，维护困难且变更风险高。
- 业务痛点：统计表 `bip_dashboard_statistics` 需展示 `retailer_id` 与 `retailer_name` 的差异化统计，现状不够清晰可审计。
- 技术风险：
  - 数据库方言差异（H2 vs MySQL）：保留字冲突（`year`, `month`）、函数缺失（`DATE_FORMAT`）。
  - 端到端验证多为黑盒，不利于定位与 TDD 精准保障。
- 交付压力：在不重写全项目的前提下提升可靠性、可维护性与验证可复制性。

## Task（任务）
- 以“绞杀式”策略重构统计链路：在不移除旧路径前提下引入新控制器/服务/计算器，支持并行对比与回退。
- 严格遵循 TDD：先写测试、确保新 Java 统计逻辑 100% 行/分支/条件覆盖。
- 建立跨方言一致性：针对 H2/MySQL 做函数与标识符差异处理，保证结果一致。
- 提供工程化验证：输出 E2E 验证方法、可复现教程与术语定义，降低协作成本。

## Action（行动）
- 领域模型强化与去硬编码：
  - 将 `Customer.customerType` 与 `Project.projectType` 提取为枚举，统一编码来源，消除魔法常量。
  - 在模拟数据与定时任务链路中全面替换硬编码，提升类型安全与可读性。
- 方言与写链路修复：
  - 设计 H2/MySQL 分支：H2 下对 `"year"`, `"month"` 进行引号保护，替代不可用函数；MySQL 用原生语法。
  - 在服务层引入 `JdbcTemplate` 写入分支，确保插入语句在两种方言均稳定。
- 绞杀式重构落地：
  - 新增纯 Java 计算器 `MonthlyStatisticsCalculator`（纯函数设计，复刻 SQL 语义：月份集合、增量与累计、分类聚合、排序）。
  - 新增 `RefactoredStatisticsTaskService` 与 `RefactoredStatisticsController`（V2），与旧路径并行运行，支持手动触发与定时任务。
- TDD 与覆盖率保障：
  - 编写 `MonthlyStatisticsCalculatorTest`，构建空输入、最小样本、边界与过滤等用例，做到 100% 行/分支/条件覆盖。
  - 以“期望 vs 实际”断言累计值与分类计数，锁定核心语义正确性。
- 验证与知识沉淀：
  - 输出 E2E 验证方法与比对结论至根目录，编写 `tuto/HowToVerifyResults.md` 详细教程，细化每步计算与 SQL 对照。
  - 解释异步线程池参数（`AsyncConfig.taskExecutor`），给出生产调优建议与负载建模。
  - 分析在现状下的单元验证可行性与边界，编写 `wiki/UnitVerify.md`；补充“方言/方言分支”标准定义与官方引用。
  - 在 `wiki/Refactoring.md` 记录设计与过程，研判“保留旧代码是否等于重写”等观点并给出结论。

## Result（结果）
- 业务成效：`bip_dashboard_statistics` 成功呈现 `retailer_id` 与 `retailer_name` 的差异项，验证链路清晰、可复现。
- 质量保障：
  - 新统计核心（Java）实现 100% 覆盖度（行/分支/条件），黑盒验证可转化为白盒保障。
  - H2/MySQL 跨方言一致输出，解决 3 类兼容问题（保留字、函数缺失、插入语法）。
- 可维护性：
  - 以绞杀式策略将巨无霸 SQL 迁出核心计算，复杂度转移到可测试的 Java 纯函数。
  - 并行保留旧路径，降低回归风险；控制器与服务分层简化后续迭代成本。
- 团队协作：
  - 验证方法、教程与术语沉淀到仓库，开发/QA/架构协同效率提升，入职学习曲线变缓。

---

## 架构摘示（ASCII UML）

```
[Client/Job]                          
    |                                    
    v                                    
[/api/statistics] (V1) ----> [SQL Mapper V1] ---> [DB Write]
    \
     \-- [/api/statistics/v2] ---> [Service V2] ---> [Calculator]
                                   |                    |
                                   |                [Aggregation]
                                   v                    |
                               [Dialect Writer] <-------/
                                   |-- H2: quoted identifiers
                                   |-- MySQL: native syntax
```

---

## 关键产出（可查阅文件）
- 纯 Java 统计计算器：`src/main/java/.../service/MonthlyStatisticsCalculator.java`
- 新版服务与控制器：
  - `src/main/java/.../service/RefactoredStatisticsTaskService.java`
  - `src/main/java/.../controller/RefactoredStatisticsController.java`
- 单元测试（100% 覆盖）：`src/test/java/.../MonthlyStatisticsCalculatorTest.java`
- 验证方法与教程：
  - 根目录：`E2E验证统计结果.md`
  - 教程：`tuto/HowToVerifyResults.md`
- 线程池参数说明：`wiki/ThreadPoolParams.md`
- 单元验证可行性与术语：`wiki/UnitVerify.md`
- 重构设计与研判：`wiki/Refactoring.md`

---

## 数字化亮点（简历友好）
- 新核心统计逻辑 100% 行/分支/条件覆盖；跨方言一致；可回退。
- 消除 3 类方言兼容问题；减少黑盒验证时间成本与定位难度。
- 输出 5 类单元验证场景与完整教程，支持新人 1 小时内复现。
- 枚举化与异步化提升稳定性与吞吐，明确生产调优策略。

---

## 附录：电话面试逐字稿（可 5–8 分钟）

（开场）
- 您好，我负责一次“月度统计任务”的绞杀式重构与工程化验证。核心目标是以 TDD 将巨无霸 SQL 迁移到可维护、可测试、可回退的纯 Java 逻辑，同时保证跨 H2/MySQL 的一致产出。

（背景痛点）
- 旧逻辑集中在一个庞大的 SQL 中，包含窗口函数与月份累计，改动稍有不慎就会引发回归。验证主要依赖端到端黑盒对比，问题定位成本高。

（我的做法）
- 我采用绞杀式策略：并行保留旧路径，同时引入新的控制器与服务，并使用一个纯 Java 计算器复刻 SQL 的业务语义。这样既能稳妥上线，又能对比与回退。
- 我坚持 TDD：先写 `MonthlyStatisticsCalculatorTest`，覆盖空数据、最小样本、边界/过滤等分支，做到新逻辑 100% 行/分支/条件覆盖。
- 针对方言差异，我在写链路中做了 H2 与 MySQL 的分支处理：H2 下对保留字加引号、替换不可用函数；MySQL 用原生语法，确保两端一致的统计结果。

（工程化落地）
- 我将 `Customer.customerType` 与 `Project.projectType` 提取为枚举，统一编码，避免魔法常量。
- 新增 `RefactoredStatisticsTaskService`（含定时与手动触发）与 `RefactoredStatisticsController`，与旧路径并存，对照验证更直观。
- 我输出了 E2E 验证方法与详细教程，明确每一步数据与累计计算规则，让开发与 QA 均可复现，不再只依赖黑盒。
- 我补充了线程池参数说明与调优建议，确保异步执行在生产负载下也可控。

（结果与价值）
- 统计表现在能清楚区分 `retailer_id` 与 `retailer_name`，并具备跨方言一致性。
- 新逻辑具有 100% 覆盖度与可回退能力，极大降低维护风险。
- 团队协作方面，教程与术语沉淀缩短了入职学习路径，提高了验证与审查效率。

（取舍与经验）
- 我们没有一次性删除旧路径，而是通过并行替换逐步迁移，兼顾安全与效率。这并不是“仅仅重写”，而是“演进替换”。
- 在现有约束下，单元测试关注纯函数核心与可控的写链路契约，黑盒 E2E 则作为最终结果验收，二者协同提升信心。

（总结）
- 整体而言，本次重构在保证业务连续性的前提下，显著提升了可维护性、可验证性与跨环境一致性，且为后续迭代预留了清晰扩展点。