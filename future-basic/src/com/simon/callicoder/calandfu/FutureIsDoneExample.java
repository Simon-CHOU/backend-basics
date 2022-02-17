package com.simon.callicoder.calandfu;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureIsDoneExample {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Future<String> future = executorService.submit(()->{
            Thread.sleep(1000);
            return "Lambda Callable";
        });

        while(!future.isDone()) {
            System.out.println("Task is still not done");
            Thread.sleep(200);
        }

        System.out.println("Task completed! Retrieving the result");
        String result = future.get();
        System.out.println(result);

        executorService.shutdown();

//        Task is still not done
//        Task is still not done
//        Task is still not done
//        Task is still not done
//        Task is still not done
//        Task completed! Retrieving the result
//        Lambda Callable
    }
}
