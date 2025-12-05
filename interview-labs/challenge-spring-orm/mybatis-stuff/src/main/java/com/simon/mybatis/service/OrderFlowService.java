package com.simon.mybatis.service;

import com.simon.mybatis.domain.Order;
import com.simon.mybatis.domain.OrderItem;
import com.simon.mybatis.domain.User;
import com.simon.mybatis.mapper.OrderMapper;
import com.simon.mybatis.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderFlowService {
    private final UserMapper users;
    private final OrderMapper orders;

    public OrderFlowService(UserMapper users, OrderMapper orders) {
        this.users = users;
        this.orders = orders;
    }

    @Transactional
    public Long createUserWithOrder(String email, String name) {
        User u = new User(); u.setEmail(email); u.setName(name);
        users.insert(u);

        Order o = new Order(); o.setUserId(u.getId()); o.setTotal(0);
        orders.insertOrder(o);

        OrderItem i1 = new OrderItem(); i1.setOrderId(o.getId()); i1.setSku("SKU-A"); i1.setQty(1);
        OrderItem i2 = new OrderItem(); i2.setOrderId(o.getId()); i2.setSku("SKU-B"); i2.setQty(2);
        orders.insertItem(i1); orders.insertItem(i2);
        return o.getId();
    }

    @Transactional(readOnly = true)
    public Order loadOrderWithItems(Long orderId) {
        return orders.findOrderWithItemsById(orderId);
    }

    @Transactional
    public void renameUser(Long userId, String newName) {
        users.updateNameById(userId, newName);
    }
}

