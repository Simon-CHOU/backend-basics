package lab1;

/**
 * Lab 1 测试类
 * 演示最基础的Bean容器功能
 */
public class Lab1Test {
    
    public static void main(String[] args) {
        System.out.println("=== Lab 1: 理解容器本质 - HashMap ===\n");
        
        // 1. 创建容器
        SimpleContainer container = new SimpleContainer();
        System.out.println("步骤1: 创建了一个空容器\n");
        
        // 2. 显示空容器状态
        container.showContainerStatus();
        
        // 3. 创建Bean实例
        System.out.println("步骤2: 手动创建Bean实例");
        UserService userService = new UserService();
        OrderService orderService = new OrderService();
        System.out.println();
        
        // 4. 注册Bean到容器
        System.out.println("步骤3: 将Bean注册到容器中");
        container.registerBean("userService", userService);
        container.registerBean("orderService", orderService);
        System.out.println();
        
        // 5. 显示容器状态
        container.showContainerStatus();
        
        // 6. 从容器获取Bean
        System.out.println("步骤4: 从容器中获取Bean");
        UserService retrievedUserService = (UserService) container.getBean("userService");
        OrderService retrievedOrderService = (OrderService) container.getBean("orderService");
        System.out.println();
        
        // 7. 验证Bean是同一个实例（单例）
        System.out.println("步骤5: 验证Bean的单例特性");
        System.out.println("原始userService == 获取的userService: " + (userService == retrievedUserService));
        System.out.println("原始orderService == 获取的orderService: " + (orderService == retrievedOrderService));
        System.out.println();
        
        // 8. 使用Bean
        System.out.println("步骤6: 使用Bean执行业务逻辑");
        retrievedUserService.createUser("张三");
        retrievedOrderService.createOrder("ORDER-001");
        System.out.println();
        
        // 9. 测试容器的查询功能
        System.out.println("步骤7: 测试容器查询功能");
        System.out.println("容器中是否包含userService: " + container.containsBean("userService"));
        System.out.println("容器中是否包含notExistService: " + container.containsBean("notExistService"));
        System.out.println("容器中Bean的总数: " + container.getBeanCount());
        System.out.println();
        
        // 10. 尝试获取不存在的Bean（演示异常处理）
        System.out.println("步骤8: 尝试获取不存在的Bean");
        try {
            container.getBean("notExistService");
        } catch (RuntimeException e) {
            System.out.println("捕获异常: " + e.getMessage());
        }
        System.out.println();
        
        // 11. 注册更多Bean测试
        System.out.println("步骤9: 注册更多Bean");
        UserService anotherUserService = new UserService("AnotherUserService");
        container.registerBean("anotherUserService", anotherUserService);
        container.showContainerStatus();
        
        // 12. 总结
        System.out.println("=== Lab 1 总结 ===");
        System.out.println("1. 容器本质就是一个HashMap<String, Object>");
        System.out.println("2. Bean就是存储在容器中的Java对象");
        System.out.println("3. 容器提供Bean的注册、获取、查询功能");
        System.out.println("4. 默认情况下Bean是单例的（同一个实例）");
        System.out.println("5. Spring容器就是这个概念的高级版本");
        System.out.println("=== Lab 1 完成 ===");
    }
}