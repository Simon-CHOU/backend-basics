package com.simon.callicoder.calandfu;

import java.util.concurrent.*;

/**
 * Future get 设置超市时间
 *
 * future.get()是阻塞的，设置超市时间可以防范远程调用无法正常返回时，应用一直无响应
 */
public class FutureGetTimeoutExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<String> future = executorService.submit(()->{
            return "Titan";
        });
        System.out.println(future.get(1, TimeUnit.SECONDS));

    }
}
