# 异步线程池参数解释（taskExecutor）

**核心结论（先看结论）**
- 设计目标：稳定执行长耗时、以数据库为主的 I/O 统计任务；提供可观测性与可靠背压，避免压垮数据库与主线程。
- 配置原则：与数据库连接池保持同量级、适度并发、有限队列、可回退到调用线程执行，确保不丢任务。
- 当前取值在开发与准生产场景下是保守且安全的默认值；生产可据监控数据按下文规则调整。

**业务负载画像（理解任务形态）**
- 触发来源：定时任务（每日 23:40）与手动触发接口（并发触发可能存在）。
- 执行内容：按月份与客户 ID 计算累计指标，随后写入看板表，每个 `(year, month, retailer_id)` 会写入 6 条记录。
- I/O 特征：以 SQL 读写为主，CPU 计算较轻，典型 I/O 密集任务；需要控制并发避免数据库饱和。
- 规模估算：若 7 个月 × 100 客户 × 6 指标 → 看板写入约 4,200 行；统计查询和写入均属中等批量。

---

## 参数逐项解释与取值理由

代码位置：`src/main/java/com/simon/case_study_statistics_by_month/config/AsyncConfig.java:20-35`

- 核心线程数 `corePoolSize = 5`
  - 作用：常驻工作线程数量，即便空闲也不回收；承担稳定负载。
  - 理由：
    - 统计任务 I/O 密集，不需要很高 CPU 并发；5 个核心线程可并行处理多个独立触发（手动 + 定时）。
    - 与 Spring Boot 默认 Hikari 连接池（常见默认 10）保持一半规模，以防线程过多竞争数据库连接。
    - 开发机/单实例环境下，5 能兼顾并发与稳定，不会造成频繁上下文切换。
  - 经验法则：I/O 密集型的核心线程数通常设置为数据库连接池的一半或不超过其规模。

- 最大线程数 `maxPoolSize = 10`
  - 作用：在队列尚未满、负载突增时，线程池可扩容到的最大工作线程数量。
  - 理由：
    - 设为 10 与常见 Hikari 默认连接池规模相当，处理突发并发时能充分利用连接资源但不进一步增压数据库。
    - 与核心线程的 5 保持 1:2 的扩展比，既有弹性又避免极端膨胀。
  - 行为：当核心线程忙且队列未满时，优先入队；队列满后再扩到 `maxPoolSize`。

- 队列容量 `queueCapacity = 100`
  - 作用：缓存待执行任务的数量，核心线程繁忙时先入队排队。
  - 理由：
    - 100 能吸收短时触发峰值（例如同一时间多个手动触发），形成缓冲层，避免立即扩容到最大线程数。
    - 有限队列提供背压，当队列满时触发拒绝策略，防止无限制堆积导致内存压力。
  - 取值平衡：过小会频繁触发拒绝策略、增加调用线程负担；过大则变相“吞”峰值，延长尾延迟并掩盖问题。

- 线程名前缀 `threadNamePrefix = "StatisticsTask-"`
  - 作用：为线程命名，利于日志检索与问题定位。
  - 理由：
    - 日志中可快速分辨统计任务相关线程，便于筛选 `StatisticsTask-*` 进行链路分析。
    - 与业务强关联，方便告警规则与监控面板设置。

- 拒绝策略 `CallerRunsPolicy`
  - 作用：当“核心线程已占满 + 已达最大线程数 + 队列已满”时的新任务处理方式。
  - 选择理由：
    - 由提交任务的调用线程直接执行该任务，实现同步化退让与节流（天然背压），不丢任务。
    - 相比 `AbortPolicy`（抛异常）和 `DiscardPolicy`（丢任务）更安全；相比 `DiscardOldestPolicy` 不会丢掉排队中的旧任务。
  - 业务契合：统计任务是可延后但不可丢失的后台作业，采用调用方执行可快速降低生产者速率。

```
@startuml
title 线程池与队列在高负载下的行为
actor Producer
queue Q as "队列(容量=100)"
participant Pool as "线程池(核心=5, 最大=10)"

Producer -> Q : 提交任务
Q -> Pool : 线程空闲则取出执行
note right of Pool
  当核心线程满 → 入队
  队列满 → 扩容到最大线程数
  队列仍满 & 线程达上限 →
  CallerRunsPolicy：由调用线程执行
end note
@enduml
```

---

## 与业务代码的关系与注意事项

- 触发入口：
  - 定时入口：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:40-44`
  - 异步方法：`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:50-81`
- 重要提示：当前实现内部使用 `CompletableFuture.runAsync(...)`（未显式传入线程池），这会落到 JDK 公共线程池，而非 `taskExecutor`。
  - 为确保使用 `taskExecutor`，可采用以下任一改进思路（不影响口径，仅优化执行器选择）：
    - 在方法上使用 `@Async("taskExecutor")` 并直接执行主体逻辑，返回 `CompletableFuture<Void>`。
    - 或使用 `CompletableFuture.runAsync(runnable, taskExecutor)` 显式传入线程池。
- 写入看板与日志定位：`processAndSaveStatistics` 在循环中写入 6 项指标（`src/main/java/com/simon/case_study_statistics_by_month/service/StatisticsTaskService.java:140-146`），线程前缀有助于在日志中快速定位相关写入链路。

---

## 生产调优建议（如何根据监控调整数值）

- 观测指标：
  - 数据库连接池使用率与等待时间、慢 SQL 比例、线程池活跃线程数与队列长度、任务耗时分布。
- 调整策略：
  - 若数据库仍有充足连接且线程长期饱和：提升 `corePoolSize` 至 6-8；`maxPoolSize` 不超过数据库池大小。
  - 若出现排队过久：增大 `queueCapacity`（如 200-300），或在不增加数据库压力的前提下略增 `maxPoolSize`。
  - 若数据库出现饱和：降低 `maxPoolSize` 与 `corePoolSize`，同时优化 SQL 或分批写入；必要时对任务做限流。
  - 日志可观测性不足：调整 `threadNamePrefix` 并在关键链路打印摘要统计。

---

## 总结
- 当前参数是一套“以数据库安全为第一优先”的保守配置：5/10 线程并发、100 队列容量、调用方回退执行，保证不丢任务、不过压数据库、易于定位问题。
- 随着数据量与触发频率增长，建议先基于监控做微调，而不要一次性扩大并发；线程池大小应与数据库连接池规模保持同量级并受其约束。