$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$completeDir = Split-Path -Parent $scriptDir
$jarFile = Join-Path $completeDir "target\protocol-benchmark-1.0.0.jar"
$protoFile = Join-Path $completeDir "src\main\proto\orderdetail.proto"

function Run-Test {
    param (
        [string]$Name,
        [scriptblock]$Command
    )

    Write-Host "--- Starting Test: $Name ---"
    $proc = Start-Process java -ArgumentList "-Xms256m", "-Xmx256m", "-XX:+UseG1GC", "-jar", "$jarFile" -PassThru -NoNewWindow
    Write-Host "Started Java PID: $($proc.Id). Waiting 10s..."
    Start-Sleep -Seconds 10
    
    # Warmup? No, JMeter handles warmup or we just run it
    
    Write-Host "Running Benchmark..."
    Invoke-Command -ScriptBlock $Command
    
    Write-Host "Gathering jstat for $($proc.Id)..."
    jstat -gc $($proc.Id) > "$scriptDir\results\$Name-jstat.txt"
    jcmd $($proc.Id) GC.heap_info > "$scriptDir\results\$Name-jcmd.txt"
    
    Write-Host "Stopping Java PID: $($proc.Id)..."
    Stop-Process -Id $($proc.Id) -Force
    Start-Sleep -Seconds 3
}

Run-Test -Name "rest" -Command {
    jmeter -n -t $scriptDir\jmeter\rest-benchmark.jmx -Jthreads=100 -Jduration=60 -l $scriptDir\results\rest2.csv
}

Run-Test -Name "graphql" -Command {
    jmeter -n -t $scriptDir\jmeter\graphql-benchmark.jmx -Jthreads=100 -Jduration=60 -l $scriptDir\results\graphql2.csv
}

Run-Test -Name "grpc" -Command {
    & $scriptDir\tools\ghz\ghz.exe --insecure --proto $protoFile --call benchmark.OrderDetailService/GetOrderDetail -d '{\"order_id\":\"ORD-000500\"}' -z 60s -c 100 localhost:9090 > $scriptDir\results\grpc-summary2.txt
}

Write-Host "All done!"