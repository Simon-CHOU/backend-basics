# Future 异步调用与多线程的最佳实践

某个项目需要从多个第三方系统系统查询信息，合并到一起后，再传递给前端。
涉及到异步多线程。
为了写好这部分代码，并且满足性能要求，需要一些前置知识：
- Callable、Future和FutureTask
- Spring Boot @Async
- Feign Client mutli-thread
- Executor Service, Fork/Join, Parallel Stream
- ThreadLocal
等等……

所以，在这个项目里，通过跑通 GeeksForGeeks, Tutorial, 还有 Core Java 书上的代码实例，
我希望我能对多线程异步调用有一个比较完备的认知。