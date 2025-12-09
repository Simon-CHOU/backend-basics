package com.simon.mybatis;

import com.simon.mybatis.domain.Order;
import com.simon.mybatis.domain.OrderItem;
import com.simon.mybatis.domain.User;
import com.simon.mybatis.mapper.OrderMapper;
import com.simon.mybatis.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 测试数据初始化器
 * 用于生成丰富的测试数据以支持各种实验场景
 */
@Component
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    private Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // 只在测试环境中或特定条件下初始化数据
        if (shouldInitializeData()) {
            System.out.println("开始初始化MyBatis测试数据...");
            initializeTestData();
            System.out.println("MyBatis测试数据初始化完成！");
        }
    }

    private boolean shouldInitializeData() {
        // 检查是否需要初始化数据
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("test") || profile.contains("dev");
    }

    public void initializeTestData() {
        // 清理现有数据 - MyBatis需要显式清理
        orderMapper.deleteAllItems();
        orderMapper.deleteAllOrders();
        userMapper.deleteAllUsers();

        // 创建多样化的测试用户
        List<User> users = createUsers();

        // 创建不同类型的订单
        createOrders(users);

        // 打印数据统计
        System.out.println(getDataStatistics());
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        String[] firstNames = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
        String[] lastNames = {"伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "洋", "艳"};
        String[] domains = {"qq.com", "163.com", "gmail.com", "outlook.com", "126.com"};

        // 创建100个不同特征的用户
        for (int i = 0; i < 100; i++) {
            User user = new User();
            String firstName = firstNames[i % firstNames.length];
            String lastName = lastNames[i % lastNames.length];
            String domain = domains[i % domains.length];

            user.setName(firstName + lastName + (i / 10));
            user.setEmail("user" + i + "@" + domain);

            userMapper.insertUser(user);
            users.add(user);
        }

        // 创建一些特殊用户用于测试
        createSpecialUsers(users);

        return users;
    }

    private void createSpecialUsers(List<User> users) {
        // 测试用户
        User testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        userMapper.insertUser(testUser);
        users.add(testUser);

        // VIP用户
        for (int i = 0; i < 10; i++) {
            User vipUser = new User();
            vipUser.setName("VIP Customer " + i);
            vipUser.setEmail("vip" + i + "@vip.com");
            userMapper.insertUser(vipUser);
            users.add(vipUser);
        }

        // 批量用户
        for (int i = 0; i < 50; i++) {
            User bulkUser = new User();
            bulkUser.setName("Bulk User " + i);
            bulkUser.setEmail("bulk" + i + "@bulk.com");
            userMapper.insertUser(bulkUser);
            users.add(bulkUser);
        }
    }

    private void createOrders(List<User> users) {
        // 为每个用户创建订单
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            // 每个用户创建1-5个订单
            int orderCount = (i % 5) + 1;

            for (int j = 0; j < orderCount; j++) {
                Order order = new Order();
                order.setUserId(user.getId());
                order.setTotal(generateOrderTotal());

                orderMapper.insertOrder(order);

                // 为订单创建订单项
                createOrderItems(order);
            }
        }

        // 创建一些大订单用于性能测试
        createLargeOrders();
    }

    private void createOrderItems(Order order) {
        String[] skus = {"LAPTOP", "MOUSE", "KEYBOARD", "MONITOR", "PHONE",
                        "TABLET", "HEADPHONE", "SPEAKER", "CAMERA", "PRINTER"};

        // 每个订单创建1-3个订单项
        int itemCount = random.nextInt(3) + 1;

        for (int i = 0; i < itemCount; i++) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setSku(skus[random.nextInt(skus.length)] + "-" + random.nextInt(1000));
            item.setQty(random.nextInt(10) + 1);

            orderMapper.insertOrderItem(item);
        }
    }

    private void createLargeOrders() {
        List<User> users = userMapper.findAllUsers();
        if (users.isEmpty()) return;

        // 创建一些包含大量订单项的大订单用于测试
        for (int i = 0; i < 10 && i < users.size(); i++) {
            User user = users.get(i);
            Order largeOrder = new Order();
            largeOrder.setUserId(user.getId());
            largeOrder.setTotal(50000 + random.nextInt(50000));

            orderMapper.insertOrder(largeOrder);

            // 创建大量订单项
            for (int j = 0; j < 50; j++) {
                OrderItem item = new OrderItem();
                item.setOrderId(largeOrder.getId());
                item.setSku("BULK-ITEM-" + j);
                item.setQty(random.nextInt(100) + 1);

                orderMapper.insertOrderItem(item);
            }
        }
    }

    private int generateOrderTotal() {
        // 生成不同金额范围的订单
        double rand = random.nextDouble();
        if (rand < 0.6) {
            // 60% 的小订单 (100-1000)
            return 100 + random.nextInt(900);
        } else if (rand < 0.9) {
            // 30% 的中等订单 (1000-5000)
            return 1000 + random.nextInt(4000);
        } else {
            // 10% 的大订单 (5000-20000)
            return 5000 + random.nextInt(15000);
        }
    }

    /**
     * 获取数据统计信息
     */
    public String getDataStatistics() {
        long userCount = userMapper.countUsers();
        long orderCount = orderMapper.countOrders();

        return String.format("MyBatis数据统计 - 用户: %d, 订单: %d", userCount, orderCount);
    }

    /**
     * 创建特定场景的测试数据
     */
    public void createScenarioData(String scenario) {
        switch (scenario.toLowerCase()) {
            case "performance":
                createPerformanceTestData();
                break;
            case "nplus1":
                createNPlusOneTestData();
                break;
            case "batch":
                createBatchTestData();
                break;
            default:
                System.out.println("未知的场景类型: " + scenario);
        }
    }

    private void createPerformanceTestData() {
        // 创建大量用户用于性能测试
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setName("Perf User " + i);
            user.setEmail("perf" + i + "@perf.com");
            userMapper.insertUser(user);
        }
        System.out.println("创建了1000个性能测试用户");
    }

    private void createNPlusOneTestData() {
        // 创建用于测试N+1问题的数据
        List<User> users = userMapper.findAllUsers();
        if (users.isEmpty()) {
            // 如果没有用户，先创建一些
            users = createUsers();
        }

        // 为前10个用户创建每个包含20个订单项的订单
        for (int i = 0; i < Math.min(10, users.size()); i++) {
            User user = users.get(i);
            Order order = new Order();
            order.setUserId(user.getId());
            order.setTotal(10000);
            orderMapper.insertOrder(order);

            // 创建20个订单项
            for (int j = 0; j < 20; j++) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setSku("NPLUS1-ITEM-" + j);
                item.setQty(j + 1);
                orderMapper.insertOrderItem(item);
            }
        }
        System.out.println("创建了N+1测试数据");
    }

    private void createBatchTestData() {
        // 创建用于批量操作测试的数据
        for (int i = 0; i < 500; i++) {
            User user = new User();
            user.setName("Batch User " + i);
            user.setEmail("batch" + i + "@batch.com");
            userMapper.insertUser(user);
        }
        System.out.println("创建了500个批量测试用户");
    }
}