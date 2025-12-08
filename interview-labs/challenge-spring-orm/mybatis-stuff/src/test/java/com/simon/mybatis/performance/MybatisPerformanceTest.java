package com.simon.mybatis.performance;

import com.simon.mybatis.domain.Order;
import com.simon.mybatis.domain.OrderItem;
import com.simon.mybatis.domain.User;
import com.simon.mybatis.mapper.OrderMapper;
import com.simon.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MybatisPerformanceTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    private Random random = new Random();

    @BeforeEach
    void setUp() {
        // 清理数据 - MyBatis需要显式清理
        orderMapper.deleteAllItems();
        orderMapper.deleteAllOrders();
        userMapper.deleteAllUsers();
    }

    @Test
    void testBulkInsertPerformance() {
        int userCount = 1000;
        int ordersPerUser = 5;
        int itemsPerOrder = 3;

        long startTime = System.currentTimeMillis();

        // 批量插入用户
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setName("User " + i);
            userMapper.insertUser(user);
            users.add(user);
        }
        long userInsertTime = System.currentTimeMillis();
        System.out.println("MyBatis - 批量插入 " + userCount + " 用户耗时: " + (userInsertTime - startTime) + "ms");

        // 为每个用户创建订单
        List<Order> orders = new ArrayList<>();
        for (User user : users) {
            for (int j = 0; j < ordersPerUser; j++) {
                Order order = new Order();
                order.setUserId(user.getId());
                order.setTotal(random.nextInt(10000));
                orderMapper.insertOrder(order);
                orders.add(order);

                // 为订单创建订单项
                for (int k = 0; k < itemsPerOrder; k++) {
                    OrderItem item = new OrderItem();
                    item.setOrderId(order.getId());
                    item.setSku("SKU-" + random.nextInt(1000));
                    item.setQty(random.nextInt(10) + 1);
                    orderMapper.insertOrderItem(item);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("MyBatis - 批量插入订单和订单项耗时: " + (endTime - userInsertTime) + "ms");
        System.out.println("MyBatis - 总插入耗时: " + (endTime - startTime) + "ms");
        System.out.println("MyBatis - 插入数据量: " + userCount + " 用户, " + orders.size() + " 订单");

        // 验证数据
        assertEquals(userCount, userMapper.countUsers());
        assertEquals(userCount * ordersPerUser, orderMapper.countOrders());
    }

    @Test
    void testQueryPerformance() {
        // 准备测试数据
        prepareTestData(5000);

        // 测试简单查询性能
        long startTime = System.currentTimeMillis();
        List<User> users = userMapper.findAllUsers();
        long queryTime = System.currentTimeMillis();
        System.out.println("MyBatis - 查询所有 " + users.size() + " 用户耗时: " + (queryTime - startTime) + "ms");

        // 测试分页查询性能
        startTime = System.currentTimeMillis();
        List<User> pageUsers = userMapper.findUsersWithPagination(20, 0);
        long pageQueryTime = System.currentTimeMillis();
        System.out.println("MyBatis - 分页查询(0,20)耗时: " + (pageQueryTime - startTime) + "ms");
        assertEquals(20, pageUsers.size());

        // 测试条件查询性能
        startTime = System.currentTimeMillis();
        List<User> filteredUsers = userMapper.findUsersByNameLike("User");
        long filteredQueryTime = System.currentTimeMillis();
        System.out.println("MyBatis - 条件查询耗时: " + (filteredQueryTime - startTime) + "ms");
        assertTrue(filteredUsers.size() > 0);
    }

    @Test
    void testJoinQueryPerformance() {
        // 准备测试数据
        prepareTestData(1000);

        // 测试关联查询性能 - 一次性获取订单及其订单项
        long startTime = System.currentTimeMillis();
        List<Order> orders = orderMapper.findOrdersWithItems();
        int totalItems = 0;
        for (Order order : orders) {
            totalItems += order.getItems().size();
        }
        long joinQueryTime = System.currentTimeMillis();

        System.out.println("MyBatis - JOIN查询耗时: " + (joinQueryTime - startTime) + "ms");
        System.out.println("MyBatis - 查询订单数: " + orders.size() + ", 总订单项数: " + totalItems);

        // 测试分别查询的性能对比
        startTime = System.currentTimeMillis();
        List<Order> ordersOnly = orderMapper.findAllOrders();
        int totalItems2 = 0;
        for (Order order : ordersOnly) {
            List<OrderItem> items = orderMapper.findItemsByOrderIdList(order.getId());
            totalItems2 += items.size();
        }
        long separateQueryTime = System.currentTimeMillis();

        System.out.println("MyBatis - 分别查询耗时: " + (separateQueryTime - startTime) + "ms");
        System.out.println("MyBatis - 性能对比 JOIN vs 分别查询: " +
            ((double)(separateQueryTime - startTime) / (joinQueryTime - startTime)) + "x");

        assertEquals(totalItems, totalItems2);
    }

    @Test
    void testDynamicQueryPerformance() {
        // 准备测试数据
        prepareTestData(3000);

        // 测试动态SQL查询性能
        long startTime = System.currentTimeMillis();

        // 只按名称查询
        List<User> result1 = userMapper.findUsersByConditions("User 1%", null);
        long time1 = System.currentTimeMillis();

        // 只按邮箱查询
        List<User> result2 = userMapper.findUsersByConditions(null, "%@example.com");
        long time2 = System.currentTimeMillis();

        // 按名称和邮箱查询
        List<User> result3 = userMapper.findUsersByConditions("User 1%", "%@example.com");
        long time3 = System.currentTimeMillis();

        System.out.println("MyBatis - 动态查询(仅名称)耗时: " + (time1 - startTime) + "ms");
        System.out.println("MyBatis - 动态查询(仅邮箱)耗时: " + (time2 - time1) + "ms");
        System.out.println("MyBatis - 动态查询(名称+邮箱)耗时: " + (time3 - time2) + "ms");

        assertTrue(result1.size() > 0);
        assertTrue(result2.size() > 0);
        assertTrue(result3.size() >= 0); // 可能为0
    }

    @Test
    void testUpdatePerformance() {
        // 准备测试数据
        List<User> users = prepareTestData(1000);

        long startTime = System.currentTimeMillis();

        // 批量更新 - MyBatis需要显式更新
        for (User user : users) {
            user.setName(user.getName() + " - Updated");
            userMapper.updateUser(user);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("MyBatis - 批量更新 " + users.size() + " 用户耗时: " + (endTime - startTime) + "ms");

        // 验证更新
        User updatedUser = userMapper.findUserById(users.get(0).getId());
        assertTrue(updatedUser.getName().contains("Updated"));
    }

    @Test
    void testConcurrentUpdatePerformance() throws InterruptedException {
        // 准备测试数据
        List<User> users = prepareTestData(100);

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);

        // 模拟并发更新
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        User user = userMapper.findUserById(users.get(threadId * 10 + j).getId());
                        if (user != null) {
                            user.setName(user.getName() + " - Thread" + threadId);
                            userMapper.updateUser(user);
                        }
                        Thread.sleep(1); // 模拟处理时间
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long endTime = System.currentTimeMillis();

        System.out.println("MyBatis - 并发更新耗时: " + (endTime - startTime) + "ms");
    }

    @Test
    void testBatchOperationPerformance() {
        // 准备测试数据
        prepareTestData(1000);

        // 测试批量操作性能
        long startTime = System.currentTimeMillis();

        // 批量删除订单项
        orderMapper.deleteItemsByOrderIdRange(1, 1000);
        long time1 = System.currentTimeMillis();

        // 批量删除订单
        orderMapper.deleteOrdersByIdRange(1, 1000);
        long time2 = System.currentTimeMillis();

        // 批量删除用户
        userMapper.deleteUsersByIdRange(1, 1000);
        long endTime = System.currentTimeMillis();

        System.out.println("MyBatis - 批量删除订单项耗时: " + (time1 - startTime) + "ms");
        System.out.println("MyBatis - 批量删除订单耗时: " + (time2 - time1) + "ms");
        System.out.println("MyBatis - 批量删除用户耗时: " + (endTime - time2) + "ms");

        assertEquals(0, userMapper.countUsers());
    }

    private List<User> prepareTestData(int userCount) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setName("User " + i);
            userMapper.insertUser(user);
            users.add(user);
        }
        return users;
    }
}