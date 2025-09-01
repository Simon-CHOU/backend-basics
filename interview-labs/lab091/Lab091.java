import java.util.List;
import java.util.Optional;
import java.util.Arrays;

/**
 * ifPresent
 * orElse
 * orElseGet
 * map
 * flatmap
 * filter
 * 
 */

public class Lab091 {
    static List<User> users = Arrays.asList(
            new User(1L, "John"),
            new User(2L, "Jane"),
            new User(3L, "Doe"));

    static public class User {
        long id;
        String name;

        public User(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static User findUserByIdOld(long id) {
        User res = null;
        for (User user : users) {
            if (user.getId() == id) {
                res = user;
                break;
            }
        }
        return res;
    }

    public static Optional<User> findUserByIdv2(long id) {
        return users.stream().filter(e -> e.getId() == id).findFirst(); // 1️⃣不需要 toList
    }

    // Optional
    public static void main(String[] args) {

        // old style
        User userS = findUserByIdOld(33L);
        if (null == userS) {
            System.out.println("user is null");  // user is null
        } else {
            System.out.println(userS.getName());
        }

        // test isPresent
        Optional<User> optional = findUserByIdv2(1L);
        optional.ifPresent(user -> System.out.println(user.getName())); // 2️⃣不是 ()-> // John
        findUserByIdv2(39L).ifPresent(user -> System.out.println(user.getName())); // nothing happens

        // test isPresentOrElse
        findUserByIdv2(39L).ifPresentOrElse(user -> System.out.println(user.getName()), // 3️⃣ if not is
                () -> System.out.println("user is null")); // user is null

        // test orElse
        User user = findUserByIdv2(39L).orElse(new User(999, "Guest"));
        System.out.println(user.getName()); // Guest

        // test orElseGet
        User user2 = findUserByIdv2(39L).orElseGet(() -> new User(888, "Guest2")); // 4️⃣ orElseGet( invoke)
        System.out.println(user2.getName()); // Guest2

        // test isEmpty
        System.out.println(findUserByIdv2(39L).isEmpty()); // true

        // test of
        Optional<User> user3 = Optional.of(new User(100, "Kim"));
        System.out.println(user3.get().getName()); // Kim


        // test map
        Optional<String> name = findUserByIdv2(2L).map(u -> u.getName()); // AKA map(User::getName);
        System.out.println(name.get()); // Jane

        //test flatmap
        Optional<String>  name2 = findUserByIdv2(3L).flatMap(u -> Optional.of(u.getName())); // 5️⃣ flatMap return Optional
        System.out.println(name2.get()); // Doe

        // test filter
        Optional<User> name3 = findUserByIdv2(3L).filter(u-> u.getName().equals("Doe")); // not name-> name.equals("Doe")
        System.out.println(name3.get().getName()); // Doe
        Optional<User> name4 = users.stream().filter(u-> u.getName().equals("1Doe")).findFirst(); // not name-> name.equals("Doe")
        System.out.println(name4); // Optional.empty
        System.out.println(name4.get()); //  NoSuchElementException: No value present
        System.out.println(name4.get().getName()); // NoSuchElementException: No value present
    }
}
