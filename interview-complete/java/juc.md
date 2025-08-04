

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
