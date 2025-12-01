# Spring 事务（Spring Transaction）一文吃透（2025/2026 版）

> 目标：让你在 Java Senior SDE 面试中，把 Spring 事务讲到“第一性原理 + 可复现 + 会踩坑能自救”的水平。

---

## 0. 学习路线与核心概念（拓扑排序，先后依赖）

1) ACID 与数据库本质
- 原子性（Atomicity）：要么全部成功，要么全部回滚
- 一致性（Consistency）：事务前后数据满足不变式
- 隔离性（Isolation）：并发下的可见性与互不干扰
- 持久性（Durability）：提交后的数据不会丢失（WAL/redo 日志）

2) MySQL 事务与隔离
- 隔离级别：`READ UNCOMMITTED`、`READ COMMITTED`、`REPEATABLE READ`（MySQL 默认）、`SERIALIZABLE`
- MVCC（基于快照的并发控制）、锁（行锁/间隙锁/Next-Key 锁）、死锁与回滚

3) Spring 事务机制总览
- 注解入口：`@Transactional`
- 核心结构：AOP 代理 + `TransactionInterceptor` + `PlatformTransactionManager`
- 事务定义：`TransactionDefinition`（传播、隔离、超时、只读、回滚规则）
- 事务状态：`TransactionStatus`（新事务、保存点、完成与否）
- 线程绑定：`TransactionSynchronizationManager`（ThreadLocal 绑定连接/资源/同步回调）

4) Spring vs Spring Boot（边界）
- Spring Framework：提供事务抽象与执行机制
- Spring Boot：自动配置（创建合适的 `PlatformTransactionManager`，启用声明式事务）

5) 传播行为与边界
- REQUIRED/REQUIRES_NEW/NESTED 及其它（SUPPORTS、MANDATORY、NOT_SUPPORTED、NEVER）
- 传播的本质：是否加入当前事务或新开事务，保存点与独立提交

6) 失效场景认知
- 自调用（同类内方法互调）、非 `public` 方法、`final`/`private`/构造器、异步线程、事务管理器缺失、Checked 异常默认不回滚、Reactive 与 Imperative 混用

7) Java 21/25 与运行时代码
- 虚拟线程（Project Loom）：`ThreadLocal` 仍可用，但线程切换与跨线程边界要谨慎
- 结构化并发：事务边界不要跨任务传播

8) 分布式一致性与替代方案
- 本地事务 vs 分布式事务（XA/JTA）：成本高
- 业务最佳实践：Outbox、Saga、Idempotency、重试与补偿

---

## 1. 面试直答速记（先给结论，后给证明）

- `@Transactional` 底层原理：AOP 代理拦截方法，`TransactionInterceptor` 在方法前后调用 `PlatformTransactionManager.getTransaction/commit/rollback`，并通过 `ThreadLocal`（`TransactionSynchronizationManager`）把连接与事务上下文绑定到当前线程。
- `spring transactional` 怎么实现：声明式事务基于 AOP，编程式事务用 `TransactionTemplate`/`PlatformTransactionManager`，两者都走同一事务抽象。
- 它是 Spring 的功能还是 Spring Boot 的功能：事务抽象与机制属于 Spring Framework；Spring Boot 负责自动配置数据源与合适的事务管理器，简化使用。
- 哪些场景会失效：同类内自调用、非 `public`、`final`/`private` 方法、构造器、`@Async`/新线程、Reactive 流与阻塞混用、Checked 异常不回滚、事务管理器缺失或不匹配。
- 你们业务的事务要求：本地事务包裹“写模型”，读模型隔离级别控制；审计/日志可用 `REQUIRES_NEW`；跨边界用 Outbox/Saga；确保幂等与补偿。
- 项目里数据库事务：以 `@Transactional`（REQUIRED）包裹服务层；细粒度控制传播与隔离；只读查询标注 `readOnly=true`。
- MySQL 隔离级别：`READ UNCOMMITTED`、`READ COMMITTED`、`REPEATABLE READ`（默认）与 `SERIALIZABLE`，注意幻读、间隙锁与 Next-Key 锁。

---

## 2. 第一性原理：`@Transactional` 为什么有效（5-Why）

- Why1：为什么一个注解能开事务？
  - 因为 Spring 容器在创建 Bean 时为标注了 `@Transactional` 的方法生成代理（JDK/CGLIB），方法调用先进入拦截器链。
- Why2：拦截器做了什么？
  - `TransactionInterceptor` 根据 `TransactionAttribute`（传播/隔离等）向事务管理器请求 `TransactionStatus`，决定是否新开事务或加入现有事务。
- Why3：事务如何与线程绑定？
  - `TransactionSynchronizationManager` 用 ThreadLocal 记录当前线程的连接、事务状态与同步回调，确保同线程下的 DAO 复用同一连接与事务。
- Why4：数据库为什么真的提交/回滚了？
  - 事务管理器（如 `DataSourceTransactionManager`/`JpaTransactionManager`）在 `commit/rollback` 时调用 JDBC/JPA API，最终落到 MySQL 的 redo/undo/MVCC 机制。
- Why5：为什么失败场景会失效？
  - 因为没有经过代理（自调用/非 `public`/构造器），或跨线程导致 ThreadLocal 上下文丢失，或异常类型未触发回滚规则，或没有匹配到事务管理器。

---

## 3. 负向定义与边界（Define by Contrast & Boundary）

- `@Transactional` 不等于 分布式事务：它默认是本地事务（单资源）；跨服务/多库需 JTA/XA 或业务级 Saga/Outbox。
- `readOnly=true` 不等于 强制数据库只读：它是“意图提示”，JPA 可避免不必要的 flush，数据库层是否只读取决于连接/会话设置。
- 传播行为 不等于 隔离级别：传播是“是否加入哪个事务”；隔离级别是“并发可见性快照”。
- `REQUIRES_NEW` 不等于 新线程：它是在同线程里挂起外层事务并创建独立事务。
- Reactive 事务 不等于 ThreadLocal：反应式通过 `Context` 传播事务，不依赖线程栈；阻塞与反应式混用会破坏边界。
- 反事实：如果没有代理拦截（关键条件 Y），`@Transactional` 就无法调用事务管理器——因此本质属性是“代理化的方法入口”。

---

## 4. 传播行为与实务策略

- REQUIRED（默认）：加入当前事务，没有则新建；适合绝大多数写操作。
- REQUIRES_NEW：挂起外层，创建独立事务；用于审计/积分入账等“必须成功”的支线。
- NESTED：在当前事务里创建保存点；外层回滚不影响已提交的保存点；需底层支持保存点（JDBC）。
- 其它：SUPPORTS（有则加，无则非事务）、MANDATORY（必须存在事务，否则异常）、NOT_SUPPORTED（挂起事务，非事务执行）、NEVER（必须无事务，否则异常）。

---

## 5. 常见失效与自救清单

- 同类内自调用：方法未经过代理，`@Transactional`不生效
  - 重构到单独 Bean，或通过 `AopContext.currentProxy()` 间接调用（需暴露代理）。
- 非 `public` 方法 / `final`/`private`/构造器：AOP 不拦截
  - 保证 `@Transactional` 方法为 `public`，类可被代理。
- 异步/新线程（含 `@Async`、虚拟线程）：ThreadLocal 上下文丢失
  - 在线程边界内重建事务，或使用编程式事务。不要在事务中跨线程执行数据库操作。
- Checked 异常不回滚：默认只对 `RuntimeException`/`Error` 回滚
  - 用 `rollbackFor=Exception.class` 或抛出受检异常包裹为运行时异常。
- Reactive 与阻塞混用：上下文不一致
  - 反应式用 `TransactionalOperator` 或 `@Transactional`（R2DBC），避免在 reactive 流中调用阻塞 JDBC。
- 事务管理器缺失或不匹配：比如 JPA 与 JDBC 混配
  - 明确配置唯一且正确的 `PlatformTransactionManager`。

---

## 6. MySQL 隔离级别与并发现象

- READ UNCOMMITTED：可能脏读
- READ COMMITTED：消除脏读，可能不可重复读
- REPEATABLE READ（默认）：消除不可重复读，可能幻读（MySQL 通过 Next-Key/GAP 锁在某些场景抑制幻读）
- SERIALIZABLE：完全串行，代价高

示例并发现象（两事务 T1/T2）：
- 不可重复读：T1 两次读取同一行，中间 T2 更新并提交，T1 第二次读到新值（在 RC）
- 幻读：T1 按条件计数，中间 T2 插入满足条件的新行，T1 再数值变多（RR 下依靠锁策略）

---

## 7. Java21+ 示例代码（避免注释，专注语义）

### 7.1 基础服务层 REQUIRED

```java
// Service 层，默认 REQUIRED
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;

    public OrderService(OrderRepository orderRepository, AccountRepository accountRepository) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Long placeOrder(Long accountId, BigDecimal amount) {
        var account = accountRepository.findById(accountId).orElseThrow();
        if (account.getBalance().compareTo(amount) < 0) throw new IllegalStateException("insufficient");
        account.debit(amount);
        accountRepository.save(account);
        var order = new Order(accountId, amount, Instant.now());
        orderRepository.save(order);
        return order.getId();
    }
}
```

### 7.2 REQUIRES_NEW 审计/日志

```java
@Service
public class AuditService {
    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String type, String payload) {
        auditRepository.save(new Audit(type, payload, Instant.now()));
    }
}

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AuditService auditService;

    public PaymentService(PaymentRepository paymentRepository, AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void pay(Long orderId) {
        paymentRepository.save(new Payment(orderId, Instant.now()));
        try {
            auditService.record("PAY", orderId.toString());
        } catch (Exception e) {
            // 审计失败不影响外层提交，因为是独立事务
        }
    }
}
```

### 7.3 NESTED 保存点

```java
@Service
public class BatchService {
    private final ItemRepository itemRepository;

    public BatchService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void importAll(List<String> names) {
        for (var name : names) {
            try {
                importOne(name);
            } catch (RuntimeException ex) {
                // 某一个失败回滚到保存点，其它不受影响
            }
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    public void importOne(String name) {
        itemRepository.save(new Item(name));
        if (name.startsWith("bad")) throw new IllegalStateException("invalid");
    }
}
```

### 7.4 自调用失效与修复

```java
@Service
public class SelfCallService {
    @Transactional
    public void outer() {
        // 直接调用同类方法，未经过代理，inner 的事务不生效
        inner();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void inner() {
        // 期望新事务
    }
}
```

修复方式一：拆分 Bean

```java
@Service
public class CallerService {
    private final CalleeService callee;

    public CallerService(CalleeService callee) { this.callee = callee; }

    @Transactional
    public void outer() { callee.inner(); }
}

@Service
public class CalleeService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void inner() { }
}
```

### 7.5 Checked 异常回滚

```java
@Service
public class RollbackService {
    private final EntityRepository repo;

    public RollbackService(EntityRepository repo) { this.repo = repo; }

    @Transactional(rollbackFor = Exception.class)
    public void doWork() throws Exception {
        repo.save(new Entity("x"));
        if (true) throw new Exception("checked");
    }
}
```

### 7.6 只读事务意图

```java
@Service
public class QueryService {
    private final EntityRepository repo;

    public QueryService(EntityRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<Entity> list() { return repo.findTop10ByOrderByIdDesc(); }
}
```

### 7.7 虚拟线程与事务边界

```java
@Service
public class VirtualThreadService {
    private final ItemRepository repo;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public VirtualThreadService(ItemRepository repo) { this.repo = repo; }

    public void unsafe() {
        // 不要在一个事务中跨线程执行数据库操作
        executor.submit(() -> createItem());
    }

    @Transactional
    public void createItem() { repo.save(new Item("v")); }
}
```

### 7.8 Reactive（R2DBC）风格

```java
@Service
public class ReactiveService {
    private final TransactionalOperator operator;
    private final ReactiveItemRepository repo;

    public ReactiveService(ReactiveTransactionManager tm, ReactiveItemRepository repo) {
        this.operator = TransactionalOperator.create(tm);
        this.repo = repo;
    }

    public Mono<Void> createAll(Flux<String> names) {
        return operator.execute(status -> names.flatMap(n -> repo.save(new Item(n))).then());
    }
}
```

---

## 8. 编程式事务（无注解时的底层）

```java
@Component
public class ProgrammaticTx {
    private final PlatformTransactionManager tm;

    public ProgrammaticTx(PlatformTransactionManager tm) { this.tm = tm; }

    public void run(Runnable r) {
        var def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        var status = tm.getTransaction(def);
        try {
            r.run();
            tm.commit(status);
        } catch (RuntimeException e) {
            tm.rollback(status);
            throw e;
        }
    }
}
```

---

## 9. SOP：可复现指引（Docker Compose + Spring Boot）

- 准备 `docker-compose.yml`（MySQL）

```yaml
a version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=secret
      - MYSQL_DATABASE=app
      - MYSQL_USER=app
      - MYSQL_PASSWORD=secret
    ports:
      - "3306:3306"
    command: ["--default-authentication-plugin=mysql_native_password"]
```

- `application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/app?useSSL=false&serverTimezone=UTC
    username: app
    password: secret
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
logging.level.org.hibernate.SQL: debug
```

- 依赖（Maven）

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

- 运行步骤
- 启动 MySQL：`docker compose up -d`
- 启动应用：`mvn spring-boot:run` 或 `./mvnw spring-boot:run`
- 编写一个 `@Transactional` 的服务并调用，观察日志中 `begin/commit/rollback` 以及 SQL 执行顺序。

---

## 10. 深入实现细节（源码视角）

- `@Transactional` 元数据 → `TransactionAttributeSource`
- AOP 入口 → `TransactionInterceptor.invoke`
- 管理器选择 → `PlatformTransactionManager`（JDBC、JPA、JTA、Reactive）
- 线程上下文 → `TransactionSynchronizationManager`（资源绑定、同步回调）
- JDBC 绑定 → `DataSourceUtils.doGetConnection`（确保同事务复用连接）
- 保存点 → `Connection.setSavepoint`/`releaseSavepoint`/`rollback` 到保存点（NESTED）

---

## 11. 最佳实践清单

- 服务层作为事务边界，控制粒度在“一个业务用例”的范围
- 默认用 REQUIRED；审计/外呼用 REQUIRES_NEW；批量子步骤用 NESTED
- 查询用 `readOnly=true`，避免不必要 flush；大量读取考虑只读连接
- 避免跨线程执行数据库操作；虚拟线程中也保持“每任务一事务”
- Checked 异常显式声明 `rollbackFor`
- Reactive 场景统一使用 R2DBC 与 `TransactionalOperator`
- 分布式场景通过 Outbox/Saga/幂等补偿，而不是滥用 XA

---

## 12. 面试延伸问题的结构化回答模板

- 概念：`@Transactional` 基于 AOP 拦截，驱动事务管理器开关事务
- 机制：ThreadLocal 绑定资源与状态，确保同线程 DAO 复用连接
- 传播：决定加入或新建事务；NESTED 通过保存点
- 隔离：控制并发可见性，与传播无关
- 失效：自调用、非 `public`、跨线程、异常类型不匹配、Reactive 混用
- 案例：审计 `REQUIRES_NEW`、批量 `NESTED`、查询只读、补偿与幂等

---

## 13. 结语

- 评价标准：是否能将注解 → 拦截器 → 管理器 → 线程上下文 → DB 行为串起来。
- 过关要点：能说清失效边界并给出替代方案（编程式事务、拆分 Bean、REQUIRES_NEW、Reactive Operator、Outbox）。

