package com.simon.callicoder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

//        CompletableFuture.supplyAsync(()->{
//            System.out.println("then Return example");
//            return "a:";// 这个return 好像没什么用
//        }).thenRun(()->{
//            System.out.println("Computation Finished.");
//        });

//        doThenApplyAsync();

//        combineTwoCompletableFutureWithThenCompose();

//        combineTwoCompletableFutureWithThenCombine();

        // combine multiple CompletableFuture together: allOf anyOf
//        combineMultiCompletableFutureWithAllOf();
        combineMultiCompletableFutureWithAnyOf();


    }

    private static void combineMultiCompletableFutureWithAnyOf() throws InterruptedException, ExecutionException {
        // anyOf
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Result of Future1";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Result of Future2";
        });
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Result of Future3";
        });
        CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(future1, future2, future3);
        System.out.println(anyOfFuture.get()); //Result of Future2
        // that if you have CompletableFutures that return results of different types,
        // then you won’t know the type of your final CompletableFuture.
    }

    private static void combineMultiCompletableFutureWithAllOf() throws InterruptedException, ExecutionException {
        // allOf 待全部完成后，合并并行的诸多结果
        List<String> webPageLinks = Arrays.asList("a","b");
        // Download contents of all the web pages asynchronously
        List<CompletableFuture<String>> pageContentFutures =  webPageLinks.stream()
                .map(webPageLink -> downloadWebPage(webPageLink))
                .collect(Collectors.toList());
        //create a combine Future using allOf
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                pageContentFutures.toArray(new CompletableFuture[pageContentFutures.size()])
        );
        CompletableFuture<List<String>> allPageContentFuture = allFutures.thenApply(v->{
            return pageContentFutures.stream()
                    .map(pageContentFuture-> pageContentFuture.join())
                    .collect(Collectors.toList());
        });// since we're calling future.join() when all the futures are complete, we're not blocking anywhere
        //join vs get: .join() throws an unchecked exception if the underlying CompletableFuture completes exceptionally

        //处理结果
        // count the number of web page having the "CompletableFuture" keyword.
        CompletableFuture<Long> countFuture = allPageContentFuture.thenApply(pageContents ->{
            return pageContents.stream()
                    .filter(pageContent -> pageContent.contains("CompletableFuture"))
                    .count();
        });
        System.out.println("Number of Web Pages having CompletableFuture keyword - " +
                countFuture.get());
    }

    private static CompletableFuture<String> downloadWebPage(String pageLink){
        return CompletableFuture.supplyAsync(()->{
            //Code to download and return the web page's content
            return "html";
        });
    }

    private static void combineTwoCompletableFutureWithThenCombine() throws InterruptedException, ExecutionException {
        //combine two independent futures user thenCombine()
        // thenCompose是串联，thenCombine是并联
        System.out.println("Retrieving weight.");
        CompletableFuture<Double> weightInKgFuture = CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return 65.0;
        });
        System.out.println("Retrieving height.");
        CompletableFuture<Double> heightInCmFuture = CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return 177.8;
        });
        System.out.println("Calculation BMI");
        CompletableFuture<Double> combineFuture = weightInKgFuture
                .thenCombine(heightInCmFuture, (weightKg, heightCm)->{
                    Double heightInMeter  = heightCm/100;
                    return weightKg /(heightInMeter* heightInMeter);
                });
        System.out.println("Your BMI is - " + combineFuture.get());
    }

    private static void combineTwoCompletableFutureWithThenCompose() throws InterruptedException, ExecutionException {
        //combine two completableFutures
        CompletableFuture<CompletableFuture<Double>> result = getUserDetail("Jamie")
                .thenApply(user -> getCreditRating(user));
        System.out.println(result.get().get());//1.1
        // final result to be a top-level Future
        // If your callback fucntion returns a CompletableFuture,and you wang a flattened
        // result from the CompletableFuture chain (which in most cases you would), then use thenCompose()
        CompletableFuture<Double> res = getUserDetail("James")
                .thenCompose(user -> getCreditRating(user));
        System.out.println(res.get());//1.1
    }

    static CompletableFuture<User> getUserDetail(String userId) {
        return CompletableFuture.supplyAsync(()->{
            return UserService.getUserDetail(userId);
        });
    }
    static CompletableFuture<Double> getCreditRating(User user){
        return CompletableFuture.supplyAsync(()->{
            return CreditRatingService.getCreditRatingUser(user);
        });
    }

    private static void doThenApplyAsync() {
        CompletableFuture.supplyAsync(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return "Some Result";
        }).thenApply(result -> {
            /*
              Executed in the same thread where the supplyAsync() task is executed
              or in the main thread if the supplyAsync() task completes immediately(try removing sleep() call to verify)
             */
            return "Processed Result";
        });
        // use async callbacks to have more control over the thread
        CompletableFuture.supplyAsync(()->{
            System.out.println("Some Result");//打印出来了
            System.out.println("Some Result" +Thread.currentThread().getName());//打印不出来,
            return "Some Result";
        }).thenApplyAsync(result -> {
            /*
              Executed in a different thread from ForkJoinPool.commonPool()
             */
            System.out.println("Processed Result " + Thread.currentThread().getName());//打印不出来
            return "Processed Result";
        });
        // execute in a thread obtained from the Executor's thread pool
        Executor executor = Executors.newFixedThreadPool(2); // 有了executor后，Thread.currentThread().getName()就能打印出来
        CompletableFuture.supplyAsync(()->{
            return "Some Result";
         }).thenApplyAsync(result->{
             return "Process Result";
         }, executor);
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
