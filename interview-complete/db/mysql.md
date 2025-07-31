

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

SQL解决方案：
基于游标的分页（Cursor-based Pagination）
子查询优化（延迟关联）​​子查询获取第n条记录的id 减少回表
覆盖索引（Covering Index）​​ 无需回表
分段查询

架构级优化：
分区

证明：
时间复杂度分析
analyze 评估

lab: 深分页实验。PageHelper深分页劣化benckmark.
lab:你怎么证明 dmbs的处理方式会是“传统的LIMIT X OFFSET Y查询，数据库需要扫描并跳过Y条记录，然后才开始返回X条记录。” 
lab: 如果主键无序uuid/snowfalke 又如何处理
lab: pagehelper, spring-data-jpa 又如何实现深分页优化。

TOUG001 MVCC 是什么？   
MVCC是多版本并发控制，核心思想是通过维护数据的多个版本，
让读写操作不互相阻塞，从而提升数据库并发性能。  


M - Multi-Version (多版本机制)
V - Visibility (可见性规则) 
C - Concurrency (并发控制)
C - Consistency (一致性保证)
S - Scenario (应用场景)
T - Technology (技术实现)
A - Advantage (核心优势)
R - Risk (潜在问题)

工作原理，关键概念和运行流程？

lab: 如何证明MVCC在工作。不懂MVCC会遇到哪些问题。



JITU005 一个查询sql  where created_date between 一个时间段，已经加了索引，但效率还是不高，可能是哪些方面的原因？怎么解决？
原因：
索引：类型不当，*符合索引顺序，统计信息过期
条件：范围过大、数据分布不匀（数据倾斜data skew），表数据量大。

解决：
使用覆盖索引
避免函数操作 DATE(created_date) between x and y

诊断：
执行计划分析explain
慢查询日志
性能监控



JITU006 explain 进行执行计划的查询，你会关注哪些参数？去定位到性能问题呢？
type: system > const > eq_ref > ref > range > index > ALL
key
rows
extra
filtered

JITU007 一张表created_date自动，数据离散度可能不高，时间 between A and B ，可能整个数据库就2个月，请问这种情况下如何优化查询效率？
B+Tree index性能下降
当数据离散度低时，传统的B+树索引效率会下降，因为：

- 索引选择性差（Selectivity低）
- 大量数据集中在少数几个时间段
- BETWEEN A AND B 查询可能扫描大量数据页

优先级排序 ：复合索引 > 分区表 > 查询重写 > 缓存策略
测试验证 ：每个优化都要通过 EXPLAIN 分析执行计划
监控指标 ：关注查询时间、扫描行数、索引命中率
渐进优化 ：从简单到复杂，逐步实施优化措施

lab: 不改表结构，如何优化？