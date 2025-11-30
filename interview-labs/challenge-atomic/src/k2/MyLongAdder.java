package k2;

import java.util.concurrent.atomic.AtomicLong;

// Java21 的 LongAdder 简化版
public class MyLongAdder {
    private final Stripe[] stripes;
    private final AtomicLong base = new AtomicLong(0);

    // 动态分段
    static class Stripe extends AtomicLong {
        private static final long serialVersionUID = 1L;
    }

    public MyLongAdder() {
        // 虚拟线程：用可用处理器数 * 2
        int n = Runtime.getRuntime().availableProcessors() * 2;
        stripes = new Stripe[n];
        for (int i = 0; i < n; i++) stripes[i] = new Stripe();
    }

    public void add(long x) {
        int h = Thread.currentThread().hashCode(); // 线程哈希
        int index = (h ^ (h >>> 16)) % stripes.length; // 扰动+取模

        // 尝试更新分段
        if (!stripes[index].compareAndSet(
                stripes[index].get(), stripes[index].get() + x)) {
            // 冲突，退化为 base
            base.addAndGet(x);
        }
    }

    public long sum() {
        long sum = base.get();
        for (Stripe s : stripes) sum += s.get();
        return sum;
    }
}