# Future 异步调用与多线程的最佳实践

某个项目需要从多个第三方系统系统查询信息，合并到一起后，再传递给前端。
涉及到异步多线程。
为了写好这部分代码，并且满足性能要求，需要一些前置知识：
- Callable、Future和FutureTask
- CompleteFuture
- Spring Boot @Async
- Feign Client Mutli-Thread
- Executor Service, Fork/Join, Parallel Stream
- ThreadLocal
- Thread pools, Blocking Queue
- synchronized
等等……

所以，在这个项目里，通过跑通 GeeksForGeeks, Tutorial, 还有 Core Java，Java Concurrency in Practice 书上的代码实例，
我希望我能对多线程异步调用有一个比较完备的认知。

##Thread

[Thread and Runnable](https://www.callicoder.com/java-multithreading-thread-and-runnable-tutorial/)

## FutureTask


prerequisite:

[Thread in Operating System](https://www.geeksforgeeks.org/thread-in-operating-system/)
[Multithreading in Java](https://www.geeksforgeeks.org/multithreading-in-java/)

↓

[Callable and Future in Java](https://www.geeksforgeeks.org/callable-future-java/)
[Calli coder: JavaCallable and Future Tutorial](https://www.callicoder.com/java-callable-and-future-tutorial/)

↓

[Future and FutureTask in java](https://www.geeksforgeeks.org/future-and-futuretask-in-java/?ref=gcse)


## Concurrency

[The Java Tutorials: Lesson: Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html)

[Executors](https://docs.oracle.com/javase/tutorial/essential/concurrency/executors.html)

[Thread Pools](https://docs.oracle.com/javase/tutorial/essential/concurrency/pools.html)

[Executor Service & Thread Pool](https://www.callicoder.com/java-executor-service-and-thread-pool-tutorial/)
- [Java Concurrency API](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/package-summary.html)
Using worker threads minimizes the overhead due to thread creation.

## Structuring Concurrent Applications

《Java Concurrency in Practice》

## Fork

[ForkJoinPool](https://www.geeksforgeeks.org/forkjoinpool-class-in-java-with-examples/)
[Difference Between Fork/Join Framework and ExecutorService in Java](https://www.geeksforgeeks.org/difference-between-fork-join-framework-and-executorservice-in-java/)

[Introduction of Process Synchronization](https://www.geeksforgeeks.org/introduction-of-process-synchronization/)

↓
 
[Fork and Join Constructs in Concurrency](https://www.geeksforgeeks.org/fork-and-join-constructs-in-concurrency/)


## CompleteFuture

https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFut【】ure.html

- [Java CompletableFuture Tutorial with Examples](https://www.callicoder.com/java-8-completablefuture-tutorial/) !!!
  - 比 Future 强在异常处理、多个组合一起用 
  - runAsync() 没有返回值
  - supplyAsync() 有返回值
  - Executor 配合使用，自定义线程池，runAsync、supplyAsync 都有 Executor 入参
  - CompletableFuture.get() 方法是阻塞的，需要等Future结果，这与“异步返回”初衷相悖
  - thenApply(), thenAccept() and thenRun() 不阻塞地异步调用
- [Guide To CompletableFuture](https://www.baeldung.com/java-completablefuture)
- [What is CompletableFuture?](https://www.javatpoint.com/completablefuture-in-java)

## Synchronization
- [Callicoder: Java Concurrency issues and Thread Synchronization](https://www.callicoder.com/java-concurrency-issues-and-thread-synchronization/)
  - Thread interference errors (Race Conditions)
  - Memory consistency errors