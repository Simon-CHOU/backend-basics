package k2;

import java.time.Duration;

/**
 * 第4章：可见性问题实战（第一个代码示例）
 */
public class VisibilityProblem {
    // 不用 volatile，不用 atomic
    private static boolean stop = false;

    public static void main(String[] args) throws InterruptedException {
        // 线程1：死循环检查 stop 变量
        Thread worker = new Thread(() -> {
            long count = 0;
            while (!stop) {
                count++; // 做点事，防止 JIT 优化掉循环
            }
            System.out.println("Worker stopped! count=" + count);
        });
        worker.start();

        // 主线程：睡眠 1 秒后修改 stop
        Thread.sleep(Duration.ofSeconds(1));
        System.out.println("Main setting stop = true");
        stop = true; // 这里修改可能永远对 worker 不可见！

        worker.join(Duration.ofSeconds(5));
        if (worker.isAlive()) {
            System.out.println("❌ BUG: Worker didn't see the change!");
        }
    }
}