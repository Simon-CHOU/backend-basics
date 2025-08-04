

ANTG006
说一下你对jvm内存模型的理解？

首先，线程共享的区域主要有两块：

1.
   堆（Heap） ：这是JVM内存管理中最大的一块。几乎所有的 对象实例 和 数组 都在这里分配。堆是垃圾回收（GC）发生的主要场所。为了优化GC效率，堆内部通常会被划分为 新生代 和 老年代 。作为一名资深开发者，我认为理解堆的结构和现代垃圾收集器（如G1、ZGC）的工作原理，对于进行系统性能调优、解决内存泄漏等问题至关重要。
2.
   方法区（Method Area） ：这个区域用于存储已被虚拟机加载的 类信息、常量、静态变量、以及即时编译器（JIT）编译后的代码缓存 等数据。这里有一个重要的演进：在Java 8之前，方法区通常由 永久代（PermGen） 实现；从Java 8开始，永久代被彻底移除，取而代之的是 元空间（Metaspace） 。这个改变意义重大，因为元空间使用的是 本地内存（Native Memory） ，不再受限于固定的JVM堆大小，从而极大地降低了因类加载过多而导致的 OutOfMemoryError: PermGen space 的风险。另外，值得一提的是，字符串常量池在Java 7时也从永久代移到了堆中。
其次，是每个线程独有的区域，这保证了线程执行的独立性和安全性：

1.
   程序计数器（Program Counter Register） ：这是一块非常小的内存空间，它可以看作是当前线程所执行的 字节码的行号指示器 。在多线程场景下，当一个线程被切换后，就需要依赖程序计数器来恢复到正确的执行位置。它是JVM规范中唯一一个**没有规定任何 OutOfMemoryError **的区域。
2.
   Java虚拟机栈（JVM Stack） ：每个线程在创建时都会创建一个虚拟机栈。每当一个 方法被调用 时，JVM会同步创建一个 栈帧（Stack Frame） 并压入栈中，方法执行完毕后，栈帧出栈。栈帧里存储了 局部变量表、操作数栈、动态链接和方法返回地址 等信息。我们平时遇到的 StackOverflowError ，通常就是因为无限递归或者方法调用链路太深，导致栈的深度超出了限制。
3.
   本地方法栈（Native Method Stack） ：它和虚拟机栈的作用非常相似，区别在于虚拟机栈为执行Java方法服务，而本地方法栈则为执行 本地（Native）方法 服务。在像HotSpot这样的主流虚拟机中，已经将本地方法栈和虚拟机栈合二为一了。

lab: 读jsr133。设法观测jvm各个空间的变化。
lab: 考虑业务价值：生产监控如何使用探针监控JMM状态，#Althas#Promethus#Agent#Probe

ANTG007
我们经常会遇到异常:就是内存溢出。OOM 这个应该是对哪一块的内存进行调优？

Java堆 (Heap)
java.lang.OutOfMemoryError: Java heap space

java.lang.OutOfMemoryError: GC overhead limit exceeded
  - 这个错误本质上还是和堆有关，但情况更特殊。

元空间 (Metaspace)
> 在JDK 8及以后版本，这个错误取代了旧版的 PermGen space 错误。
java.lang.OutOfMemoryError: Metaspace

JVM栈 (JVM Stacks) / 本地内存 (Native Memory)
java.lang.OutOfMemoryError: Unable to create new native thread


ANTG008
万一说线上出现这种（OOM）问题应该应该怎么样去进行应急，应急的思路是怎么样的？

处理线上OOM问题的总体思路可以清晰地划分为三个阶段： 紧急恢复、根因分析、总结预防

阶段一：紧急止损，恢复服务 (Emergency Recovery)
摘除流量，控制影响面，避免问题扩大化
dump收集证据后重启。

阶段二：离线分析，定位根因 (Root Cause Analysis)
heap.hprof 分析

阶段三：修复根治，防范未然 (Fix & Prevention)
修复后重新上线。加强监控和自动化语境。复盘-知识沉淀和分享。

> lab: 流量摘除：新发布的oom可以立刻回滚。如果发布有一段时间了，是否只能直接重启？
> lab: 恢复服务时，内存中在途的请求、没执行完的任务，如何无损恢复？oom应用卡死，还能优雅启停吗？（业务量小无所谓，如果业务量大，中间态的请求应该不少）#CleanArchitecture
> 考虑 x 分钟 - m级事务 设计和业务价值。 业界有哪些OOM事故响应评级的通行标准？