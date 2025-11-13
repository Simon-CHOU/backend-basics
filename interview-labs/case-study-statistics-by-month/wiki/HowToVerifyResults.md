# 看板统计结果端到端验证（手把手复现教程）

**你将获得什么**
- 完整的复现实操：从清库、造数、触发统计到结果对比的每一步。
- 两条路径：一键测试法（适合快速验证）与手工数据库法（适合深度理解）。
- 最小数据样例与逐月累计计算方法，确保你能独立核算并确认正确性。

**关键概念（先讲清楚）**
- 维度定义：以 `(year, month, retailer_id)` 为统计维度，每月累计的 6 个指标。
- 时间边界：所有“当月新增”均按左闭右开区间 `[当月首日 00:00:00, 次月首日 00:00:00)`。
- 累计逻辑：每月值 = 前月累计 + 当月新增（窗口函数 `SUM ... OVER (PARTITION BY retailer ORDER BY month)`）。
- 指标口径：
  - 主动型客户累计：`bip_customer.del_flag='0' AND customer_type=Type0` 的当月新增累计。
  - 配合型客户累计：`bip_customer.del_flag='0' AND customer_type=Type1` 的当月新增累计。
  - 解绑累计：`bip_binding.del_flag='1'` 的当月新增累计。
  - 项目信息累计：以项目建档时间 `bip_project.create_time` 为口径，且要求该项目与该客户有绑定关系。
  - 有效项目累计：同上，但 `bip_project.del_flag='0'`。
  - 重要 BFO 项目累计：同上，但 `bip_project.project_type=Type0`（BFO）。
- 项目口径的关键细节：月度分桶使用的是项目表的 `create_time`，而不是绑定表的时间。这意味着“某客户在 12 月绑定了 11 月创建的项目”，其“新增项目”会在 11 月计入（按项目创建月）。

```
@startuml
title 统计任务数据流
actor User
participant "bip_customer" as C
participant "bip_project" as P
participant "bip_binding" as B
participant "SQL计算(StatisticsMapper.selectMonthlyStatistics)" as SQL
participant "StatisticsTaskService.processAndSaveStatistics" as S
database "bip_dashboard_statistics" as D

User -> SQL : 计算每客户每月增量与累计
SQL -> S : 返回 StatisticsResult(yyyyMm, retailerName, retailerId, 各累计值)
S -> D : saveStatisticItem(year, month, sub_task, result)
note right of D
  每个 (year, month, retailer_id)
  写入 6 行 sub_task 记录
end note
@enduml
```

---

## 路径 A：一键复现（推荐先跑通）

- 运行环境：JDK 21，默认使用 H2 内存库（测试配置已内置）。
- 入口测试：`src/test/java/com/simon/case_study_statistics_by_month/StatisticsE2EVerifyTest.java:21-65, 67-163, 213-253`
- 如何执行：
  - WSL/Linux/Mac：
    - `./mvnw -q -Dtest=StatisticsE2EVerifyTest#verifyEndToEnd test`
  - Windows PowerShell：
    - `mvnw.cmd -q -Dtest=StatisticsE2EVerifyTest#verifyEndToEnd test`
- 你将看到：控制台输出 `E2E verify passed`，并打印 `months / retailers / rows` 摘要。该测试做了以下事情：
  - 清库：`TRUNCATE bip_dashboard_statistics / bip_binding / bip_project / bip_customer`。
  - 造数：在 2024-11 至 2025-05 的月份区间按枚举 `CustomerType / ProjectType` 批量插入数据。
  - 触发统计：调用 `StatisticsTaskService.triggerManualStatistics()`（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:213-216`）。
  - 比对：自行按口径计算“期望值”，并与看板表实际值逐项断言。

这条路径保证你本机无需额外依赖即可跑通端到端。

---

## 路径 B：手工数据库复现（深度理解）

### 第 0 步：准备数据库
- 选项 1（推荐）：Docker Compose 启动 MySQL 8
  - 建议在 WSL2 中运行，示例 `docker-compose.yml`：
    ```yaml
    version: '3.8'
    services:
      mysql:
        image: mysql:8.0
        container_name: csdb-mysql
        ports:
          - "3306:3306"
        environment:
          - MYSQL_DATABASE=case_study_db
          - MYSQL_USER=app_user
          - MYSQL_PASSWORD=app_password
          - MYSQL_ROOT_PASSWORD=root
        command: ["--default-authentication-plugin=mysql_native_password"]
        volumes:
          - csdb_data:/var/lib/mysql
    volumes:
      csdb_data:
    ```
  - 启动：`docker compose up -d`
  - 连接检查：`mysql -h 127.0.0.1 -P 3306 -u app_user -papp_password`

- 选项 2：使用内置 H2（仅用于学习，不用于生产）
  - 测试配置位于 `src/test/resources/application.properties:2-7, 6-11`。
  - H2 会自动按 `schema.sql` 建表（`src/test/resources/schema.sql:1-54`）。

### 第 1 步：启动应用（连接 MySQL）
- 应用配置（已内置）：`src/main/resources/application.properties:3-17`。
- 如需覆盖连接信息，使用环境变量：
  - `SPRING_DATASOURCE_URL`（默认 `jdbc:mysql://localhost:3306/case_study_db?...`）
  - `SPRING_DATASOURCE_USERNAME`（默认 `app_user`）
  - `SPRING_DATASOURCE_PASSWORD`（默认 `app_password`）
- 启动命令：
  - WSL/Linux/Mac：`./mvnw -q spring-boot:run`
  - Windows PowerShell：`mvnw.cmd -q spring-boot:run`

### 第 2 步：清库（手工）
- 连接到数据库后执行：
  ```sql
  TRUNCATE TABLE bip_dashboard_statistics;
  TRUNCATE TABLE bip_binding;
  TRUNCATE TABLE bip_project;
  TRUNCATE TABLE bip_customer;
  ```

### 第 3 步：插入“最小样例数据”
- 目标：准备 2 个客户、2 个项目与 3 条绑定，覆盖不同月份与项目类型，便于核算累计逻辑。
- 建表结构参考：`src/test/resources/schema.sql:1-54`。
- 插入数据（MySQL/H2 通用）：
  ```sql
  -- 客户（Type0=主动型，Type1=配合型；均为有效 del_flag='0'）
  INSERT INTO bip_customer (id, customer_name, customer_code, customer_type, del_flag, created_by, create_time, update_by, update_time)
  VALUES
    ('CUSTA', '客户A', 'CUSTA_CODE', 'Type0', '0', 'seed', '2024-11-10 10:00:00', 'seed', '2024-11-10 10:00:00'),
    ('CUSTB', '客户B', 'CUSTB_CODE', 'Type1', '0', 'seed', '2024-12-05 09:00:00', 'seed', '2024-12-05 09:00:00');

  -- 项目（Type0=BFO，Type1=标准，全部有效 del_flag='0'）
  INSERT INTO bip_project (id, project_name, project_code, project_type, del_flag, created_by, create_time, update_by, update_time)
  VALUES
    ('PROJ1', '项目1', 'PROJ1_CODE', 'Type0', '0', 'seed', '2024-11-12 12:00:00', 'seed', '2024-11-12 12:00:00'),
    ('PROJ2', '项目2', 'PROJ2_CODE', 'Type1', '0', 'seed', '2025-01-03 08:30:00', 'seed', '2025-01-03 08:30:00');

  -- 绑定（注意：解绑以 del_flag='1' 体现）
  INSERT INTO bip_binding (id, customer_id, project_id, del_flag, created_by, create_time, update_by, update_time)
  VALUES
    ('BIND1', 'CUSTA', 'PROJ1', '0', 'seed', '2024-11-15 13:00:00', 'seed', '2024-11-15 13:00:00'),
    ('BIND2', 'CUSTA', 'PROJ2', '1', 'seed', '2025-02-01 10:00:00', 'seed', '2025-02-01 10:00:00'),
    ('BIND3', 'CUSTB', 'PROJ1', '0', 'seed', '2024-12-20 16:00:00', 'seed', '2024-12-20 16:00:00');
  ```

### 第 4 步：触发统计任务
- 方式 1：REST 手动触发
  - `GET http://localhost:8080/api/statistics/trigger`
  - 控制器入口：`src/main/java/com/simon/case_study_statistics_by_month/controller/StatisticsController.java:27-58`
- 方式 2：在控制台查看日志，确认“统计任务执行完成”字样。

### 第 5 步：构造“期望值”（逐月增量 + 累计）

以月份 2024-11、2024-12、2025-01、2025-02 为例，分别计算两位客户的“当月新增”，再做逐月累计。

- 客户 A（`retailer_id='CUSTA'`，`retailer_name='客户A'`）
  - 当月新增（按口径区分）：
    - 2024-11：
      - 新增主动型客户：1（客户A创建于 2024-11，Type0）
      - 新增配合型客户：0
      - 新增解绑：0
      - 新增项目总数：1（PROJ1 创建于 2024-11，且与客户A绑定）
      - 新增有效项目：1（PROJ1 有效）
      - 新增 BFO 项目：1（PROJ1 为 Type0=BFO）
    - 2024-12：全部 0（当月无新增）
    - 2025-01：
      - 新增项目总数：1（PROJ2 创建于 2025-01，且与客户A绑定）
      - 新增有效项目：1（PROJ2 有效）
    - 2025-02：
      - 新增解绑：1（BIND2 为解绑记录）
  - 逐月累计：
    - 2024-11：主动型 1，配合型 0，解绑 0，总项 1，有效 1，BFO 1
    - 2024-12：主动型 1，配合型 0，解绑 0，总项 1，有效 1，BFO 1
    - 2025-01：主动型 1，配合型 0，解绑 0，总项 2，有效 2，BFO 1
    - 2025-02：主动型 1，配合型 0，解绑 1，总项 2，有效 2，BFO 1

- 客户 B（`retailer_id='CUSTB'`，`retailer_name='客户B'`）
  - 当月新增：
    - 2024-11：
      - 新增项目总数：1（PROJ1 创建于 2024-11，且客户B有绑定）
      - 新增有效项目：1（PROJ1 有效）
      - 新增 BFO 项目：1（PROJ1 为 Type0）
    - 2024-12：
      - 新增配合型客户：1（客户B创建于 2024-12，Type1）
    - 2025-01/02：全部 0（当月无新增）
  - 逐月累计：
    - 2024-11：主动型 0，配合型 0，解绑 0，总项 1，有效 1，BFO 1
    - 2024-12：主动型 0，配合型 1，解绑 0，总项 1，有效 1，BFO 1
    - 2025-01：主动型 0，配合型 1，解绑 0，总项 1，有效 1，BFO 1
    - 2025-02：主动型 0，配合型 1，解绑 0，总项 1，有效 1，BFO 1

### 第 6 步：查询“实际值”（看板透视）

- MySQL 透视 SQL：
  ```sql
  SELECT d.year, d.month, d.retailer_id,
         SUM(CASE WHEN d.sub_task='主动型客户数量' THEN CAST(d.result AS UNSIGNED) END) AS proactive_customer_count,
         SUM(CASE WHEN d.sub_task='配合行客户数量' THEN CAST(d.result AS UNSIGNED) END) AS cooperative_customer_count,
         SUM(CASE WHEN d.sub_task='解绑数量' THEN CAST(d.result AS UNSIGNED) END) AS unbinding_count,
         SUM(CASE WHEN d.sub_task='项目信息累计数量' THEN CAST(d.result AS UNSIGNED) END) AS total_project_count,
         SUM(CASE WHEN d.sub_task='有效项目累计数量' THEN CAST(d.result AS UNSIGNED) END) AS active_project_count,
         SUM(CASE WHEN d.sub_task='重要BFO ID项目累计数量' THEN CAST(d.result AS UNSIGNED) END) AS bfo_project_count
  FROM bip_dashboard_statistics d
  GROUP BY d.year, d.month, d.retailer_id
  ORDER BY d.retailer_id, d.year, d.month;
  ```

- H2 透视 SQL（类型转换用 `INT`）：
  ```sql
  SELECT d."year" AS year, d."month" AS month, d.retailer_id,
         SUM(CASE WHEN d.sub_task='主动型客户数量' THEN CAST(d.result AS INT) END) AS proactive_customer_count,
         SUM(CASE WHEN d.sub_task='配合行客户数量' THEN CAST(d.result AS INT) END) AS cooperative_customer_count,
         SUM(CASE WHEN d.sub_task='解绑数量' THEN CAST(d.result AS INT) END) AS unbinding_count,
         SUM(CASE WHEN d.sub_task='项目信息累计数量' THEN CAST(d.result AS INT) END) AS total_project_count,
         SUM(CASE WHEN d.sub_task='有效项目累计数量' THEN CAST(d.result AS INT) END) AS active_project_count,
         SUM(CASE WHEN d.sub_task='重要BFO ID项目累计数量' THEN CAST(d.result AS INT) END) AS bfo_project_count
  FROM bip_dashboard_statistics d
  GROUP BY d."year", d."month", d.retailer_id
  ORDER BY d.retailer_id, d."year", d."month";
  ```

### 第 7 步：与“期望值”逐项核对
- 用第 5 步中计算出的累计表，与第 6 步 SQL 输出逐月比对：
  - 客户A的 2024-11 至 2025-02 六项值与上文累计一致。
  - 客户B的 2024-11 至 2025-02 六项值与上文累计一致。
- 若存在差异：
  - 回到口径检查：是否错误地使用了绑定时间而非项目创建时间？客户是否 `del_flag='0'`？项目是否有效？
  - 检查是否跨越区间边界（例如 2025-02 的解绑应进入 2025-02 当月）。

---

## 附：源码锚点（便于快速定位）
- 统计查询 SQL：`src/main/resources/mapper/StatisticsMapper.xml:20-115`
- 看板写入 SQL：`src/main/resources/mapper/StatisticsMapper.xml:117-126`
- 统计任务入口（定时）：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:40-44`
- 异步执行统计：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:50-81`
- H2 回退计算：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:83-127`
- 看板写入实现：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:140-196`
- H2 检测：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:198-208`
- 手动触发接口：`src/main/java/com/simon/case_study_statistics_by_month/controller/StatisticsController.java:27-58`

---

## 结论与最佳实践
- 强口径、强边界：统一按“项目创建月”计入各客户，避免混用绑定时间。
- 小样例先行：先用 2 客户 × 2 项目 × 3 绑定跑通，再扩大数据范围。
- 自动化与手工并重：路径 A 保障回归，路径 B 提供可解释的手工验证闭环。
- 数据库差异：MySQL 使用 `DATE_FORMAT` 与 `UNSIGNED`，H2 使用引号列名与 `INT`。代码已通过方言兼容确保一致性。

按本教程逐步执行，你可以在本机完整复现并理解整套 verify 过程，无需额外上下文。