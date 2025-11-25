import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 目标：证明StringBuilder多线程不安全，StringBuffer安全
 * 环境：至少4核CPU，Java21
 */
private static final int THREAD_COUNT = 10;
private static final int APPENDS_PER_THREAD = 1000;

void main() throws InterruptedException {
    // 实验组1：StringBuilder（会失败）
    StringBuilder unsafe = new StringBuilder();
    CountDownLatch latch1 = new CountDownLatch(THREAD_COUNT);

    for (int i = 0; i < THREAD_COUNT; i++) {
        new Thread(() -> {
            for (int j = 0; j < APPENDS_PER_THREAD; j++) {
                unsafe.append("X");
            }
            latch1.countDown();
        }).start();
    }
    latch1.await();
    System.out.println(STR."StringBuilder结果: \{unsafe.length()} / 期望: \{THREAD_COUNT * APPENDS_PER_THREAD}");
    // 结果可能 < 10000，证明数据丢失

    // 实验组2：StringBuffer（成功）
    StringBuffer safe = new StringBuffer();
    CountDownLatch latch2 = new CountDownLatch(THREAD_COUNT);

    for (int i = 0; i < THREAD_COUNT; i++) {
        new Thread(() -> {
            for (int j = 0; j < APPENDS_PER_THREAD; j++) {
                safe.append("X");
            }
            latch2.countDown();
        }).start();
    }
    latch2.await();
    System.out.println(STR."StringBuffer结果: \{safe.length()} / 期望: \{THREAD_COUNT * APPENDS_PER_THREAD}");
    // 结果一定是10000

    // 实验组3：现代方案（锁分离）
    StringBuilder modern = new StringBuilder();
    ReentrantLock lock = new ReentrantLock();
    CountDownLatch latch3 = new CountDownLatch(THREAD_COUNT);

    for (int i = 0; i < THREAD_COUNT; i++) {
        new Thread(() -> {
            for (int j = 0; j < APPENDS_PER_THREAD; j++) {
                lock.lock(); // 精确控制锁范围
                try {
                    modern.append("X");
                } finally {
                    lock.unlock();
                }
            }
            latch3.countDown();
        }).start();
    }
    latch3.await();
    System.out.println(STR."现代方案结果: \{modern.length()}");
    // 结果一定是10000，且性能优于StringBuffer
}