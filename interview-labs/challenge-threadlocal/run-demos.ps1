$ErrorActionPreference = "Stop"
$src = "src"
$cp = $src
Write-Host "Compile and run BasicThreadLocalDemo"
javac --release 21 "$src\BasicThreadLocalDemo.java"
java -cp $cp BasicThreadLocalDemo
Write-Host "Compile and run PoolLeakDemo"
javac --release 21 "$src\PoolLeakDemo.java"
java -cp $cp PoolLeakDemo
Write-Host "Compile and run VirtualThreadDemo"
javac --release 21 "$src\VirtualThreadDemo.java"
java -cp $cp VirtualThreadDemo
Write-Host "Compile and run InheritDemo"
javac --release 21 "$src\InheritDemo.java"
java -cp $cp InheritDemo
Write-Host "Compile and run ScopedValueDemo (preview)"
javac --release 21 --enable-preview "$src\ScopedValueDemo.java"
java --enable-preview -cp $cp ScopedValueDemo
