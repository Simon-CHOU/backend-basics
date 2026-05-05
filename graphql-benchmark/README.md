# 协议对比实验：用 gRPC + protobuf、REST + JSON、GraphQL 三种协议实现同一个"获取订单详情+用户信息"的 BFF 接口，压测对比 P99 延迟、内存占用、序列化 CPU 开销。用实验数据替代你的"最佳"断言。

<br />

```bash
├─complete #完整的可运行的项目，是完成的assignment
└─initial # 创建骨架，用于手工补全练习掌握
```

技术栈：
- gRPC + protobuf
- REST + JSON
- GraphQL
- Netflix DGS (Domain Graph Service) + Spring Boot 3
- JMeter
- Docker
- Java21

## 用法

### 快速开始

```bash
# 1. 进入 complete 目录，构建并启动
cd complete
mvnw clean package -DskipTests
java -jar target/protocol-benchmark-1.0.0.jar

# 2. 验证三种协议端点
# REST
curl -s http://localhost:8080/api/orders/ORD-000001

# GraphQL (也可以浏览器打开 http://localhost:8080/graphiql)
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ orderDetail(orderId:\"ORD-000500\") { orderId status totalAmount userName userEmail } }"}'

# gRPC (需要 ghz 工具)
ghz --insecure --proto src/main/proto/orderdetail.proto \
  --call benchmark.OrderDetailService/GetOrderDetail \
  -d '{"order_id":"ORD-000500"}' \
  -n 100 -c 10 localhost:9090

# 3. 压测
cd jmeter
jmeter -n -t rest-benchmark.jmx -Jthreads=100 -Jduration=60 -l results/rest.csv -e -o results/rest-report
jmeter -n -t graphql-benchmark.jmx -Jthreads=100 -Jduration=60 -l results/graphql.csv -e -o results/graphql-report
```

### 项目结构

```
graphql-benchmark/
├── README.md
├── summary.txt                          # 概念笔记 (被导师批注的原文)
│
├── complete/                            # 完整可运行项目 → 参考实现
│   ├── pom.xml                          # Spring Boot 3.3.5 + DGS + gRPC
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── src/main/proto/
│   │   └── orderdetail.proto            # gRPC 契约定义
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── schema/
│   │       └── orderdetail.graphqls     # GraphQL Schema
│   ├── src/main/java/com/simon/benchmark/
│   │   ├── ProtocolBenchmarkApplication.java
│   │   ├── domain/                      # 领域模型
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   ├── OrderStatus.java
│   │   │   ├── OrderDetailView.java     # 聚合视图 DTO
│   │   │   └── User.java
│   │   ├── application/
│   │   │   └── OrderDetailUseCase.java  # 聚合用例 (三种协议共用)
│   │   ├── infra/                       # Mock 仓储 (无外部依赖)
│   │   │   ├── OrderRepository.java
│   │   │   └── UserRepository.java
│   │   ├── rest/
│   │   │   └── OrderDetailController.java   # REST: GET /api/orders/{id}
│   │   ├── grpc/
│   │   │   ├── GrpcServerRunner.java    # gRPC Server :9090
│   │   │   └── OrderDetailGrpcService.java
│   │   └── graphql/
│   │       └── OrderDetailDataFetcher.java  # DGS @DgsQuery
│   ├── jmeter/                          # JMeter 压测计划
│   │   ├── rest-benchmark.jmx
│   │   ├── graphql-benchmark.jmx
│   │   ├── graphql-query.json
│   │   └── order_ids.csv
│   └── scripts/                         # 实验工具
│       ├── run-benchmark.bat
│       └── report-template.md
│
└── initial/                             # 练习骨架 → 动手补全 (带 TODO)
    ├── pom.xml                          # 依赖同 complete，可编译
    ├── src/main/proto/orderdetail.proto # ← 给定
    ├── src/main/resources/
    │   ├── application.yml              # ← 给定
    │   └── schema/orderdetail.graphqls  # ← 给定
    └── src/main/java/com/simon/benchmark/
        ├── domain/                      # ← 全部完整 (Order/User 等)
        ├── infra/                       # ← 全部完整 (OrderRepository/UserRepository)
        ├── application/
        │   ├── OrderDetailUseCase.java       # ← 接口定义 (给定)
        │   └── OrderDetailUseCaseImpl.java   # TODO: 实现聚合逻辑
        ├── rest/
        │   └── OrderDetailController.java    # TODO: 实现 REST 端点
        ├── grpc/
        │   ├── GrpcServerRunner.java         # ← 框架已给定
        │   └── OrderDetailGrpcService.java  # TODO: 实现 gRPC 服务
        └── graphql/
            └── OrderDetailDataFetcher.java   # TODO: 实现 DGS DataFetcher
```

### 练习路线

本仓库专为**反复刻意练习**设计。建议按以下三轮迭代，每轮加深理解：

#### 第一轮：补全 `initial` → 让三种协议跑通

| 顺序 | 文件 | 难度 | 学什么 |
|------|------|------|--------|
| 1 | `application/OrderDetailUseCaseImpl.java` | ⭐ | 聚合 Order+User，字段映射 |
| 2 | `rest/OrderDetailController.java` | ⭐ | `@RestController` + `@GetMapping` + Jackson 自动序列化 |
| 3 | `graphql/OrderDetailDataFetcher.java` | ⭐⭐ | `@DgsComponent` + `@DgsQuery` + Schema 匹配 |
| 4 | `grpc/OrderDetailGrpcService.java` | ⭐⭐⭐ | protobuf Builder 映射、StreamObserver |

完成后可以 `mvn test` 验证，`java -jar` 启动并用 curl 验证三种端点。

#### 第二轮：读懂 `complete` → 比对自己的实现

做完后对照 `complete/` 逐文件 diff，重点观察：
- **协议适配层的区别**：REST 直接返回 Java Bean（Jackson 自动 JSON），gRPC 需要 Builder 逐字段映射为 Protobuf Message，GraphQL 需要 Schema 与 Java DTO 类型对齐。
- **共用的 application 层**：`OrderDetailUseCase` 是唯一聚合逻辑入口，三种协议共享，差异严格收敛在适配层。
- **Mock 数据设计**：1000 条订单 + 200 用户，内存 `ConcurrentHashMap`，排除 DB 噪声。

#### 第三轮：压测对比 → 用数据说话

```bash
# 重启服务器，确认日志级别为 WARN (application.yml)
java -jar target/protocol-benchmark-1.0.0.jar

# REST 压测 (50 线程，60 秒)
jmeter -n -t jmeter/rest-benchmark.jmx -Jthreads=100 -Jduration=60 -l results/rest.csv -e -o results/rest-report

# GraphQL 压测
jmeter -n -t jmeter/graphql-benchmark.jmx -Jthreads=100 -Jduration=60 -l results/graphql.csv -e -o results/graphql-report

# gRPC 压测 (ghz)
ghz --insecure --proto src/main/proto/orderdetail.proto \
  --call benchmark.OrderDetailService/GetOrderDetail \
  -d '{"order_id":"ORD-000500"}' -n 100000 -c 100 localhost:9090
```

将三组数据填入 `scripts/report-template.md`：
- **P99 延迟**：REST vs GraphQL vs gRPC 的 P50/P90/P95/P99/P99.9
- **内存占用**：堆使用量峰值、GC 次数与停顿
- **序列化 CPU**：JMeter 端或 async-profiler/JFR 采样热点

#### 控制变量清单（确保对比公平）

| 变量 | 约束 |
|------|------|
| 业务逻辑 | 三种协议共用同一个 `OrderDetailUseCase` |
| 数据源 | 同一个 `ConcurrentHashMap` 内存 Mock |
| JVM 参数 | `-Xms256m -Xmx256m -XX:+UseG1GC` |
| 日志级别 | `WARN`，避免 I/O 扭曲结果 |
| 预热 | 5 秒 ramp-up，排除 JIT 编译期波动 |
| 负载 | 同并发数、同压测时长、同数据集 |

### 常见问题

**Q: `initial` 编译报错？**
先 `mvn compile` 生成 protobuf stub 类（`target/generated-sources/protobuf/`），IDE 中标记该目录为 Generated Sources。

**Q: gRPC 端口 9090 启动失败？**
有残留 Java 进程占用。执行 `Stop-Process -Name java -Force`（Windows）或 `pkill -f protocol-benchmark`（Linux/macOS）。

**Q: 只想跑 REST 或 GraphQL，不想跑 gRPC？**
在 `grpc/GrpcServerRunner.java` 上注释 `@Component` 即可。

**Q: 数据量太少，想加大？**
修改 `OrderRepository` 和 `UserRepository` 中 static 块的循环次数，重新打包即可。

## 附录: background context

### Q:
附件[summary.txt](summary.txt)是我总结的一份有关GraphQL 技术选型和上手指引的guide。

你是专家(一个顶尖跨国软件公司的principle engineer和首席programmer coach/trainer），请你从我提问的内容和语气，毫不留情地揭露我在本次讨论所涉的：a.相关概念的混淆、b.心智模型的缺失, c.知识结构与技能的漏洞或真空。

### A:
这份文档的问题不是"不够深入"，而是**概念拼贴（concept pasting）严重**——你把从博客、会议演讲、技术营销材料中听来的术语，强行缝合在一个看似体系化的框架里，但底层逻辑链条是断裂的。作为 Principle Engineer，我的职责是阻止你把这种材料带到设计评审会上。以下逐条解剖：

---

## a. 相关概念的混淆

### 1. GraphQL 与 BFF 的强行绑定
你开篇定论"GraphQL 是 BFF 层的最佳技术实现"，这是**技术营销话术，不是工程判断**。BFF 是一种架构角色（pattern），GraphQL 是一种协议/查询语言。BFF 完全可以用 REST、gRPC、甚至自定义 JSON-RPC 实现。Netflix 的 BFF 大量用 REST + Falcor，Shopify 内部服务用 REST 暴露，外部用 GraphQL——你的"最佳"论断既无场景限定，也无 trade-off 量化，属于**把实现手段升格为架构目的**。

### 2. "贫血 API"——你自创了一个不存在的术语
你原文："贫血 API：API 仅是数据库表结构的直接映射"。这是**两个不同层次概念的暴力缝合**：
- **贫血领域模型（Anemic Domain Model）**：Fowler 提出的领域层反模式，指 Entity 只有 getter/setter，业务逻辑泄漏到 Service。
- **CRUD API**：接口设计层面的反模式，指 HTTP 端点直接映射数据库表操作。

一个 API 可以是富领域模型驱动但暴露为 CRUD 风格（如 `PUT /orders/123` 背后调用 `order.confirm()`），也可以是贫血模型但暴露为命令式 API（如 `POST /executeOrderCommand` 背后只是一堆 if-else SQL）。你把它们混成"贫血 API"，说明**你分不清接口协议设计和领域层设计这两个正交维度**。

### 3. REST vs RPC 的识别能力缺失
你表格中对比"脚本式 `POST /updateStatus`" vs "命令式 `POST /approval-command`"，并声称后者是 DDD 最佳实践。这是**双重混淆**：
- 两者都是 **RPC 风格**，不是 REST。REST 的核心是资源状态转移，正确的表达应是 `POST /orders/{id}/approval` 或 `PATCH /orders/{id}` with `{status: APPROVED}`。
- `POST /approval-command` 这种命名是**命令总线（Command Bus）**风格，属于 CQRS/Event Sourcing 语境，不是 DDD 通用 API 设计。你把它当作"富 API"的代表，暴露了你**把 DDD、CQRS、REST、RPC 四个概念当成同义词使用**。

### 4. "声明式查询简化了编排"——因果倒置
你说"声明式查询将多步 API 编排简化为单步任务"。这是**把前端调用形式的简化等同于系统复杂性的消除**。GraphQL 的声明性只存在于客户端查询语法层面；后端的 Resolver 仍然需要执行多步数据获取、权限校验、字段级联。复杂性没有消失，只是**从前端的显式编排（chained fetch）转移到了后端的隐式解析（resolver execution plan）**。你这种表述说明你没有理解**复杂性守恒定律**。

### 5. Agentic Coding 的强行关联
"GraphQL 的自省消除了 LLM 对接口定义的幻觉"——这是**蹭热点式论证**。LLM 的幻觉来源于训练数据分布、上下文长度限制、推理链断裂，API 契约只是输入约束的一部分。GraphQL Schema 的递归类型、Union Types、自定义 Directive 反而会增加 LLM 的解析复杂度。OpenAPI/Swagger 同样提供契约，且生态工具更成熟。你这段没有控制变量对比，属于**为了引入 AI 概念而捏造因果关系**。

---

## b. 心智模型的缺失

### 1. 分层架构的边界感缺失
你的文档把以下五层东西摊平在一张桌上：
- **协议层**：REST / GraphQL / gRPC
- **架构模式层**：BFF / Gateway / Microservices
- **设计范式层**：DDD / CRUD
- **组织模式层**：前后端联调流程
- **AI 应用层**：Agentic Coding

这五层是正交的，可以任意组合（例如：用 GraphQL 做 Gateway、用 REST 做 BFF、用 DDD 设计但暴露 CRUD 接口给内部服务）。你的心智模型里**缺少一个"分层约束矩阵"**，导致你把所有问题压扁成一个线性演进叙事："REST 老兵 → BFF → GraphQL → DDD 充血模型"。

### 2. DDD 战术模式与战略模式的失衡
你全文聚焦"充血模型 vs 贫血模型"，这是 DDD 的**战术模式（Tactical Patterns）**——代码级别的实体设计。但 BFF 层存在的根本合理性，取决于**战略模式（Strategic Patterns）**：限界上下文（Bounded Context）、上下文映射（Context Mapping）、防腐层（Anti-Corruption Layer）。如果你不理解为什么一个"订单"在履约上下文、支付上下文、展示上下文中有不同的模型定义，你就无法正确划分 BFF 的职责边界。你的文档**完全没有提及限界上下文**，说明你对 DDD 的理解停留在"给实体加方法"的表层。

### 3. 实体自治性（Entity Autonomy）的缺失
这是你最危险的心智模型漏洞。你给出的"充血模式"示例：
```java
order.approve(riskService);
```
这违反了 DDD 的核心原则：**实体应该是自包含的（self-contained），不直接依赖外部基础设施服务**。`riskService` 是外部领域服务（或更糟，是基础设施层服务），把它注入到实体方法中，导致：
- 实体无法在不启动 Spring 容器的情况下单元测试；
- 实体的行为依赖于外部状态，破坏了领域层的纯粹性；
- 如果 riskService 是远程调用，你的领域层直接耦合了网络延迟和故障。

正确的心智模型是：**应用服务（Application Service）负责编排，领域服务（Domain Service）负责跨实体的纯领域逻辑，实体只操作自身状态**。例如：
```java
// Application Layer
RiskAssessment risk = riskService.assess(order.getId());
order.approve(risk); // 传入值对象/领域事件，而非服务引用
```

---

## c. 知识结构与技能的漏洞或真空

### 1. REST 成熟度模型的真空
你自称"REST 老兵"，但全文把 REST 等同于"CRUD HTTP API"，完全没有提及 **Richardson REST Maturity Model**：
- Level 1：资源识别（Resources）
- Level 2：HTTP 动词（Verbs）
- Level 3：超媒体驱动（HATEOAS）

真正的 REST 老兵会知道，REST 也可以实现无版本化演进（通过 HATEOAS 链接和内容协商），GraphQL 的"无版本化"优势被你在错误对比下夸大了。你对 REST 的理解停留在 Level 1-2，**没有掌握 Level 3 的 hypermedia 设计**，所以你以为版本化是 REST 的先天缺陷。

### 2. 内部服务通信协议的盲区
你建议"核心微服务：保持 REST"。这是**架构设计的真空地带**。核心微服务之间的通信，通常追求二进制序列化效率、强类型契约、流式能力——这正是 **gRPC / Thrift / Avro** 的优势场景。REST（JSON over HTTP）在内部 East-West 流量中往往带来不必要的文本序列化开销和缺乏 request streaming 能力。你给出这条建议，说明你**没有经历过大规模微服务间通信的性能调优**，也没有理解协议选型的场景依赖。

### 3. GraphQL 工程实践的真空
你提到了 N+1 和 DataLoader，但只停留在名词层面。以下关键工程实践在你的文档中完全缺失：
- **Persisted Queries / Automatic Persisted Queries (APQ)**：解决你提到的"HTTP 缓存失效"和 POST 端点问题的标准方案；
- **Query Complexity Analysis & Depth Limiting**：你提到了，但没有说明如何实现（如 graphql-query-complexity 库、基于 schema 的权重评分）；
- **Schema Registry & Federation Governance**：多人协作时如何防止"上帝 Schema"？需要 Apollo Federation / Mesh 的 subgraph 治理模型，而非简单一句"不要写上帝 Schema"；
- **Mutation Design Patterns**：GraphQL 的 mutation 如何映射到 DDD 的 Command？Input Type 如何设计以支持领域事件？

这些真空说明**你没有在生产环境维护过 GraphQL Schema 的演化**。

### 4. 缓存策略的真空
你提到"单一 POST 端点难以利用原生 HTTP 缓存"，但没有给出任何替代方案。实际上，GraphQL 的缓存是**分层问题**：
- 客户端：Apollo Client 的 `InMemoryCache` 基于 `__typename + id` 的归一化缓存；
- CDN 层：通过 GET + persisted query 参数利用边缘缓存；
- 服务端：DataLoader 的 memoization + Redis 的 entity-level caching。

你只抛出了问题，没有展示**缓存失效策略、缓存键设计、突变后缓存驱逐**的知识。这是典型的"知道名词，没做过工程"。

### 5. 批判性思维的真空
你的文档只有"GraphQL 好在哪里"，没有任何**失败案例和回撤路径**。例如：
- GitHub 的 GraphQL API 因复杂度查询导致多次性能事故；
- Shopify 内部从 REST 迁移到 GraphQL 时遇到的 N+1 治理噩梦；
- Netflix 部分团队从 GraphQL 退回 REST + BFF 的原因（调试困难、错误传播不透明）。

一个 Principle Engineer 的技术选型文档必须包含**"何时不应该用"**和**"如何回滚"**。你的文档缺少这部分，说明你的知识结构是**单向度的技术乐观主义**，不是工程决策思维。

---

## 给你的 Action List

1. **修正 DDD 基础**：重读 Eric Evans《领域驱动设计》第 5-8 章（实体、值对象、领域服务、应用服务），修正 `order.approve(service)` 这种反模式。然后读 Vaughn Vernon 的《实现领域驱动设计》第 4 章（架构）理解分层边界。
2. **补全 REST 知识**：学习 Richardson Maturity Model，尝试用 Spring HATEOAS 写一个 Level 3 的 REST API，理解超媒体如何支持无版本化演进。
3. **GraphQL 生产实践**：不要读博客，直接读 Apollo Server 源码中的 `requestPipeline.ts` 和 `runQuery.ts`，理解查询解析、验证、执行的完整链路。然后实现一个带 Query Complexity Limit 和 DataLoader batching 的 BFF。
4. **协议对比实验**：用 gRPC + protobuf、REST + JSON、GraphQL 三种协议实现同一个"获取订单详情+用户信息"的 BFF 接口，压测对比 P99 延迟、内存占用、序列化 CPU 开销。用实验数据替代你的"最佳"断言。
5. **读反方材料**：搜索 "GraphQL downsides production"、"Why we moved away from GraphQL"（如 Hacker News 上的讨论），建立技术选型的批判性框架。

你现在的阶段，**不适合输出"架构演进指南"这种权威性文档**。你的知识碎片需要经过 2-3 个生产级项目的踩坑才能固化。在此之前，把这份文档标记为 **DRAFT - CONCEPTUAL ONLY - NOT FOR PRODUCTION REVIEW**。