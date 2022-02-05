package com.simon.gfg.cf;

import java.util.concurrent.*;

// https://www.callicoder.com/java-8-completablefuture-tutorial/
public class CompletableFutureDemo {
    static class Product {
        public Long id;
        public String name;

        public Product(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

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

//        doSupplierAsync();

//        doSupplyAsyncWithExecutor();

//        doThenApply();

//        CompletableFuture.supplyAsync(()->{
//            return getProductDetail(1L);
//        }).thenAccept(product->{
//            System.out.println("Got product detail from remote service  "+product.name);
//        });

        CompletableFuture.supplyAsync(()->{
            System.out.println("then Return example");
            return "a:";// 这个return 好像没什么用
        }).thenRun(()->{
            System.out.println("Computation Finished.");
        });
    }
    private static Product getProductDetail(Long productId) {
        return new Product(1L,"Simon");
    }

    private static void doThenApply() throws InterruptedException, ExecutionException {
        CompletableFuture<String> welcomeText = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Rajeev";
        }).thenApply(name->{
            return "Hello " + name;
        }).thenApply(greeting ->{
            return greeting +",Welcome to the CalliCoder Blog";
        });

//        CompletableFuture<String> greetingFuture = whatsYourNameFuture.thenApply(name -> {
//            return "Hello " + name;
//        });

//        System.out.println(greetingFuture.get());
        System.out.println(welcomeText.get());
    }

    private static void doSupplyAsyncWithExecutor() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<String> future = CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Result of the asynchronous computation";
        }, executor);
        System.out.println(future.get());
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
