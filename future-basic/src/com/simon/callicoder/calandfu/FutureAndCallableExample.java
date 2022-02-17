package com.simon.callicoder.calandfu;

import java.util.concurrent.*;

/**
 * 获取Callable的返回值
 */
public class FutureAndCallableExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Callable<String> callable = () -> {
            System.out.println("Entered Callable");
            Thread.sleep(2000);
//            TimeUnit.SECONDS.sleep(2);
            System.out.println("BBBBBBBBBBBBB");

            return "Hello from Callable";
        };

        System.out.println("Submitting Callable");
        Future<String> future = executorService.submit(callable);
        //一旦你获得了一个future，
        // 你可以在你提交的任务执行的同时并行执行其他任务，然后使用future.get()方法来获取future的结果。

        //This line executes immediately
        System.out.println("Do something else while callable is getting executed");

        System.out.println("Retrieve the result of the future");
        // Future get() blocks until the result is available
        String result = future.get();
        System.out.println("AAAAAAAAAAAAA");
        System.out.println(result);

        executorService.shutdown();

//        打印顺序：
//        Submitting Callable
//        Do something else while callable is getting executed
//        Retrieve the result of the future
//        Entered Callable
//        BBBBBBBBBBBBB
//        AAAAAAAAAAAAA
//        Hello from Callable
    }
}
