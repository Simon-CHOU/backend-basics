package tongyi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VirtualThreadLocalExample {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        // Java 21: 创建虚拟线程池
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                final int taskId = i;

                executor.submit(() -> {
                    // 设置线程上下文
                    CONTEXT.set("virtual-task-" + taskId);

                    // 模拟IO操作
                    try {
                        Thread.sleep(10); // 虚拟线程会挂起而非阻塞OS线程
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.println("[" + Thread.currentThread() + "] 任务 " + taskId +
                            " 完成，上下文: " + CONTEXT.get());

                    // 清理 - 对虚拟线程更重要，因为虚拟线程可能被池化重用
                    CONTEXT.remove();
                    return null;
                });
            }

            // 等待所有虚拟线程完成
            Thread.sleep(100);
        }

        // 验证主线程上下文不受影响
        System.out.println("主线程上下文: " + CONTEXT.get());// null
    }
}