
++coutn

MyObject
MyRunnable


一个 runnable，两个thread

2个 runnable，两个thread

share 
filed can be share
pass through arg constructor
both referencing the same myobject on heap.


share some date
cause 
race conditions
two thread- share date-
both read&write
and they do not do so in synchronized fashion.

1 update to 2 
and write back

has note been written back to main memory
so it is not yet visible to the thread

- so this is a update visibility 
solve: volatile synchronized block




======cache====

cpu wirte main-main
write to cahche .


=====
count 10000

share below 20000
===


全文就讲了，
线程-线程私有
Heap 公有

count，因为线程不可见，带来的竞争。
synchronized(this){
    count++
}


java中，哪里在 heap，哪里在 stack。



所谓“共享”是指“是否在线程之间共享”。

> 我正在根据代码和注释中记录的运行结果，整理出本地变量是否会在线程共享的规律。
> 请你根据代码中的例子，
> 绘制一个判断变量是否在线程中共享的速查表。 
> 表头可以是：Runnable 是否共享 ，myobject是否共享等等。 
> 我希望能帮助我理解 Java Memory Model 中线程是否共享，
> 以及字段在JVM中，究竟是在线程内的stack上还是 heap中存储。

多个引用可以指向不同的对象、或者同一个对象

【深入浅出Java内存模型】 【精准空降到 03:02】 https://www.bilibili.com/video/BV1WMjzzuEGG/?t=182
每个线程都有自己的runnable 对象
local  variable 是线程私有的。在线程栈上创建。
！局部变量绝不会在线程间共享。
vs
只创1个runnable 对象，创建2个不同线程。
i 有2份，count 字段只有1份。
i 在线程栈上，count 在线程堆上。
两个线程访问同一个count 字段。原因是heap上只有1个myrunnable 对象。
count这个字段在线程间共享。
vs
在 runnable run方法中创建本地的 myobject对象。不共享，每个线程都创建不同的myobject对象。
即便共享同一个runnable对象, myobject 也是不同的。

vs
改写contsturctor，把myobject作为类成员，就可以在线程之间共享了。
打印object hashcode 就可以看到，是同一个对象。

jvm vs cpu
cpu thread- cpu reigister -l cache -main memory
竞态条件
1
+1  +1
w 2   w2
2
本来是3，结果是2。

线程可见性


====示例。才开始演示 count==
2个runnable，两个thread
刚好加出来。
一个runnable，两个thread
无法精确计数。

sycnrhonize(this) {
count++
}


总结：
实验的不变量: 2thread
实验的变量 : 1 or 2 runnable, myobject/count is local variable or field.
观察手段 ： print myobj hashcode, print count.
其中， constructor 其实只是因为 obj filed 需要赋值 。