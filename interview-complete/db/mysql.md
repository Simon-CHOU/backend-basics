

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
出现selectivity下降的问题，这种问题是不是表明一开始的设计就不良？不是，日期是自然写入


CITI014 数据库的隔离级别
数据库隔离级别是ACID特性中**隔离性(Isolation)**的具体实现，用于控制并发事务之间的相互影响程度。它解决了多个事务同时访问数据库时可能出现的数据一致性问题。
理解三个核心并发问题：

dirty read
```
事务A: BEGIN -> UPDATE account SET balance=100 WHERE id=1 -> (未提交)
事务B: BEGIN -> SELECT balance FROM account WHERE id=1 -> 读到100 -> COMMIT
事务A: ROLLBACK
```
事务B读取了事务A未提交的数据，但事务A最终回滚了。

non-repetable read
```
事务A: BEGIN -> SELECT balance FROM account WHERE id=1 -> 读到50
事务B: BEGIN -> UPDATE account SET balance=100 WHERE id=1 -> COMMIT
事务A: SELECT balance FROM account WHERE id=1 -> 读到100 -> COMMIT
```
同一事务内两次读取同一数据得到不同结果。

phantom read
```
事务A: BEGIN -> SELECT COUNT(*) FROM account WHERE balance>50 -> 读到5条
事务B: BEGIN -> INSERT INTO account VALUES(6, 60) -> COMMIT
事务A: SELECT COUNT(*) FROM account WHERE balance>50 -> 读到6条 -> COMMIT
```
同一事务内两次范围查询得到不同的记录数。

* 同样是一个事务内两次读结果不一样，non-repeatable 是值，phantom是记录数。
为什么要分别讨论？

4个隔离级别
read uncommited
read committed  fix ：解决脏读
repeatable read fix ：解决不可重复读
serializable fix ：解决幻读

*注：MySQL InnoDB在RR级别下通过Gap Lock解决了幻读

隔离级别的实现：

锁机制

共享锁(S锁): 读锁，多个事务可同时持有
排他锁(X锁): 写锁，独占访问
意向锁: 表级锁，提高锁检测效率
Gap锁: 间隙锁，防止幻读

MVCC

原理: 为每行数据维护多个版本
实现: 通过undo log和read view
优势: 读写不冲突，提高并发性能



MEIT001 现有一个需求中的字符串，长度为20。请问应该如何设计表字段？使用什么字段类型？ char varchar 还是 text

VARCHAR(20) 为宜。这种设计既保证了存储效率，又维持了良好的查询性能，是最符合实际业务需求的选择。

避免 : CHAR(20) - 除非确定所有值都是20字符
推荐 : TEXT - 对于短字符串过度设计

存储空间对比（以"Hello"为例）：
+----------+----------+----------+
| 类型     | 存储字节 | 说明     |
+----------+----------+----------+
| CHAR(20) | 20字节   | 浪费15字节|
| VARCHAR(20)| 6字节  | 5字符+1前缀|
| TEXT     | 10字节   | 5字符+指针|
+----------+----------+----------+

