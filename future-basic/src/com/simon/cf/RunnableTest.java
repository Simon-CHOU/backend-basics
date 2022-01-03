package com.simon.cf;

// Java program to illustrate Runnable
// for random number generation
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

// https://www.geeksforgeeks.org/callable-future-java/?ref=gcse
class RunnableExample implements Runnable
{
    // Shared object to store result
    private Object result = null;

    public void run()
    {
        Random generator = new Random();
        Integer randomNumber = generator.nextInt(5);

        // As run cannot throw any Exception
        try
        {
            Thread.sleep(randomNumber * 1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // Store the return value in result when done
        result = randomNumber;

        // Wake up threads blocked on the get() method
        synchronized(this)
        {
            notifyAll();
        }
    }

    public synchronized Object get()
            throws InterruptedException
    {
        while (result == null)
            wait();

        return result;
    }
}

// Code is almost same as the previous example with a
// few changes made to use Runnable instead of Callable
public class RunnableTest
{
    public static void main(String[] args) throws Exception
    {
        RunnableExample[] randomNumberTasks = new RunnableExample[5];

        for (int i = 0; i < 5; i++)
        {
            randomNumberTasks[i] = new RunnableExample();
            Thread t = new Thread(randomNumberTasks[i]);
            t.start();
        }

        for (int i = 0; i < 5; i++)
            System.out.println(randomNumberTasks[i].get());
    }
}
