package com.simon.gfg.cf;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

// https://www.callicoder.com/java-8-completablefuture-tutorial/
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        CompletableFuture<String> completableFuture = new CompletableFuture<String>();
//        String result = completableFuture.get(); //The get() method blocks until the Future is complete.
//
//        boolean manually = completableFuture.complete("Future's Result");//manually complete
//        System.out.println(manually)  ;



//        // Run a task specified by a Runnable Object asynchronously.
//        CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
//            @Override
//            public void run() {
//                // Simulate a long-running Job
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    throw new IllegalStateException(e);
//                }
//                System.out.println("I'll run in a separate thread than the main thread.");
//            }
//        });
//
//       // Block and wait for the future to complete
//        future.get();


//        doRunAsync();

        doSupplierAsync();
    }

    private static void doSupplierAsync() throws InterruptedException, ExecutionException {
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(new Supplier<String>() {
//            @Override
//            public String get() {
//                try {
//                    TimeUnit.SECONDS.sleep(2);
//                } catch (InterruptedException e) {
//                    throw new IllegalStateException(e);
//                }
//                return "Result of the asynchronous computation";
//            }
//        });
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Result of the asynchronous computation";
        });
        String result = future.get();
        System.out.println(result);
    }

    private static void doRunAsync() throws InterruptedException, ExecutionException {
        // Using Lambda Expression
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // Simulate a long-running Job
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            System.out.println("I'll run in a separate thread than the main thread.");
        });
        future.get();
    }
}
