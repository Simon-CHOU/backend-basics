package com.simon.jpa;

import com.simon.jpa.domain.Order;
import com.simon.jpa.domain.OrderItem;
import com.simon.jpa.domain.User;
import com.simon.jpa.repo.OrderRepository;
import com.simon.jpa.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab5_CascadeOrphanRemovalTest {
    @Autowired UserRepository users;
    @Autowired OrderRepository orders;
    @PersistenceContext EntityManager em;

    @Test
    @Transactional
    void AC_orphanRemovalDeletesChildOnDetachFromParent() {
        User u = new User();
        u.setEmail("c@x.com");
        u.setName("C");
        users.save(u);

        Order o = new Order();
        o.setUser(u);
        OrderItem i1 = new OrderItem(); i1.setSku("A"); i1.setQty(1);
        OrderItem i2 = new OrderItem(); i2.setSku("B"); i2.setQty(2);
        o.addItem(i1); o.addItem(i2);
        orders.save(o);

        em.flush(); em.clear();

        Order agg = orders.findById(o.getId()).orElseThrow();
        OrderItem toRemove = agg.getItems().iterator().next();
        agg.removeItem(toRemove);
        orders.save(agg);
        em.flush(); em.clear();

        Order re = orders.findWithItemsById(o.getId()).orElseThrow();
        assertEquals(1, re.getItems().size());
    }
}

