public class MyRunnable implements Runnable {

private int count = 0;

@Override
public void run() {

for(int i=0; i<1_800_000; i++) {
this.count++;

System.out.println(
Thread.currentThread().getName()
" : "+ this.count);
