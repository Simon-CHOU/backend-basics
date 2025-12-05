package com.simon.jpa.service;

import com.simon.jpa.domain.Order;
import com.simon.jpa.domain.OrderItem;
import com.simon.jpa.domain.User;
import com.simon.jpa.dto.UserSummary;
import com.simon.jpa.repo.OrderRepository;
import com.simon.jpa.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderFlowService {
    private final UserRepository users;
    private final OrderRepository orders;

    public OrderFlowService(UserRepository users, OrderRepository orders) {
        this.users = users;
        this.orders = orders;
    }

    @Transactional
    public Long createUserWithOrder(String email, String name) {
        User u = new User();
        u.setEmail(email);
        u.setName(name);
        users.save(u);

        Order o = new Order();
        o.setUser(u);
        o.setTotal(0);
        OrderItem i1 = new OrderItem(); i1.setSku("SKU-A"); i1.setQty(1);
        OrderItem i2 = new OrderItem(); i2.setSku("SKU-B"); i2.setQty(2);
        o.addItem(i1); o.addItem(i2);
        orders.save(o);
        return o.getId();
    }

    @Transactional(readOnly = true)
    public Order loadOrderWithItems(Long orderId) {
        return orders.joinFetchItemsById(orderId).orElseThrow();
    }

    @Transactional
    public void renameUser(Long userId, String newName) {
        User u = users.findById(userId).orElseThrow();
        u.setName(newName);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> listSummaries() {
        return users.findAllSummaries();
    }
}

