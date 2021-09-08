package com.simon.threadlocal.practice;

/**
 * http://tutorials.jenkov.com/java-concurrency/threadlocal.html
 */
public class Main {

    public static void main(String[] args) {
        Process process = new Process();
        process.something();
        System.out.println(process.display());
    }
}
