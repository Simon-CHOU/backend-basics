package k2;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AtomicProblem {
    private static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();

        // 1000 个线程，每个加 1000 次
        for (int i = 0; i < 1000; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter++; // 非原子操作！
                }
            }));
        }

        // Java21: 虚拟线程更高效地展示问题
        var vThreads = Thread.ofVirtual().name("worker-", 0).factory();
        List<Thread> virtualThreads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            virtualThreads.add(vThreads.newThread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter++;
                }
            }));
        }

        virtualThreads.forEach(Thread::start);
        for (var vt : virtualThreads) vt.join();

        System.out.println("Expected: 1000000");
        System.out.println("Actual:   " + counter);
        System.out.println("Lost:     " + (1000000 - counter));
    }
}
