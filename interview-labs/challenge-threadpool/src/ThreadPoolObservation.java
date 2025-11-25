import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class one {
    static void main() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3, 5, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10), // 等待队列容量10
                new ThreadPoolExecutor.AbortPolicy()
        );

    }
}
