# MQ 中间件通关手册（Message Broker）2025–2026 云原生与 Java 21 实战

> 读者画像：为 Java Senior SDE 面试做准备；要求“傻子也能懂”，但做到工程可落地。关键术语附英文（English），从第一性原理出发，递归 5-Why 到代码层。遵循金字塔结构：先结论与核心概念，再原理与实现，最后 SOP 与速查表。

## 0. 关键概念与拓扑排序（Key Concepts Up Front）
- 消息中间件（Message Broker）：在生产者（Producer）与消费者（Consumer）之间转发消息（Message），提供持久化（Durability）、确认（Ack）、重试（Retry）、死信队列（DLQ, Dead Letter Queue）、路由（Routing）、顺序（Ordering）。
- 队列（Queue）与主题（Topic）：队列提供点对点传递；主题提供发布订阅（Pub/Sub）。Kafka 与 RocketMQ 用 `Topic`，RabbitMQ 用 `Exchange + Queue`。
- 交换机（Exchange）：RabbitMQ 中根据 `routingKey` 将消息路由到队列（Queue）。类型包括 `direct`、`topic`、`fanout`、`headers`。
- 分区（Partition）：Kafka/RocketMQ 将一个主题拆成多个并行分区；“分区内有序（Ordering in partition）”。
- 消费者组（Consumer Group）：Kafka 中同一组内分区被独占消费，保证分区内有序；RocketMQ 也有类似分组语义。
- 位移（Offset）：Kafka 中消费者在分区里的进度指针；与事务结合可保证“处理与提交位置的一致性”。
- 交付语义（Delivery Semantics）：
  - 至多一次（At-most-once）：不重试，可能丢（Loss）。
  - 至少一次（At-least-once）：会重试，不丢但可能重复（Duplicate）。
  - 实际工程的“Exactly-once”：指端到端不重复不遗漏的效果，一般靠“幂等性（Idempotency）+ 去重（Deduplication）+ 事务/一致性协议”实现，并非魔法。
- 幂等性（Idempotency）：同一业务操作执行一次或多次结果一致，关键靠“唯一业务键（Business Key）/消息键（MessageId/Idempotency-Key）+ 约束/日志”。
- 可靠性（Reliability）：持久化（Durability）、多副本（Replication）、发布确认（Publisher Confirms）、消费者确认（Ack）、重试（Retry）、死信（DLQ）。
- 顺序性（Ordering）：工程上保证“同一键（Key）”落同一分区/队列，并以串行方式处理。
- 一致性（Consistency）：数据库（DB）状态与消息发送一致。主流做法：Outbox Pattern、Kafka 事务、RocketMQ 事务型消息；不推荐跨资源 2PC/XA。
- Outbox Pattern：将业务变更与“待发布的消息”一起写入同一数据库事务（本地事务），随后异步可靠发布并标记已发布或用 CDC（Change Data Capture）。
- 事务型消息（Transactional Messaging）：RocketMQ 的半消息 + 本地事务回查；Kafka 的生产者事务（Producer Transaction）与位移提交的一致性。
- 补偿（Compensation / Saga）：无法“撤回”已投递消息，用补偿事件反向修正状态。
- 反事实边界（Define by Contrast & Boundary）：
  - Broker ≠ ESB（企业服务总线）：ESB 做编排与转换；Broker 专注消息传递与可靠性。
  - Exactly-once ≠ 完全无重复：工程实现依赖幂等与一致性约束。
  - “回滚消息” ≠ 从队列删除历史：已被持久化或消费，需要补偿而非撤回。

---

## 1. 工作原理与参与者分工（RabbitMQ / Kafka / RocketMQ）

### 1.1 RabbitMQ（AMQP）
- 参与者：`Producer`、`Exchange`、`Queue`、`Binding`、`Consumer`。
- 发布确认（Publisher Confirms）：`Channel.confirmSelect()` 后，Broker 持久化成功才确认，保证可靠到达。
- 流控：`prefetch` 控制每消费者未确认消息数，避免积压导致内存爆。
- 死信：`DLX` 接收多次重试失败或过期（TTL）的消息，便于离线排查。
- 顺序：队列内天然顺序；并发消费会打乱“跨队列或跨键”的整体顺序。

ASCII（Exchange 路由）

```
Producer -> [Exchange: direct/topic] --routingKey--> [Queue per key] -> Consumer
```

### 1.2 Kafka
- 参与者：`Topic`、`Partition`、`Broker`、`Producer`、`Consumer Group`、`Offset`。
- 分区复制：`ISR`（In-Sync Replica）保证副本一致性与高可用。
- 事务：`Transactional Producer` + `sendOffsetsToTransaction`，将消息发布与位移提交绑定到同一事务，避免“已处理未提交”或“已提交未处理”。
- 顺序：分区内严格有序；跨分区不保证。

ASCII（分区并行）

```
Producer --hash(key)--> Partition 0..N -> ConsumerGroup: each partition -> one consumer
```

### 1.3 RocketMQ
- 参与者：`Topic`、`Message Queue`、`Producer`、`Consumer`。
- 事务型消息：先写半消息，执行本地事务，Broker 回查决定提交或丢弃。
- 延迟/定时：`delayLevel` 或新版本的 `deliverTime` 定时投递。
- 顺序：Message Queue 内有序，需保证同键映射到同队列。

---

## 2. DB 与消息一致性的“正确打开方式”

### 2.1 Outbox Pattern（强烈推荐）
- 机制：业务更新与写入 outbox（待发消息）同一个本地事务；提交后异步发布；发布成功后标记或删除 outbox 记录。
- 优点：不依赖跨资源事务；可审计；容忍重试；云原生扩展友好。
- 失败窗口：发布器崩溃或重复发布，靠幂等消费与发布确认兜底。

### 2.2 Kafka 事务（场景契合 Kafka）
- 机制：`initTransactions` → `beginTransaction` → 发送消息 → `sendOffsetsToTransaction` → `commitTransaction`。
- 优点：与消费位移原子一致，适合流处理拓扑。
- 注意：事务开销与复杂度；跨系统的一致性仍需 Outbox/Saga 设计。

### 2.3 RocketMQ 事务型消息（场景契合 RocketMQ）
- 机制：半消息持久化，执行业务本地事务；Broker 失败回查接口决定提交或回滚半消息。
- 优点：将本地事务结果与消息最终可见性绑定。
- 注意：回查实现要稳健；幂等与补偿仍必要。

### 2.4 为什么不推荐 2PC/XA（跨资源分布式事务）
- 原因：阻塞、耦合、性能差、故障复杂、云原生伸缩不友好；大多数 Broker 不提供对外资源的 XA。

---

## 3. “消息已发但事务回滚”怎么办
- 结论：不存在“撤回已发布消息”的通用能力；采用补偿（Compensation）事件或反向状态修复。
- 方法：
  - 用状态机（State Machine）设计可逆操作；记录版本（Version/Fence Token）。
  - 发布“取消/回滚”事件，消费者执行相反操作；幂等保证不二次污染。
  - 审计（Audit）：只追加不修改，保留因果链证据。

---

## 4. 下游积压（Backlog）与时效性保障（消费者视角）
- 并发扩展：Kubernetes HPA 扩容；Java 虚拟线程（Virtual Threads）提高并发。
- 拉取控制：RabbitMQ `prefetch`、Kafka `max.poll.records`；批处理提高吞吐。
- 路由优化：热点键拆分更多分区/队列；优先级队列处理紧急消息。
- 降级与限流：过期（TTL）、DLQ；对非核心功能降级；防止级联故障。

---

## 5. 可靠性与顺序性设计
- 可靠性：持久化、复制、发布确认、重试退避（Backoff）、DLQ 终止、监控告警（Metrics/Tracing）。
- 顺序性：按业务键路由到同一分区/队列；消费者对同键串行处理；必要时用“单键执行器”。

---

## 6. RabbitMQ 实例定向路由（WebSocket 定点推送）
- 思路：每个 WebSocket 服务器实例在启动时声明一个专属队列（含 `instanceId`），并绑定到 `direct exchange`；生产者以 `routingKey=instanceId` 投递。
- 会话定位：维护 `sessionId → instanceId` 映射（如 Redis/注册表），从而知道消息应路由到哪台实例。

ASCII

```
[Exchange: direct]
  routingKey: instanceA -> [Queue.instanceA] -> WebSocketServer A
  routingKey: instanceB -> [Queue.instanceB] -> WebSocketServer B
```

---

## 7. 业务场景：“更改状态后发送通知”的一致性最佳实践
- 方案优先级：
  1) Outbox Pattern + 发布确认/CDC（通用、可靠、易审计）。
  2) Kafka 事务（偏 Kafka 生态的流式架构）。
  3) RocketMQ 事务型消息（RocketMQ 生态）。
- 反例：将 DB 更新与消息发送放进同一个跨资源事务，会导致耦合高、可用性差、性能退化。

---

## 8. 性能量级（经验参考）
- DB 写：2–20ms 常见；复杂事务/锁争用 20–100ms+。
- 消息发布：同机/同区 1–5ms；跨区或复制 5–30ms+；峰值或拥塞更高。

---

## 9. 幂等性与重复消息治理（库存 -1 示例）
- 关键：唯一业务键或消息键约束（`unique(message_id)`）、Inbox 表记录已处理、版本/乐观锁控制。

---

## 10. RocketMQ 延迟消息实现
- 方式：`delayLevel`（如 1s/5s/10s 等固定级别）；或新版 `deliverTime` 精确时间投递。

---

## 11. 面试题模板速答（Quick Answers）
- 消息中间件有没有用过：说明 RabbitMQ/Kafka/RocketMQ 的使用场景与对比；强调可靠性、顺序性、幂等、Outbox、一致性方案。
- DB 写与发消息的事务性：首选 Outbox Pattern；或 Kafka/RocketMQ 的事务型消息；不推荐 2PC。
- 暴露内部回滚接口带来审计风险：避免“撤回”，改用补偿事件；保留不可变审计日志；对接口做鉴权与最小化。 
- Broker 工作原理与参与者：见第 1 章；以 ASCII 图解释。
- 已发送成功但事务回滚：使用补偿事件；或用事务型消息让“未提交”消息不可见。
- 下游积压时效：消费者扩缩、并发、prefetch、限流与 TTL、DLQ；热点拆分与优先级处理。
- 可靠性与顺序性：发布/消费者确认、复制、重试、DLQ；同键同分区/队列保证局部顺序。
- RabbitMQ 如何路由到指定实例：direct exchange + per-instance queue；`sessionId→instanceId` 映射存储。
- 更改状态后发送通知一致性：Outbox/CDC；或 Kafka/RocketMQ 事务。
- 把更新与发送放同一事务缺点：耦合高、阻塞、性能差、云原生不友好。
- DB 与发送消息耗时数量级：见第 8 章经验值。
- 消费端收到重复消息的幂等性：唯一键约束、Inbox 表、版本/乐观锁。
- RocketMQ 延迟消息：`delayLevel` 或 `deliverTime`。

---

## 12. 5-Why 递归到第一性原理
- 一致性为何必须：避免“状态和消息”分叉 → 因为分叉导致不同系统语义不一致 → 因为人/系统基于事件驱动 → 因为消息是事实传播的载体 → 因为事实必须可验证与可纠正 → 所以用 Outbox/事务型消息/补偿构建可验证闭环。
- 顺序为何只保障“单键”：系统要扩展并行度 → 因为多分区并行才能提升吞吐 → 因为全局有序会形成单瓶颈 → 因此以键为粒度保证局部顺序。
- 幂等为何是工程基石：重试不可避免 → 因为网络/节点会失败 → 因为 at-least-once 是可靠性的常态 → 没有幂等就会重复扣减 → 所以必须用唯一键/日志/乐观锁等约束。
- 为什么不用 2PC：分布式锁与协调开销巨大 → 因为故障时难以恢复 → 因为云原生下动态伸缩 → 因此选择“本地事务 + 事件驱动 + 补偿”。
- 为什么审计要不可变：防篡改与合规 → 因为需要重现因果链 → 因为纠错与责任追溯 → 所以只追加不修改。

---

## 13. SOP（可复现步骤，Windows + Docker Compose）

### 13.1 Docker Compose（最小形态）

RabbitMQ：
```yaml
services:
  rabbitmq:
    image: rabbitmq:3.13-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS: "-rabbitmq_management load_definitions '/etc/rabbitmq/definitions.json'"
```

Kafka（KRaft 单节点示例）：
```yaml
services:
  kafka:
    image: bitnami/kafka:3.6.1
    ports:
      - "9092:9092"
    environment:
      KAFKA_ENABLE_KRAFT: "yes"
      KAFKA_CFG_NODE_ID: "1"
      KAFKA_CFG_PROCESS_ROLES: "broker,controller"
      KAFKA_CFG_LISTENERS: "PLAINTEXT://:9092"
      KAFKA_CFG_ADVERTISED_LISTENERS: "PLAINTEXT://localhost:9092"
      KAFKA_CFG_CONTROLLER_LISTENER_NAMES: "PLAINTEXT"
      KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: "PLAINTEXT:PLAINTEXT"
      KAFKA_CFG_CONTROLLER_QUORUM_VOTERS: "1@localhost:9092"
```

RocketMQ（简化示例，具体版本与端口可能需调整）：
```yaml
services:
  namesrv:
    image: apache/rocketmq:5.2.0
    command: ["sh","-c","/bin/mqnamesrv"]
    ports:
      - "9876:9876"
  broker:
    image: apache/rocketmq:5.2.0
    depends_on: [namesrv]
    command: ["sh","-c","/bin/mqbroker -n namesrv:9876"]
    ports:
      - "10911:10911"
      - "10909:10909"
```

运行：
```
docker compose up -d
```

### 13.2 应用结构（文本说明）
- 模块：`domain`（实体与事件）、`application`（用例服务与事务）、`infrastructure`（Outbox 仓储、MQ 适配器、消费者）。
- Outbox：同库同事务写入；发布器定期扫描并可靠发布；消费者幂等处理。

### 13.3 验证用例
- 重复消息：连续投递同一 `dedupKey`，消费者只处理一次。
- 顺序：同键串行；跨键并行；观察乱序与治理。
- 积压：降低消费者并发、调大生产速率，观察 DLQ 与扩容恢复。

---

## 14. Java 21 示例代码（无注释）

### 14.1 Outbox 记录与服务
```java
public record OutboxMessage(Long id, String aggregateType, String aggregateId, String type, String payload, java.time.Instant createdAt, String status) {}
```
```java
public sealed interface DomainEvent permits UserStatusChanged {}
```
```java
public final class UserStatusChanged implements DomainEvent {
  private final String userId;
  private final String newStatus;
  public UserStatusChanged(String userId, String newStatus) { this.userId = userId; this.newStatus = newStatus; }
  public String userId() { return userId; }
  public String newStatus() { return newStatus; }
}
```
```java
@org.springframework.transaction.annotation.Transactional
public class UserService {
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;
  public UserService(org.springframework.jdbc.core.JdbcTemplate jdbc) { this.jdbc = jdbc; }
  public void changeStatusAndOutbox(String userId, String status) {
    jdbc.update("update users set status=? where id=?", status, userId);
    var payload = java.util.Base64.getEncoder().encodeToString(("{\"userId\":\""+userId+"\",\"status\":\""+status+"\"}").getBytes());
    jdbc.update("insert into outbox(aggregate_type,aggregate_id,type,payload,created_at,status) values(?,?,?,?,?,?)",
      "User",""+userId,"UserStatusChanged",payload,java.time.Instant.now(),"NEW");
  }
}
```

### 14.2 Outbox 发布器（RabbitMQ 发布确认）
```java
public class OutboxPublisherRabbit {
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;
  private final com.rabbitmq.client.Connection connection;
  private final String exchange;
  public OutboxPublisherRabbit(org.springframework.jdbc.core.JdbcTemplate jdbc, com.rabbitmq.client.Connection connection, String exchange) { this.jdbc = jdbc; this.connection = connection; this.exchange = exchange; }
  public void publishBatch() throws Exception {
    var channel = connection.createChannel();
    channel.confirmSelect();
    var list = jdbc.query("select id,aggregate_type,aggregate_id,type,payload from outbox where status='NEW' order by id limit 100", (rs,i)-> new OutboxMessage(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), java.time.Instant.now(), "NEW"));
    for (var m : list) {
      var rk = m.aggregateType()+":"+m.aggregateId();
      channel.basicPublish(exchange, rk, new com.rabbitmq.client.AMQP.BasicProperties.Builder().deliveryMode(2).build(), java.util.Base64.getDecoder().decode(m.payload()));
      jdbc.update("update outbox set status='SENT' where id=?", m.id());
    }
    channel.waitForConfirms();
    channel.close();
  }
}
```

### 14.3 Kafka 事务型生产者
```java
public class TxKafkaProducer {
  public void send(java.util.List<org.apache.kafka.clients.producer.ProducerRecord<String,String>> batch) {
    var props = new java.util.Properties();
    props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
    props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
    props.put(org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    props.put(org.apache.kafka.clients.producer.ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-1");
    var producer = new org.apache.kafka.clients.producer.KafkaProducer<String,String>(props);
    producer.initTransactions();
    producer.beginTransaction();
    for (var r : batch) producer.send(r);
    producer.commitTransaction();
    producer.close();
  }
}
```

### 14.4 RocketMQ 延迟消息
```java
public class RocketDelayProducer {
  public void send() throws Exception {
    var producer = new org.apache.rocketmq.client.producer.DefaultMQProducer("p1");
    producer.setNamesrvAddr("localhost:9876");
    producer.start();
    var msg = new org.apache.rocketmq.common.message.Message("topicA", "hello".getBytes());
    msg.setDelayTimeLevel(3);
    producer.send(msg);
    producer.shutdown();
  }
}
```

### 14.5 幂等消费者模板（Inbox 表去重）
```java
public class IdempotentConsumer {
  private final org.springframework.jdbc.core.JdbcTemplate jdbc;
  public IdempotentConsumer(org.springframework.jdbc.core.JdbcTemplate jdbc) { this.jdbc = jdbc; }
  public void handle(String messageId, String payload) {
    var inserted = jdbc.update("insert into inbox(message_id,processed_at) values(?,?)", messageId, java.time.Instant.now());
    if (inserted == 1) apply(payload);
  }
  private void apply(String payload) {
    jdbc.update("update inventory set qty=qty-1 where sku=?", payload);
  }
}
```

### 14.6 虚拟线程并发消费
```java
public class VirtualThreadRunner {
  public void run(java.util.List<Runnable> tasks) throws Exception {
    try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
      for (var t : tasks) executor.submit(t);
    }
  }
}
```

### 14.7 RabbitMQ 实例路由（Redis 映射）
```java
public class InstanceRouter {
  private final redis.clients.jedis.Jedis jedis;
  public InstanceRouter(redis.clients.jedis.Jedis jedis) { this.jedis = jedis; }
  public String findInstanceIdBySession(String sessionId) { return jedis.get("ws:"+sessionId); }
}
```

---

## 15. 边界与负向排除
- Broker vs ESB：Broker 传递消息；ESB 做编排与转换。
- Exactly-once 的工程含义：通过幂等、去重、事务语义组合达到“效果上的不重不漏”。
- 回滚 vs 补偿：消息不可撤回，采用补偿事件修正状态。
- Saga vs 2PC：Saga 为长事务分解的有向图，靠补偿；2PC 是阻塞协调协议。

---

## 16. 速查表（Cheat Sheet）
- 一致性首选：Outbox；Kafka/RocketMQ 事务为生态特定的替代。
- 有序保障：同键同分区/队列；消费者同键串行。
- 重复治理：Inbox/唯一键；幂等函数。
- 积压处理：扩缩并发、限流退避、TTL 与 DLQ。
- 发布可靠：Publisher Confirms、重试与告警。
- 审计合规：只追加不修改，补偿而非撤回。

---

## 17. 面试答题模板（可直接背诵，附扩展点）
- 用过哪些 Broker：RabbitMQ（路由与确认）、Kafka（分区与事务）、RocketMQ（事务与延迟）。
- DB+消息一致性：Outbox/CDC；Kafka 事务；RocketMQ 事务；不做 2PC。
- 审计风险：不可撤回，做补偿；审计日志不可变；接口最小化与鉴权。
- 原理与参与者：见第 1 章；说明路由、复制、确认、分区。
- 已发消息回滚：补偿事件；或事务型消息避免“未提交消息可见”。
- 积压时效：并发扩缩、prefetch、优先级、限流与 TTL、DLQ。
- 可靠性与顺序：持久化与复制；发布确认；同键路由与串行。
- RabbitMQ 定向路由：per-instance queue + direct exchange；Redis 映射。
- 状态更改与通知一致性：Outbox。
- 同事务缺点：耦合、阻塞、性能与伸缩差。
- 时延数量级：DB 写 ms 级；消息发布 ms~几十 ms。
- 幂等性：唯一键/Inbox/版本号；自然键扣减。
- RocketMQ 延迟：`delayLevel` 或 `deliverTime`。

---

## 18. 结语
- 云原生最佳实践：事件驱动 + 本地事务 + Outbox/CDC + 幂等 + 补偿 + 监控与追踪，按键局部有序，拥塞时降级与扩缩。避免跨资源 2PC，拥抱可恢复性与审计可验证性。

