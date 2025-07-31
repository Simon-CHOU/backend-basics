

KCZN002
PostgreSQL和MySQL有什么区别？
> 这个问题的关键不是哪个数据库"更好"，而是理解它们的设计哲学差异：PostgreSQL追求功能完整性和标准合规，MySQL追求性能和易用性。作为Senior SDE，需要根据具体的业务需求、团队技能、性能要求和数据完整性需求来做出技术选型决策。

#选型，设计和架构特性，技术前边，社区生态，开源前途（发展趋势）、业务价值、工程代价。

面对我们一开始提到的问题，我不要光答 “sql 写法有别”这个层次的内容。
因为，两个不同的数据库，写法当然有别了。
我再怎么声称自己对两者的sql语法区别有充分认识，真到了生产环境，还不是该查文档查文档RTFM。
所以“声称我真的十分了解他们的语法差异”，不能真正地提高我的胜任力。

lab: 根据高观点下的差异，性能、部署、迁移、集群、分库分表等选型逻辑。

参考 

postgre anniversary  ucberkeley
PostgreSQL History: From Berkeley to Global Standard
https://tech-champion.com/general/unveiling-the-history-of-postgresql-from-berkeley-to-global-standard/

#去IOE#传统IT架构vs互联网架构#技术史
https://www.zhihu.com/question/23119444

平安 汪洋 文集 
中国平安 Postgresql 选型之路 汪洋
https://pic.huodongjia.com/ganhuodocs/2017-07-10/1499668681.24.pdf
https://www.modb.pro/db/186516
https://www.163.com/dy/article/D6O1AJ9M0511FQO9.html

uber 
https://www.uber.com/blog/postgres-to-mysql-migration/


Uber migrated a significant portion of its core database infrastructure from PostgreSQL to MySQL in the mid-2010s. This decision was primarily driven by scalability challenges and operational limitations encountered with PostgreSQL in a rapidly growing, high-transaction environment.
Reasons for the Migration:

Schema Changes and Downtime:

PostgreSQL's approach to schema alterations often required table-level locks, leading to significant downtime for critical services during schema migrations. MySQL, particularly with the InnoDB storage engine and custom tooling developed by Uber, allowed for online, non-blocking schema changes, minimizing service interruptions.

Replication and Data Consistency:

Uber found PostgreSQL's replication mechanisms to be less efficient and prone to issues that could lead to data inconsistencies across their distributed microservices. MySQL's mature replication capabilities, including features like semi-synchronous replication, offered better guarantees for data consistency and fault tolerance under heavy write loads.

Write-Heavy Workloads and Performance:

PostgreSQL's MVCC (Multi-Version Concurrency Control) implementation, while providing transactional integrity, could lead to write amplification and increased disk I/O, particularly with frequent updates and numerous indexes. MySQL, with its different write-handling characteristics, was deemed more suitable for Uber's intensive write-heavy workloads.

Horizontal Scalability and Sharding:

While both databases can be scaled, Uber found MySQL's design and ecosystem more conducive to building out their sharding layer, known as "Schemaless," which enabled efficient horizontal scaling to handle massive data volumes and user traffic.

Challenges and Solutions:

The migration was not without challenges, including potential data loss and inconsistencies during the transition. Uber addressed these by developing specialized tools and processes to ensure data integrity and a smooth transition during the migration. This included developing a schema change tool for online, asynchronous, and lock-free alterations, and tools to ensure data consistency during these changes.

Outcome:

Uber's migration to MySQL ultimately aimed to achieve more efficient and flexible database operations, enabling improved scalability, reliability, and reduced operational overhead for their rapidly expanding services.