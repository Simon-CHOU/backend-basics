package tongyi;

public class MemoryLeakExample {
    // 问题: 如果不调用remove()，即使线程结束，ThreadLocalMap中的Entry也不会被GC
    private static final ThreadLocal<byte[]> LARGE_DATA = ThreadLocal.withInitial(() -> new byte[1024*1024]);

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                LARGE_DATA.get(); // 初始化大对象
                // 缺少 LARGE_DATA.remove()
            }).start();
        }

        // 等待所有线程完成
        Thread.sleep(1000);
        System.gc(); // 尝试GC，但内存不会完全释放

        // 检查内存使用情况
        Runtime runtime = Runtime.getRuntime();
        System.out.println("已用内存: " + (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + "MB");
    }
}