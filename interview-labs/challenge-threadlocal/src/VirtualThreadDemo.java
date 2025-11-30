import java.util.concurrent.*;
public class VirtualThreadDemo {
    static final ThreadLocal<Integer> local = ThreadLocal.withInitial(() -> 0);
    public static void main(String[] args) throws Exception {
        try (ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
            es.submit(() -> { local.set(1); try { System.out.println(local.get()); } finally { local.remove(); } }).get();
            es.submit(() -> { System.out.println(local.get()); }).get();
        }
    }
}
