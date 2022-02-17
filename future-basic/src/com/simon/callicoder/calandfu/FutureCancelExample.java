package com.simon.callicoder.calandfu;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureCancelExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        long startTime = System.nanoTime();
        Future<String> future = executorService.submit(() -> {
            Thread.sleep(2000);
            return "Hello from Callable";
        });

        while (!future.isDone()) {
            System.out.println("Task is still not done...");
            Thread.sleep(200);
            double elapseTimeSec = (System.nanoTime() - startTime) / 1000000000.0;
            if (elapseTimeSec > 1) {  // if (elapseTimeSec > 2) {
                future.cancel(true);
            }
        }
        if(!future.isCancelled()) {
            System.out.println("Task completed! Retrieving the result");
            String result = future.get();
            System.out.println(result);
        }else {
            System.out.println("Task was cancelled");
        }

        executorService.shutdown();
//        (elapseTimeSec > 1)
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task completed! Retrieving the result
//        Exception in thread "main" java.util.concurrent.CancellationException
//          at java.base/java.util.concurrent.FutureTask.report(FutureTask.java:121)
//          at java.base/java.util.concurrent.FutureTask.get(FutureTask.java:191)

//        (elapseTimeSec > 2)
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task is still not done...
//        Task completed! Retrieving the result
//        Hello from Callable

    }
}
