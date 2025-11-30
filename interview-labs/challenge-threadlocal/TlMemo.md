# Java ThreadLocal 彻底搞懂（面试与实战备忘录，适配 JDK 21）

## 顶层路线图（先导概念与拓扑排序）

1) 线程与栈帧：操作系统调度执行单位是线程；每个线程有独立调用栈与栈帧。
2) 共享内存与竞态：同一进程内多个线程共享堆内存，读写同一对象会产生竞态条件。
3) Java 内存模型（JMM）：规定可见性、有序性与原子性；`volatile`、锁与`final`语义源于此。
4) 线程封闭（Thread Confinement）：将可变状态限制在单线程中，避免锁与竞态。
5) 上下文传递问题：跨多层调用传递“当前用户、请求ID、时间格式器”等成本高、侵入大。
6) ThreadLocal 概念：为“每个线程”提供一份独立变量副本，实现线程封闭与便捷的上下文存取。
7) ThreadLocalMap 实现：每个线程对象里放一个 Map，键是 ThreadLocal 的弱引用，值是实际对象。
8) 生命周期与泄漏风险：线程池复用时，未`remove()`会把旧值“遗留”到后续任务，形成逻辑污染与内存泄漏。
9) InheritableThreadLocal：子线程可继承父线程的值；注意线程池与复杂执行模型下的局限。
10) 虚拟线程（JDK 21）：每个虚拟线程也有自己的 ThreadLocal；与线程池复用不同，生命周期更短更安全。
11) ScopedValue（JDK 21 预览）：结构化、不可变、作用域安全的上下文传递替代方案，优于 ThreadLocal。
12) 最佳实践：`try-finally remove()`、避免存大对象、谨慎用于线程池、优先考虑 ScopedValue。

---

## 精确定义与边界（Define by Contrast & Boundary）

- ThreadLocal 是“每线程独享变量副本”的工具，不是锁，不是共享容器，不是全局变量。
- ThreadLocal 不等于：
  - 同步原语：`synchronized`/`ReentrantLock` 解决共享数据并发一致性；ThreadLocal 规避共享，不做并发控制。
  - 线程安全集合：`ConcurrentHashMap` 用于跨线程共享数据；ThreadLocal 用于不共享。
  - 全局静态变量：静态变量对所有线程共享；ThreadLocal 对每线程隔离。
  - 缓存组件：如 LRU 缓存；ThreadLocal 不提供逐出策略，不适合存大量数据长期驻留。
  - 请求上下文自动传播器：如异步链路的上下文传播；ThreadLocal 默认不跨线程传播。

核心差异与适用场景：
- 适合：为每个线程准备独立、轻量、与该线程生命周期一致的上下文或工具对象（如`DateTimeFormatter`、`DecimalFormat`、请求ID）。
- 不适合：跨线程共享与通信；长生命周期大对象；需要结构化传播的异步/并发任务链。

反事实推理（缺关键条件则不成立）：
- 若没有“每个线程持有独立 Map”的条件，ThreadLocal 将退化为普通共享变量，失去线程封闭本质。
- 若线程被线程池复用且不`remove()`，则“每请求独立上下文”不成立，旧值泄漏到新任务，业务错误。
- 若上下文需要跨线程/异步传播，ThreadLocal 的“线程边界”被跨越，语义不成立，应改用 ScopedValue 或显式传参/传播器。

---

## 第一性原理（5-Why 深度递归）

1. 为什么需要 ThreadLocal？
   因为要把可变状态“线程封闭”，既避免锁的复杂性，又降低跨层传参的成本。
2. 为什么线程封闭重要？
   因为共享可变状态会产生竞态与一致性问题；封闭后不共享，自然无锁、无竞态。
3. 为什么不直接层层传参？
   因为横切关注（如请求ID、用户上下文）贯穿多层，显式传参侵入大、易遗漏、可维护性差。
4. 为什么不把它做成静态全局？
   因为全局共享会产生并发污染，且每线程上下文语义要求隔离。
5. 为什么 ThreadLocal 要弱引用 key？
   因为 ThreadLocal 对象不再可达时，允许其被 GC；弱引用促使 Entry 成为“陈旧条目”，在后续访问中被清理，降低泄漏风险。

推到代码层的底层机制：每个 `Thread` 持有 `ThreadLocalMap`；`ThreadLocal.set/get/remove` 操作该 Map；键是弱引用，值是强引用；清理发生在访问路径上。

---

## 内部实现总览（面试高频）

UML（简化）：

```
Thread
  └─ ThreadLocalMap table[ ]
       └─ Entry: WeakReference<ThreadLocal<?>> key
                 Object value
```

关键点：
- 存储位置：Map 存在“线程对象”里，而不是 ThreadLocal 里；因此每线程一份。
- 键弱引用：`key` 弱引用保证 ThreadLocal 可回收；但 `value` 是强引用，若不 `remove()`，会因缺少键导致“值悬挂”，直到再次访问触发清理。
- 清理策略：`get/set` 时发现“key 已 GC”则清理该条目；不是周期性后台清理。
- 冲突与探测：哈希取模与线性探测解决冲突；失败后可能扩容与再散列。
- 生命周期：线程结束时，Map 随线程回收；线程池复用时，线程不结束，Map 中的值会保留。

---

## 使用 SOP（可复制粘贴的步骤）

场景 A：在普通线程中放置轻量上下文
1) 定义 `ThreadLocal<T>`，必要时用 `withInitial` 提供初始值。
2) 在进入点 `set` 值；执行业务逻辑；退出点 `finally remove`。
3) 不在对象里放大数据结构；不跨线程用。

场景 B：在线程池任务中使用
1) 每次提交任务前，在任务体内部 `set` 并 `try-finally remove`。
2) 不在池的“工作线程”上长期持有大对象；避免逻辑污染。
3) 更推荐使用 `ScopedValue` 或显式传参；Spring 用 `TaskDecorator` 做传播与清理。

场景 C：虚拟线程（JDK 21）
1) 使用 `Executors.newVirtualThreadPerTaskExecutor()` 创建执行器。
2) 在任务体中正常 `set/get/remove`；虚拟线程生命周期短，结束即回收其 ThreadLocalMap。

场景 D：结构化上下文（推荐替代 ThreadLocal）
1) 用 `ScopedValue` 声明不可变上下文。
2) 在 `ScopedValue.where(key, value).run(runnable)` 作用域内访问，自动作用域化与退出时清理。

---

## 代码范例（JDK 21，避免注释侵入）

范例 1：基础用法与 `try-finally remove`

```java
public class BasicThreadLocalDemo {
    static final ThreadLocal<String> ctx = ThreadLocal.withInitial(() -> "none");
    static String work() { return "user=" + ctx.get(); }
    public static void main(String[] args) {
        ctx.set("alice");
        try { System.out.println(work()); }
        finally { ctx.remove(); }
    }
}
```

范例 2：线程池中的污染与修复

```java
import java.util.concurrent.*;
public class PoolLeakDemo {
    static final ThreadLocal<String> ctx = new ThreadLocal<>();
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(() -> { ctx.set("reqA"); try { System.out.println(ctx.get()); } finally { ctx.remove(); } }).get();
        pool.submit(() -> { System.out.println(ctx.get()); }).get();
        pool.shutdown();
    }
}
```

范例 3：虚拟线程（JDK 21）

```java
import java.util.concurrent.*;
public class VirtualThreadDemo {
    static final ThreadLocal<Integer> local = ThreadLocal.withInitial(() -> 0);
    public static void main(String[] args) throws Exception {
        try (ExecutorService es = Executors.newVirtualThreadPerTaskExecutor()) {
            es.submit(() -> { local.set(1); try { System.out.println(local.get()); } finally { local.remove(); } }).get();
            es.submit(() -> { System.out.println(local.get()); }).get();
        }
    }
}
```

范例 4：InheritableThreadLocal

```java
public class InheritDemo {
    static final InheritableThreadLocal<String> ih = new InheritableThreadLocal<>();
    public static void main(String[] args) throws Exception {
        ih.set("parent");
        Thread t = new Thread(() -> System.out.println(ih.get()));
        t.start(); t.join();
        ih.remove();
    }
}
```

范例 5：ScopedValue（JDK 21 预览，需要启用预览）

```java
import java.lang.ScopedValue;
public class ScopedValueDemo {
    static final ScopedValue<String> USER = ScopedValue.newInstance();
    public static void main(String[] args) {
        ScopedValue.where(USER, "bob").run(() -> System.out.println(USER.get()));
    }
}
```

---

## 面试高频问答（要点速答）

- 原理是什么？每个 `Thread` 内有 `ThreadLocalMap`；`ThreadLocal` 操作该 Map；键弱引用、值强引用，访问时清理陈旧条目。
- 为什么键用弱引用？让 `ThreadLocal` 可回收，避免因键强引用导致 Map 持续持有。
- 会不会内存泄漏？会：键被 GC 后，值仍强引用悬挂，直到访问触发清理；线程池复用又延长泄漏时间，所以必须 `remove()`。
- 线程池中的风险？线程复用导致旧值残留到新任务；在任务内使用并清理，或改用 `ScopedValue`/显式传参。
- InheritableThreadLocal 有什么用？子线程默认继承父值；线程池与复杂执行器下不可依赖继承语义。
- 与虚拟线程关系？每个虚拟线程独立 ThreadLocalMap；生命周期短，泄漏风险低，但仍需 `remove()` 以确保语义清晰。
- 与 MDC/日志上下文？MDC 常用 ThreadLocal 存储上下文；异步或线程池需显式传播与清理（装饰器/拦截器）。
- ScopedValue 与 ThreadLocal 的区别？ScopedValue 不可变、作用域化、安全传播，退出自动清理；ThreadLocal 可变、手动清理、易误用。

---

## 负向定义与核心边界

- ThreadLocal 不是并发控制，不解决共享一致性问题。
- ThreadLocal 不是跨线程上下文传播器；默认不传播。
- ThreadLocal 不是长期缓存；不适合存大对象或集合。
- ThreadLocal 不是全局变量；其值对每线程隔离。

---

## 反事实场景构造（定位本质）

- 若把 ThreadLocal 换成静态变量，则不同线程读到同一份值，线程封闭消失，本质不再成立。
- 若不做 `remove()`，线程池工作线程的 Map 将保留旧值，新请求读到旧上下文，业务语义失真。
- 若需要跨线程传递上下文，ThreadLocal 无法满足；缺少“线程内封闭”的关键条件，必须用 ScopedValue 或参数传递。

---

## 最佳实践清单（含虚拟线程与预览特性）

- 始终 `try-finally remove()`；把清理当作语义的一部分。
- 不存放大对象、连接、缓冲区；避免长期驻留与 GC 压力。
- 线程池中仅在任务体内使用；不要在池的工作线程初始化时设置并长期保留。
- 对需要传播的上下文，优先考虑 `ScopedValue` 或框架提供的传播器。
- 与 Spring 集成：使用 `TaskDecorator` 在提交任务时复制/清理上下文；或改用显式参数。
- 与虚拟线程结合：更简洁，但依然遵循 `set→use→remove` 的结构化使用。

---

## 可复现实验（SOP）

前置：JDK 21；若用 ScopedValue，编译与运行需启用预览。

1) 编译并运行基础示例
```
javac --release 21 BasicThreadLocalDemo.java
java BasicThreadLocalDemo
```

2) 观察线程池污染与修复
```
javac --release 21 PoolLeakDemo.java
java PoolLeakDemo
```

3) 虚拟线程示例
```
javac --release 21 VirtualThreadDemo.java
java VirtualThreadDemo
```

4) ScopedValue（预览）
```
javac --release 21 --enable-preview ScopedValueDemo.java
java --enable-preview ScopedValueDemo
```

---

## 思维导图式小结

- 问题：跨层上下文传递难、共享状态并发风险高
- 方法：线程封闭；ThreadLocal 提供每线程副本
- 机制：Thread.threadLocalMap 弱键强值，访问时清理
- 风险：线程池复用导致值残留；必须 remove
- 替代：JDK 21 ScopedValue 作用域安全、不可变
- 实战：仅存轻量上下文；任务内 set→use→remove

---

## 附：常见陷阱速查

- 在过滤器/拦截器 `set` 后忘记 `remove`，下一个请求读到旧值。
- 在工具类静态初始化中 `set`，导致所有任务共享或难以清理。
- 存放大对象造成堆占用与长时间保留。
- 误用 InheritableThreadLocal 期望线程池自动继承。
- 与异步回调混用，回调线程不同导致读不到或读到错误上下文。

---

## 进一步阅读

- JEP 444: Virtual Threads
- JEP 447: Scoped Values（预览）
- JMM 相关资料与《Java 并发编程实战》
