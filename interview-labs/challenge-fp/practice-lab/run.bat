@echo off
REM Windows 批处理启动脚本 - 修复中文乱码问题

REM 设置控制台代码页为 UTF-8
chcp 65001 >nul 2>&1

REM 设置 Java 文件编码
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8

REM 检查 JAR 是否存在
if not exist "target\practice-lab-1.0-SNAPSHOT.jar" (
    echo 错误: 未找到 target\practice-lab-1.0-SNAPSHOT.jar
    echo 请先运行: mvn clean package
    exit /b 1
)

REM 运行 Java 程序
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar "target\practice-lab-1.0-SNAPSHOT.jar" %*

exit /b %ERRORLEVEL%
