package k2;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicStampedReference;

public class SolveABA {
    // 带版本号的原子引用
    private static AtomicStampedReference<Integer> ref =
            new AtomicStampedReference<>(100, 0); // 初始值+版本号

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            int stamp = ref.getStamp(); // 获取当前版本号
            System.out.println("T1 read: " + ref.getReference() + ", stamp: " + stamp);

            try { Thread.sleep(Duration.ofSeconds(1)); } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // CAS 时检查版本号
            boolean success = ref.compareAndSet(
                    100, 101,    // 期望值 → 新值
                    stamp, stamp + 1 // 期望版本号 → 新版本号
            );
            System.out.println("T1 CAS success: " + success); // false！
        });

        Thread t2 = new Thread(() -> {
            try { Thread.sleep(Duration.ofMillis(100)); } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            ref.compareAndSet(100, 200, ref.getStamp(), ref.getStamp() + 1); // 版本号+1
            ref.compareAndSet(200, 100, ref.getStamp(), ref.getStamp() + 1); // 版本号+1
            // 版本号已变，T1 的 CAS 会失败
        });

        t1.start(); t2.start();
        t1.join(); t2.join();
    }
}