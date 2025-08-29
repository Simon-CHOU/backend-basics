package com.simon.callicoder.chm;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import static  java.lang.Thread.*;

class CHMapProofTest {
//2、版本1:使用线程池+Future实现需求

    private static void sleep(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
//使用线程池异步执行step1
        Future<String> step1 = executorService.submit(() -> {
            sleep(500);
            return "获取商品基本信息";
        });
//使用线程池异步执行step2
        Future<String> step2 = executorService.submit(() -> {
//这里需要等到step1执行完毕
            step1.get();
            sleep(500);
            return "获取商品折扣信息";
        });
//使用线程池异步执行step3
        Future<String> step3 = executorService.submit(() -> {
            sleep(500);
            return "获取商品描述信息";

        });

//这里需要等到3个步骤都执行完成,这里可以不用写step1.get(),因为step2依赖于step1
        step2.get();
        step3.get();
        System.out.println("#8(ms): " + (System.currentTimeMillis() - startTime));

    }


//    4、版本2:线程池+CompletableFuture 实现需求

    @Test
    public void test2() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
//使用线程池异步执行step1
        CompletableFuture<String> step1 = CompletableFuture.supplyAsync(() -> {
            sleep(500);
            return "获取商品基本信息";
        }, executorService);

//使用线程池异步执行step2
        CompletableFuture<String> step2 = step1.thenApplyAsync((goodsInfo) -> {
            sleep(500);
            return "获取商品折扣信息";
        }, executorService);

//使用线程池异步执行step3
        CompletableFuture<String> step3 = CompletableFuture.supplyAsync(() -> {
            sleep(500);
            return "获取商品描述信息";
        }, executorService);

//这里需要等到3个步骤都执行完成,这里可以不用写step1,因为step2依赖于step1
        CompletableFuture.allOf(step2, step3).get();

        System.out.println("#€8j(ms): " + (System.currentTimeMillis() - startTime));
    }

//    private static void sleep(long timeout) {
//        try {
//            TimeUnit.MILLISECONDS.sleep(timeout);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//
//        }
//    }

//    private static void sleep(int timeout) {
//        try {
//            TimeUnit.MILLISECONDS.sleep(timeout);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//
//        }
//    }

}