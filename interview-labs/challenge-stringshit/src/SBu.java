import java.io.IOException;

void main() throws IOException {
    // 问题：String慢得像乌龟
    long start = System.nanoTime();
    String slow = "";
    for (int i = 0; i < 100; i++) {
        slow += i; // 创建100个新对象，复制100次
    }
    long stringTime = System.nanoTime() - start;

    // 100次循环 ≈ 10ms，内存占用爆炸

    // 解决方案：StringBuilder像火箭
    start = System.nanoTime();
    StringBuilder fast = new StringBuilder();
    for (int i = 0; i < 100; i++) {
        fast.append(i); // 只用一个对象，不复制
    }
    long stringBuilderTime = System.nanoTime() - start;

    // 100次循环 ≈ 0.01ms，快1000倍

    System.out.println("String time: " + stringTime + " ns");
    System.out.println("StringBuilder time: " + stringBuilderTime + " ns");
    System.out.println("StringBuilder is " + (stringTime / stringBuilderTime) + " times faster");


    // #ensureCapacity  #append


    // 特性：紧凑字符串传播
// StringBuilder在Java 9+同样使用byte[]而不是char[]
// 对纯ASCII内容节省50%内存

// 最佳实践：预分配容量避免扩容
    StringBuilder efficient = new StringBuilder(100_000);
// 如果你知道大概长度，指定容量性能最佳
// 避免100_000次循环中的10次扩容

// Java21中StringBuilder实现了哪些接口？
// Appendable, CharSequence, Serializable
// 这意味着你可以：
    Appendable appender = new StringBuilder();
    appender.append("type safe"); // 面向接口编程
}