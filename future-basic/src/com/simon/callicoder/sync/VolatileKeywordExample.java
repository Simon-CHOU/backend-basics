package com.simon.callicoder.sync;

/**
 *  avoid doing any optimizations to the variable
 *  the compiler won’t optimize or reorder instructions around that variable
 * The variable’s value will always be read from the main memory instead of temporary registers.
 */
public class VolatileKeywordExample {
    private static volatile boolean sayHello = false;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(()->{
           while (!sayHello) {

           }
            System.out.println("Hello World!");

           while(sayHello){

           }
            System.out.println("Good Bye!");
        });

        thread.start();

        Thread.sleep(1000);
        System.out.println("Say Hello..");
        sayHello = true;

        Thread.sleep(1000);
        System.out.println("Say Bye..");
        sayHello = false;
//        Say Hello..
//        Hello World!
//        Say Bye..
//        Good Bye!

    }
}
