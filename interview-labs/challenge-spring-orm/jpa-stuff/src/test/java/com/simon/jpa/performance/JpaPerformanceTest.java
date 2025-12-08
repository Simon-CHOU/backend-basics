package com.simon.jpa.performance;

import com.simon.jpa.domain.Order;
import com.simon.jpa.domain.OrderItem;
import com.simon.jpa.domain.User;
import com.simon.jpa.repo.OrderRepository;
import com.simon.jpa.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JpaPerformanceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Random random = new Random();

    @BeforeEach
    void setUp() {
        // 清理数据
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void testBulkInsertPerformance() {
        int userCount = 1000;
        int ordersPerUser = 5;
        int itemsPerOrder = 3;

        long startTime = System.currentTimeMillis();

        // 批量创建用户
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setName("User " + i);
            users.add(user);
        }
        userRepository.saveAll(users);
        long userInsertTime = System.currentTimeMillis();
        System.out.println("JPA - 批量插入 " + userCount + " 用户耗时: " + (userInsertTime - startTime) + "ms");

        // 为每个用户创建订单和订单项
        List<Order> allOrders = new ArrayList<>();
        for (User user : users) {
            for (int j = 0; j < ordersPerUser; j++) {
                Order order = new Order();
                order.setUser(user);
                order.setTotal(random.nextInt(10000));

                Set<OrderItem> items = Set.of(
                    createOrderItem("SKU-" + random.nextInt(1000), random.nextInt(10) + 1),
                    createOrderItem("SKU-" + random.nextInt(1000), random.nextInt(10) + 1),
                    createOrderItem("SKU-" + random.nextInt(1000), random.nextInt(10) + 1)
                );
                order.setItems(items);
                allOrders.add(order);
            }
        }

        orderRepository.saveAll(allOrders);
        long endTime = System.currentTimeMillis();

        System.out.println("JPA - 批量插入订单耗时: " + (endTime - userInsertTime) + "ms");
        System.out.println("JPA - 总插入耗时: " + (endTime - startTime) + "ms");
        System.out.println("JPA - 插入数据量: " + userCount + " 用户, " + allOrders.size() + " 订单");

        // 验证数据
        assertEquals(userCount, userRepository.count());
        assertEquals(userCount * ordersPerUser, orderRepository.count());
    }

    @Test
    void testQueryPerformance() {
        // 准备测试数据
        prepareTestData(5000);

        // 测试简单查询性能
        long startTime = System.currentTimeMillis();
        List<User> users = userRepository.findAll();
        long queryTime = System.currentTimeMillis();
        System.out.println("JPA - 查询所有 " + users.size() + " 用户耗时: " + (queryTime - startTime) + "ms");

        // 测试分页查询性能
        startTime = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = userRepository.findAll(pageable);
        long pageQueryTime = System.currentTimeMillis();
        System.out.println("JPA - 分页查询(0,20)耗时: " + (pageQueryTime - startTime) + "ms");
        assertTrue(userPage.hasNext());

        // 测试条件查询性能
        startTime = System.currentTimeMillis();
        Specification<User> spec = (root, query, cb) ->
            cb.like(root.get("name"), "%User%");
        Page<User> filteredUsers = userRepository.findAll(spec, pageable);
        long filteredQueryTime = System.currentTimeMillis();
        System.out.println("JPA - 条件查询耗时: " + (filteredQueryTime - startTime) + "ms");
        assertTrue(filteredUsers.hasContent());
    }

    @Test
    void testNPlusOneQueryPerformance() {
        // 准备测试数据
        prepareTestData(1000);

        // 测试N+1问题
        long startTime = System.currentTimeMillis();
        List<Order> orders = orderRepository.findAll();

        // 触发懒加载
        int totalItems = 0;
        for (Order order : orders) {
            totalItems += order.getItems().size();
        }
        long nplusOneTime = System.currentTimeMillis();

        System.out.println("JPA - N+1查询耗时: " + (nplusOneTime - startTime) + "ms");
        System.out.println("JPA - 查询订单数: " + orders.size() + ", 总订单项数: " + totalItems);

        // 测试解决N+1问题后的性能
        startTime = System.currentTimeMillis();
        List<Order> ordersWithFetch = orderRepository.findAllWithItems();
        int totalItems2 = ordersWithFetch.stream()
            .mapToInt(order -> order.getItems().size())
            .sum();
        long optimizedTime = System.currentTimeMillis();

        System.out.println("JPA - JOIN FETCH查询耗时: " + (optimizedTime - startTime) + "ms");
        System.out.println("JPA - 性能提升: " + ((double)(nplusOneTime - startTime) / (optimizedTime - startTime)) + "x");

        assertEquals(totalItems, totalItems2);
    }

    @Test
    @Transactional
    void testUpdatePerformance() {
        // 准备测试数据
        List<User> users = prepareTestData(1000);

        long startTime = System.currentTimeMillis();

        // 批量更新
        for (User user : users) {
            user.setName(user.getName() + " - Updated");
        }
        userRepository.saveAll(users);

        long endTime = System.currentTimeMillis();
        System.out.println("JPA - 批量更新 " + users.size() + " 用户耗时: " + (endTime - startTime) + "ms");

        // 验证更新
        User updatedUser = userRepository.findById(users.get(0).getId()).orElseThrow();
        assertTrue(updatedUser.getName().contains("Updated"));
    }

    @Test
    @Transactional
    void testOptimisticLockPerformance() {
        // 准备测试数据
        List<User> users = prepareTestData(100);

        long startTime = System.currentTimeMillis();

        // 模拟并发更新
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    User user = userRepository.findById(users.get(threadId * 10 + j).getId()).orElseThrow();
                    try {
                        Thread.sleep(1); // 模拟处理时间
                        user.setName(user.getName() + " - Thread" + threadId);
                        userRepository.save(user);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.out.println("乐观锁冲突: " + e.getMessage());
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("JPA - 乐观锁并发更新耗时: " + (endTime - startTime) + "ms");
    }

    private OrderItem createOrderItem(String sku, int qty) {
        OrderItem item = new OrderItem();
        item.setSku(sku);
        item.setQty(qty);
        return item;
    }

    private List<User> prepareTestData(int userCount) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setName("User " + i);
            users.add(user);
        }
        return userRepository.saveAll(users);
    }
}