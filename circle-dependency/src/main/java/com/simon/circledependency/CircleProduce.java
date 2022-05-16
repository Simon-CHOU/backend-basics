package com.simon.circledependency;

/**
 * 循环依赖问题体现
 * <p>你中有我，我中有你，运行就报错 </p>
 * <p>这样的循环依赖代码是没法解决的</p>
 */
public class CircleProduce {
    public static void main(String[] args) {
        new ClazzA();
//        Exception in thread "main" java.lang.StackOverflowError
//        at com.simon.circledependency.ClazzA.<init>(CircleProduce.java:10)
//        at com.simon.circledependency.ClazzB.<init>(CircleProduce.java:14)
//        at com.simon.circledependency.ClazzA.<init>(CircleProduce.java:10)
//        at com.simon.circledependency.ClazzB.<init>(CircleProduce.java:14)
//        at com.simon.circledependency.ClazzA.<init>(CircleProduce.java:10)
//        at com.simon.circledependency.ClazzB.<init>(CircleProduce.java:14)
        //...
    }
}

class ClazzA {
    private ClazzB b = new ClazzB();
}

class ClazzB {
    private ClazzA a = new ClazzA();
}
