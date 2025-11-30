package tongyi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalBasics {

    // 1. 声明一个ThreadLocal变量
    private static final ThreadLocal<String> USER_CONTEXT = ThreadLocal.withInitial(() -> "defaultUser");

    // Java 21新特性: 使用try-with-resources自动关闭ExecutorService
    public static void main(String[] args) {
        try (ExecutorService executor = Executors.newFixedThreadPool(3)) {
            for (int i = 0; i < 5; i++) {
                final int userId = i;
                executor.submit(() -> {
                    // 2. 为当前线程设置值
                    USER_CONTEXT.set("user" + userId);

                    // 3. 在线程内任何地方获取值
                    System.out.println(Thread.currentThread().getName() +
                            " - 初始用户: " + USER_CONTEXT.get());

                    processRequest();

                    // 4. 清理资源，防止内存泄漏
                    USER_CONTEXT.remove();
                });
            }
        } // 自动调用executor.shutdown()
    }

    private static void processRequest() {
        // 无需传递参数，直接获取当前线程的变量
        String currentUser = USER_CONTEXT.get();
        System.out.println(Thread.currentThread().getName() +
                " - 处理请求的用户: " + currentUser);

        // Java 21新特性: 使用模式匹配简化instanceof
        if (currentUser instanceof String userStr) {
            System.out.println("用户长度: " + userStr.length());
        }
    }
}