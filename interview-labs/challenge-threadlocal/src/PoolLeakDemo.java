import java.util.concurrent.*;
public class PoolLeakDemo {
    static final ThreadLocal<String> ctx = new ThreadLocal<>();
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(() -> { ctx.set("reqA"); try { System.out.println(ctx.get()); } finally { ctx.remove(); } }).get();
        pool.submit(() -> { System.out.println(ctx.get()); }).get();
        pool.shutdown();
    }
}
