-- 核心思路：
-- 1. 创建一个包含所有相关月份的时间轴 CTE (`all_months`) 作为统计基准。
-- 2. 创建三个核心 CTE (`cte1`, `cte2`, `cte3`) 分别计算客户、解绑和项目的月度增量。
-- 3. 利用窗口函数 `SUM(...) OVER (...)` 在最终查询中直接计算累计值。
-- 这种方法结构清晰，易于理解和维护。

WITH
-- CTE for all months in the data range
-- 使用 UNION 从三个表中提取所有月份，确保时间轴的完整性
all_months AS (
    SELECT DISTINCT DATE_FORMAT(create_time, '%Y-%m') AS yyyy_mm FROM bip_customer
    UNION
    SELECT DISTINCT DATE_FORMAT(create_time, '%Y-%m') AS yyyy_mm FROM bip_project
    UNION
    SELECT DISTINCT DATE_FORMAT(create_time, '%Y-%m') AS yyyy_mm FROM bip_binding
),

-- CTE1: Customer monthly increments
-- 统计每个月新增的“主动型”和“配合型”客户数量
-- 假设: customer_type 'Type0' = 主动型, 'Type1' = 配合型
cte1 AS (
    SELECT
        DATE_FORMAT(create_time, '%Y-%m') AS yyyy_mm,
        COUNT(CASE WHEN customer_type = 'Type0' THEN 1 END) AS new_proactive_customers,
        COUNT(CASE WHEN customer_type = 'Type1' THEN 1 END) AS new_cooperative_customers
    FROM
        bip_customer
    WHERE
        del_flag = '0' -- 只统计有效客户
    GROUP BY
        yyyy_mm
),

-- CTE2: Unbinding monthly increments
-- 统计每个月的解绑事件数量
cte2 AS (
    SELECT
        DATE_FORMAT(create_time, '%Y-%m') AS yyyy_mm,
        COUNT(id) AS new_unbindings
    FROM
        bip_binding
    WHERE
        del_flag = '1' -- 只统计解绑记录
    GROUP BY
        yyyy_mm
),

-- CTE3: Project monthly increments
-- 统计每个月新增的各类项目数量
-- 假设: project_type 'Type0' = 重要BFO ID项目
cte3 AS (
    SELECT
        DATE_FORMAT(create_time, '%Y-%m') AS yyyy_mm,
        COUNT(id) AS new_total_projects,
        COUNT(CASE WHEN del_flag = '0' THEN 1 END) AS new_active_projects,
        COUNT(CASE WHEN project_type = 'Type0' THEN 1 END) AS new_bfo_projects
    FROM
        bip_project
    GROUP BY
        yyyy_mm
),

-- Intermediate CTE to join all monthly increments
-- 将所有月度增量数据合并到时间轴上，并用0填充缺失值
monthly_increments AS (
    SELECT
        m.yyyy_mm,
        COALESCE(c1.new_proactive_customers, 0) AS new_proactive_customers,
        COALESCE(c1.new_cooperative_customers, 0) AS new_cooperative_customers,
        COALESCE(c2.new_unbindings, 0) AS new_unbindings,
        COALESCE(c3.new_total_projects, 0) AS new_total_projects,
        COALESCE(c3.new_active_projects, 0) AS new_active_projects,
        COALESCE(c3.new_bfo_projects, 0) AS new_bfo_projects
    FROM all_months m
    LEFT JOIN cte1 c1 ON m.yyyy_mm = c1.yyyy_mm
    LEFT JOIN cte2 c2 ON m.yyyy_mm = c2.yyyy_mm
    LEFT JOIN cte3 c3 ON m.yyyy_mm = c3.yyyy_mm
)

-- Final SELECT to calculate cumulative sums and format the output
-- 在最终查询中，使用窗口函数基于月度增量计算累计值
SELECT
    i.yyyy_mm AS `yyyy-MM`,
    SUM(i.new_proactive_customers) OVER (ORDER BY i.yyyy_mm) AS `主动型客户数量`,
    SUM(i.new_cooperative_customers) OVER (ORDER BY i.yyyy_mm) AS `配合行客户数量`,
    SUM(i.new_unbindings) OVER (ORDER BY i.yyyy_mm) AS `解绑数量`, -- 注意：此为累计解绑数量，与CTE2描述一致
    SUM(i.new_total_projects) OVER (ORDER BY i.yyyy_mm) AS `项目信息累计数量`,
    SUM(i.new_active_projects) OVER (ORDER BY i.yyyy_mm) AS `有效项目累计数量`,
    SUM(i.new_bfo_projects) OVER (ORDER BY i.yyyy_mm) AS `重要BFO ID项目累计数量`
FROM
    monthly_increments i
ORDER BY
    i.yyyy_mm;