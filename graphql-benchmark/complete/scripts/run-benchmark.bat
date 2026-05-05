@echo off
REM ============================================================
REM Protocol Benchmark Runner
REM ============================================================
REM Prerequisites: Maven, JMeter, Docker (optional)
REM ============================================================

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  Protocol Benchmark Experiment Runner
echo ========================================
echo.

set STEP=%%1
if "%STEP%"=="" set STEP=all

if "%STEP%"=="all" goto :build
if "%STEP%"=="build" goto :build
if "%STEP%"=="start" goto :start
if "%STEP%"=="benchmark" goto :benchmark
if "%STEP%"=="report" goto :report
if "%STEP%"=="stop" goto :stop
echo Unknown step: %STEP%
echo Usage: run-benchmark.bat [build^|start^|benchmark^|report^|stop^|all]
goto :eof

:build
echo [1/4] Building project...
call mvnw.cmd clean package -DskipTests -q
if errorlevel 1 (
    echo BUILD FAILED
    exit /b 1
)
echo Build OK
if not "%STEP%"=="all" goto :eof

:start
echo [2/4] Starting benchmark server...
start "benchmark-server" java -jar target\protocol-benchmark-1.0.0.jar --server.port=8080
echo Waiting for server to start...
timeout /t 15 /nobreak >nul

REM Verify server is up
curl -s http://localhost:8080/actuator/health >nul 2>&1
if errorlevel 1 (
    REM If actuator not available, try the REST endpoint
    curl -s http://localhost:8080/api/orders/ORD-000001 >nul 2>&1
    if errorlevel 1 (
        echo SERVER NOT RESPONDING - check logs
        exit /b 1
    )
)
echo Server is running
if not "%STEP%"=="all" goto :eof

:benchmark
echo [3/4] Running benchmarks...

echo --- REST Benchmark ---
jmeter -n -t jmeter\rest-benchmark.jmx -Jhost=localhost -Jport=8080 -Jthreads=100 -Jduration=60 -l results\rest-results.csv -e -o results\rest-report
echo REST benchmark complete

echo --- GraphQL Benchmark ---
jmeter -n -t jmeter\graphql-benchmark.jmx -Jhost=localhost -Jport=8080 -Jthreads=100 -Jduration=60 -l results\graphql-results.csv -e -o results\graphql-report
echo GraphQL benchmark complete

echo --- gRPC Benchmark ---
echo gRPC benchmark requires ghz tool. Install: go install github.com/bojand/ghz/cmd/ghz@latest
echo Run manually: ghz --insecure --proto src/main/proto/orderdetail.proto --call benchmark.OrderDetailService/GetOrderDetail -d '{"order_id":"ORD-000500"}' -n 10000 -c 100 localhost:9090
echo.
if not "%STEP%"=="all" goto :eof

:report
echo [4/4] Generating report...
echo.
echo ========================================
echo  Experiment Results
echo ========================================
echo.
echo P99 Latency (ms):
echo   REST:    see results/rest-report/index.html
echo   GraphQL: see results/graphql-report/index.html
echo   gRPC:    run ghz manually and compare
echo.
echo Memory (Docker):
echo   docker stats benchmark-server
echo.
echo Serialization CPU:
echo   Use async-profiler or JFR during benchmark run:
echo   java -jar target/protocol-benchmark-1.0.0.jar -XX:StartFlightRecording=filename=profile.jfr
echo.
echo Report template: see report-template.md
echo.
if not "%STEP%"=="all" goto :eof

:stop
echo Stopping server...
taskkill /fi "WINDOWTITLE eq benchmark-server" /f >nul 2>&1
echo Server stopped
goto :eof
