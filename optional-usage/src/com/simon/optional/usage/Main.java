package com.simon.optional.usage;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        AtomicInteger counter = new AtomicInteger();
        Optional<AtomicInteger> optional = Optional.ofNullable(counter);
        System.out.println(optional); //Optional[0]
        counter.incrementAndGet(); //Increment using counter directly
        System.out.println(optional); //Optional[1]
        optional.get().incrementAndGet(); //Retrieve contained value and increment
        System.out.println(optional); //Optional[2]
        optional = Optional.ofNullable(new AtomicInteger()); //Optional reference can be reassigned
    }
}
