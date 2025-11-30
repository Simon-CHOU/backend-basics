package k2;


import sun.misc.Unsafe;

import java.util.function.IntUnaryOperator;



// Java21 虚拟线程友好版（减少自旋消耗）
public class MyAtomicInteger {
    private volatile int value;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long VALUE_OFFSET;

    static {
        try {
            VALUE_OFFSET = U.objectFieldOffset(
                    MyAtomicInteger.class.getDeclaredField("value"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public MyAtomicInteger(int initialValue) {
        this.value = initialValue;
    }

    // 考点 1：CAS 核心
    public boolean compareAndSet(int expected, int newValue) {
        return U.compareAndSwapInt(this, VALUE_OFFSET, expected, newValue);
    }

    // 考点 2：getAndIncrement 必须带重试
    public int getAndIncrement() {
        int current;
        do {
            current = U.getIntVolatile(this, VALUE_OFFSET); // volatile 读
        } while (!compareAndSet(current, current + 1)); // 重试直到成功
        return current;
    }

    // 考点 3：incrementAndGet 顺序不同
    public int incrementAndGet() {
        int current, newValue;
        do {
            current = U.getIntVolatile(this, VALUE_OFFSET);
            newValue = current + 1;
        } while (!compareAndSet(current, newValue));
        return newValue;
    }

    // 考点 4：支持 Lambda 的更新（Java8+）
    public int updateAndGet(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = get(); // volatile 读
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public int get() {
        return U.getIntVolatile(this, VALUE_OFFSET);
    }

}