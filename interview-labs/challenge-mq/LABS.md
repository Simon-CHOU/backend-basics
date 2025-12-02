# MQ Labs 与验收标准

## Lab01 Outbox 一致性 + RabbitMQ
- 目标：将用户状态变更与消息发送实现最终一致性，避免跨资源 2PC。
- 操作：
  - 启动依赖：`docker compose up -d`
  - 启动应用：`mvn -q spring-boot:run`
  - 调用：`POST http://localhost:8080/lab01/users/{id}/status?value=ACTIVE`
  - 等待发布器定期扫 outbox 并投递到 `app.events`，消费者写入 `notifications`。
- 验收（AC）：
  - `users` 表中状态更新成功。
  - `outbox` 记录从 `NEW` 变为 `SENT`。
  - RabbitMQ 管理界面可见 `user.events` 队列有入站消息。
  - `notifications` 表针对该用户计数增加一次。
  - 重复调用同一用户多次，消费者按消息数增加计数，每条消息仅处理一次。

## Lab02 Kafka 事务型生产者（占位）
 目标：演示事务性生产使发布在同一事务中提交，消费端幂等处理。
 操作：
  - 启动依赖与应用。
  - 调用：`POST http://localhost:8080/lab02/users/{id}/status?value=ACTIVE`
  - 主题：`lab02.user-status`，分区数 3。
 验收（AC）：
  - Kafka Producer 使用事务前缀 `tx-lab02-` 完成发送。
  - 消费者入库 `kafka_inbox` 去重后更新 `kafka_notifications`。
  - 同一 `{id}` 多次调用仅按消息次数累加一次处理，重复消息不重复执行。

## Lab03 RocketMQ 事务与延迟（占位）
 目标：演示延迟消息按级别延迟后送达并幂等处理。
 操作：
  - 启动依赖与应用。
  - 发送：`POST http://localhost:8080/lab03/delay?userId={id}&level=3`
  - 主题：`lab03.user-status`。
 验收（AC）：
  - 消息在设定延迟后到达，消费者更新 `rocket_notifications`。
  - `rocket_inbox` 仅记录每个消息一次，重复不重复执行。

## Lab04 幂等消费者 Inbox（占位）
 目标：演示入库去重确保同一消息仅处理一次。
 操作：
  - 启动依赖与应用。
  - 调用：`POST http://localhost:8080/lab04/process?messageId={m}&sku=SKU-1`
  - 重复调用同一 `messageId`。
 验收（AC）：
  - `lab04_inbox` 仅记录一次该 `messageId`。
  - `lab04_counts` 对应 `sku` 的计数只增加一次。

## Lab05 键有序与并行（占位）
 目标：同键串行、跨键并行写入顺序日志。
 操作：
  - 调用：`GET http://localhost:8080/lab05/ordered?keys=a,b,c&perKey=5`
  - 查看 `lab05_log`。
 验收（AC）：
  - 对每个键 `k`，`seq` 从 1 递增且无间断。
  - 不同键的写入交错存在，证明并行发生。

## Lab06 积压与 DLQ（占位）
 目标：失败消息重试 3 次后进入 DLQ。
 操作：
  - 调用：`POST http://localhost:8080/lab06/send?userId=U1&value=OK`
  - 调用：`POST http://localhost:8080/lab06/send?userId=U2&value=FAIL`
  - RabbitMQ 管理界面查看 `lab06.main` 与 `lab06.dlq`。
 验收（AC）：
  - `U1` 被成功消费并在 `lab06_counts` 中计数增加。
  - `U2` 的消息重试 3 次后进入 `lab06.dlq`。

## Lab07 RabbitMQ 实例定向路由（占位）
 目标：根据 `instanceId` 定向投递到该实例队列。
 操作：
  - 注册实例队列：`POST http://localhost:8080/lab07/register?instanceId=A`
  - 发送：`POST http://localhost:8080/lab07/send?instanceId=A&userId=U1`
  - 可注册多个实例 `B` 并交叉发送。
 验收（AC）：
  - `lab07_ws` 中 `U1` 在 `A` 的计数增加。
  - 不同 `instanceId` 的消息不会互相串扰。
