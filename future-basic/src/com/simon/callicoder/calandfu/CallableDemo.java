package com.simon.callicoder.calandfu;

import java.util.concurrent.*;

/**
 * Callable就是有返回值和能抛出checked Exception的Runnable
 */
public class CallableDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "Murder Ballad";
            }
        };//new 创建callable
        Callable<String>  callable = ()->{
            System.out.println();
            return "Sting";
        };// lambda 创建callable

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<String> submit = executorService.submit(callable);
        System.out.println(submit.get());

    }
}
