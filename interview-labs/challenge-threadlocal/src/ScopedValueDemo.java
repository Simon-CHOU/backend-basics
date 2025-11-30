public class ScopedValueDemo {
    static final java.lang.ScopedValue<String> USER = java.lang.ScopedValue.newInstance();
    public static void main(String[] args) {
        java.lang.ScopedValue.where(USER, "bob").run(() -> System.out.println(USER.get()));
    }
}
