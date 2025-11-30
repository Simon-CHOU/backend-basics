package k2;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class ABAProblem {
    private static AtomicInteger atomicRef = new AtomicInteger(100);

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            int old = atomicRef.get();
            System.out.println("T1 read: " + old);

            // 模拟耗时操作
            try { Thread.sleep(Duration.ofSeconds(1)); } catch (Exception e) {}

            // 期望还是 100，但实际上已经被改了两次
            boolean success = atomicRef.compareAndSet(old, 101);
            System.out.println("T1 CAS success: " + success); // true，但它不知道中间变了！
        });

        Thread t2 = new Thread(() -> {
            try { Thread.sleep(Duration.ofMillis(100)); } catch (Exception e) {}
            atomicRef.set(200);  // 100 → 200
            System.out.println("T2 changed to 200");
            atomicRef.set(100);  // 200 → 100（ABA！）
            System.out.println("T2 changed back to 100");
        });

        t1.start(); t2.start();
        t1.join(); t2.join();
    }
}