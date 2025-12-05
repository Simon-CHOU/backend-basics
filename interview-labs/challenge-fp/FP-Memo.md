# Java函数式编程面试指南

## 核心概念总览

```
Java函数式编程知识体系
├── Lambda表达式 (语法糖层面)
│   ├── 编译机制 (invokedynamic)
│   ├── 类型推断
│   └── 方法引用
├── 纯函数 (核心思想)
│   ├── 无副作用
│   ├── 引用透明性
│   └── 幂等性
├── 函数式接口 (类型系统)
│   ├── @FunctionalInterface
│   ├── 四大核心接口
│   └── 自定义接口
└── Stream API (应用层面)
    ├── 中间操作
    ├── 终止操作
    └── 并行处理
```

## 1. Lambda表达式编译机制

### 1.1 核心答案

**Lambda表达式在编译后生成的是使用`invokedynamic`字节码指令的实现**，而不是传统的匿名内部类。这是Java 7引入的字节码指令，专门用于支持动态语言特性。

### 1.2 技术细节

#### 编译过程分解：
```java
// 源代码
List<String> list = Arrays.asList("a", "b", "c");
list.forEach(s -> System.out.println(s));

// 编译后等效代码（概念性）
list.forEach([动态生成的MethodHandle]);
```

#### invokedynamic工作原理：
1. **首次调用**：JVM调用bootstrap方法生成CallSite
2. **方法句柄**：生成指向Lambda实现的方法句柄(MethodHandle)
3. **后续调用**：直接使用缓存的CallSite，避免重复生成

### 1.3 性能优势

| 特性 | 匿名内部类 | Lambda表达式 |
|------|------------|-------------|
| 类加载 | 每次创建新.class文件 | 运行时动态生成 |
| 内存占用 | 每个实例独立类 | 共享实现代码 |
| 初始化成本 | 较高 | 较低 |
| JIT优化 | 受限 | 更好的内联优化 |

### 1.4 验证方法

```bash
# 查看字节码
javap -c -p YourClass.class

# 使用jclasslib等工具分析invokedynamic指令
```

## 2. 纯函数(Pure Function)

### 2.1 核心定义

**纯函数**是指满足以下两个条件的函数：
1. **无副作用**：不修改任何外部状态
2. **引用透明**：相同输入总是产生相同输出

### 2.2 关键特性

#### 2.2.1 无副作用(No Side Effects)
- 不修改传入的参数
- 不修改类的字段
- 不进行I/O操作
- 不抛出受检异常（除非重新抛出）

#### 2.2.2 引用透明(Referential Transparency)
- 函数调用可以被其返回值替换
- 支持等式推理(Equational Reasoning)
- 便于缓存和记忆化(memoization)

### 2.3 代码示例

#### 纯函数示例：
```java
// 纯函数 - 符合所有条件
public static int add(int a, int b) {
    return a + b;
}

// 纯函数 - 使用不可变对象
public static String concatenate(String s1, String s2) {
    return s1 + s2;
}
```

#### 非纯函数示例：
```java
// 非纯函数 - 有副作用（修改外部状态）
private int counter = 0;
public int increment() {
    return counter++; // 副作用：修改实例变量
}

// 非纯函数 - 依赖外部状态
public double calculateTax(double amount) {
    return amount * taxRate; // 依赖外部变量taxRate
}

// 非纯函数 - I/O操作
public String readFile() {
    return Files.readString(Path.of("data.txt")); // I/O操作
}
```

### 2.4 纯函数的优势

1. **可测试性**：无需mock外部依赖
2. **可缓存性**：相同输入可缓存结果
3. **并行安全**：无竞态条件
4. **推理简单**：便于理解和维护
5. **组合性**：易于构建复杂操作

### 2.5 实际应用场景

```java
// 数据处理管道中的纯函数
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// 纯函数组合
List<Integer> result = numbers.stream()
    .filter(n -> n % 2 == 0)        // 纯函数
    .map(n -> n * 2)                 // 纯函数
    .sorted()                        // 纯函数（基于比较器）
    .collect(Collectors.toList());
```

## 3. 面试回答策略

### 3.1 Lambda表达式相关问题

**面试官**："Lambda表达式编译出来之后是什么东西？"

**推荐回答结构**：
1. **直接答案**：使用invokedynamic字节码指令
2. **技术细节**：解释动态方法句柄和CallSite机制
3. **性能对比**：与匿名内部类的区别
4. **实际意义**：为什么这种设计更好

### 3.2 纯函数相关问题

**面试官**："介绍一下pure function"

**推荐回答结构**：
1. **核心定义**：无副作用 + 引用透明
2. **具体特征**：详细解释两个条件
3. **代码示例**：正反例对比
4. **实际价值**：为什么在函数式编程中重要
5. **应用场景**：在Stream API中的体现

## 4. 高级话题准备

### 4.1 函数式编程原则
- **不可变性**：使用final和不可变集合
- **高阶函数**：函数作为参数或返回值
- **函数组合**：andThen、compose方法
- **惰性求值**：Stream的中间操作

### 4.2 常见陷阱
- ** effectively final**要求
- 并行流中的线程安全问题
- 异常处理策略
- 性能考虑（装箱/拆箱成本）

## 5. 实战代码示例

```java
// 完整的函数式编程示例
public class FunctionalExample {
    
    // 纯函数：计算平方
    public static Function<Integer, Integer> square = x -> x * x;
    
    // 纯函数：判断偶数
    public static Predicate<Integer> isEven = x -> x % 2 == 0;
    
    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        // 函数式数据处理管道
        List<Integer> result = numbers.stream()
            .filter(isEven)               // 过滤偶数
            .map(square)                  // 计算平方
            .collect(Collectors.toList());
            
        System.out.println(result); // [4, 16, 36, 64, 100]
    }
}
```

## 总结

掌握Java函数式编程的关键在于理解：
1. **Lambda不仅是语法糖**，而是基于invokedynamic的运行时优化
2. **纯函数是函数式编程的基石**，强调无副作用和引用透明
3. **实践重于理论**，在Stream API中熟练应用这些概念
4. **理解底层机制**，才能写出高效且正确的函数式代码

这份指南涵盖了Senior Java面试中函数式编程的核心考点，建议结合实际编码练习来深化理解。