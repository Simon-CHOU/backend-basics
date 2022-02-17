package com.simon.callicoder.calandfu;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * 注意和  CompletableFuture.allOf 对比
 */
public class InvokeAllExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        Callable<String> task1 = () -> {
            Thread.sleep(2000);
            return "Result of Task1";
        };
        Callable<String> task2 = () -> {
            Thread.sleep(1000);
            return "Result of Task2";
        };
        Callable<String> task3 = () -> {
            Thread.sleep(5000);
            return "Result of Task3";
        };

        List<Callable<String>> taskList = Arrays.asList(task1, task2, task3);

        List<Future<String>> futures = executorService.invokeAll(taskList);

        for(Future<String> future : futures) {
            // The result is printed only after all the futures are complete
            System.out.println(future.get());
        } //在上面的程序中，第一次调用 future.get() 语句会阻塞，直到所有的Future都完成。即结果将在 5 秒后打印。

        executorService.shutdown();

        // 打印顺序就是提交顺序，因为get()阻塞的，挨个等，并不是并发
//        Result of Task1
//        Result of Task2
//        Result of Task3
    }
}
