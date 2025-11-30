/**
 * 本包包含ThreadLocal相关的核心示例代码，从基础用法到高级技巧，再到常见陷阱及解决方案。
 * 所有示例均针对Java Senior SDE面试准备，特别强调Java 21新特性的应用。
 *
 * <h2>包内类详细说明</h2>
 *
 * <h3>{@link tongyi.ThreadLocalBasics}</h3>
 * <p>ThreadLocal基础用法演示。展示了如何声明、设置、获取和清理ThreadLocal变量。
 * 使用Java 21的try-with-resources自动关闭ExecutorService，并演示了在线程内跨方法传递上下文的场景。
 * 核心价值：理解ThreadLocal的基本API和典型应用场景。</p>
 *
 * <h3>{@link tongyi.InheritableThreadLocalExample}</h3>
 * <p>InheritableThreadLocal用法演示。展示子线程如何继承父线程的ThreadLocal值，
 * 以及子线程如何覆盖继承的值而不影响父线程。核心价值：理解父子线程间上下文传递机制，
 * 适用于请求链路追踪等场景。</p>
 *
 * <h3>{@link tongyi.VirtualThreadLocalExample}</h3>
 * <p>Java 21虚拟线程与ThreadLocal结合使用演示。使用虚拟线程池(Executor.newVirtualThreadPerTaskExecutor)，
 * 展示在高并发虚拟线程环境下ThreadLocal的行为特性。核心价值：掌握现代Java并发模型下ThreadLocal的安全使用，
 * 理解虚拟线程与平台线程在ThreadLocal处理上的异同。</p>
 *
 * <h3>{@link tongyi.MemoryLeakExample}</h3>
 * <p>ThreadLocal内存泄漏问题复现与分析。通过创建大量线程并设置大对象到ThreadLocal但不清理，
 * 演示典型的内存泄漏场景。核心价值：深刻理解ThreadLocal内存泄漏的根本原因，
 * 以及如何通过合理的资源管理避免此类问题。</p>
 *
 * <h3>{@link tongyi.ThreadPoolThreadLocalExample}</h3>
 * <p>线程池环境下ThreadLocal使用注意事项演示。展示在固定线程池中复用线程时，
 * ThreadLocal值可能从上一个任务"泄露"到下一个任务的问题。核心价值：掌握在Web容器、
 * 应用服务器等使用线程池的环境中安全使用ThreadLocal的最佳实践。</p>
 *
 * <h3>{@link tongyi.SafeThreadLocalUsage}</h3>
 * <p>ThreadLocal安全使用模式示例。展示在try-finally块中使用ThreadLocal的标准模式，
 * 确保即使发生异常也能正确清理资源。使用SimpleDateFormat作为典型非线程安全对象的包装案例。
 * 核心价值：掌握企业级应用中ThreadLocal的防御式编程技巧。</p>
 *
 * <h3>{@link tongyi.AutoCloseableThreadLocal}</h3>
 * <p>结合Java 21新特性，通过实现AutoCloseable接口使ThreadLocal支持try-with-resources语法。
 * 提供现代化的资源管理方式，减少手动调用remove()的疏忽风险。核心价值：将传统API与现代Java
 * 语言特性结合，提升代码可读性和安全性。</p>
 *
 * <h2>学习路径建议</h2>
 * <ol>
 *   <li>先运行并理解{@link tongyi.ThreadLocalBasics}建立基础认知</li>
 *   <li>学习{@link tongyi.InheritableThreadLocalExample}掌握父子线程传递</li>
 *   <li>通过{@link tongyi.MemoryLeakExample}和{@link tongyi.ThreadPoolThreadLocalExample}理解陷阱</li>
 *   <li>掌握{@link tongyi.SafeThreadLocalUsage}和{@link tongyi.AutoCloseableThreadLocal}的安全模式</li>
 *   <li>最后学习{@link tongyi.VirtualThreadLocalExample}了解现代Java并发</li>
 * </ol>
 *
 * <h2>重要警告</h2>
 * <p>所有涉及线程池的示例（特别是Web应用环境），必须严格遵守"set-use-remove"原则，
 * 否则将导致严重的内存泄漏和数据污染问题。在生产代码中，建议优先考虑使用
 * {@link tongyi.AutoCloseableThreadLocal}等自动资源管理方案。</p>
 *
 * @author Qwen
 * @since Java 21
 * @see java.lang.ThreadLocal
 * @see java.lang.InheritableThreadLocal
 * @see java.util.concurrent.ExecutorService
 */
package tongyi;