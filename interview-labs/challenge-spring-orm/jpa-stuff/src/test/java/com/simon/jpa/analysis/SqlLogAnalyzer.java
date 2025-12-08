package com.simon.jpa.analysis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.simon.jpa.domain.Order;
import com.simon.jpa.domain.OrderItem;
import com.simon.jpa.domain.User;
import com.simon.jpa.repo.OrderRepository;
import com.simon.jpa.repo.UserRepository;

import java.util.List;
import java.util.Set;

/**
 * SQL执行日志分析器
 * 用于分析JPA在不同场景下生成的SQL语句，帮助理解其行为模式
 */
@SpringBootTest
public class SqlLogAnalyzer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
    public void analyzeBasicCrudSql() {
        System.out.println("\n=== 分析基本CRUD操作的SQL ===");

        // INSERT
        System.out.println("\n1. INSERT操作:");
        User user = new User();
        user.setEmail("sql-test@example.com");
        user.setName("SQL Test User");
        userRepository.save(user);
        System.out.println("预期SQL: INSERT INTO users (email, name, version) VALUES (?, ?, ?)");

        // SELECT by ID
        System.out.println("\n2. SELECT by ID操作:");
        User foundUser = userRepository.findById(user.getId()).orElse(null);
        System.out.println("预期SQL: SELECT u1_0.id, u1_0.email, u1_0.name, u1_0.version FROM users u1_0 WHERE u1_0.id=?");

        // UPDATE (脏检查)
        System.out.println("\n3. UPDATE操作(脏检查):");
        foundUser.setName("Updated Name");
        userRepository.save(foundUser); // 实际上不需要save，事务提交时会自动更新
        System.out.println("预期SQL: UPDATE users SET email=?, name=?, version=? WHERE id=? AND version=?");

        // DELETE
        System.out.println("\n4. DELETE操作:");
        userRepository.delete(foundUser);
        System.out.println("预期SQL: DELETE FROM users WHERE id=?");
    }

    @Test
    @Transactional
    public void analyzeRelationshipSql() {
        System.out.println("\n=== 分析关系操作的SQL ===");

        // 准备数据
        User user = new User();
        user.setEmail("relation-test@example.com");
        user.setName("Relation Test User");
        userRepository.save(user);

        // 创建订单和订单项
        Order order = new Order(user, 1000);
        OrderItem item1 = new OrderItem();
        item1.setOrder(order);
        item1.setSku("TEST-SKU-1");
        item1.setQty(2);
        OrderItem item2 = new OrderItem();
        item2.setOrder(order);
        item2.setSku("TEST-SKU-2");
        item2.setQty(3);
        order.setItems(Set.of(item1, item2));

        System.out.println("\n1. 级联保存操作:");
        orderRepository.save(order);
        System.out.println("预期SQL顺序:");
        System.out.println("  1. INSERT INTO orders (user_id, total) VALUES (?, ?)");
        System.out.println("  2. INSERT INTO order_items (order_id, sku, qty) VALUES (?, ?, ?)");
        System.out.println("  3. INSERT INTO order_items (order_id, sku, qty) VALUES (?, ?, ?)");

        System.out.println("\n2. 查询订单(不包含订单项):");
        Order foundOrder = orderRepository.findById(order.getId()).orElse(null);
        System.out.println("预期SQL: SELECT o1_0.id, o1_0.total, o1_0.user_id FROM orders o1_0 WHERE o1_0.id=?");

        System.out.println("\n3. 懒加载订单项(触发N+1问题):");
        if (foundOrder != null) {
            int itemCount = foundOrder.getItems().size();
            System.out.println("订单项数量: " + itemCount);
            System.out.println("预期SQL:");
            System.out.println("  1. SELECT i1_0.order_id, i1_0.id, i1_0.qty, i1_0.sku FROM order_items i1_0 WHERE i1_0.order_id=?");
        }
    }

    @Test
    public void analyzeNPlusOneProblem() {
        System.out.println("\n=== 分析N+1问题 ===");

        // 准备数据
        prepareNPlusOneTestData();

        System.out.println("\n1. 查询所有订单(不使用JOIN FETCH):");
        long startTime = System.currentTimeMillis();
        List<Order> orders = orderRepository.findAll();
        long queryTime = System.currentTimeMillis();
        System.out.println("查询订单耗时: " + (queryTime - startTime) + "ms");
        System.out.println("预期SQL: SELECT o1_0.id, o1_0.total, o1_0.user_id FROM orders o1_0");

        System.out.println("\n2. 遍历订单项(触发N+1问题):");
        startTime = System.currentTimeMillis();
        int totalItems = 0;
        for (Order order : orders) {
            totalItems += order.getItems().size(); // 这里会触发额外的查询
        }
        long accessTime = System.currentTimeMillis();
        System.out.println("访问订单项耗时: " + (accessTime - startTime) + "ms");
        System.out.println("总订单数: " + orders.size() + ", 总订单项数: " + totalItems);
        System.out.println("预期SQL: 将执行 " + orders.size() + " 次查询订单项的SQL");
        System.out.println("  SELECT i1_0.order_id, i1_0.id, i1_0.qty, i1_0.sku FROM order_items i1_0 WHERE i1_0.order_id=?");

        System.out.println("\n3. 使用JOIN FETCH解决N+1问题:");
        startTime = System.currentTimeMillis();
        List<Order> ordersWithItems = orderRepository.findAllWithItems();
        queryTime = System.currentTimeMillis();
        int totalItems2 = ordersWithItems.stream().mapToInt(order -> order.getItems().size()).sum();
        accessTime = System.currentTimeMillis();
        System.out.println("JOIN FETCH查询耗时: " + (accessTime - startTime) + "ms");
        System.out.println("预期SQL: SELECT DISTINCT o FROM Order o JOIN FETCH o.items");
        System.out.println("实际执行的SQL会包含LEFT JOIN");
        System.out.println("总订单项数: " + totalItems2);
    }

    @Test
    public void analyzeQueryOptimization() {
        System.out.println("\n=== 分析查询优化 ===");

        // 准备测试数据
        prepareQueryTestData();

        System.out.println("\n1. 派生查询:");
        long startTime = System.currentTimeMillis();
        List<User> usersByName = userRepository.findByNameContaining("Test");
        long time1 = System.currentTimeMillis();
        System.out.println("查询耗时: " + (time1 - startTime) + "ms");
        System.out.println("预期SQL: SELECT u1_0.id, u1_0.email, u1_0.name, u1_0.version FROM users u1_0 WHERE u1_0.name LIKE ? ESCAPE ''");

        System.out.println("\n2. JPQL查询:");
        startTime = System.currentTimeMillis();
        List<User> usersByJpql = userRepository.findByNameWithJpql("Test");
        long time2 = System.currentTimeMillis();
        System.out.println("查询耗时: " + (time2 - startTime) + "ms");
        System.out.println("预期SQL: SELECT u FROM User u WHERE u.name LIKE :name");

        System.out.println("\n3. 原生查询:");
        startTime = System.currentTimeMillis();
        long count = userRepository.countByNativeQuery();
        long time3 = System.currentTimeMillis();
        System.out.println("查询耗时: " + (time3 - startTime) + "ms");
        System.out.println("预期SQL: SELECT count(*) FROM users");

        System.out.println("\n4. 投影查询(Record):");
        startTime = System.currentTimeMillis();
        List<Object[]> summaries = userRepository.findUserSummaries();
        long time4 = System.currentTimeMillis();
        System.out.println("投影查询耗时: " + (time4 - startTime) + "ms");
        System.out.println("预期SQL: SELECT u.id, u.name FROM User u");
    }

    @Test
    @Transactional
    public void analyzeCascadingOperations() {
        System.out.println("\n=== 分析级联操作 ===");

        // 准备数据
        User user = new User();
        user.setEmail("cascade-test@example.com");
        user.setName("Cascade Test User");
        userRepository.save(user);

        Order order = new Order(user, 2000);
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setSku("CASCADE-SKU");
        item.setQty(1);
        order.setItems(Set.of(item));
        orderRepository.save(order);

        System.out.println("\n1. 级联保存已完成");

        System.out.println("\n2. 孤儿删除操作:");
        Order managedOrder = orderRepository.findById(order.getId()).orElse(null);
        if (managedOrder != null && !managedOrder.getItems().isEmpty()) {
            OrderItem itemToRemove = managedOrder.getItems().iterator().next();
            managedOrder.removeItem(itemToRemove);
            System.out.println("移除订单项后，预期会自动删除该订单项");
            System.out.println("预期SQL: DELETE FROM order_items WHERE id=?");
        }

        System.out.println("\n3. 级联删除操作:");
        userRepository.delete(user);
        System.out.println("删除用户后，预期会级联删除相关订单和订单项");
        System.out.println("预期SQL顺序:");
        System.out.println("  1. DELETE FROM order_items WHERE order_id IN (SELECT id FROM orders WHERE user_id=?)");
        System.out.println("  2. DELETE FROM orders WHERE user_id=?");
        System.out.println("  3. DELETE FROM users WHERE id=?");
    }

    @Test
    @Transactional
    public void analyzeOptimisticLocking() {
        System.out.println("\n=== 分析乐观锁机制 ===");

        // 创建测试用户
        User user = new User();
        user.setEmail("lock-test@example.com");
        user.setName("Lock Test User");
        userRepository.save(user);
        System.out.println("初始版本: " + user.getVersion());

        // 第一次更新
        user.setName("Updated Name 1");
        userRepository.save(user);
        System.out.println("第一次更新后版本: " + user.getVersion());
        System.out.println("预期SQL: UPDATE users SET email=?, name=?, version=? WHERE id=? AND version=?");

        // 第二次更新
        user.setName("Updated Name 2");
        userRepository.save(user);
        System.out.println("第二次更新后版本: " + user.getVersion());
        System.out.println("预期SQL: UPDATE users SET email=?, name=?, version=? WHERE id=? AND version=?");

        System.out.println("\n乐观锁通过version字段防止并发更新冲突");
    }

    private void prepareNPlusOneTestData() {
        // 创建用户
        User user = new User();
        user.setEmail("nplus1-test@example.com");
        user.setName("N+1 Test User");
        userRepository.save(user);

        // 创建多个订单，每个订单包含多个订单项
        for (int i = 0; i < 5; i++) {
            Order order = new Order(user, 1000 * (i + 1));
            OrderItem item1 = new OrderItem();
            item1.setOrder(order);
            item1.setSku("SKU-" + i + "-1");
            item1.setQty(i + 1);
            OrderItem item2 = new OrderItem();
            item2.setOrder(order);
            item2.setSku("SKU-" + i + "-2");
            item2.setQty(i + 2);
            order.setItems(Set.of(item1, item2));
            orderRepository.save(order);
        }
    }

    private void prepareQueryTestData() {
        // 创建测试用户
        for (int i = 0; i < 50; i++) {
            User user = new User();
            user.setEmail("query-test" + i + "@example.com");
            user.setName("Test User " + i);
            userRepository.save(user);
        }
    }
}