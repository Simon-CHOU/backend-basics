SANO002
java版本，你知道的evolution 8 11 17 21

## Java版本核心改进 - Key Action List
### Java 8 (2014) - 函数式编程革命
核心价值：从命令式到声明式编程范式转变

Key Actions:

- ✅ Lambda表达式 - 简化匿名函数，提升代码可读性
- ✅ Stream API - 声明式数据处理，支持并行计算
- ✅ Optional类 - 优雅处理null值，减少NPE
- ✅ CompletableFuture - 异步编程支持，链式调用
- ✅ 接口默认方法 - 向后兼容的API演进
- ✅ 新时间API - 线程安全的日期时间处理
### Java 11 (2018) - 现代化工具链
核心价值：提升开发效率，减少外部依赖

Key Actions:

- ✅ HTTP Client - 原生HTTP/2支持，替代第三方库
- ✅ var关键字 - 局部变量类型推断，简化代码
- ✅ String新方法 - isBlank(), lines(), strip()等实用方法
- ✅ Flight Recorder - 生产级性能监控工具
- ✅ ZGC垃圾收集器 - 低延迟GC，支持大堆内存
- ✅ 模块系统优化 - Jigsaw项目成熟化
### Java 17 (2021) - 企业级稳定版
核心价值：现代语言特性 + 长期支持

Key Actions:

- ✅ Record类 - 不可变数据载体，消除样板代码
- ✅ Pattern Matching - instanceof的模式匹配
- ✅ Sealed Classes - 受限继承，更好的领域建模
- ✅ Text Blocks - 多行字符串支持，提升可读性
- ✅ Switch表达式 - 更简洁的分支逻辑
- ✅ 强封装JDK内部API - 提升安全性和稳定性
### Java 21 (2023) - 并发编程新纪元
核心价值：高并发性能突破 + 现代语言特性

Key Actions:

- ✅ Virtual Threads - 轻量级线程，百万级并发支持
- ✅ Structured Concurrency - 结构化并发编程模型
- ✅ Pattern Matching for switch - 完整的模式匹配支持
- ✅ Record Patterns - 解构Record的模式匹配
- ✅ String Templates - 安全的字符串插值
- ✅ Sequenced Collections - 有序集合的统一接口


> lab: 构建一个multi-module playground，切换jdk编译运行，一览每个版本的特性。

SANO003
介绍一下 pure function ，也就是 functional programming 最重要的部分。

Pure Function（纯函数） = 确定性输出 + 无副作用
Pure Function 的两个黄金法则：
1. 相同输入 → 相同输出 (Deterministic)
2. 无副作用 (No Side Effects)

架构层面的价值
1. 可测试性 (Testability) 纯函数测试简单直接 Given-When-Then
2. 并发安全 (Thread Safety) 纯函数天然线程安全,无共享状态，无竞态条件
3. 缓存优化 (Memoization) 纯函数结果可以安全缓存

> lab: clean code 和 重构中如何讨论 pure function


SANO004
为什么人们建议写Java代码的时候不要用 null ？如果有null，怎么办

为什么避免？
因为null带来三个主要问题：

1. 运行时不确定性 - NPE是最常见的运行时异常，破坏系统稳定性
2. 认知负担 - 每个方法调用都需要考虑null检查，增加心智模型复杂度
3. API契约模糊 - 方法签名无法表达是否可能返回null，文档和实现容易不一致

避免不了怎么解决？

采用分层防护策略：

编译时层面 ：优先使用Optional作为返回类型，让类型系统表达可能为空的语义。虽然Optional有性能开销，但在API边界使用是值得的。

运行时层面 ：Guard clauses做防御性编程，但更重要的是 Fail-fast原则 - 在数据进入系统边界时就验证，而不是在使用时才检查。

架构层面 ：通过Domain Modeling避免null的产生 - 比如使用Builder模式确保对象完整性，使用工厂方法控制对象创建。

> lab: 避免Null的架构设计，和DDD有何关联？

SANO009

TOUG002

TOUG006