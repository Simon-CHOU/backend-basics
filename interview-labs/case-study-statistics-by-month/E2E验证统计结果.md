# 看板统计结果端到端验证方法与结论

## 验证目标
- 针对 `bip_dashboard_statistics` 的月度累计统计结果，进行端到端验证，确保与底层来源数据计算逻辑一致。
- 范围涵盖：客户类型累计、解绑累计、项目总数累计、有效项目累计、BFO 项目累计。

## 验证环境与前置
- 运行环境：Spring Boot 3 + MyBatis；测试环境采用 `H2` 内存库（MySQL 模式）。
- 关键代码参考：
  - 定时任务入口与实现：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:31-35, 41-67`
  - 统计 SQL（生产同源）：`src/main/resources/mapper/StatisticsMapper.xml:20-115`
  - 看板写入 SQL：`src/main/resources/mapper/StatisticsMapper.xml:117-126`
  - 手动触发接口：`src/main/java/com/simon/case_study_statistics_by_month/controller/StatisticsController.java:31`

## 验证步骤
- 清库（测试环境）：
  - 执行 `TRUNCATE` 依次清空 `bip_dashboard_statistics`、`bip_customer`、`bip_project`、`bip_binding`；在测试用例中通过 `JdbcTemplate` 执行。
- 生成模拟数据：
  - 运行 `GenMimicDataTest.gen` 将 2024-11 至 2025-05 范围内的客户、项目、绑定关系数据批量写入；客户类型与项目类型均以枚举代码入库（`Type0/Type1/Type2`）。
- 触发统计：
  - 手动触发统计任务 `GET /api/statistics/trigger` 或在测试中直接调用 `StatisticsTaskService.triggerManualStatistics()`，由定时任务逻辑执行统计。
- 构造“期望值”：
  - 使用与生产一致的统计逻辑：对底表按月份计算当月新增（基于时间边界：`[当月起, 次月起)`），并做窗口累计生成每月累计结果。
  - 客户累计：`customer_type=Type0/Type1` 且 `del_flag='0'`。
  - 解绑累计：`bip_binding.del_flag='1'`。
  - 项目累计：绑定过的项目总数、有有效标记项目总数（`del_flag='0'`）、`project_type=Type0` 的 BFO 项目数。
- 对比“实际值”：
  - 从 `bip_dashboard_statistics` 读取 `(year, month, retailer_id)` 维度的六个 `sub_task` 值，透视为列并与期望值逐项比对。

## 对比所用 SQL（生产 MySQL）
```sql
SELECT d.year, d.month, d.retailer_id,
       SUM(CASE WHEN d.sub_task='主动型客户数量' THEN CAST(d.result AS UNSIGNED) END) AS proactive_customer_count,
       SUM(CASE WHEN d.sub_task='配合行客户数量' THEN CAST(d.result AS UNSIGNED) END) AS cooperative_customer_count,
       SUM(CASE WHEN d.sub_task='解绑数量' THEN CAST(d.result AS UNSIGNED) END) AS unbinding_count,
       SUM(CASE WHEN d.sub_task='项目信息累计数量' THEN CAST(d.result AS UNSIGNED) END) AS total_project_count,
       SUM(CASE WHEN d.sub_task='有效项目累计数量' THEN CAST(d.result AS UNSIGNED) END) AS active_project_count,
       SUM(CASE WHEN d.sub_task='重要BFO ID项目累计数量' THEN CAST(d.result AS UNSIGNED) END) AS bfo_project_count
FROM bip_dashboard_statistics d
GROUP BY d.year, d.month, d.retailer_id;
```

## 一致性检查要点
- 行量校验：`行数 = 月份数 × 零售商数 × 6`（六个统计项）。
- 单调性：对同一 `retailer_id`，各累计指标随月份递增不下降。
- 完整性：`retailer_id/retailer_name` 成对存在；`task='项目统计信息'`、`unit='个'`、`del_flag='0'`。

## 实际执行结论（测试环境）
- 统计任务执行完成且对比通过：
  - 控制台输出摘要：
    - `E2E verify passed`
    - `months=7, retailers=35, rows=1470`
  - 行量与期望一致：`7 × 35 × 6 = 1470` 行。
  - 期望值与看板表各指标逐列比对完全一致。

## 说明
- 领域模型类型枚举化：
  - `Customer.customerType` 改造为 `CustomerType`（`src/main/java/com/simon/case_study_statistics_by_month/domain/Customer.java:14`）。
  - `Project.projectType` 改造为 `ProjectType`（`src/main/java/com/simon/case_study_statistics_by_month/domain/Project.java:14`）。
  - 统计任务和模拟数据写库均通过枚举 `.getCode()` 与 DB 代码一致，避免硬编码。
- 方言兼容策略：
  - 测试环境采用 `H2`，在 `StatisticsTaskService.saveStatisticItem` 中对 `year/month` 列名使用引号并用 `JdbcTemplate` 插入（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:104-126`）。
  - 生产环境保持 MyBatis SQL（`StatisticsMapper.xml`）不变。

## 推荐的生产验证流程
- 在生产或准生产库中：
  - 运行同源统计查询（`StatisticsMapper.selectMonthlyStatistics`）获取期望结果（`src/main/resources/mapper/StatisticsMapper.xml:20-115`）。
  - 用上文“对比所用 SQL”对 `bip_dashboard_statistics` 透视后逐项比对。
  - 抽样核对：选择 1 个 `retailer_id` 与 2-3 个连续月份，直接核算底表增量与累计逻辑，确保无新增月时累计保持不变。

