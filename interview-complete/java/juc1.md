
JDON008
completablefuture 结果的合并你是怎么做的？

> 生产场景是我使用completableFuture并发同步调用5个第三方系统，并合并5个系统的JSON返回结果后，继续接下来的transactional业务流程。请问应该如何合并同步调用结果?

allOf() + 自定义线程池（推荐） ExecutorService executor
 CompletableFuture.allOf
 thenApply

> lab: thenApply vs thenCompose

- thenApply ：只需要对上一步结果做简单处理（同步或轻量级异步），比如格式转换、拼接字符串。
- thenCompose ：需要基于上一步结果再发起新的异步操作，比如先查用户，再查用户的订单。
