

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



JDON008
completablefuture 结果的合并你是怎么做的？

> 生产场景是我使用completableFuture并发同步调用5个第三方系统，并合并5个系统的JSON返回结果后，继续接下来的transactional业务流程。请问应该如何合并同步调用结果?

allOf() + 自定义线程池（推荐） ExecutorService executor
 CompletableFuture.allOf
 thenApply

> lab: thenApply vs thenCompose

- thenApply ：只需要对上一步结果做简单处理（同步或轻量级异步），比如格式转换、拼接字符串。
- thenCompose ：需要基于上一步结果再发起新的异步操作，比如先查用户，再查用户的订单。

ALIJ001
线程池参数设置？线程池拒绝策略都有哪些？

，线程池参数设置我会从业务需求、系统资源和性能目标三个层面综合考量：

1. 线程池核心参数包括 corePoolSize（核心线程数）、maximumPoolSize（最大线程数）、keepAliveTime（空闲线程存活时间）、workQueue（任务队列）、threadFactory（线程工厂）、handler（拒绝策略）。

2. corePoolSize 通常根据 CPU 核心数和任务类型（CPU 密集型/IO 密集型）来设定。CPU 密集型建议 corePoolSize ≈ CPU 核心数，IO 密集型可适当放大，避免线程过多导致上下文切换开销。

3. maximumPoolSize 结合系统最大并发能力和业务高峰流量预估，设置为 corePoolSize 的 2~4 倍，确保高峰期有足够线程处理任务。

4. workQueue 选择时要权衡任务堆积风险与响应延迟。短任务建议用有界队列（如 ArrayBlockingQueue），防止 OOM。长任务或高吞吐场景可用无界队列，但要配合监控和报警。

5. keepAliveTime 主要影响非核心线程的回收速度，通常设置为几十秒到几分钟，避免线程频繁创建销毁。

6. 拒绝策略（handler）常用有四种：AbortPolicy（抛异常）、CallerRunsPolicy（调用者执行）、DiscardPolicy（直接丢弃）、DiscardOldestPolicy（丢弃最老任务）。实际生产推荐自定义报警或降级策略，避免任务静默丢失。

7. 最佳实践是结合业务压力测试和监控，动态调整线程池参数，确保系统既能高效利用资源，又能平稳应对突发流量。

> 有没有保底不出错的 corePollSize, maximumPoolSize 经验公式?

- CPU 密集型任务：corePoolSize = CPU 核心数（如 2c pod 设为 2）
- IO 密集型任务：corePoolSize = CPU 核心数 × 2 或更高（需结合压测和内存评估）
- maximumPoolSize = corePoolSize × 2~4
最佳实践 ：
- 先按经验公式设置，结合业务压测和监控动态调整
- 关注 CPU、内存、队列长度、线程池拒绝次数等指标

CITI003
线程池策略有哪些？jdk提供了哪些拒绝策略？拒绝策略如何影响线程池的行为？

创建线程池的策略：
Executors.newFixedThreadPool(n)
Executors.newCachedThreadPool()
Executors.newSingleThreadExecutor()
Executors.newScheduledThreadPool()
ScheduledThreadPool
实际生产中，我更推荐自定义ThreadPoolExecutor，因为默认实现在高并发下有风险。

ThreadPoolExecutor 的底层任务调度策略
在线程池中，任务提交后的调度策略主要分为三部分：
核心线程执行；
阻塞队列缓存；
最大线程补充；
达到上限后会触发 拒绝策略（RejectedExecutionHandler）。

JDK 提供了四种内置拒绝策略，分别是：

- AbortPolicy（默认）：抛出 RejectedExecutionException；
- CallerRunsPolicy：由提交任务的线程自己执行，起到“背压”作用；
- DiscardPolicy：直接丢弃任务，不抛异常；
- DiscardOldestPolicy：丢弃队列头部最旧的任务，再尝试提交新任务。

这些策略直接影响线程池的行为：
AbortPolicy 更适合早期暴露问题；
CallerRunsPolicy 适合控制提交速率、避免 OOM；
DiscardPolicy 和 DiscardOldestPolicy 更适合对延迟不敏感的场景。

实际项目中我也实现过自定义的 RejectedExecutionHandler，做任务日志记录、报警或者落盘重试。

> 建议线程池参数要根据业务特点调优：CPU密集型任务线程数约等于CPU核数，IO密集型可以设置为2倍CPU核数，队列大小要考虑内存限制和响应时间要求。


ALIJ002
为什么使用 concurrentHashMap？concurrentHashMap 如何实现线程安全?
- 在高并发场景下，传统的HashMap线程不安全，容易导致数据不一致甚至死循环。
- ConcurrentHashMap专为并发设计，能在保证线程安全的同时，最大化利用多核CPU提升性能，是业界最佳实践。

- 它采用分段锁（JDK8前）或CAS+链表/红黑树（JDK8后），将整个Map拆分为多个桶，每个桶独立加锁或用CAS原子操作，极大减少锁竞争。
- 读操作基本无锁，写操作只锁定相关桶或节点，保证高吞吐。

补充
- 根据业务并发量、热点key分布等，合理选择并发容器和线程池参数，避免过度设计，也不牺牲可维护性。
- 实际项目中，除了ConcurrentHashMap，还会关注整体架构的瓶颈，比如热点key、锁粒度、内存占用等，必要时用LongAdder、分段缓存等进一步优化。
> lab 理解上述两个补充的内涵


CITI005
concurrenthashmap如何使用？


ConcurrentHashMap 是Java并发包中的核心工具，我通常从三个层面来理解和使用它： 核心定位、关键用法和典型场景 。”

“ 首先，它的核心定位是一个高性能的线程安全哈希表。 与 Hashtable 或 Collections.synchronizedMap 不同，它的高性能来自于内部精巧的并发控制。在JDK 8以后，它主要通过 CAS操作 和 synchronized 锁住哈希桶的头节点 来实现。这种细粒度的锁机制，只有在发生哈希冲突时才会加锁，极大地提升了并发环境下的吞吐量，尤其是在读多写少的场景。”

“ 其次，在关键用法上，除了像普通 Map 一样使用 put 和 get 之外，作为资深开发者，我更关注它的原子复合操作，这是避免并发编程中竞态条件的关键。 ”

“一个典型的例子就是‘检查再更新’（check-then-act）。比如，我们不能写 if (map.get(key) == null) { map.put(key, value); } ，因为这在并发下不是原子的。正确的做法是直接使用 putIfAbsent(key, value) 。类似地，对于更新和计数场景，我会优先使用 compute 和 merge 这类方法，它们能保证整个操作的原子性，代码也更简洁。”

“ 最后，在典型场景中，我主要在两个地方使用它： ”

“ 第一，作为高并发服务的本地缓存。 它的高读取性能和线程安全特性使其成为理想选择。”

“ 第二，作为多线程共享的计数器或状态容器。 例如，我会用 ConcurrentHashMap<String, LongAdder> 来实现一个高性能的并发计数器，通过 computeIfAbsent 来原子地初始化计数器，这样性能会优于使用 AtomicLong 。”

“总结一下，我认为用好 ConcurrentHashMap 的关键在于： 理解它细粒度锁的原理，并充分利用它提供的原子复合操作来编写正确且高效的并发代码，而不是简单地把它当作一个线程安全的 HashMap 来用。 ”



CITI010
synchronized和reentrant锁的区别

Synchronized 是 JVM 内置的隐式锁，简单但功能有限；ReentrantLock 是显式锁，提供更多控制如公平性、可中断和多条件。

具体来说：

1. 实现上，synchronized 自动管理，ReentrantLock 需要手动 lock/unlock。
2. 两者都可重入，但 ReentrantLock 支持公平锁和中断。
3. ReentrantLock 有 Condition 接口，支持多个等待队列，而 synchronized 只有 wait/notify。
4. 性能上，现代 JVM 中二者相似，但 ReentrantLock 更灵活。
在实际项目中，我根据需求选择：简单用 synchronized，复杂用 ReentrantLock。这体现了我的经验，能处理高并发挑战。如果需要深入某个点，我可以展开。

Synchronized 是 Java 内置的监视器锁，简单易用但功能有限；ReentrantLock 是显式锁，提供更多灵活性，如可中断、公平锁和条件变量，适合复杂场景。但在性能上，现代 JVM 中二者相似，选择取决于需求。

对比维度：
基础实现（Implementation）
可重入性（Reentrancy）
公平性（Fairness）
可中断性（Interruptibility）
条件变量（Conditions）
性能与优化（Performance）
异常处理（Exception Handling）


CITI011
synchronized修饰方法，当类是静态类和非静态类的异同

synchronized 是 Java 中用于实现线程同步的关键字。当它修饰方法时，会在方法执行时添加锁机制，确保同一时刻只有一个线程能执行该方法，从而实现互斥访问。核心区别在于锁的对象：

- 非静态方法 ：锁住的是实例对象（this）。
- 静态方法 ：锁住的是类对象（Class）。
相似点：两者都提供线程安全的互斥机制，防止并发问题如数据不一致。
不同点：锁对象不同，导致适用场景和行为差异——非静态方法锁实例级，静态方法锁类级。

> synchronized 修饰方法时，具体的同步行为（加锁），是否只跟方法是否被static修饰有关，而与 方法所在的类对象是静态还是非静态无关？
是的，synchronized 修饰方法的同步行为（加锁）完全取决于方法是否为 static，而与方法所在的类是否为静态类（静态内部类或非静态类）无关

> java 是否只有静态 变量、方法、代码块，没有“静态类”
在 Java 中，没有“静态类”（static class）这个独立概念，但有一个重要的例外：静态内部类（static nested class）


LINGX006
你说用HashMap存储结果，你觉得在并发条件下你用HashMap会有问题吗？

在并发条件下直接使用HashMap存储结果会有严重线程安全问题。HashMap不是线程安全的，多个线程同时put或get时，可能导致数据丢失、脏读，甚至死循环（如扩容时链表形成环）
最佳实践是：

1. 如果需要并发读写，应该用ConcurrentHashMap替代HashMap，它采用分段锁或CAS机制，保证线程安全且性能较优。
2. 如果只需要读一致性，可以考虑用Collections.synchronizedMap包裹HashMap，但性能不如ConcurrentHashMap。
3. 如果业务场景允许，尽量减少共享状态，优先考虑线程局部变量或无共享设计。

所以，在多线程合并Future结果时，推荐用ConcurrentHashMap来存储id到Future的映射，确保线程安全和高性能。

> lab: 复现 Map<businessId, Future> 多线程并发同步调用RESTful API并合并返回。