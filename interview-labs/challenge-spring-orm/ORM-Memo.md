JPA/ORM 速通备忘录（For Dummies，Java 21）

关键概念拓扑排序（从零到一）
- 关系型数据库与SQL：表、行、列、主键、外键、事务、索引、约束是数据的物理/逻辑现实。没有这些，任何ORM都无从谈起。
- JDBC（低层I/O API）：Java访问数据库的原生接口，提供连接、语句、结果集。是ORM最终落地执行SQL的通道。
- ORM（对象关系映射）：把面向对象的实体与关系表之间做一一或多对多映射，自动生成SQL并管理对象生命周期。
- JPA（规范）：Java Persistence API 是一套接口与注解的标准，不是具体实现；定义实体、查询、事务、实体管理器等。
- Hibernate（JPA实现）：最常用的JPA实现者，负责真正把JPA规范落地成SQL与数据库交互。
- Spring Data JPA（抽象层）：在Spring之上提供Repository接口与派生查询，让你几乎不写SQL也不直接操作EntityManager。
- Entity（实体）：用`@Entity`声明的Java类，映射到某张（或多张）表，有`@Id`主键，是JPA的核心操作对象。
- Persistence Context（持久化上下文）：一个事务里被管理的实体集合（一级缓存）；负责“脏检查”“延迟写出”。
- Transaction（事务）：原子性、一致性、隔离性、持久性；JPA的所有写操作都必须绑定事务，通常用`@Transactional`开启。
- Fetch（抓取策略）：`LAZY`与`EAGER`决定关联属性是否立即加载；错误选择会导致N+1问题或过度加载。
- Cascade（级联）：`PERSIST`、`MERGE`、`REMOVE`等级联对关联实体的生命周期传播。
- Locking（锁）：乐观锁（`@Version`）与悲观锁（`LockModeType.PESSIMISTIC_*`）处理并发写入冲突。
- JPQL/Criteria/Native：JPA的查询语言、类型安全的Criteria API、以及必要时的原生SQL。
- Repository（仓库接口）：`CrudRepository`/`JpaRepository`提供CRUD、分页排序、派生查询、`@Query`。
- Caching（缓存）：一级缓存=持久化上下文；二级缓存=跨上下文可选配置（如EHCache）。

负向定义与边界（Define by Contrast & Boundary）
- JPA ≠ Hibernate：JPA是标准，Hibernate是实现。缺少实现，JPA抽象无法执行。
- JPA ≠ Spring Data JPA：后者是对JPA+Spring的便捷封装（Repository抽象、派生查询）。没有Spring Data JPA，你仍可用`EntityManager`写JPA。
- JPA ≠ MyBatis：MyBatis是半自动SQL框架，核心是你写SQL与映射；JPA是自动生成SQL与对象生命周期管理。
- 实体类 ≠ DTO/Record：实体是可被持久化管理的“有生命周期”的对象；DTO仅为数据传输，不参与持久化上下文。
- 反事实推理：如果没有事务（关键条件Y），JPA的脏检查与延迟写出就不成立；你对实体属性的修改不会自动变成`UPDATE`。

第一性原理的5-Why递归（核心机制）
- 为什么需要ORM？因为对象世界（聚合、关系）与表世界（行、外键）存在阻抗不匹配；ORM自动映射降低手写SQL与手工装配的成本。
- 为什么JPA需要持久化上下文？因为需要跟踪实体的状态与变更，才能在提交时生成最小而正确的SQL（脏检查）。
- 为什么必须事务？因为数据库写入必须满足ACID；没有事务就无法保证一致性，也无法触发flush/commit。
- 为什么会有N+1？因为`LAZY`下每次访问关联属性才单独查询；循环访问时触发大量细碎SQL。解决=抓取图/批量抓取。
- 为什么乐观锁有效？因为在提交前比较版本字段，若版本不匹配则拒绝覆盖，保护并发写的正确性；本质=比较与拒绝。

从零搭建的SOP（可复现）
- 使用Docker Compose启动PostgreSQL：
```
version: '3.8'
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_USER: app
      POSTGRES_PASSWORD: app
      POSTGRES_DB: appdb
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```
- Spring Boot依赖（pom片段）：
```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```
- application.yaml：
```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/appdb
    username: app
    password: app
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate.format_sql: true
      hibernate.show_sql: true
```
- 最小实体与Repository：
```
@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, unique = true)
  private String email;
  @Column(nullable = false)
  private String name;
  @Version
  private long version;
  // getters/setters
}

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
```
- 事务与脏检查示例：
```
@Service
public class UserService {
  private final UserRepository repo;
  public UserService(UserRepository repo) { this.repo = repo; }

  @Transactional
  public void rename(Long userId, String newName) {
    User u = repo.findById(userId).orElseThrow();
    u.setName(newName); // 不需要显式save，事务结束前flush自动生成UPDATE
  }
}
```

核心理论到代码（逐点讲透）
- 实体生命周期与状态
  - transient：新建未持久化的对象。
  - managed：被持久化上下文管理（`find`/`save`后），属性变更会被跟踪。
  - detached：上下文结束或`detach`后，变更不再被跟踪。
  - removed：标记删除，提交时执行`DELETE`。
  - 代码动作：只有managed状态才有脏检查与自动SQL。
- `@Transactional`的本质
  - 开启数据库事务，绑定一个持久化上下文（一级缓存）。
  - Why1：为什么绑定？因为需要跟踪同一事务内的实体变更。
  - Why2：为什么自动flush？提交时把缓存中的变更序列化为SQL。
  - Why3：为什么读写隔离重要？避免并发读写产生不一致视图。
  - Why4：为什么在Service层而非Controller？边界清晰，便于组合业务操作成一个原子单元。
  - Why5：为什么有只读事务？优化不必要的写跟踪，降低开销。
- 抓取策略与N+1
  - LAZY：按需查询，适合大多数关联；EAGER：立即加载，容易过载。
  - 出现场景：列表页迭代访问子集合，触发多次查询。
  - 解决动作：使用`EntityGraph`或`join fetch`一次抓取，或批量抓取。
  - 代码示例（EntityGraph）：
```
@Entity
@NamedEntityGraph(name = "user.withOrders",
  attributeNodes = @NamedAttributeNode("orders"))
public class User { /* ... */ @OneToMany(mappedBy = "user", fetch = FetchType.LAZY) Set<Order> orders; }

public interface UserRepository extends JpaRepository<User, Long> {
  @EntityGraph(value = "user.withOrders", type = EntityGraph.EntityGraphType.LOAD)
  Optional<User> findWithOrdersById(Long id);
}
```
- 乐观锁与并发控制
  - `@Version`字段每次更新自增，提交时比对版本；不一致抛异常防止丢失更新。
  - 反事实：没有`@Version`，并发写会后写覆盖前写，数据丢失。
  - 悲观锁示例：
```
@Transactional
public void reprice(Long orderId) {
  Order o = entityManager.find(Order.class, orderId, LockModeType.PESSIMISTIC_WRITE);
  o.setPrice(calculate());
}
```
- 查询方式的边界与适用
  - 派生查询：`findByEmailAndStatus`，适合简单条件。
  - `@Query`（JPQL）：适合中等复杂、与实体关联的查询。
  - Criteria API：类型安全、动态拼装复杂条件。
  - 原生SQL：性能/复杂度需求或数据库方言特性时使用。
  - 示例（JPQL与分页）：
```
@Query("select u from User u where u.name like concat('%', :kw, '%')")
Page<User> searchByName(@Param("kw") String keyword, Pageable pageable);
```
- 规格（Specification）与动态查询
```
public class UserSpecs {
  public static Specification<User> nameContains(String kw) {
    return (root, q, cb) -> cb.like(root.get("name"), "%" + kw + "%");
  }
}

repo.findAll(UserSpecs.nameContains("Alice"), PageRequest.of(0, 20));
```
- DTO与Java 21 `record`投影
  - record不适合做实体，但非常适合只读投影与接口式投影。
```
public record UserSummary(Long id, String email, String name) {}

@Query("select new com.example.UserSummary(u.id, u.email, u.name) from User u")
List<UserSummary> findAllSummaries();
```
- 虚拟线程与JPA（JDK 21）
  - 虚拟线程可并发执行I/O密集任务，但连接池是硬资源。
  - 实操：限制并发=连接池大小，避免虚拟线程淤积在获取连接：
```
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
  List<Callable<Optional<User>>> tasks = ids.stream()
    .<Callable<Optional<User>>>map(id -> () -> repo.findById(id))
    .toList();
  executor.invokeAll(tasks);
}
```
- 二级缓存与性能
  - 一级缓存=事务级；二级缓存=跨事务命中，需配置提供者（EHCache/Infinispan）。
  - 边界：缓存与一致性冲突时，以事务与锁为基准解决。

与MyBatis的核心差异与迁移SOP
- 差异
  - SQL所有权：MyBatis=你写SQL；JPA=框架生成或JPQL。
  - 对象生命周期：MyBatis不管理对象状态；JPA有上下文与脏检查。
  - 调优路径：MyBatis以SQL/索引为主；JPA以抓取策略/缓存/锁为主。
- 迁移SOP
  - Step1：识别聚合根与实体关系，画UML（见下图）。
  - Step2：为每张表建实体，补齐`@Id`、约束、关联映射。
  - Step3：把Mapper方法分为“简单查询→派生查询/JPQL”“复杂查询→原生SQL/Specification”。
  - Step4：在Service上加`@Transactional`，把原先Mapper调用替换为Repository调用。
  - Step5：审查N+1（日志）→使用抓取图或`join fetch`。
  - Step6：并发场景加`@Version`或悲观锁，回归测试。
  - Step7：必要时保留部分MyBatis用于特定复杂SQL，混合架构。

UML/ASCII示意
```
+-----------+        1     *      +------------+
|   User    |---------------------|   Order    |
| id (PK)   |                     | id (PK)    |
| email     |                     | user_id FK |
| name      |                     | total      |
+-----------+                     +------------+
        | 1     *
        |------------------+
        v                  v
  +-----------+       +-----------+
  | Address   |       | OrderItem |
  | id (PK)   |       | id (PK)   |
  | user_idFK |       | order_idFK|
  | city      |       | sku, qty  |
  +-----------+       +-----------+
```

常见坑与对策（含反事实）
- 坑：没有事务却修改实体属性，期望自动UPDATE。
  - 事实：不会发生；反事实说明了事务是必要条件。
  - 对策：所有写操作放入`@Transactional`服务方法。
- 坑：EAGER导致一次查询载入海量数据，内存飙升。
  - 对策：默认`LAZY`，用抓取图按需扩大抓取范围。
- 坑：N+1在列表页循环读取子集合。
  - 对策：日志定位，`join fetch`或`EntityGraph`，或批量抓取。
- 坑：并发写丢失更新。
  - 对策：`@Version`或悲观锁；测试竞争场景。
- 坑：错误的双向关联`equals/hashCode`引发集合行为异常。
  - 对策：基于`id`或业务唯一键实现，避免使用可变字段。

测试与验证SOP
- 单元测试（`@DataJpaTest`）：
```
@DataJpaTest
class UserRepositoryTest {
  @Autowired UserRepository repo;
  @Test void canPersistAndFind() {
    User u = new User(); u.setEmail("a@b.com"); u.setName("Alice");
    repo.save(u);
    assertTrue(repo.findByEmail("a@b.com").isPresent());
  }
}
```
- Testcontainers集成（真实PG容器）：
```
@Testcontainers
class PgIT {
  @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16");
}
```
- 验证抓取策略：打开SQL日志，观察是否存在多次细碎查询，调整`EntityGraph`或`join fetch`。

实战模板（代码片段汇总）
- 关联映射：
```
@Entity
class Order {
  @Id @GeneratedValue Long id;
  @ManyToOne(fetch = FetchType.LAZY) User user;
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  Set<OrderItem> items = new HashSet<>();
}

@Entity
class OrderItem {
  @Id @GeneratedValue Long id;
  @ManyToOne(fetch = FetchType.LAZY) Order order;
  int qty; String sku;
}
```
- `join fetch`消除N+1：
```
@Query("select o from Order o join fetch o.items where o.id = :id")
Optional<Order> findWithItems(@Param("id") Long id);
```
- 分页与排序：
```
Page<Order> page = orderRepo.findAll(PageRequest.of(0, 20, Sort.by("id").descending()));
```

选择与最佳实践清单
- 默认`LAZY`，通过抓取图或`join fetch`按需扩。
- 在Service层边界用`@Transactional`，控制一个用例的原子性。
- 对聚合根使用`@Version`，保护并发写。
- 用record做只读投影，避免把DTO当实体。
- 对复杂查询优先`@Query`/JPQL，其次原生SQL；动态条件用Specification。
- 打开SQL日志，定期审查N+1与慢查询；必要时回退到手写SQL。
- 不滥用EAGER与无限级联；集合使用`Set`避免重复，合理实现`equals/hashCode`。

边界条件的再强调（反事实归纳）
- 没有`@Id`就不是实体：JPA无法识别与跟踪对象。
- 没有事务就没有脏检查：修改不会成为SQL。
- 没有持久化上下文就没有一级缓存：每次查询都访问数据库。
- 没有抓取图/批量抓取就难以避免N+1：循环访问导致雪崩。
- 没有版本字段就无法防丢失更新：并发写覆盖不可避免。

结语
- 把JPA“玩明白”的判据：
  - 能准确选择查询方式（派生/JPQL/Specification/原生）。
  - 能设计实体关系、抓取策略，主动消除N+1。
  - 能用事务与锁保证并发一致性。
  - 能用record/投影分离实体与DTO边界。
  - 能在日志与SQL层验证并纠偏，必要时回退到原生SQL。

