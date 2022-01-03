package com.simon.gfg.ft;

// Java program to demonstrate the working of
// ExecutorService

// Importing required libraries
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Class 1
// helper class extending Runnable interface
class Service implements Runnable {

    // member variable of this class
    int i;

    // Constructor of this class
    public Service(int i)
    {

        // Initializing the counter variable
        this.i = i;
    }

    // Method
    // @Override
    public void run()
    {

        // Printing the counter
        System.out.println(i + " ");

        // Try block to check for exceptions
        try {

            // Making thread to sleep for 1 second
            // using the sleep() method
            Thread.sleep(1000);
        }

        // Catch block to handle the exceptions
        catch (InterruptedException e) {

            // Print the line number and the corresponding
            // exception occured
            e.printStackTrace();
        }
    }
}

// Class 2
// Main class
// ExecutorUtility
public class ForkJoinDemoExecutorUtility {

    // Main driver method
    public static void main(String[] args)
    {

        // Creating an object of ExecutorService class to
        // create fixed size thread pool
        ExecutorService es
                = Executors.newFixedThreadPool(5);

        // Print the time difference before completion
        System.out.println(new Date());

        for (int i = 0; i < 25; i++) {

            // Executes the given command at some time in
            // the future
            es.execute(new Service(i));
        }

        // Executor is shut down so that
        // its task can be considered complete
        es.shutdown();

        // Print the time difference after completion
        System.out.println(new Date());
    }
}
