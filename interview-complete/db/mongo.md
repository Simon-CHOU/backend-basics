
XXWJ013 有用过MongoDB吗？
没有，但是知道一些理论。
它是一个文档型NoSQL数据库，具有以下特点：

- 文档存储 ：使用BSON格式存储数据，比关系型数据库更灵活
- 水平扩展 ：支持分片(Sharding)，适合大数据量场景
- 高可用性 ：支持副本集(Replica Set)保证数据安全
- 索引支持 ：提供丰富的索引类型提升查询性能

MongoDB特别适合以下场景：

- 需要快速迭代的项目（schema灵活）
- 大数据量的读写操作
- 需要存储复杂嵌套数据结构
- 实时分析和内容管理系统

对比关系型数据库
- ACID vs BASE ：了解MongoDB的最终一致性特点
- JOIN操作 ：知道MongoDB通过$lookup实现类似功能
- 事务支持 ：了解MongoDB 4.0+支持多文档事务
提及相关技术栈
- 如果熟悉Node.js，可以提到Mongoose ODM
- 如果用过Spring，可以提到Spring Data MongoDB
- 了解MongoDB Atlas云服务

*这些场景看起来 mysql/postgre并非不能替代，为什么要用mongo

lab: spring data mogodb 构建一个MVP(minimum viable product)