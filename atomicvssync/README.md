# 并发性能测试项目：原子整型 vs 同步整型

## 项目概述

本项目基于Clean Code和TDD原则，实现了一个完整的并发性能测试框架，用于比较`AtomicInteger`和`synchronized int`在不同竞争强度和线程数下的性能表现。

## 核心问题分析

### 测试目标
- **原子整型 (AtomicInteger)**: 基于CAS(Compare-And-Swap)操作的无锁并发安全整型
- **同步整型 (synchronized int)**: 基于ReentrantLock的传统同步机制

### 测试维度
1. **线程数档位**: 1, 2, 4, 6, 8, 10, 12 (共7档)
2. **竞争强度档位**: 1-10 (共10档，数值越高竞争越激烈)
3. **性能指标**: 绝对执行时间、性能倍数比、胜出者

## 项目结构

```
├── ConcurrencyPerformanceTest.java  # 主测试类
├── visualization.html               # Three.js可视化页面
├── AvS.csv                         # 测试结果数据
└── README.md                       # 项目说明文档
```

## 核心设计原则

### 1. Clean Code实践
- **单一职责**: 每个类和方法都有明确的单一职责
- **命名清晰**: 使用有意义的变量和方法名
- **函数简洁**: 每个方法保持简短且专注
- **注释恰当**: 在关键逻辑处添加说明性注释

### 2. TDD测试驱动开发
- **测试先行**: 先定义测试场景，再实现功能
- **结果验证**: 每次测试都验证计算结果的正确性
- **多轮测试**: 通过预热和多轮平均确保结果可靠性

### 3. 架构设计模式

```
┌─────────────────────────────────────────────────────────────┐
│                    ConcurrencyPerformanceTest               │
├─────────────────────────────────────────────────────────────┤
│  + main(String[] args)                                      │
│  + runSingleTest(int, int): TestResult                      │
│  + benchmarkAtomic(int, int): long                          │
│  + benchmarkSynchronized(int, int): long                    │
│  + writeResultsToCsv(TestResult[]): void                    │
│  + printSummary(TestResult[]): void                         │
│  + analyzeResults(TestResult[]): void                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ├── AtomicCounter
                              │   ├── increment(): void
                              │   ├── getValue(): int
                              │   └── reset(): void
                              │
                              ├── SynchronizedCounter  
                              │   ├── increment(): void
                              │   ├── getValue(): int
                              │   └── reset(): void
                              │
                              └── TestResult
                                  ├── threadCount: int
                                  ├── contentionLevel: int
                                  ├── atomicTime: long
                                  ├── synchronizedTime: long
                                  ├── speedupRatio: double
                                  └── winner: String
```

## 使用方法

### 1. 编译和运行测试
```bash
# 编译
javac ConcurrencyPerformanceTest.java

# 运行测试
java -cp "c:\Users\simon\IdeaProjects\leetcode-in-action\src" com.simon.jcip.ConcurrencyPerformanceTest
```

### 2. 查看可视化结果
```bash
# 启动HTTP服务器
python -m http.server 8000

# 在浏览器中访问
http://localhost:8000/visualization.html
```

### 3. 数据文件说明
生成的`AvS.csv`文件包含以下字段：
- `ThreadCount`: 线程数
- `ContentionLevel`: 竞争强度
- `AtomicTime(ns)`: 原子整型执行时间(纳秒)
- `SynchronizedTime(ns)`: 同步整型执行时间(纳秒)
- `SpeedupRatio`: 性能倍数比
- `Winner`: 胜出者

## 测试结果分析

### 关键发现

1. **无竞争场景 (线程数=1)**:
   - 原子整型在所有竞争强度下都表现更好
   - 性能优势约为1.17x - 1.66x

2. **低竞争场景 (线程数=2-4)**:
   - 原子整型显著优于同步整型
   - 性能优势可达1.03x - 3.69x

3. **高竞争场景 (线程数=8-12)**:
   - 同步整型在大部分情况下表现更好
   - 但差距相对较小，通常在0.88x - 1.16x之间

### 性能特征分析

```
竞争强度 vs 性能表现:
                    
低竞争 ────────────► 原子整型优势明显
  │                 (CAS操作开销小)
  │                    
  ▼                    
高竞争 ────────────► 同步整型略有优势
                    (锁机制在高竞争下更稳定)
```

### 结论

1. **选择建议**:
   - **低并发场景**: 优先选择AtomicInteger
   - **高并发场景**: 根据具体测试结果选择，差距不大
   - **通用场景**: AtomicInteger是更好的默认选择

2. **技术原理**:
   - AtomicInteger基于CAS操作，在低竞争时开销更小
   - synchronized基于锁机制，在高竞争时能提供更稳定的性能
   - 实际性能差异通常在可接受范围内

## 可视化功能

### Three.js 3D图表特性
- **3D折线图**: X轴为竞争强度，Y轴为执行时间
- **多线程对比**: 不同颜色代表不同线程数
- **交互操作**: 鼠标拖拽旋转，滚轮缩放
- **对数坐标**: 支持线性和对数坐标切换
- **实时数据**: 支持上传CSV文件动态更新

### 控制面板功能
- 加载CSV数据文件
- 切换对数/线性坐标
- 重置视角
- 性能统计显示

## 技术栈

- **后端**: Java 21, JDK标准库
- **前端**: HTML5, Three.js, JavaScript ES6+
- **数据**: CSV格式
- **服务器**: Python HTTP Server

## 扩展建议

1. **测试场景扩展**:
   - 添加更多并发原语测试(如Semaphore, CountDownLatch)
   - 测试不同JVM参数的影响
   - 添加内存使用情况分析

2. **可视化增强**:
   - 添加热力图展示
   - 支持实时性能监控
   - 添加性能回归分析

3. **测试框架优化**:
   - 集成JMH(Java Microbenchmark Harness)
   - 添加统计显著性检验
   - 支持自动化CI/CD集成

## 许可证

本项目基于MIT许可证开源。