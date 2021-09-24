package com.simon.optional.usage;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        //        AtomicInteger counter = new AtomicInteger();
        //        Optional<AtomicInteger> optional = Optional.ofNullable(counter);
        //        System.out.println(optional); //Optional[0]
        //        counter.incrementAndGet(); //Increment using counter directly
        //        System.out.println(optional); //Optional[1]
        //        optional.get().incrementAndGet(); //Retrieve contained value and increment
        //        System.out.println(optional); //Optional[2]
        //        optional = Optional.ofNullable(new AtomicInteger()); //Optional reference can be reassigned

        //        retrievingValueFromOptional();


    }

    private static void retrievingValueFromOptional() {
        Optional<String> firstEven =
                Stream.of("five", "even", "length", "string", "values")
                        .filter(s -> s.length() % 2 == 0)
                        .findFirst();
        System.out.println(firstEven); //Optional[five]
        //if none, print: Optional.empty
        System.out.println(firstEven.get()); //five // Don't do this, even if it works
        Optional<String> firstOdd =
                Stream.of("five", "even", "length", "string", "values")
                        .filter(s -> s.length() % 2 != 0)
                        .findFirst();
        System.out.println(firstOdd);
        //        System.out.println(firstOdd.get());// Exception, no value present
        // you should never call get on an
        //Optional unless you’re sure it contains a value or you risk throwing the exception

        //alternative way: Only call get if isPresent returns true
        //While this works, you’ve only traded null checking for isPresent checking, which
        //doesn’t feel like much of an improvement
        System.out.println(firstEven.isPresent() ? firstEven.get() : "No even length strings"); //five

        // good alternative!
        //The orElse method returns the contained value if one is present, or a supplied
        //default otherwise
        System.out.println(firstOdd.orElse("No odd length strings")); //No odd length strings


        System.out.println(firstEven.orElseThrow(NoSuchElementException::new));
        // ifPresent method allows you to provide a Consumer that is only executed
        //when the Optional contains a value
        firstEven.ifPresent(val -> System.out.println("Found an even-length string"));
        firstOdd.ifPresent(val -> System.out.println("Found an odd-length string"));
    }

    //create optional
    public static <T> Optional<T> createOptionalTheHardWay(T value) {
        return value == null ? Optional.empty() : Optional.of(value);
    }

    public static <T> Optional<T> createOptionalTheEasyWay(T value) {
        return Optional.ofNullable(value);
    }

    public void createPrimitiveOptional() {
        OptionalInt optionalInt = OptionalInt.of(1);
        OptionalLong optionalLong = OptionalLong.of(1L);
        OptionalDouble optionalDouble = OptionalDouble.of(1.0);
    }
}
