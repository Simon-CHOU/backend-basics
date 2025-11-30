package tongyi;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolThreadLocalExample {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    public static void main(String[] args) {
        // 线程池复用线程，ThreadLocal会保留上次的值
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 5; i++) {
                final int userId = i;
                executor.submit(() -> {
                    try {
                        USER_ID.set("user-" + userId);
                        System.out.println(Thread.currentThread().getName() +
                                " - 设置用户: " + USER_ID.get());
                        Thread.sleep(100); // 模拟处理
                        System.out.println(Thread.currentThread().getName() +
                                " - 仍在处理用户: " + USER_ID.get());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        USER_ID.remove(); // 必须清理!
                    }
                    return null;
                });
            }
        }
    }
}