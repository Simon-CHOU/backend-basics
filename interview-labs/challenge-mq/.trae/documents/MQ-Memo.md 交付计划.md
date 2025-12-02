# MQ-Memo.md：Java 高阶面试与云原生时代的消息中间件通关手册

## 目标与交付
- 目标：用最小学习颗粒解释 MQ（Message Queue / Message Broker）在 2025–2026 Java/Spring 云原生背景下的原理、事务一致性、可靠性、顺序性、幂等性与工程落地。
- 交付：生成一份完整的 `MQ-Memo.md`（中文，关键术语附英文），包含图示、原理、边界、SOP（可复现步骤）、Java 21 代码示例（Spring Boot 风格但不依赖特定版本特性）。
- 适配：Windows 11 + Docker Compose；示例对 RabbitMQ、Kafka、RocketMQ 进行对照与深度拆解。

## 文档结构（拓扑排序 + 金字塔）
### 1. 关键概念总览（Key Concepts Up Front）
- 参与者与术语：`Producer`、`Consumer`、`Broker`、`Queue`、`Topic`、`Exchange`、`Partition`、`Consumer Group`、`Offset`、`Ack`、`DLQ`、`Retry`、`Prefetch`、`Delivery Semantics`（at-most/at-least/exactly-once）、`Ordering`、`Idempotency`、`Durability`、`Replication`、`Routing Key`、`Consistency`、`Outbox Pattern`、`Transactional Messaging`、`2PC/XA`、`Saga/Compensation`、`CDC`。
- 负向定义与边界（Define by Contrast & Boundary）：Broker ≠ ESB；Exactly-once ≠ 完全不重复；事务型消息 ≠ 分布式数据库事务；“回滚消息” ≠ 从队列撤回。
- 反事实：如果无“幂等性”，则 at-least-once 将导致错误累加；如果无“键一致路由”，则无法保证单键有序；如果无“确认机制”，则无法达到持久可靠交付。

### 2. 工作原理与参与者分工（RabbitMQ/Kafka/RocketMQ）
- RabbitMQ：`Exchange`（direct/topic/fanout）、`Queue`、`Binding`、`Publisher Confirms`、`Prefetch`、`DLX`；队列内顺序，实例可多。
- Kafka：`Topic`、`Partition`、`Broker`、`ISR` 复制、`Producer` 事务、`Consumer Group` 再均衡；分区内顺序。
- RocketMQ：`Topic`、`Message Queue`、`Producer`、`Consumer`、`Transactional Message`、`Delay/Scheduled`；队列内顺序。
- ASCII 图示：数据流、确认与重试、DLQ 迁移、分区路由。

### 3. 数据库与消息一致性（强一致的错觉 vs 可行工程）
- 方案对比：
  - Outbox Pattern（本地事务写业务 + outbox，异步发布 + 发布确认/CDC）。
  - Kafka 事务（`sendOffsetsToTransaction` 保证写与位移的原子性）。
  - RocketMQ 事务型消息（半消息 + 本地事务回查）。
  - RabbitMQ 没有跨资源 2PC，使用 Outbox + Confirms。
- 为什么 2PC/XA 不推荐：阻塞、耦合、可用性差、云原生不友好。
- 审计与合规：不可修改历史，用追加与补偿；记录因果链与证据。

### 4. “消息已发但事务回滚”如何处理
- 本质：消息一旦持久化并可能被消费，不存在“撤回”；采用补偿（Compensation/Saga）事件。
- 设计：状态机 + 可逆动作；用唯一业务键与版本（fence token）避免并发写入污染。

### 5. 下游积压的治理（消费者视角）
- 策略：自动扩缩（K8s HPA）/并发（虚拟线程）/`prefetch`/`max.poll.records`/批处理/优先级/限流/熔断/过期（TTL）/DLQ。
- 架构：为热点键增加分区；区分实时与离线；重算与旁路。

### 6. 可靠性与顺序性
- 可靠性：持久化、复制、发布确认、重试策略、DLQ、幂等消费、告警与追踪。
- 顺序性：单键（routing/partition）一致投递；消费并发需按键串行或“同键同分区”。

### 7. RabbitMQ 实例定向路由（WebSocket 场景）
- 方案：每实例自建专属队列（命名含 `instanceId`），通过 `direct exchange` 使用 `routingKey=instanceId`；会话 `sessionId→instanceId` 映射放入 Redis/注册表。
- 可选：`consistent-hash exchange` 插件；或按 `userId` 做一致哈希。

### 8. 业务场景“更改状态后发送通知”的一致性实现
- 首选：Outbox Pattern + 发布确认/CDC。
- 替代：Kafka 事务；RocketMQ 事务型消息。
- 反例：把 DB 与 Broker 放在同一个分布式事务中（缺点与风险列举）。

### 9. 性能量级（经验区间）
- DB 写：本地 SSD、OLTP 2–20ms；复杂事务/锁争用 20–100ms+。
- 消息发布：本机到 Broker 1–5ms；跨区/复制 5–30ms+；遥距与峰值更高。

### 10. 幂等性与重复消息治理
- 设计：`messageId`/`dedupKey` 唯一约束；Inbox 表/处理日志；版本号/乐观锁；自然键合并与上限。

### 11. RocketMQ 延迟消息
- 用 `delayLevel` 或 `deliverTime`（按版本）；对比 Scheduled 实现与约束。

### 12. SOP（可复现）
- 用 Docker Compose 启动 RabbitMQ、Kafka、RocketMQ（最小配置）。
- Spring Boot 应用：
  - Outbox 表 + JPA；事务服务写业务与 outbox。
  - 发布器：定时/事件驱动读取 outbox，发往三类 MQ（可切换实现）。
  - 消费者：幂等处理（Inbox/去重）+ 顺序保障（同键串行）。
- 验证：重复消息、乱序、积压与扩缩；DLQ 与补偿演示。

### 13. Java 21 示例代码块（无注释，贴近实用）
- Outbox 实体 `record`、事务服务、发布器（RabbitMQ/Kafka/RocketMQ 各一版）。
- 幂等消费者模板（唯一键 + 乐观锁）。
- RocketMQ 延迟消息示例。
- 虚拟线程并发消费示例。

### 14. 边界与负向排除清单
- Broker vs Event Bus vs ESB；Exactly-once 的工程含义；“回滚”与“补偿”的边界；Saga vs 2PC。

### 15. 5-Why 递归到第一性原理
- 对一致性、顺序性、可靠性分别做 5-Why 展开，直到落到存储持久化、复制一致性协议（如 ISR/Quorum）、事务语义与代码层动作。

### 16. 速查表与面试答题模板
- 常见问答的一句话要点 + 可延伸的技术细节列表。

## 实施步骤
1. 生成 `MQ-Memo.md`，按照上述目录填充内容：原理、图示、边界、反事实、5-Why、SOP、代码块。
2. 附带 Docker Compose 片段与最小 Spring 项目结构描述（文本形式，不改动环境）。
3. 校对用词（中英对照）、确保示例不绑定特定 Spring Boot 次版本能力。

## 验证与交付标准
- 内容完整且自洽；包含三类 MQ 的对照与代码示例。
- SOP 步骤可在 Windows + Docker Compose 环境执行（用户自行运行）。
- 面试问题覆盖到位，可直接背诵的答题模板与工程设计要点。

## 后续可选增强
- 增加 CDC（Debezium）方案章；增加 K8s HPA/YPA 实战说明；增加 Trace/Metric（OpenTelemetry）示例。
