@echo off
if "%1"=="" (
    echo Usage: run-lab.bat [lab number]
    echo Example: run-lab.bat 1
    exit /b 1
)

set LAB_NUM=%1
echo === Spring Bean Lab %LAB_NUM% Runner ===

echo Checking Java version...
java -version
if errorlevel 1 (
    echo Error: Java not found, please ensure Java 21 is installed
    exit /b 1
)

echo.
echo Compiling Lab %LAB_NUM%...
if not exist target\classes mkdir target\classes
javac -d target\classes src\main\java\lab%LAB_NUM%\*.java
if errorlevel 1 (
    echo Compilation failed
    exit /b 1
)

echo Compilation successful
echo.
echo Running Lab %LAB_NUM%...
java -cp target\classes lab%LAB_NUM%.Lab%LAB_NUM%Test

if errorlevel 1 (
    echo Lab %LAB_NUM% execution failed
) else (
    echo.
    echo Lab %LAB_NUM% completed successfully
)