# 分布式事务解决方案演示

## 项目概述

本项目演示了分布式系统中事务一致性问题及其解决方案，包括：
- **问题场景**：消息发送与数据库事务的不一致性
- **Outbox模式**：通过事件表确保最终一致性
- **Saga模式**：通过补偿机制处理长事务

## 理论背景

### 分布式事务问题

在分布式系统中，当一个业务操作涉及多个服务或资源时，传统的ACID事务无法保证跨服务的一致性：

```
┌─────────────────┐    ┌─────────────────┐
│   Database      │    │   Message Queue │
│                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │ Transaction │ │    │ │   Message   │ │
│ │   Commit    │ │    │ │    Send     │ │
│ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘
        │                       │
        └───────────────────────┘
              可能不一致
```

**常见问题**：
1. 数据库事务成功，消息发送失败
2. 消息发送成功，数据库事务回滚
3. 网络分区导致的部分失败

### Outbox模式

**核心思想**：将消息作为事件存储在数据库中，与业务数据在同一事务中处理。

```
┌─────────────────────────────────────┐
│            Database                 │
│  ┌─────────────┐ ┌─────────────────┐│
│  │ Business    │ │   Outbox        ││
│  │   Data      │ │   Events        ││
│  └─────────────┘ └─────────────────┘│
└─────────────────────────────────────┘
              │
              ▼
    ┌─────────────────┐
    │ Event Publisher │
    │   (Scheduler)   │
    └─────────────────┘
              │
              ▼
    ┌─────────────────┐
    │ Message Queue   │
    └─────────────────┘
```

**优势**：
- 保证最终一致性
- 支持事件重试
- 可追踪事件状态

**劣势**：
- 需要额外的事件表
- 异步处理延迟
- 需要处理重复消息

### Saga模式

**核心思想**：将长事务分解为一系列本地事务，每个本地事务都有对应的补偿操作。

```
Saga Transaction Flow:

Step 1: Create Order
   ├─ Success → Step 2
   └─ Failure → End

Step 2: Send Message  
   ├─ Success → Step 3
   └─ Failure → Compensate Step 1

Step 3: Update Status
   ├─ Success → Complete
   └─ Failure → Compensate Step 2 → Compensate Step 1

Compensation Chain:
Step 3 Fail → Undo Step 2 → Undo Step 1 → Saga Failed
```

**优势**：
- 支持长事务
- 灵活的补偿机制
- 可视化事务状态

**劣势**：
- 实现复杂
- 补偿逻辑设计困难
- 可能出现补偿失败

## 项目结构

```
lab109/
├── src/main/java/com/example/demo/
│   ├── config/                    # 配置类
│   │   ├── JacksonConfig.java     # JSON序列化配置
│   │   ├── RabbitConfig.java      # RabbitMQ配置
│   │   └── SchedulingConfig.java  # 定时任务配置
│   ├── entity/                    # 实体类
│   │   ├── Order.java             # 订单实体
│   │   ├── OutboxEvent.java       # Outbox事件实体
│   │   └── SagaTransaction.java   # Saga事务实体
│   ├── enums/                     # 枚举类
│   │   ├── OrderStatus.java       # 订单状态
│   │   ├── OutboxEventStatus.java # Outbox事件状态
│   │   └── SagaStatus.java        # Saga状态
│   ├── repository/                # 数据访问层
│   │   ├── OrderRepository.java
│   │   ├── OutboxEventRepository.java
│   │   └── SagaTransactionRepository.java
│   ├── service/                   # 业务服务层
│   │   ├── ProblematicOrderService.java  # 问题场景
│   │   ├── OutboxOrderService.java       # Outbox模式
│   │   └── SagaOrderService.java         # Saga模式
│   ├── saga/                      # Saga模式实现
│   │   ├── SagaOrchestrator.java  # Saga编排器
│   │   ├── SagaStep.java          # Saga步骤接口
│   │   ├── SagaData.java          # Saga数据传递
│   │   ├── SagaStepResult.java    # Saga步骤结果
│   │   └── steps/                 # 具体步骤实现
│   │       ├── CreateOrderStep.java
│   │       ├── SendMessageStep.java
│   │       └── UpdateOrderStatusStep.java
│   ├── processor/                 # 定时处理器
│   │   ├── OutboxEventProcessor.java
│   │   └── SagaTransactionProcessor.java
│   └── controller/                # REST控制器
│       ├── ProblematicOrderController.java
│       ├── OutboxOrderController.java
│       └── SagaOrderController.java
├── src/test/                      # 测试代码
└── docker-compose.yml             # Docker配置
```

## 快速开始

### 1. 环境准备

确保已安装：
- JDK 21+
- Docker & Docker Compose
- Maven 3.6+

### 2. 启动依赖服务

```bash
# 启动PostgreSQL和RabbitMQ
docker-compose up -d

# 检查服务状态
docker-compose ps
```

### 3. 运行应用

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

### 4. 访问管理界面

- **应用**: http://localhost:8080
- **RabbitMQ管理**: http://localhost:15672 (guest/guest)
- **数据库**: localhost:5432 (postgres/password)

## API接口演示

### 问题场景演示

```bash
# 1. 事务前发送消息（消息丢失）
curl -X POST http://localhost:8080/api/problematic/create-before-transaction \
  -H "Content-Type: application/json" \
  -d '{"productName":"iPhone","quantity":1,"price":999.99}'

# 2. 事务后发送消息（事务回滚，消息仍发送）
curl -X POST http://localhost:8080/api/problematic/create-after-transaction \
  -H "Content-Type: application/json" \
  -d '{"productName":"iPad","quantity":1,"price":599.99}'

# 3. 事务内发送消息（消息发送失败）
curl -X POST http://localhost:8080/api/problematic/create-in-transaction \
  -H "Content-Type: application/json" \
  -d '{"productName":"MacBook","quantity":1,"price":1999.99}'
```

### Outbox模式演示

```bash
# 1. 正常创建订单
curl -X POST http://localhost:8080/api/outbox/orders \
  -H "Content-Type: application/json" \
  -d '{"productName":"iPhone","quantity":2,"price":999.99}'

# 2. 取消订单
curl -X POST http://localhost:8080/api/outbox/orders/1/cancel

# 3. 批量创建订单
curl -X POST http://localhost:8080/api/outbox/orders/batch \
  -H "Content-Type: application/json" \
  -d '[{"productName":"iPad","quantity":1,"price":599.99},{"productName":"MacBook","quantity":1,"price":1999.99}]'

# 4. 手动触发事件处理
curl -X POST http://localhost:8080/api/outbox/events/process

# 5. 查看事件统计
curl http://localhost:8080/api/outbox/events/stats
```

### Saga模式演示

```bash
# 1. 正常创建订单
curl -X POST http://localhost:8080/api/saga/orders/normal \
  -H "Content-Type: application/json" \
  -d '{"productName":"iPhone","quantity":1,"price":999.99}'

# 2. 消息发送失败场景
curl -X POST http://localhost:8080/api/saga/orders/message-fail \
  -H "Content-Type: application/json" \
  -d '{"productName":"iPad","quantity":1,"price":599.99}'

# 3. 状态更新失败场景
curl -X POST http://localhost:8080/api/saga/orders/status-fail \
  -H "Content-Type: application/json" \
  -d '{"productName":"MacBook","quantity":1,"price":1999.99}'

# 4. 恢复Saga事务
curl -X POST http://localhost:8080/api/saga/transactions/recover
```

## 测试用例

运行测试：

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=OutboxOrderServiceTest
mvn test -Dtest=SagaOrderServiceTest
mvn test -Dtest=ProblematicOrderServiceTest
```

## 监控和观察

### 1. 数据库监控

```sql
-- 查看订单表
SELECT * FROM orders ORDER BY created_at DESC;

-- 查看Outbox事件
SELECT * FROM outbox_events ORDER BY created_at DESC;

-- 查看Saga事务
SELECT * FROM saga_transactions ORDER BY created_at DESC;
```

### 2. RabbitMQ监控

访问 http://localhost:15672 查看：
- 队列消息数量
- 消息发送/接收速率
- 连接状态

### 3. 应用日志

```bash
# 查看应用日志
tail -f logs/application.log

# 过滤特定日志
grep "SAGA\|OUTBOX" logs/application.log
```

## 核心概念对比

| 特性 | 问题场景 | Outbox模式 | Saga模式 |
|------|----------|------------|----------|
| **一致性** | 无保证 | 最终一致性 | 最终一致性 |
| **实现复杂度** | 简单 | 中等 | 复杂 |
| **性能影响** | 最小 | 中等 | 较大 |
| **适用场景** | 演示问题 | 事件驱动 | 长事务 |
| **错误恢复** | 无 | 自动重试 | 补偿机制 |
| **可观测性** | 差 | 好 | 很好 |

## 最佳实践

### Outbox模式

1. **事件设计**：
   - 事件应该是幂等的
   - 包含足够的上下文信息
   - 使用版本控制

2. **处理策略**：
   - 实现指数退避重试
   - 设置最大重试次数
   - 监控失败事件

3. **性能优化**：
   - 批量处理事件
   - 定期清理已处理事件
   - 使用索引优化查询

### Saga模式

1. **步骤设计**：
   - 每个步骤应该是原子的
   - 补偿操作必须是幂等的
   - 考虑补偿失败的情况

2. **状态管理**：
   - 持久化Saga状态
   - 实现超时处理
   - 提供手动干预机制

3. **错误处理**：
   - 区分可重试和不可重试错误
   - 实现熔断机制
   - 记录详细的错误信息

## 故障排查

### 常见问题

1. **数据库连接失败**
   ```bash
   # 检查Docker容器状态
   docker-compose ps
   
   # 重启数据库
   docker-compose restart postgres
   ```

2. **RabbitMQ连接失败**
   ```bash
   # 检查RabbitMQ状态
   docker-compose logs rabbitmq
   
   # 重启RabbitMQ
   docker-compose restart rabbitmq
   ```

3. **事件处理停滞**
   ```sql
   -- 检查待处理事件
   SELECT status, COUNT(*) FROM outbox_events GROUP BY status;
   
   -- 手动重置失败事件
   UPDATE outbox_events SET status = 'PENDING', retry_count = 0 
   WHERE status = 'FAILED' AND retry_count < 3;
   ```

4. **Saga事务卡住**
   ```sql
   -- 检查Saga状态
   SELECT status, COUNT(*) FROM saga_transactions GROUP BY status;
   
   -- 查看超时事务
   SELECT * FROM saga_transactions 
   WHERE status IN ('STARTED', 'EXECUTING') 
   AND updated_at < NOW() - INTERVAL '1 hour';
   ```

## 扩展阅读

- [Microservices Patterns - Chris Richardson](https://microservices.io/patterns/)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- [Event Sourcing](https://microservices.io/patterns/data/event-sourcing.html)

## 许可证

MIT License