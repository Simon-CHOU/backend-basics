import java.util.concurrent.atomic.AtomicStampedReference;

public class AbaProblemSolver {
    public static void main(String[] args) {
        String initialRef = "A";
        int initialStamp = 1;

        // 定义：(引用, 版本号)
        var atomicRef = new AtomicStampedReference<>(initialRef, initialStamp);

        // 模拟线程1：想把 A 改成 C，前提是版本号没变
        int stamp = atomicRef.getStamp(); // 拿到旧版本号 1
        String reference = atomicRef.getReference(); // 拿到旧值 A

        // 模拟中间有人搞鬼（线程2）：A -> B -> A
        atomicRef.compareAndSet(reference, "B", stamp, stamp + 1);
        atomicRef.compareAndSet("B", "A", stamp + 1, stamp + 2);

        // 线程1现在尝试修改：期望是版本 1，但现在已经是 3 了
        boolean success = atomicRef.compareAndSet(
                reference,  // 期望值 A
                "C",        // 新值 C
                stamp,      // 期望版本 1
                stamp + 1   // 新版本 2
        );

        System.out.println("修改成功了吗? " + success); // false
    }
}