void main() throws InterruptedException {
    // 测试1：StringBuilder多线程（会出错）
    StringBuilder builder = new StringBuilder();
    Runnable task1 = () -> {
        for (int i = 0; i < 1000; i++) builder.append("A");
    };

    Thread t1 = new Thread(task1);
    Thread t2 = new Thread(task1);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
    System.out.println("StringBuilder长度: " + builder.length());
    // 结果可能 < 2000，因为数据丢失

    // 测试2：StringBuffer多线程（安全但慢）
    StringBuffer buffer = new StringBuffer();
    Runnable task2 = () -> {
        for (int i = 0; i < 1000; i++) buffer.append("A");
    };

    Thread t3 = new Thread(task2);
    Thread t4 = new Thread(task2);
    t3.start();
    t4.start();
    t3.join();
    t4.join();
    System.out.println("StringBuffer长度: " + buffer.length());
    // 结果一定是2000，但耗时是单线程的3-5倍


//    StringBuilder.append()耗时: ~5纳秒
//    StringBuffer.append()耗时: ~50纳秒
// 10倍差距来自：
// 1. 获取锁（10ns）
// 2. 释放锁（10ns）
// 3. 内存屏障（防止指令重排序）（20ns）
// 4. 操作系统调度开销（偶尔发生）


    // 注意：Java21中StringBuffer几乎被废弃！
// 99%场景应该用StringBuilder + 外部锁

// 不推荐：
    StringBuffer oldStyle = new StringBuffer();

// 推荐（单线程）：
    StringBuilder modern = new StringBuilder();

// 推荐（多线程，精确控制锁范围）：
    StringBuilder withExternalLock = new StringBuilder();
    synchronized (withExternalLock) { // 只在需要时锁
        withExternalLock.append("Critical");
    }

// Java21虚拟线程场景：
// 虚拟线程很轻量，StringBuffer的锁可能导致"_PINNED"问题
// 应该用：java.util.concurrent.locks.ReentrantLock
}