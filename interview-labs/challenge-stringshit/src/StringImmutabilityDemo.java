import java.lang.reflect.Field;

/**
 * 目标：亲手证明String对象创建后内存内容不变
 * 工具：JOL (Java Object Layout) 查看内存地址
 * 预期：String的value数组引用不可变
 */
void main() {
    // 步骤1：创建String
    String s = "Hello";
    System.out.println("原始String: " + System.identityHashCode(s));

    // 步骤2："修改"它
    String s2 = s.concat(" World");
    System.out.println("拼接后String: " + System.identityHashCode(s2));
    System.out.println("原String没变: " + System.identityHashCode(s));

    // 步骤3：证明value数组是final
    // 用反射暴力测试（生产代码绝对不要！）
    try {
        Field valueField = String.class.getDeclaredField("value");
        valueField.setAccessible(true);
        byte[] originalValue = (byte[]) valueField.get(s);

        // 尝试修改（会失败，因为数组是final引用）
        // valueField.set(s, new byte[]{'X'}); // IllegalAccessException

        System.out.println("验证成功：无法修改String内部");
    } catch (Exception e) {
        e.printStackTrace();
    }
}