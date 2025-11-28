# Java GC 与内存区域：给“自认愚钝”的博士生的深度指南

> **目标读者:** 自称“智力低下”的博士研究生。
> **目标:** 零门槛、从第一性原理出发，彻底讲透 JVM 内存、GC 和 OOM，并结合 Java 21 实战。

---

## 🏗️ 第一阶段：建立心理模型 (Mental Model)

在讨论复杂的“GC”或“OOM”之前，我们需要先建立一个绝对稳固的认知地基。我们使用**“超级工厂”**作为类比。

### 1.1 核心组件 (Building Blocks)

1.  **JVM 进程 (工厂厂房):** 操作系统分配给 Java 的有限土地资源（内存）。
2.  **线程 (Thread = 工人):** 负责干活的实体。每个工人都有自己专属的**操作台**。
3.  **堆内存 (Heap = 公共大仓库):** 所有工人共享的区域。生产出来的**对象 (Object)** 都堆放在这里。
4.  **栈内存 (Stack = 专属操作台):** 工人手边的私有区域，放着当前正在处理的工具和临时数据。别人不能碰。
5.  **垃圾回收 (GC = 清洁工):** 一个在后台默默工作的团队，负责把仓库里没人用的破烂清理掉。

### 1.2 知识拓扑 (先学什么，后学什么)
*   **先懂 Heap (仓库)**，才能懂 **GC (清洁)** —— 只有仓库满了才需要清洁。
*   **先懂 Stack Frame (操作台上的任务单)**，才能懂 **StackOverflow** —— 任务单堆太高就倒了。
*   **先懂 STW (全厂停工)**，才能懂 **CMS** 的价值 —— 它是为了减少停工时间而生的。

---

## 🧠 第二阶段：JVM 内存区域详解 (地形图)

### 2.1 区域地图与 GC/OOM 概览

| 内存区域 (Area) | 类型 | 类比 | 会发生 GC 吗？ | 会发生 OOM 吗？ |
| :--- | :--- | :--- | :--- | :--- |
| **程序计数器 (PC Register)** | 私有 | 工人的“当前步骤记录本” | ❌ **不会** | ❌ **绝对不会** |
| **虚拟机栈 (JVM Stack)** | 私有 | 工人的操作台 (Java方法) | ❌ 不会 (只有入栈出栈) | ✅ 会 (StackOverflow / OOM) |
| **本地方法栈 (Native Stack)** | 私有 | C++ 调用的操作台 | ❌ 不会 | ✅ 会 |
| **堆 (Heap)** | **共享** | **公共大仓库** | ✅ **GC 主战场** | ✅ **会 (最常见)** |
| **方法区 (Method Area/Metaspace)** | **共享** | **图纸资料室** | ✅ 会 (较少见) | ✅ 会 (Metaspace OOM) |

---

### 2.2 深度递归：为什么 PC 计数器不会 OOM？(Deep Dive)

*   **现象:** 程序计数器 (Program Counter Register) 是 JVM 中唯一一个在规范中**没有规定任何 OutOfMemoryError 情况**的区域。
*   **5-Why 深度挖掘:**
    1.  **它是干嘛的？** 它存储当前线程执行到了哪一行字节码指令的**地址**。
    2.  **为什么不耗内存？** 它只需要存储一个**固定长度**的数值（Native Pointer，比如 64位系统中就是 64bit）。
    3.  **为什么不膨胀？** 无论程序多复杂，"当前指令地址"永远只是一个数字。它不会像 List 一样不断 add 元素，也不会像 Stack 一样不断递归变深。
    4.  **第一性原理 (物理层):** 在 CPU 硬件层面，PC 是一个寄存器 (Register)。在 JVM 软件层面，它只是一个极其微小的、固定大小的变量。
    5.  **结论:** **因为它不需要动态分配内存，大小恒定，所以物理上不可能 OOM。**

### 2.3 虚拟机栈：StackOverflow vs OOM

*   **StackOverflowError:**
    *   **场景:** 递归太深，或者方法调用链太长。
    *   **原理:** 每个方法调用都会在栈上放一个“栈帧” (Stack Frame)。如果栈的深度（比如允许放1000个框）满了，你还要放，就溢出了。
*   **OutOfMemoryError (在栈中):**
    *   **场景:** 线程开太多了。
    *   **原理:** 既然每个线程都要一个栈（比如 1MB），如果内存不够创建新的线程，或者无法扩展栈的大小，就会报 OOM。
    *   *注意:* 在 HotSpot 虚拟机中，栈容量通常是固定的，所以这里的 OOM 几乎都是因为“线程数过多”导致的。

### 2.4 堆 (Heap) 与 方法区 (Method Area)

*   **堆 (Heap):**
    *   `new Object()` 出来的内容全在这。
    *   **OOM:** `java.lang.OutOfMemoryError: Java heap space` —— 仓库塞满了，清洁工（GC）拼命打扫也腾不出地儿。
*   **方法区 (元空间 Metaspace):**
    *   存放 **类 (Class)** 的结构信息、常量、静态变量。
    *   **OOM:** `java.lang.OutOfMemoryError: Metaspace` —— 加载了太多的类（比如 Spring 动态代理生成了无数个动态类），把存放图纸的房间塞满了。

---

## 🗑️ 第三阶段：GC 机制与时机 (Garbage Collection)

### 3.1 新生代 (Young Gen) 什么时候回收？

*   **核心假设:** **弱分代假说 (Weak Generational Hypothesis)** —— “绝大多数对象都是朝生夕死的”。
*   **触发时机:** 当新生代中的 **Eden 区 (伊甸园)** 被新对象填满时。
*   **执行动作:** **Minor GC (Young GC)**。
*   **回收机制:** **复制算法 (Replication)**。
    1.  把 Eden 区和 Survivor From 区里还**活着**的少数对象，全部**复制**到 Survivor To 区。
    2.  把 Eden 和 From 区直接清空（全部算作垃圾）。
    3.  交换 From 和 To 的身份。

### 3.2 Full GC (全量回收) 在哪执行？什么时候触发？

*   **执行区域:** **整个堆 (Young + Old)** 加上 **方法区 (Metaspace)**。是一次全厂大扫除。
*   **触发条件 (Trigger):**
    1.  **老年代 (Old Gen) 空间不足:** Young GC 后有对象要晋升到老年代，但老年代放不下了。
    2.  **元空间 (Metaspace) 空间不足:** 类加载太多，触发清理。
    3.  **System.gc():** 代码里显式调用（虽然现代框架通常会忽略或禁用它）。
    4.  **CMS 的并发失败 (Concurrent Mode Failure):** 也就是清洁工还没扫完，垃圾产生太快把仓库堵死了。

---

## ⚡ 第四阶段：CMS (Concurrent Mark Sweep) 工作机制深度解析

> **⚠️ 前方高能预警:** CMS 在 Java 9 废弃，Java 14 彻底移除。既然你用 Java 21，默认是 G1 垃圾收集器。但为了回答你的问题，我们将 CMS 作为经典教材来解剖。

### 4.1 核心目标
**最小化 STW (Stop-The-World)**。STW 意味着所有 Java 线程暂停，只有 GC 线程在跑（全厂停工）。

### 4.2 CMS 的四个阶段 (SOP)

1.  **初始标记 (Initial Mark) 🛑 [STW]:**
    *   **动作:** 仅仅标记一下 **GC Roots** 能**直接**关联到的对象。
    *   **特点:** 速度极快。
2.  **并发标记 (Concurrent Mark) 🏃 [并发]:**
    *   **动作:** 从第一步标记的对象出发，顺藤摸瓜遍历整个对象图。
    *   **特点:** 耗时最长，但**不需要停顿**，GC 线程和用户线程一起跑。
    *   *问题:* 用户线程还在跑，可能会修改引用关系，导致标记不准（产生“浮动垃圾”或“漏标”）。
3.  **重新标记 (Remark) 🛑 [STW]:**
    *   **动作:** 修正并发标记期间因为用户程序变动而导致的那一部分标记记录。
    *   **原理:** 利用**写屏障 (Write Barrier)** 技术记录下变化。
    *   **特点:** 比第一步慢点，但远比第二步快。
4.  **并发清除 (Concurrent Sweep) 🏃 [并发]:**
    *   **动作:** 清理掉未被标记的死对象。

### 4.3 第一性原理：三色标记法 (Tri-color Marking)
CMS 如何在不暂停程序的情况下标记垃圾？

*   **⚪ 白色 (White):** 还没访问过（可能是垃圾）。
*   **⚫ 黑色 (Black):** 自己和子节点都访问过了（肯定是活的）。
*   **🔘 灰色 (Grey):** 自己访问过了，但子节点还没访问完（中间状态）。

**CMS 的挑战 (漏标问题):**
如果用户线程在并发标记期间，把一个**白色**对象的引用，从**灰色**对象移到了**黑色**对象下面：
1.  灰色对象已经扫描完了，不会再看那个白色对象。
2.  黑色对象认为自己已经扫描结束了，也不会再回头看。
3.  **结果:** 那个白色对象虽然还有人用，但会被当成垃圾清除掉！💥

**CMS 的解法:** **增量更新 (Incremental Update)**。
一旦黑色对象指向了白色对象，就把这个黑色对象**变回灰色**，或者记录下来，在“重新标记”阶段再查一遍。

---

## 🧪 第五阶段：实证与复现 SOP (Java 21)

### 5.1 堆内存溢出 (Heap OOM)
**SOP:**
1.  参数: `-Xms20m -Xmx20m` (限制堆大小为 20MB)
2.  代码: 不断创建大对象并放入 List 防止被回收。

```java
import java.util.ArrayList;
import java.util.List;

public class HeapOOM {
    static class HeavyObject {
        // 1MB 的字节数组
        byte[] data = new byte[1024 * 1024];
    }

    public static void main(String[] args) {
        List<HeavyObject> list = new ArrayList<>();
        while (true) {
            list.add(new HeavyObject());
            // 预期结果: java.lang.OutOfMemoryError: Java heap space
        }
    }
}
```

### 5.2 栈溢出 (StackOverflow)
**SOP:**
1.  参数: `-Xss128k` (限制栈大小为 128k)
2.  代码: 无限递归。

```java
public class StackSOF {
    private int stackLength = 1;

    public void stackLeak() {
        stackLength++;
        stackLeak(); // 递归调用，不断压栈
    }

    public static void main(String[] args) {
        StackSOF sof = new StackSOF();
        try {
            sof.stackLeak();
        } catch (Throwable e) {
            System.out.println("栈深度: " + sof.stackLength);
            throw e; // 预期结果: java.lang.StackOverflowError
        }
    }
}
```

### 5.3 观察 GC 日志 (Java 21)
Java 9 之后，GC 日志参数变了。不要再用 `-XX:+PrintGCDetails`。

**命令:**
```bash
# 运行 HeapOOM 并输出详细日志到 gc.log
java -Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=5,filesize=10m -Xms20m -Xmx20m HeapOOM
```
*   **-Xlog:gc*:** 打印所有 GC 相关的标签。
*   在日志中你会看到 `Pause Young (Allocation Failure)` (Minor GC) 和 `Pause Full (Allocation Failure)` (Full GC)。

---

## 🏁 总结 (给博士生的备忘录)

1.  **程序计数器 (PC):** **唯一**不会 OOM 的净土。
2.  **Full GC:** 发生在 **堆(Heap) + 方法区(Metaspace)**。
3.  **新生代回收:** **Eden** 满时触发，用**复制算法**。
4.  **CMS:** 为了**低延迟**而生，核心是**三色标记**和**并发处理**，但代价是CPU负载高和内存碎片。
5.  **Java 21:** 时代变了，CMS 已死，G1 当立。实战中请重点关注 G1 的 `Region` 概念。

