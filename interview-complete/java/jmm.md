

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



JITU003
OOM（Out of Memory）是由哪些问题引起的？


"OOM主要分为三大类原因。

第一类是 堆内存问题 ，这是最常见的。包括内存泄漏，比如集合类没有及时清理、监听器没有注销、ThreadLocal没有remove；还有内存溢出，比如一次性加载大文件或者创建大数组。

第二类是 非堆内存问题 。在JDK8以后主要是Metaspace溢出，通常是因为动态类加载过多，比如大量使用反射、CGLib或者频繁热部署导致的。

第三类是 系统级内存问题 ，比如线程数超限导致的'Unable to create new native thread'，或者Direct Memory泄漏。

从业务角度看，根本原因往往是算法效率低、数据结构选择不当、缓存策略失效，或者JVM参数配置不合理。

我在实际项目中遇到过因为HashMap在高并发下链表过长导致的OOM，通过改用ConcurrentHashMap和优化查询算法解决的。"

> lab: 手工把上面的问题都复现一遍。
> lab: 复盘枚举业务场景下的OOM



JDON016
你是如何基于dump分析了jvm的内存文件。你是如何通过dump文件发现原始的oom问题的？

初步分析 - 宏观概览
获取内存概览

- 查看Histogram视图，按对象数量和内存占用排序
- 分析Dominator Tree找出占用内存最多的对象
- 检查Leak Suspects报告中的可疑内存泄漏点

定向分析 - 深入挖掘

大对象分析：OQL查询大对象，检查集合类对象异常增长，分析字符串长常量池

引用链分析：选择可疑对象，查看Path to GC Roots，分析对象引用了谁，被谁引用

线程分析：检查线程状态和线程局部变量，分析线程栈与大对象创建的关联、检查线程池配置与内存使用的关系

根因定位- 模式识别
memory sleak
memory overflow
metaspace overflow

高级分析- 对比与验证
多时间点对比
假设验证：提出假设，结合代码审查



JDON017
你刚才讲到了垃圾回收，能不能说一下java cms 垃圾回收的工作机制是什么？

CMS是JVM中一种老年代垃圾回收算法，属于标记-清除（Mark-Sweep）类型。它设计的目标是降低垃圾回收时的停顿时间（STW，Stop-The-World），通过让垃圾回收线程与应用线程并发执行来实现。不同于其他回收器，CMS在标记阶段大部分时间是并发的，只有少量STW阶段。

> lab: 除了cms还有哪些GC？如何选型？如何配置？如何benck回收性能？
> 从java 8 11 17 21 24 演化过来， Java CMS 垃圾回收机制有哪些关键变化？


JDON018
能不能讲一下cms的重标记过程？
暂停应用线程（STW Pause）
扫描GC Roots
处理写屏障记录（Card Table Processing）
重新遍历修改的对象

presudo cms
算法步骤：

1。
   初始化 ：暂停所有应用线程
2。
   根扫描 ：重新扫描所有GC Roots
3。
   卡表处理 ：处理写屏障记录的dirty cards
4。
   对象遍历 ：对修改过的对象进行深度遍历
5。
   标记修正 ：更新对象的标记状态
6。
   完成 ：恢复应用。