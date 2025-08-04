

TOUG002
介绍你对 completableFuture 的认识

CompletableFuture是Java 8引入的异步编程核心工具，它解决了传统Future的三个关键痛点： 不可组合、阻塞式获取结果、异常处理复杂 。在我的实际项目中，我用它来优化业务接口性能，通过并发调用第三方服务，将原本串行的300ms响应时间优化到了100ms以内。

从架构角度看，CompletableFuture实现了 响应式编程模型 。它基于 事件驱动 和 非阻塞IO ，通过 回调链 和 线程池 实现真正的异步执行。相比传统Future的pull模式，CompletableFuture采用push模式，支持 流式API 和 函数式组合 。

CompletableFuture是Java从同步向异步编程演进的重要里程碑。它为Java 21的Virtual Threads和Structured Concurrency奠定了基础。在微服务架构中，它是实现 高并发、低延迟 的核心工具

> lab: 用一个非Web示例，花式编排completabeFuture掌握API的用法。
> lab: 使用 completableFuture 和 @Async的区别
> lab: Future->CompletableFuture 的迁移，模拟迁移一个旧项目
> lab: 从 completableFuture 架构和实现原理，研究API设计和面向对象设计模式。#CleanArchitecture
[java.util.concurrent.CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)


TOUG006
谈谈你对java提供的锁的认识？ReentrantLock、CyclicBarrier、CountDownLatch、Samephore

在Java核心并发包（java.util.concurrent）中，这些工具是处理多线程同步的关键机制。我虽然在业务开发中更多处理中低并发场景，没深入使用过这些高强度控制，但基于JMM（Java内存模型）和AQS（AbstractQueuedSynchronizer）的原理，我对它们有系统理解。让我逐一简要说明，包括概念、原理和典型场景。作为资深工程师，我会强调最佳实践。

首先， ReentrantLock ：这是可重入锁，实现Lock接口，比synchronized更灵活。原理基于AQS，使用CAS操作状态变量，支持公平/非公平模式。使用场景：需要中断锁、超时获取或条件等待时，比如在高并发服务中控制资源访问。最佳实践：总是用try-finally释放锁，避免死锁。

其次， CyclicBarrier ：栅栏工具，让一组线程等待到齐后再继续。原理用ReentrantLock和Condition实现计数器。场景：多线程并行计算，如矩阵乘法，每个线程完成部分后同步。比CountDownLatch可重用。最佳实践：设置parties数匹配线程，处理BrokenBarrierException。

然后， CountDownLatch ：倒计数门闩，让线程等待计数器到零。原理也基于AQS，countDown递减，await阻塞。场景：主线程等待多个子任务完成，如初始化阶段。最佳实践：初始化计数准确，避免在循环中使用（用CyclicBarrier代替）。

最后， Semaphore ：信号量，控制并发访问数。原理AQS管理许可（permits）。场景：限流，如数据库连接池限制连接数。最佳实践：用acquire/release管理，考虑公平模式防饥饿。

总体，这些工具构建在AQS上，提供高效同步。实际中，我会根据场景选择：简单用synchronized，复杂用这些。




ANTG012
ConcurrentHashMap 能不能保证强一致性？

ConcurrentHashMap 不能保证强一致性，它仅提供弱一致性（最终一致性）。

为何不保证强一致性？  
   ├─ 设计目标：平衡并发性能与一致性（强一致性需全局锁，牺牲性能）  
   └─ 适用场景：高并发读写，允许短暂不一致的场景（如缓存、统计）  

不，ConcurrentHashMap 不能保证强一致性。它提供线程安全和最终一致性，但不确保所有操作的即时可见性。

简单解释下原理：在 JDK 8+ 中，它用 CAS 和 synchronized 实现并发读写，volatile 确保最终可见。但复合操作不是原子的，可能看到中间状态。这基于 Java Memory Model 的 happens-before 规则。

在实际业务中，如果需要强一致，我会加外部锁或用 synchronized HashMap；否则，它很适合高并发场景，能显著优化性能。

> 判别强弱的决策树:
从concurrentHashmap推广开，告诉我判别任何一个数据结构是否具备强/弱一致性的决策树是怎样的？用plantuml 活动图画出判断的流程图


实际一致性模型应当更 精细命名（如 Linearizability、Sequential Consistency、Causal Consistency、Eventual Consistency 等）。

建议尽量少用“强一致性”/“弱一致性”泛指，改用具体术语，否则不利于准确判断。


1. 有无线性化点？ → 有 = Linearizability
2. 无 → 操作是否原子 & 可见？ → 有 = per-key Linearizability
3. 是否跨 key 操作？ → 有 + 有事务 = Transactional Linearizability
                        → 有 + 无事务 = Weak consistency
4. 操作间有因果顺序？ → 有 = Causal
5. 以上都没有？ → Eventual Consistency



ANTG013
synchronized 和 ReentrantLock 有何区别？

从四个核心维度来回答：

第一，实现层面。synchronized是JVM内置关键字，基于对象监视器实现，编译器自动处理；ReentrantLock是java.util.concurrent包中的类，基于AQS框架实现。

第二，功能特性。synchronized只支持非公平锁，不能中断，不能超时获取；ReentrantLock支持公平锁选择，可以响应中断，支持tryLock超时获取，还提供多个Condition条件变量。

第三，使用方式。synchronized自动获取和释放锁，代码简洁；ReentrantLock需要手动lock和unlock，必须在finally块中释放，否则可能死锁。

第四，性能表现。JDK6之前ReentrantLock性能更好，JDK6之后synchronized引入了偏向锁、轻量级锁等优化，两者性能相当。

实际选择原则：简单场景用synchronized，需要高级控制特性时用ReentrantLock。我在项目中90%的情况用synchronized，只有在需要公平锁或中断响应时才用ReentrantLock。