import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AtomicDemo {
    // 1. 定义原子类，初始值为 0
    private static final AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 10;
        // 使用 Java 21 虚拟线程（如果是 JDK 21+ 强烈建议提到这个，面试加分项）
        // 这里为了通用演示用普通线程池，但你可以口述虚拟线程
        try (var executor = Executors.newFixedThreadPool(threadCount)) {

            var latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < 1000; j++) {
                        // 2. 核心方法：incrementAndGet (相当于 ++i)
                        // 底层就是一个死循环的 CAS
                        counter.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            latch.await(); // 等所有线程跑完
            System.out.println("最终结果: " + counter.get()); // 必是 10000
        }
    }
}