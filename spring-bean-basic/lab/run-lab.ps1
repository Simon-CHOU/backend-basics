# Spring Bean Lab 运行脚本
# 使用方法: .\run-lab.ps1 1  (运行Lab 1)

param(
    [Parameter(Mandatory=$true)]
    [int]$LabNumber
)

Write-Host "=== Spring Bean Lab $LabNumber 运行脚本 ===" -ForegroundColor Green

# 检查Java版本
Write-Host "检查Java版本..." -ForegroundColor Yellow
java -version

if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: 未找到Java，请确保Java 21已安装并配置在PATH中" -ForegroundColor Red
    exit 1
}

# 创建输出目录
$outputDir = "target/classes"
if (!(Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

# 编译Java文件
Write-Host "编译Lab $LabNumber..." -ForegroundColor Yellow
$sourceDir = "src/main/java/lab$LabNumber"

if (!(Test-Path $sourceDir)) {
    Write-Host "错误: Lab $LabNumber 不存在" -ForegroundColor Red
    exit 1
}

# 编译所有Java文件
javac -d $outputDir "$sourceDir/*.java"

if ($LASTEXITCODE -ne 0) {
    Write-Host "编译失败" -ForegroundColor Red
    exit 1
}

Write-Host "编译成功" -ForegroundColor Green

# 运行测试
Write-Host "运行Lab $LabNumber..." -ForegroundColor Yellow
java -cp $outputDir "lab$LabNumber.Lab${LabNumber}Test"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Lab $LabNumber 运行完成" -ForegroundColor Green
} else {
    Write-Host "Lab $LabNumber 运行失败" -ForegroundColor Red
}