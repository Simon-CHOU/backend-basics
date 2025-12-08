package com.simon.mybatis.analysis;

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

import java.util.List;

/**
 * SQL执行日志分析器
 * 用于分析MyBatis在不同场景下生成的SQL语句，帮助理解其行为模式
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SqlLogAnalyzer {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        // 清理数据
        orderMapper.deleteAllItems();
        orderMapper.deleteAllOrders();
        userMapper.deleteAllUsers();
    }

    @Test
    public void analyzeBasicCrudSql() {
        System.out.println("\n=== 分析基本CRUD操作的SQL ===");

        // INSERT
        System.out.println("\n1. INSERT操作:");
        User user = new User();
        user.setEmail("sql-test@example.com");
        user.setName("SQL Test User");
        userMapper.insertUser(user);
        System.out.println("预期SQL: insert into users(email, name) values(?, ?)");

        // SELECT by ID
        System.out.println("\n2. SELECT by ID操作:");
        User foundUser = userMapper.findUserById(user.getId());
        System.out.println("预期SQL: select id, email, name from users where id = ?");

        // UPDATE (显式更新)
        System.out.println("\n3. UPDATE操作(显式更新):");
        foundUser.setName("Updated Name");
        userMapper.updateUser(foundUser);
        System.out.println("预期SQL: update users set email = ?, name = ? where id = ?");
        System.out.println("注意: MyBatis需要显式调用update方法，不会自动更新");

        // DELETE
        System.out.println("\n4. DELETE操作:");
        userMapper.deleteById(user.getId());
        System.out.println("预期SQL: delete from users where id = ?");
    }

    @Test
    public void analyzeRelationshipSql() {
        System.out.println("\n=== 分析关系操作的SQL ===");

        // 准备数据
        User user = new User();
        user.setEmail("relation-test@example.com");
        user.setName("Relation Test User");
        userMapper.insertUser(user);

        // 创建订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setTotal(1000);
        orderMapper.insertOrder(order);
        System.out.println("\n1. 创建订单:");
        System.out.println("预期SQL: insert into orders(user_id, total) values(?, ?)");

        // 创建订单项
        OrderItem item1 = new OrderItem();
        item1.setOrderId(order.getId());
        item1.setSku("TEST-SKU-1");
        item1.setQty(2);
        orderMapper.insertOrderItem(item1);

        OrderItem item2 = new OrderItem();
        item2.setOrderId(order.getId());
        item2.setSku("TEST-SKU-2");
        item2.setQty(3);
        orderMapper.insertOrderItem(item2);

        System.out.println("\n2. 创建订单项:");
        System.out.println("预期SQL: insert into order_items(order_id, sku, qty) values(?, ?, ?)");
        System.out.println("注意: 需要为每个订单项执行单独的INSERT");

        // JOIN查询
        System.out.println("\n3. JOIN查询订单及其订单项:");
        Order orderWithItems = orderMapper.findOrderWithItemsById(order.getId());
        System.out.println("预期SQL:");
        System.out.println("  select o.id as order_id, o.user_id, o.total,");
        System.out.println("         i.id as item_id, i.order_id, i.sku, i.qty");
        System.out.println("  from orders o left join order_items i on o.id = i.order_id");
        System.out.println("  where o.id = ? order by i.id");
    }

    @Test
    public void analyzeDynamicSql() {
        System.out.println("\n=== 分析动态SQL ===");

        // 准备测试数据
        prepareDynamicSqlTestData();

        System.out.println("\n1. 只按名称查询:");
        List<User> usersByName = userMapper.findUsersByConditions("User 1", null);
        System.out.println("预期SQL:");
        System.out.println("  select id, email, name from users");
        System.out.println("  <where>");
        System.out.println("    <if test='name != null and name != \"\"'>");
        System.out.println("      name like #{name}");
        System.out.println("    </if>");
        System.out.println("  </where>");
        System.out.println("实际执行: select id, email, name from users WHERE name like ?");

        System.out.println("\n2. 只按邮箱查询:");
        List<User> usersByEmail = userMapper.findUsersByConditions(null, "%@test.com");
        System.out.println("预期SQL: select id, email, name from users WHERE email like ?");

        System.out.println("\n3. 按名称和邮箱查询:");
        List<User> usersByBoth = userMapper.findUsersByConditions("User 1", "%@test.com");
        System.out.println("预期SQL: select id, email, name from users WHERE name like ? and email like ?");

        System.out.println("\n4. 分页查询:");
        List<User> pageUsers = userMapper.findUsersWithPagination(5, 0);
        System.out.println("预期SQL: select id, email, name from users order by id offset ? rows fetch next ? rows only");
    }

    @Test
    public void analyzeBatchOperations() {
        System.out.println("\n=== 分析批量操作 ===");

        // 准备数据
        prepareBatchTestData();

        System.out.println("\n1. 单个删除操作:");
        long startTime = System.currentTimeMillis();
        for (int i = 1; i <= 5; i++) {
            userMapper.deleteById((long) i);
        }
        long singleDeleteTime = System.currentTimeMillis();
        System.out.println("单个删除耗时: " + (singleDeleteTime - startTime) + "ms");
        System.out.println("预期SQL: delete from users where id = ? (执行5次)");

        // 重新准备数据
        prepareBatchTestData();

        System.out.println("\n2. 批量删除操作:");
        startTime = System.currentTimeMillis();
        userMapper.deleteUsersByIdRange(1, 5);
        long batchDeleteTime = System.currentTimeMillis();
        System.out.println("批量删除耗时: " + (batchDeleteTime - startTime) + "ms");
        System.out.println("预期SQL: delete from users where id between ? and ?");
        System.out.println("性能提升: " + ((double)(singleDeleteTime - startTime) / (batchDeleteTime - startTime)) + "x");
    }

    @Test
    public void analyzeJoinQueryVsSeparateQueries() {
        System.out.println("\n=== 分析JOIN查询 vs 分别查询 ===");

        // 准备数据
        prepareJoinTestData();

        System.out.println("\n1. JOIN查询方式:");
        long startTime = System.currentTimeMillis();
        List<Order> ordersWithJoin = orderMapper.findOrdersWithItems();
        long joinTime = System.currentTimeMillis();
        int totalItems1 = ordersWithJoin.stream().mapToInt(order -> order.getItems().size()).sum();
        System.out.println("JOIN查询耗时: " + (joinTime - startTime) + "ms");
        System.out.println("订单数: " + ordersWithJoin.size() + ", 订单项数: " + totalItems1);
        System.out.println("预期SQL: 1次复杂的JOIN查询");

        System.out.println("\n2. 分别查询方式:");
        startTime = System.currentTimeMillis();
        List<Order> ordersOnly = orderMapper.findAllOrders();
        int totalItems2 = 0;
        for (Order order : ordersOnly) {
            List<OrderItem> items = orderMapper.findItemsByOrderIdList(order.getId());
            totalItems2 += items.size();
        }
        long separateTime = System.currentTimeMillis();
        System.out.println("分别查询耗时: " + (separateTime - startTime) + "ms");
        System.out.println("订单数: " + ordersOnly.size() + ", 订单项数: " + totalItems2);
        System.out.println("预期SQL: " + (1 + ordersOnly.size()) + " 次查询 (1次查订单 + N次查订单项)");

        System.out.println("\n性能对比:");
        System.out.println("JOIN查询 vs 分别查询性能比: " + ((double)(separateTime - startTime) / (joinTime - startTime)) + "x");
    }

    @Test
    public void analyzeExplicitVsImplicitOperations() {
        System.out.println("\n=== 分析显式 vs 隐式操作 ===");

        // 插入用户
        User user = new User();
        user.setEmail("explicit-test@example.com");
        user.setName("Explicit Test User");
        userMapper.insertUser(user);
        System.out.println("\n1. 插入操作:");
        System.out.println("MyBatis: 显式调用 userMapper.insertUser(user)");

        // 查询用户
        User foundUser = userMapper.findUserById(user.getId());
        System.out.println("\n2. 查询操作:");
        System.out.println("MyBatis: 显式调用 userMapper.findUserById(id)");

        // 修改用户
        foundUser.setName("Modified Name");
        System.out.println("\n3. 修改操作(不更新数据库):");
        System.out.println("MyBatis: 只修改对象状态，不自动同步到数据库");

        // 查询验证
        User unchangedUser = userMapper.findUserById(user.getId());
        System.out.println("验证: 数据库中的数据未改变 - " + unchangedUser.getName());

        // 显式更新
        userMapper.updateUser(foundUser);
        System.out.println("\n4. 显式更新:");
        System.out.println("MyBatis: 显式调用 userMapper.updateUser(user)");

        // 查询验证
        User updatedUser = userMapper.findUserById(user.getId());
        System.out.println("验证: 数据库中的数据已更新 - " + updatedUser.getName());

        System.out.println("\n总结: MyBatis要求显式操作，提供了完全的控制权");
    }

    @Test
    public void analyzeSqlControlFlexibility() {
        System.out.println("\n=== 分析SQL控制灵活性 ===");

        System.out.println("\n1. 简单统计查询:");
        long userCount = userMapper.countUsers();
        System.out.println("用户数量: " + userCount);
        System.out.println("预期SQL: select count(*) from users");

        System.out.println("\n2. 复杂统计查询:");
        long orderCount = orderMapper.countOrders();
        System.out.println("订单数量: " + orderCount);
        System.out.println("预期SQL: select count(*) from orders");

        System.out.println("\n3. 条件查询示例:");
        List<User> filteredUsers = userMapper.findUsersByNameLike("Test");
        System.out.println("包含'Test'的用户数: " + filteredUsers.size());
        System.out.println("预期SQL: select id, email, name from users where name like concat('%', ?, '%')");

        System.out.println("\n4. 自定义复杂查询:");
        System.out.println("MyBatis允许在XML中编写任意复杂的SQL，包括:");
        System.out.println("- 多表JOIN");
        System.out.println("- 子查询");
        System.out.println("- 条件逻辑");
        System.out.println("- 数据库特定函数");
        System.out.println("- 动态SQL");

        System.out.println("\n5. 数据库特定优化示例:");
        System.out.println("可以使用特定数据库的优化特性:");
        System.out.println("- MySQL: INDEX提示, FORCE INDEX");
        System.out.println("- PostgreSQL: 特定的分析函数");
        System.out.println("- Oracle: 分页优化语法");
        System.out.println("- SQL Server: OPTION查询提示");
    }

    private void prepareDynamicSqlTestData() {
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("user" + i + "@test.com");
            user.setName("User " + i);
            userMapper.insertUser(user);
        }
    }

    private void prepareBatchTestData() {
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("batch" + i + "@test.com");
            user.setName("Batch User " + i);
            userMapper.insertUser(user);
        }
    }

    private void prepareJoinTestData() {
        // 创建用户
        User user = new User();
        user.setEmail("join-test@example.com");
        user.setName("Join Test User");
        userMapper.insertUser(user);

        // 创建订单和订单项
        for (int i = 0; i < 5; i++) {
            Order order = new Order();
            order.setUserId(user.getId());
            order.setTotal(1000 * (i + 1));
            orderMapper.insertOrder(order);

            for (int j = 0; j < 3; j++) {
                OrderItem item = new OrderItem();
                item.setOrderId(order.getId());
                item.setSku("JOIN-SKU-" + i + "-" + j);
                item.setQty((i + 1) * (j + 1));
                orderMapper.insertOrderItem(item);
            }
        }
    }
}