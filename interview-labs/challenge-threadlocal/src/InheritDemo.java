public class InheritDemo {
    static final InheritableThreadLocal<String> ih = new InheritableThreadLocal<>();
    public static void main(String[] args) throws Exception {
        ih.set("parent");
        Thread t = new Thread(() -> System.out.println(ih.get()));
        t.start(); t.join();
        ih.remove();
    }
}
