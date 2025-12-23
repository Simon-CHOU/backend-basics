# PowerShell 启动脚本 - 修复中文乱码问题
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$Args
)

# 设置控制台编码为 UTF-8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 使用 cmd /c 运行以确保正确的参数传递
$jarPath = "target\practice-lab-1.0-SNAPSHOT.jar"

if (-not (Test-Path $jarPath)) {
    Write-Host "错误: 未找到 $jarPath" -ForegroundColor Red
    Write-Host "请先运行: mvn clean package -Dmaven.test.skip=true" -ForegroundColor Yellow
    exit 1
}

# 构建参数字符串
$argsString = $Args -join ' '

# 使用 cmd /c 调用 java，设置 UTF-8 编码
$expression = "chcp 65001 | java -Dfile.encoding=UTF-8 -jar `"$jarPath`" $argsString"
Invoke-Expression $expression
