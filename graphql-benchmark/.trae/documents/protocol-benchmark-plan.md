# 协议对比实验规划

## Summary
- 目标：围绕同一个“获取订单详情 + 用户信息”的 BFF 接口，分别用 gRPC + protobuf、REST + JSON、GraphQL 三种协议实现，并以实验数据比较 P99 延迟、内存占用、序列化 CPU 开销。
- 当前计划状态：可确认实验目标与技术约束，但无法做 `complete` / `initial` 目录内的代码级落地规划，因为当前工作区实际未发现这两个目录。
- 本计划因此分为两层：
  - 已验证的环境事实与阻塞项
  - 在阻塞解除后可直接执行的实现与实验蓝图

## Current State Analysis

### 已验证文件
- 已实际存在的文件只有：
  - `c:\Users\mrsim\IdeaProjects\backend-basics\graphql-benchmark\README.md`
  - `c:\Users\mrsim\IdeaProjects\backend-basics\graphql-benchmark\summary.txt`
- 通过目录枚举、模式搜索、语义检索，当前未发现：
  - `c:\Users\mrsim\IdeaProjects\backend-basics\graphql-benchmark\complete\`
  - `c:\Users\mrsim\IdeaProjects\backend-basics\graphql-benchmark\initial\`

### 从现有文档可确认的信息
- `README.md` 明确声明本实验应包含 `complete` 与 `initial` 两个目录，其中：
  - `complete`：完整可运行项目
  - `initial`：练习骨架
- `README.md` 明确声明实验技术栈：
  - gRPC + protobuf
  - REST + JSON
  - GraphQL
  - Netflix DGS + Spring Boot 3
  - JMeter
  - Docker
  - Java 21
- `README.md` 明确声明实验结论应来自测量数据，而不是预设“最佳”判断。
- `summary.txt` 提供了本实验背后的问题背景：需要用工程实验验证 GraphQL / REST / gRPC 在 BFF 场景中的真实 trade-off。

### 当前阻塞
- 无法检查 `complete` 的真实模块结构、构建工具、服务入口、协议定义、压测脚本。
- 无法检查 `initial` 的现有骨架和待补全部位。
- 因此无法输出“逐文件差异计划”或“精确到现有类/包/模块名”的实施步骤。

## Proposed Changes

### 第一阶段：解除阻塞后先做真实结构盘点
- 检查 `complete` 的模块树、构建文件、运行入口、Docker 编排、JMeter 计划。
- 检查 `initial` 的已有骨架、空实现、TODO、占位协议文件。
- 产出：
  - `complete` 的真实结构说明
  - `initial` 与 `complete` 的真实差异清单
  - 练习补全顺序

### 第二阶段：统一实验约束，避免协议比较失真
- 三种协议必须复用同一份业务语义、同一份数据集、同一份聚合逻辑。
- “获取订单详情 + 用户信息”必须保持一致的输入、输出语义与错误语义。
- 非协议因素保持一致：
  - 同一 JVM 版本
  - 同一机器与容器资源限制
  - 同一预热与压测时长
  - 同一数据规模
  - 同一并发档位
- 目标是把差异尽量收敛到：
  - 传输协议
  - 序列化格式
  - 服务端解析/执行链路

### 第三阶段：完整项目 `complete` 的实现蓝图
- 规划一个可运行的完整实验项目，至少包含以下逻辑角色：
  - 订单数据源
  - 用户数据源
  - BFF 聚合层
  - GraphQL 接口
  - REST 接口
  - gRPC 接口
  - 压测与指标采集配置
- 推荐实现原则：
  - 三种协议共用同一个 application service，例如“聚合订单详情视图”的 use case。
  - 协议适配层只负责协议对象与领域 DTO 的转换。
  - 下游数据源尽量使用内存 mock 或固定仓储，避免数据库噪声污染协议对比。
  - 序列化 CPU 开销采样通过统一 profiler 或 JFR 事件采集，不混入业务逻辑差异。

### 第四阶段：练习骨架 `initial` 的设计蓝图
- `initial` 只保留能引导学习的骨架，不直接给出所有实现。
- 练习拆分建议：
  - 先补共享领域模型与聚合用例
  - 再补 REST 版本，建立最直观基线
  - 再补 gRPC 版本，观察 protobuf 契约与 stub 调用方式
  - 最后补 GraphQL 版本，观察 schema、resolver、DataLoader/批量获取点
- 题目设计应尽量让学习者关注：
  - 协议边界
  - DTO 映射
  - 统一基准
  - 可比实验，而不是框架样板代码堆砌

### 第五阶段：实验设计与指标采集
- P99 延迟：
  - 使用同一组请求语义和同一并发模型压测三种协议。
  - 输出每组测试的吞吐量、平均延迟、P95、P99。
- 内存占用：
  - 记录运行期 RSS / 堆使用量 / GC 次数与停顿。
  - 压测期间按固定采样周期记录峰值和稳态区间。
- 序列化 CPU 开销：
  - 以统一 profiling 手段采集编码/解码热点。
  - 重点区分 JSON 编解码、GraphQL 查询解析执行、protobuf 编解码。
- 控制变量：
  - 使用固定 payload
  - 统一预热
  - 统一线程池与连接池策略
  - 统一日志级别，避免日志 I/O 扭曲结果

### 第六阶段：实验输出物
- 实验报告应至少包含：
  - 实验环境
  - 版本与构建信息
  - 测试方法
  - 请求样本
  - 原始结果表
  - 图表
  - 结论与局限性
- 结论部分必须避免“GraphQL/REST/gRPC 天生最佳”这类绝对化表述，而应改成：
  - 在本实验负载下
  - 在当前实现方式与运行环境下
  - 某协议在某指标上占优
  - 某协议以某代价换取某能力

## Assumptions & Decisions
- 决定：本次计划交付范围以 `complete + initial` 为主。
- 决定：在目录未实际出现在工作区前，不凭空指定其内部真实文件名、包名或模块名。
- 决定：计划仍然给出一套可执行的实验蓝图，以便目录同步后能立刻转入精确规划或实现。
- 假设：后续同步到工作区的 `complete` / `initial` 会与 `README.md` 的目录语义一致。
- 假设：实验重点是协议对比，而不是数据库、缓存、中间件或分布式拓扑对比。

## Verification Steps
- 验证目录存在：
  - 确认 `complete` 与 `initial` 在当前工作区可见。
- 验证 `complete` 可运行：
  - 能完成本地构建
  - 能启动 BFF 服务
  - 能暴露三种协议入口
- 验证三种协议语义等价：
  - 相同输入下返回一致的订单与用户聚合结果
  - 相同错误场景下错误语义可对齐
- 验证压测可重复：
  - 固定环境下重复运行结果波动在可接受范围
  - 可复现 P99、内存、CPU 采样流程
- 验证最终结论可追溯：
  - 报告中的每个结论都能回溯到原始测量数据
