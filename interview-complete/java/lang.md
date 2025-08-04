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



ANTG009
对java的异常，你们一般怎么处理？


对于Java异常处理，我一般采用分层和全局相结合的方式，确保代码鲁棒且符合Clean Code原则。首先，我区分Checked和Unchecked异常，只在必要时抛出Checked异常。

在代码中，使用try-catch-finally，按异常继承关系分级捕获，比如先捕获具体异常，再捕获父类。catch块里记录日志，如使用SLF4J的error级别，并尝试恢复或返回友好错误消息。绝对避免空catch或捕获太宽泛的Exception，以防隐藏bug。

在Spring Boot服务中，我会添加全局异常处理器，通过@ControllerAdvice统一处理未捕获异常，返回标准响应。这提高了可维护性。

> lab: 区分Checked和Unchecked异常 #架构设计
> lab: 考虑事务（transaction）的影响，如何讨论异常处理

```
Java异常继承体系
                    Throwable
                   /         \
               Error           Exception
                              /         \
                    RuntimeException   其他Exception
                    (Unchecked)        (Checked)

```


在事务环境中，异常处理不仅要考虑业务逻辑的正确性，更要确保 数据一致性 和 事务完整性 。核心原则是： 异常类型决定事务行为，事务边界决定异常传播策略 。这需要从事务语义、异常分类、回滚策略三个维度系统性思考。

    ## 架构设计原则
    ### 1. 异常分类原则：
    - 业务异常 ：继承BusinessException，触发回滚，返回用户友好信息
    - 系统异常 ：继承SystemException，触发回滚，记录详细日志
    - 外部异常 ：根据业务语义决定是否回滚
    ### 2. 事务边界原则：
    - 粗粒度事务 ：在服务层定义事务边界，包含完整业务操作
    - 细粒度异常 ：在具体操作点捕获和转换异常
    - 补偿机制 ：对于跨服务的操作，设计补偿逻辑
    ### 3. 监控和恢复：
    - 事务指标 ：监控事务成功率、回滚率、异常分布
    - 异常告警 ：对关键业务异常设置实时告警
    - 数据修复 ：提供数据一致性检查和修复工具


ANTG010
(前文我提到了异常类有继承关系，多个catch语句，先catch子异常类，再catch父异常类)你刚刚提到了分级处理异常（try-catch），请问处理的的不同点在于什么呢？

分级异常处理的核心不同点在于处理策略的精细化和业务语义的差异化。不同层级的异常代表不同的问题域，需要采用不同的恢复策略、日志级别和用户反馈方式。

分级处理异常的不同点主要体现在三个维度：

第一，处理策略不同 。具体异常通常有明确的恢复方案，比如FileNotFoundException我会尝试创建默认文件或提示用户选择路径；而IOException这种通用异常，我只能记录错误并向上抛出，让调用方决定。

第二，日志级别不同 。业务异常如ValidationException记录WARN级别，因为这是预期内的用户输入错误；系统异常如SQLException记录ERROR级别，需要立即关注；而像NullPointerException这种编程错误，我会记录ERROR并包含完整堆栈，便于快速定位。

第三，用户反馈不同 。具体异常能提供精确的错误信息，比如'用户名已存在'；通用异常只能返回模糊信息如'系统繁忙'，避免暴露技术细节。

这种分级处理符合Fail-fast原则，让系统在不同异常场景下都能优雅降级，既保证了用户体验，又便于运维监控

> lab: DDD 如何讨论异常处理？



ANTG011
Java 异常处理中，什么时场景应该在函数签名中将异常抛出（throw ），什么时候应该将try-catch-finally 将异常处理掉？

异常处理的核心决策原则是 职责边界 和 恢复能力 。简单说： 能恢复就捕获，不能恢复就抛出 。具体判断依据是当前层级是否有足够的上下文信息来做出正确的业务决策。


这个问题的核心是职责分离原则。我的判断标准是： 当前层级是否有能力和责任处理这个异常 。

抛出异常的场景 ：第一，当前层级缺乏处理上下文，比如DAO层遇到数据库连接异常，它不知道业务应该重试还是降级，所以抛给Service层决策。第二，需要事务回滚时，比如转账操作中任何步骤失败都应该抛出，让事务管理器统一回滚。

捕获异常的场景 ：第一，有明确恢复策略，比如缓存失败可以降级到数据库查询。第二，需要转换异常类型，比如将SQLException转换为BusinessException，避免技术细节泄露。第三，在系统边界，比如Controller层必须捕获所有异常，返回标准HTTP响应。

核心原则是异常要在合适的抽象层级处理 。底层抛出技术异常，中层转换为业务异常，上层提供用户友好的错误信息。这样既保证了系统的鲁棒性，又符合单一职责原则。

> 在clean code/clean architecture 中，有没有讨论过以下问题： 什么时场景应该在函数签名中将异常抛出（throw ），什么时候应该将try-catch-finally 将异常处理掉？
Clean Code 相关原则可引用第 7 章全文。
Clean Architecture 相关原则可引用第 22 章（依赖规则）和分层设计的章节。

