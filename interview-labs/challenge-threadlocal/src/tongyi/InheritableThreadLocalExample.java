package tongyi;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class InheritableThreadLocalExample {

    // 正确初始化InheritableThreadLocal的方式
    private static final InheritableThreadLocal<String> TRANSACTION_ID = new InheritableThreadLocal<>() {
        @Override
        protected String initialValue() {
            return "root-" + System.currentTimeMillis();
        }
    };

    public static void main(String[] args) throws InterruptedException {
        TRANSACTION_ID.set("main-transaction");

        System.out.println("主线程ID: " + TRANSACTION_ID.get());

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.submit(() -> {
                System.out.println("子线程ID(未修改): " + TRANSACTION_ID.get());
                // 子线程可以覆盖继承的值
                TRANSACTION_ID.set("child-transaction");
                System.out.println("子线程ID(修改后): " + TRANSACTION_ID.get());
                return null;
            });

            // 等待子线程完成
            Thread.sleep(100);
            System.out.println("主线程ID(子线程执行后): " + TRANSACTION_ID.get());
        }
    }
}