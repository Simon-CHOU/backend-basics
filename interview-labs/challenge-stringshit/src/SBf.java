public class SBf {
    static void main() {
        // 场景：两个工人（线程）同时写字
        StringBuilder unsafe = new StringBuilder();
// 线程A: unsafe.append("A");
// 线程B: unsafe.append("B");
// 可能结果："AB"、"BA"、"A"、"B"或乱码！

// StringBuffer = StringBuilder + 锁
        StringBuffer safe = new StringBuffer();
// 线程A: safe.append("A"); // 先拿到锁
// 线程B: safe.append("B"); // 等A释放锁后再执行
// 结果一定是"AB"或"BA"，不会错乱


        // #append 上锁、所有public 都上锁

    }
}
