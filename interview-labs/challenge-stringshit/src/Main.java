//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {
//
//        String name = "Java";
//        name = name + "21"; // 这行代码到底发生了什么？
//        System.out.println(name);


    // 这个循环创建10,000个石头，只保留最后一个
    String result = "";
    for (int i = 0; i < 10000; i++) {
        result += i; // 每次+都创建新String对象
    }
// 时间成本：O(n²)，因为每次都要复制之前所有内容
// 空间成本：临时创建约10,000个废弃对象


    String a = "Java";  // 去池子里找，没有就创建
    String b = "Java";  // 找到一样的，直接用同一个
    System.out.println(a == b); // true，a和b指向同一块石头！

// 如果String可变：
// a.setChar(0, 'K'); // 如果允许这样
// b也会变成"Kava"，这会导致灾难性bug
    String key = "user:123";
    int hash = key.hashCode(); // 算一次后缓存
    System.out.println(hash);
// 如果String可变，hashCode会变，HashMap会找不到值

    //因为 key 的内容始终是 "user:123"，所以无论程序运行多少次，只要内容不变，其 hashCode() 结果也永远不会改变，总是 -267310589。


    roadTo21();

}

static void roadTo21() {
    // 特性1：文本块（多行字符串）
    String json = """
            {
                "name": "Java21",
                "features": ["Virtual Threads", "Pattern Matching"]
            }
            """; // 自动处理换行和缩进

// 特性2：String模板（预览特性）
    String name = "Java";
    var version = 21;
    String message = STR."Hello \\{name} \\{version}!";
// 比+拼接更高效，可读性更好

// 特性3：紧凑字符串（Java 9+）
// 内部用byte[]而不用char[]，省50%内存
// "ABC"用1字节/字符，"中文"用2字节/字符
}