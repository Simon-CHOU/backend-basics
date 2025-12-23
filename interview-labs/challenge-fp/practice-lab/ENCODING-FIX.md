# Windows 中文乱码修复指南

## 问题现象

在 Windows PowerShell 中运行命令时，中文显示为乱码：

```
鈩癸笍  鎻愮ず (绾у埆 1):
馃挕 Lambda琛ㄨ揪寮忕殑鍩烘湰璇鍙?: (鍙傛暟) -> { 琛ㄨ揪寮?}
```

## ✅ 解决方案

### 方式 1: 使用批处理脚本（推荐）

创建并使用 `run.bat` 文件：

```batch
@echo off
chcp 65001 >nul 2>&1
set "JAVA_OPTS=-Dfile.encoding=UTF-8"
java %JAVA_OPTS% -jar target\practice-lab-1.0-SNAPSHOT.jar %*
```

**使用方法：**
```cmd
run.bat hint lambda-basics
run.bat list
run.bat run lambda-basics
```

### 方式 2: 使用 PowerShell 脚本

使用已提供的 `run.ps1` 文件：

```powershell
.\run.ps1 hint lambda-basics
.\run.ps1 list
```

如果遇到执行策略限制，先运行：
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

### 方式 3: 手动设置编码（CMD）

在命令提示符中：

```cmd
chcp 65001
java -Dfile.encoding=UTF-8 -jar target\practice-lab-1.0-SNAPSHOT.jar hint lambda-basics
```

### 方式 4: 手动设置编码（PowerShell）

在 PowerShell 中：

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null
java -Dfile.encoding=UTF-8 -jar target\practice-lab-1.0-SNAPSHOT.jar hint lambda-basics
```

## 永久解决方案

### 选项 1: 设置 PowerShell 配置文件

编辑 PowerShell 配置文件（如果不存在则创建）：

```powershell
notepad $PROFILE
```

添加以下内容：

```powershell
# 设置控制台编码为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
```

### 选项 2: 设置系统环境变量

1. 右键点击"此电脑" → "属性"
2. "高级系统设置" → "环境变量"
3. 添加系统变量：
   - 变量名: `JAVA_TOOL_OPTIONS`
   - 变量值: `-Dfile.encoding=UTF-8`

## 验证修复

运行以下命令验证中文是否正常显示：

```cmd
run.bat hint lambda-basics
```

应该看到：

```
ℹ️  提示 (级别 1):
💡 Lambda表达式的基本语法: (参数) -> { 表达式 }
```

## 常见问题

**Q: 为什么会出现乱码？**
A: Windows 默认使用 GBK 编码，而 Java 程序使用 UTF-8 编码输出中文，导致编码不匹配。

**Q: 每次都要设置编码吗？**
A: 使用提供的 `.bat` 或 `.ps1` 脚本可以自动处理编码问题。

**Q: Linux/Mac 用户有这个问题吗？**
A: 通常没有，Linux/Mac 默认使用 UTF-8 编码。

## 快速测试脚本

保存为 `test-encoding.bat`：

```batch
@echo off
echo 测试中文显示...
echo.
chcp 65001 >nul 2>&1
java -Dfile.encoding=UTF-8 -jar target\practice-lab-1.0-SNAPSHOT.jar hint lambda-basics
echo.
echo 如果看到正确的中文，说明编码配置成功！
pause
```
