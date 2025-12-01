

```powershell
javac --release 21 src/k2/VisibilityProblem.java
得到了
src/k2/VisibilityProblem.class


java -XX:+UnlockDiagnosticVMOptions `
     -XX:CompileCommand=print,*VisibilityProblem.run `
     -cp src k2.VisibilityProblem
     
     
     
java -XX:+UnlockDiagnosticVMOptions `
     -XX:+PrintAssembly `
     -XX:CompileCommand=print,*VisibilityProblem*lambda* `
     -cp src k2.VisibilityProblem
```
