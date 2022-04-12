package com.simon.evnconfigactive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DisplayCountCommand implements CommandLineRunner {
    @Value("${count}")
    private Integer count;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("res = " + count);
    }
}
