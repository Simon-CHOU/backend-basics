# Spring Bean 深度理解实验室 - 快速开始

## 环境要求
- Java 21 (已配置在PATH中)
- Windows 11 + PowerShell 或 命令提示符

## 快速运行

### 方法1: 使用批处理文件 (推荐)
```cmd
# 在PowerShell中
cmd /c "run-lab.bat 1"

# 或在命令提示符中
run-lab.bat 1
```

### 方法2: 手动编译运行
```cmd
# 编译
javac -d target/classes src/main/java/lab1/*.java

# 运行
java -cp target/classes lab1.Lab1Test
```

### 方法3: 使用PowerShell脚本
```powershell
.\run-lab.ps1 1
```

## 实验顺序

按照以下顺序完成实验，每个实验都建立在前一个的基础上：

1. **Lab 1: 理解容器本质** - `run-lab.bat 1`
   - 理解Spring容器就是一个高级HashMap
   - 掌握Bean的注册、获取、查询

2. **Lab 2: Bean生命周期管理** - `run-lab.bat 2`
   - 实现Bean的创建、初始化、销毁
   - 理解生命周期回调

3. **Lab 3: 依赖注入实现** - `run-lab.bat 3`
   - 实现自动依赖注入
   - 理解Setter注入原理

4. **Lab 4: 前置后置处理器** - `run-lab.bat 4`
   - 实现BeanPostProcessor
   - 理解Bean增强机制

5. **Lab 5: AOP动态代理** - `run-lab.bat 5`
   - 实现方法拦截
   - 理解切面编程

6. **Lab 6: 三级缓存解决循环依赖** - `run-lab.bat 6`
   - 理解循环依赖问题
   - 掌握三级缓存解决方案

7. **Lab 7: Spring Boot集成对比** - `run-lab.bat 7`
   - 对比自实现与Spring的差异
   - 理解Spring Boot自动配置

## 调试建议

1. **使用IDE调试**：
   - 在IntelliJ IDEA或Eclipse中导入项目
   - 在关键方法设置断点
   - 单步调试观察执行流程

2. **观察输出**：
   - 每个Lab都有详细的控制台输出
   - 注意观察Bean的创建顺序和状态变化

3. **修改代码**：
   - 尝试修改Bean的属性
   - 添加新的Bean类型
   - 观察容器的行为变化

## 故障排除

### 编译错误
- 确保Java 21已正确安装
- 检查JAVA_HOME环境变量
- 确保源文件编码为UTF-8

### 运行错误
- 检查classpath设置
- 确保target/classes目录存在
- 查看详细错误信息

## 学习建议

1. **按顺序学习**：每个Lab都是递进的，不要跳跃
2. **动手实践**：不仅要运行，还要理解每行代码
3. **对比思考**：将自实现与Spring源码对比
4. **扩展实验**：尝试添加新功能或修改现有实现

## 下一步

完成所有Lab后，建议：
1. 阅读Spring源码中的相关实现
2. 学习Spring Boot的自动配置原理
3. 实践更复杂的Spring应用场景

祝学习愉快！🚀