# Java函数式编程练习系统

欢迎来到Java函数式编程交互式练习实验室！这个系统将帮助你通过实践掌握Java函数式编程的核心概念。

## 🎯 系统特色

- **渐进式学习**: 从Lambda基础到Stream API，循序渐进
- **即时反馈**: ✅/❌ 可视化反馈，快速了解学习进度
- **智能提示**: 多级提示系统，遇到困难时获得帮助
- **成就系统**: 完成挑战获得成就徽章
- **CLI工具**: 便捷的命令行界面，支持单题和批量测试

## 🚀 快速开始

### 1. 编译并打包项目

```bash
mvn clean package
```

### 2. Windows 用户（解决中文乱码）

Windows PowerShell/CMD 默认编码可能导致中文显示乱码，请使用以下方式之一：

#### 方式1: 使用启动脚本（推荐）

**PowerShell:**
```powershell
.\run.ps1 list
.\run.ps1 hint lambda-basics
.\run.ps1 run lambda-basics
```

**命令提示符 (CMD):**
```cmd
run.bat list
run.bat hint lambda-basics
run.bat run lambda-basics
```

#### 方式2: 手动设置编码

**PowerShell:**
```powershell
# 设置控制台编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

# 运行命令
java -Dfile.encoding=UTF-8 -jar target/practice-lab-1.0-SNAPSHOT.jar hint lambda-basics
```

**命令提示符 (CMD):**
```cmd
chcp 65001
java -Dfile.encoding=UTF-8 -jar target/practice-lab-1.0-SNAPSHOT.jar hint lambda-basics
```

### 3. Linux/Mac 用户

```bash
# 直接运行
java -jar target/practice-lab-1.0-SNAPSHOT.jar list

# 或设置编码
java -Dfile.encoding=UTF-8 -jar target/practice-lab-1.0-SNAPSHOT.jar hint lambda-basics
```

### 4. 查看所有命令

```bash
# 查看帮助
.\run.ps1 --help
```

### 5. 列出所有练习

```bash
# 列出所有练习题
.\run.ps1 list

# 按类别筛选
.\run.ps1 list -c lambda

# 按难度筛选
java -jar target/practice-lab-1.0-SNAPSHOT.jar list -d BEGINNER
```

### 4. 开始练习

```bash
# 运行Lambda基础练习
java -jar target/practice-lab-1.0-SNAPSHOT.jar run lambda-basics

# 显示详细输出
java -jar target/practice-lab-1.0-SNAPSHOT.jar run lambda-basics -v
```

## 📚 练习内容

### 1. Lambda表达式基础 (`lambda-basics`)
- **难度**: 🟢 初学者
- **目标**: 掌握Lambda表达式基本语法和常用函数式接口
- **练习**: Consumer, Predicate, Function的使用
- **分数**: 100分

### 2. 函数式接口进阶 (`functional-interface`)
- **难度**: 🟡 中级
- **目标**: 深入理解函数式接口和高级函数组合
- **练习**: 自定义函数式接口、方法引用、函数组合
- **分数**: 150分

### 3. Stream API实战 (`stream-api`)
- **难度**: 🟡 中级
- **目标**: 掌握Stream API的各种操作和并行处理
- **练习**: 数据过滤、映射、收集、并行处理
- **分数**: 200分

## 💡 学习建议

### 开始练习前
1. **阅读练习描述**: 了解每个练习的目标
2. **查看源文件**: 找到`TODO`标记，了解需要完成的代码
3. **理解测试**: 查看对应的测试文件，了解预期行为

### 完成练习的步骤
1. **定位TODO**: 在练习文件中找到`TODO`注释
2. **实现代码**: 替换TODO为实际的Java代码
3. **运行测试**: 使用CLI工具验证答案
4. **查看反馈**: 根据测试结果调整代码

### 获取帮助
```bash
# 获取提示
java -jar target/practice-lab-1.0-SNAPSHOT.jar hint lambda-basics

# 获取更详细的提示
java -jar target/practice-lab-1.0-SNAPSHOT.jar hint lambda-basics -l 2
```

## 📊 进度跟踪

### 查看进度
```bash
# 查看当前学习进度
java -jar target/practice-lab-1.0-SNAPSHOT.jar progress
```

### 进度信息
- **总体完成度**: 显示已完成练习的百分比
- **得分情况**: 当前得分与总分对比
- **成就徽章**: 已解锁的成就列表

## 🏆 成就系统

完成不同的挑战可以解锁成就：

### 🌱 成长度
- **迈出第一步**: 完成第一道练习题
- **初学者**: 完成25%的练习
- **进阶者**: 完成50%的练习
- **函数式编程大师**: 完成100%的练习

### ⚡ 其他成就
- 更多特殊成就等待你去发现...

## 🔧 常用命令

### 基础命令
```bash
# 显示帮助
--help

# 显示版本
--version

# 列出练习
list [options]

# 运行指定练习
run <exerciseId> [options]

# 运行所有练习
test-all [options]

# 查看进度
progress

# 重置进度
reset [options]

# 获取提示
hint <exerciseId> [options]
```

### 选项说明
- `-v, --verbose`: 显示详细输出
- `-c, --category`: 按类别筛选
- `-d, --difficulty`: 按难度筛选
- `-f, --fail-fast`: 遇到失败立即停止
- `-y, --yes`: 确认操作（如重置进度）
- `-l, --level`: 提示级别（1-3）

## 📁 项目结构

```
practice-lab/
├── src/
│   ├── main/java/com/simon/practice/
│   │   ├── cli/                    # CLI工具
│   │   ├── core/                   # 练习引擎
│   │   ├── model/                  # 数据模型
│   │   └── utils/                  # 工具类
│   ├── practice/java/              # 练习题文件（包含TODO）
│   │   └── com/simon/practice/exercises/
│   │       ├── lambda/             # Lambda练习
│   │       ├── functional/         # 函数式接口练习
│   │       └── stream/             # Stream API练习
│   └── test/java/                  # 测试验证器
│       └── com/simon/practice/validator/
├── progress.json                   # 学习进度文件
└── config/                         # 配置文件
```

## ❓ 常见问题

### Q: 练习无法编译怎么办？
A: 检查TODO标记是否已完全替换为有效代码，确保语法正确。

### Q: 如何知道我是否完成了练习？
A: 运行测试后，系统会给出✅或❌反馈，并显示得分。

### Q: 可以跳过某些练习吗？
A: 可以，但建议按顺序完成以获得更好的学习效果。

### Q: 如何重置进度？
A: 使用 `reset -y` 命令，这将清空所有进度记录。

### Q: 成就数据保存在哪里？
A: 所有进度和成就数据都保存在 `progress.json` 文件中。

## 🤝 贡献与反馈

如果在学习过程中遇到问题或有改进建议，欢迎反馈！

## 📄 许可证

本项目仅用于教学目的，请遵守相关使用条款。

---

**祝你学习愉快！** 🎉