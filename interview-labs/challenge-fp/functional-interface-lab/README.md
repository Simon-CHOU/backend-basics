# Functional Interface Lab

## 实验目标
验证函数式接口的核心概念，包括@FunctionalInterface注解和四大核心接口

## 验收标准(AC)

### AC1: @FunctionalInterface验证
- [ ] 验证注解的编译时检查功能
- [ ] 验证单抽象方法(SAM)要求
- [ ] 验证默认方法和静态方法的兼容性

### AC2: 四大核心接口验证
- [ ] Consumer<T> - 消费型接口
- [ ] Supplier<T> - 供给型接口  
- [ ] Function<T,R> - 函数型接口
- [ ] Predicate<T> - 断言型接口

### AC3: 自定义函数式接口
- [ ] 创建自定义函数式接口
- [ ] 验证接口契约
- [ ] 演示实际应用场景

## 实验步骤

1. 研究内置函数式接口
2. 创建自定义函数式接口
3. 验证注解约束
4. 演示接口组合和使用