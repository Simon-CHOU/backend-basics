$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$completeDir = Split-Path -Parent $scriptDir
$jarFile = Join-Path $completeDir "target\protocol-benchmark-1.0.0.jar"
$protoFile = Join-Path $completeDir "src\main\proto\orderdetail.proto"

$proc = Start-Process java -ArgumentList "-Xms256m", "-Xmx256m", "-XX:+UseG1GC", "-jar", "$jarFile", "-XX:StartFlightRecording=duration=15s,filename=$scriptDir\results\rest.jfr" -PassThru -NoNewWindow
Start-Sleep -Seconds 5
jmeter -n -t $scriptDir\jmeter\rest-benchmark.jmx -Jthreads=100 -Jduration=15
Stop-Process -Id $($proc.Id) -Force
Start-Sleep -Seconds 3

$proc = Start-Process java -ArgumentList "-Xms256m", "-Xmx256m", "-XX:+UseG1GC", "-jar", "$jarFile", "-XX:StartFlightRecording=duration=15s,filename=$scriptDir\results\graphql.jfr" -PassThru -NoNewWindow
Start-Sleep -Seconds 5
jmeter -n -t $scriptDir\jmeter\graphql-benchmark.jmx -Jthreads=100 -Jduration=15
Stop-Process -Id $($proc.Id) -Force
Start-Sleep -Seconds 3

$proc = Start-Process java -ArgumentList "-Xms256m", "-Xmx256m", "-XX:+UseG1GC", "-jar", "$jarFile", "-XX:StartFlightRecording=duration=15s,filename=$scriptDir\results\grpc.jfr" -PassThru -NoNewWindow
Start-Sleep -Seconds 5
& $scriptDir\tools\ghz\ghz.exe --insecure --proto $protoFile --call benchmark.OrderDetailService/GetOrderDetail -d '{\"order_id\":\"ORD-000500\"}' -z 15s -c 100 localhost:9090 > $null
Stop-Process -Id $($proc.Id) -Force
