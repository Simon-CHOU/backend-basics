

HUAW004 具体从SQL处做深度分页优化，这个了不了解？就比如说我现在让你写一个SQL语句，需要你从100万数据开始查，查到100数据中的第一条
精确表述：
- 从第100万条记录之后开始查询
- 获取接下来的第1条记录
- 即查询第1000001条记录

> 游标不是SQL关键字，而是一种分页设计思想 ：
> 1. 核心思想 ：记住位置，顺序前进
> 2. 实现方式 ：WHERE条件 + ORDER BY + LIMIT
> 3. 关键要素 ：可排序的字段作为游标值
> 4. 性能优势 ：避免大偏移量扫描

解决方案：
限制最大页数
基于游标的分页（Cursor-based Pagination）
子查询获取第n条记录的id
延迟关联（）
分段查询

证明：
时间复杂度分析
analyze 评估

lab: 深分页实验。PageHelper深分页劣化benckmark.
lab:你怎么证明 dmbs的处理方式会是“传统的LIMIT X OFFSET Y查询，数据库需要扫描并跳过Y条记录，然后才开始返回X条记录。” 
lab: 如果主键无序uuid/snowfalke 又如何处理
lab: pagehelper, spring-data-jpa 又如何实现深分页优化。
