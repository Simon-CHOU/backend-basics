# PowerShell构建脚本
# 用于编译和运行Java函数式编程练习系统

param(
    [string]$Command = "build",
    [string]$ExerciseId = "",
    [switch]$Help = $false,
    [switch]$Verbose = $false,
    [switch]$TestAll = $false,
    [switch]$Progress = $false,
    [switch]$List = $false,
    [switch]$Reset = $false,
    [string]$Category = "",
    [string]$Difficulty = "",
    [int]$HintLevel = 0
)

Write-Host "Java函数式编程练习系统构建工具" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# 显示帮助
if ($Help) {
    Write-Host @"
使用方法: .\build.ps1 [选项]

命令:
  build          编译项目 (默认)
  run            运行指定练习
  test           运行所有测试
  clean          清理编译文件
  package        打包项目

选项:
  -ExerciseId    练习ID (如: lambda-basics)
  -Verbose       显示详细输出
  -TestAll       运行所有练习测试
  -Progress      显示学习进度
  -List          列出练习题
  -Reset         重置学习进度
  -Category      按类别筛选
  -Difficulty    按难度筛选
  -HintLevel     提示级别 (1-3)
  -Help          显示此帮助信息

示例:
  .\build.ps1                           # 编译项目
  .\build.ps1 run -ExerciseId lambda-basics
  .\build.ps1 test -Verbose
  .\build.ps1 -List -Category lambda
  .\build.ps1 -Progress
"@
    exit 0
}

# 检查Maven是否可用
function Test-Maven {
    try {
        mvn --version | Out-Null
        return $true
    } catch {
        Write-Host "错误: Maven未安装或不在PATH中" -ForegroundColor Red
        return $false
    }
}

# 编译项目
function Build-Project {
    Write-Host "正在编译项目..." -ForegroundColor Yellow

    if (-not (Test-Maven)) {
        exit 1
    }

    mvn clean compile
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 编译成功!" -ForegroundColor Green
        return $true
    } else {
        Write-Host "❌ 编译失败!" -ForegroundColor Red
        return $false
    }
}

# 运行练习
function Run-Exercise {
    param([string]$Id)

    if (-not $Id) {
        Write-Host "错误: 请指定练习ID" -ForegroundColor Red
        Write-Host "使用 '.\build.ps1 -List' 查看可用练习" -ForegroundColor Yellow
        exit 1
    }

    Write-Host "运行练习: $Id" -ForegroundColor Yellow

    $jarPath = "target\practice-lab-1.0-SNAPSHOT.jar"
    $classPath = "target\classes"

    $verboseFlag = if ($Verbose) { "-v" } else { "" }

    if (Test-Path $jarPath) {
        java -jar $jarPath run $Id $verboseFlag
    } elseif (Test-Path $classPath) {
        java -cp $classPath com.simon.practice.cli.PracticeCli run $Id $verboseFlag
    } else {
        Write-Host "错误: 未找到编译后的文件，请先运行构建" -ForegroundColor Red
        exit 1
    }
}

# 运行所有测试
function Run-All-Tests {
    Write-Host "运行所有练习测试..." -ForegroundColor Yellow

    $jarPath = "target\practice-lab-1.0-SNAPSHOT.jar"
    $classPath = "target\classes"

    $verboseFlag = if ($Verbose) { "-v" } else { "" }
    $failFastFlag = if (-not $Verbose) { "-f" } else { "" }

    if (Test-Path $jarPath) {
        java -jar $jarPath test-all $verboseFlag $failFastFlag
    } elseif (Test-Path $classPath) {
        java -cp $classPath com.simon.practice.cli.PracticeCli test-all $verboseFlag $failFastFlag
    } else {
        Write-Host "错误: 未找到编译后的文件，请先运行构建" -ForegroundColor Red
        exit 1
    }
}

# 显示进度
function Show-Progress {
    Write-Host "学习进度:" -ForegroundColor Yellow

    $jarPath = "target\practice-lab-1.0-SNAPSHOT.jar"
    $classPath = "target\classes"

    if (Test-Path $jarPath) {
        java -jar $jarPath progress
    } elseif (Test-Path $classPath) {
        java -cp $classPath com.simon.practice.cli.PracticeCli progress
    } else {
        Write-Host "错误: 未找到编译后的文件，请先运行构建" -ForegroundColor Red
        exit 1
    }
}

# 列出练习
function List-Exercises {
    Write-Host "可用练习题:" -ForegroundColor Yellow

    $jarPath = "target\practice-lab-1.0-SNAPSHOT.jar"
    $classPath = "target\classes"

    $categoryFlag = if ($Category) { "-c $Category" } else { "" }
    $difficultyFlag = if ($Difficulty) { "-d $Difficulty" } else { "" }

    if (Test-Path $jarPath) {
        java -jar $jarPath list $categoryFlag $difficultyFlag
    } elseif (Test-Path $classPath) {
        java -cp $classPath com.simon.practice.cli.PracticeCli list $categoryFlag $difficultyFlag
    } else {
        Write-Host "错误: 未找到编译后的文件，请先运行构建" -ForegroundColor Red
        exit 1
    }
}

# 重置进度
function Reset-Progress {
    Write-Host "重置学习进度..." -ForegroundColor Yellow

    $jarPath = "target\practice-lab-1.0-SNAPSHOT.jar"
    $classPath = "target\classes"

    if (Test-Path $jarPath) {
        java -jar $jarPath reset -y
    } elseif (Test-Path $classPath) {
        java -cp $classPath com.simon.practice.cli.PracticeCli reset -y
    } else {
        Write-Host "错误: 未找到编译后的文件，请先运行构建" -ForegroundColor Red
        exit 1
    }
}

# 获取提示
function Get-Hint {
    param([string]$Id, [int]$Level)

    if (-not $Id) {
        Write-Host "错误: 请指定练习ID" -ForegroundColor Red
        exit 1
    }

    Write-Host "获取练习提示: $Id" -ForegroundColor Yellow

    $jarPath = "target\practice-lab-1.0-SNAPSHOT.jar"
    $classPath = "target\classes"

    $levelFlag = if ($Level -gt 0) { "-l $Level" } else { "" }

    if (Test-Path $jarPath) {
        java -jar $jarPath hint $Id $levelFlag
    } elseif (Test-Path $classPath) {
        java -cp $classPath com.simon.practice.cli.PracticeCli hint $Id $levelFlag
    } else {
        Write-Host "错误: 未找到编译后的文件，请先运行构建" -ForegroundColor Red
        exit 1
    }
}

# 清理项目
function Clean-Project {
    Write-Host "清理项目..." -ForegroundColor Yellow
    mvn clean
}

# 打包项目
function Package-Project {
    Write-Host "打包项目..." -ForegroundColor Yellow

    if (-not (Test-Maven)) {
        exit 1
    }

    mvn clean package
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 打包成功!" -ForegroundColor Green
        Write-Host "可执行jar文件: target\practice-lab-1.0-SNAPSHOT.jar" -ForegroundColor Cyan
    } else {
        Write-Host "❌ 打包失败!" -ForegroundColor Red
    }
}

# 主逻辑
switch ($Command.ToLower()) {
    "build" {
        Build-Project
    }
    "run" {
        if (Build-Project) {
            Run-Exercise -Id $ExerciseId
        }
    }
    "test" {
        if (Build-Project) {
            Run-All-Tests
        }
    }
    "clean" {
        Clean-Project
    }
    "package" {
        Package-Project
    }
    default {
        if ($TestAll) {
            if (Build-Project) {
                Run-All-Tests
            }
        } elseif ($Progress) {
            Show-Progress
        } elseif ($List) {
            List-Exercises
        } elseif ($Reset) {
            Reset-Progress
        } elseif ($ExerciseId) {
            if (Build-Project) {
                Run-Exercise -Id $ExerciseId
            }
        } else {
            Build-Project
        }
    }
}

Write-Host "`n操作完成!" -ForegroundColor Green