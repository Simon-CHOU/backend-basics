package com.simon.jpa;

import com.simon.jpa.domain.Order;
import com.simon.jpa.domain.OrderItem;
import com.simon.jpa.domain.User;
import com.simon.jpa.repo.OrderRepository;
import com.simon.jpa.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * 测试数据初始化器
 * 用于生成丰富的测试数据以支持各种实验场景
 */
@Component
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 只在测试环境中或特定条件下初始化数据
        if (shouldInitializeData()) {
            System.out.println("开始初始化测试数据...");
            initializeTestData();
            System.out.println("测试数据初始化完成！");
        }
    }

    private boolean shouldInitializeData() {
        // 检查是否需要初始化数据
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("test") || profile.contains("dev");
    }

    @Transactional
    public void initializeTestData() {
        // 清理现有数据
        orderRepository.deleteAll();
        userRepository.deleteAll();

        // 创建多样化的测试用户
        createUsers();

        // 创建不同类型的订单
        createOrders();
    }

    private void createUsers() {
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

            userRepository.save(user);
        }

        // 创建一些特殊用户用于测试
        createSpecialUsers();
    }

    private void createSpecialUsers() {
        // 测试乐观锁的用户
        User lockTestUser = new User();
        lockTestUser.setName("Lock Test User");
        lockTestUser.setEmail("locktest@example.com");
        userRepository.save(lockTestUser);

        // 测试查询的用户
        User queryTestUser = new User();
        queryTestUser.setName("Query Test User");
        queryTestUser.setEmail("query@example.com");
        userRepository.save(queryTestUser);

        // VIP用户
        for (int i = 0; i < 10; i++) {
            User vipUser = new User();
            vipUser.setName("VIP Customer " + i);
            vipUser.setEmail("vip" + i + "@vip.com");
            userRepository.save(vipUser);
        }
    }

    private void createOrders() {
        // 获取所有用户
        var users = userRepository.findAll();

        // 为每个用户创建订单
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            // 每个用户创建1-5个订单
            int orderCount = (i % 5) + 1;

            for (int j = 0; j < orderCount; j++) {
                Order order = new Order(user, generateOrderTotal());
                orderRepository.save(order);

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
        int itemCount = (int) (Math.random() * 3) + 1;
        Set<OrderItem> items = new HashSet<>();

        for (int i = 0; i < itemCount; i++) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setSku(skus[(int) (Math.random() * skus.length)] + "-" + (int)(Math.random() * 1000));
            item.setQty((int) (Math.random() * 10) + 1);
            items.add(item);
        }

        order.setItems(items);
        orderRepository.save(order);
    }

    private void createLargeOrders() {
        // 创建一些包含大量订单项的大订单用于测试
        var users = userRepository.findAll();

        for (int i = 0; i < 10 && i < users.size(); i++) {
            User user = users.get(i);
            Order largeOrder = new Order(user, 50000 + (int)(Math.random() * 50000));
            orderRepository.save(largeOrder);

            // 创建大量订单项
            Set<OrderItem> items = new HashSet<>();
            for (int j = 0; j < 50; j++) {
                OrderItem item = new OrderItem();
                item.setOrder(largeOrder);
                item.setSku("BULK-ITEM-" + j);
                item.setQty((int) (Math.random() * 100) + 1);
                items.add(item);
            }

            largeOrder.setItems(items);
            orderRepository.save(largeOrder);
        }
    }

    private int generateOrderTotal() {
        // 生成不同金额范围的订单
        double rand = Math.random();
        if (rand < 0.6) {
            // 60% 的小订单 (100-1000)
            return 100 + (int)(Math.random() * 900);
        } else if (rand < 0.9) {
            // 30% 的中等订单 (1000-5000)
            return 1000 + (int)(Math.random() * 4000);
        } else {
            // 10% 的大订单 (5000-20000)
            return 5000 + (int)(Math.random() * 15000);
        }
    }

    /**
     * 获取数据统计信息
     */
    public String getDataStatistics() {
        long userCount = userRepository.count();
        long orderCount = orderRepository.count();

        return String.format("数据统计 - 用户: %d, 订单: %d", userCount, orderCount);
    }
}