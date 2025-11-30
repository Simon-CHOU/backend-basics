public class BasicThreadLocalDemo {
    static final ThreadLocal<String> ctx = ThreadLocal.withInitial(() -> "none");
    static String work() { return "user=" + ctx.get(); }
    public static void main(String[] args) {
        ctx.set("alice");
        try { System.out.println(work()); }
        finally { ctx.remove(); }
    }
}
