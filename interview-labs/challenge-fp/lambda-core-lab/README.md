# Lambda Core Lab

## 实验目标
验证Lambda表达式的编译机制，理解`invokedynamic`字节码指令的工作原理

## 验收标准(AC)

### AC1: Lambda编译验证
- [ ] 验证Lambda表达式编译后生成`invokedynamic`指令
- [ ] 对比Lambda与匿名内部类的字节码差异
- [ ] 确认Lambda不生成额外的.class文件

### AC2: 性能对比
- [ ] 测量Lambda与匿名内部类的内存占用差异
- [ ] 测试初始化性能差异
- [ ] 验证JIT优化效果

### AC3: 方法句柄验证
- [ ] 理解MethodHandle机制
- [ ] 验证CallSite的缓存机制

## 实验步骤

1. 编写Lambda表达式示例代码
2. 使用javap分析字节码
3. 编写性能测试用例
4. 验证理论结论