package com.simon.mybatis;

import com.simon.mybatis.domain.Order;
import com.simon.mybatis.domain.OrderItem;
import com.simon.mybatis.domain.User;
import com.simon.mybatis.mapper.OrderMapper;
import com.simon.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LabC_JoinAndCollectionMappingTest {
    @Autowired UserMapper users;
    @Autowired OrderMapper orders;

    @Test
    void AC_joinQueryMapsItemsIntoOrder() {
        User u = new User();
        u.setEmail("o@x.com");
        u.setName("O");
        users.insert(u);

        Order o = new Order();
        o.setUserId(u.getId());
        o.setTotal(0);
        orders.insertOrder(o);

        OrderItem i1 = new OrderItem(); i1.setOrderId(o.getId()); i1.setSku("A"); i1.setQty(1);
        OrderItem i2 = new OrderItem(); i2.setOrderId(o.getId()); i2.setSku("B"); i2.setQty(2);
        orders.insertItem(i1); orders.insertItem(i2);

        Order loaded = orders.findOrderWithItemsById(o.getId());
        assertNotNull(loaded);
        assertEquals(2, loaded.getItems().size());
    }
}

