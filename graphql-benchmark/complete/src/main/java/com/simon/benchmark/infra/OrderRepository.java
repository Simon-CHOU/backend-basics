package com.simon.benchmark.infra;

import com.simon.benchmark.domain.Order;
import com.simon.benchmark.domain.OrderItem;
import com.simon.benchmark.domain.OrderStatus;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {

    private static final Map<String, Order> STORE = new ConcurrentHashMap<>();

    static {
        for (int i = 1; i <= 1000; i++) {
            String orderId = "ORD-" + String.format("%06d", i);
            String userId = "USR-" + String.format("%04d", (i % 200) + 1);
            List<OrderItem> items = List.of(
                    new OrderItem("PROD-001", "Wireless Mouse", 2, new BigDecimal("29.99")),
                    new OrderItem("PROD-042", "Mechanical Keyboard", 1, new BigDecimal("149.00")),
                    new OrderItem("PROD-108", "USB-C Hub", 1, new BigDecimal("49.50"))
            );
            Order order = new Order(
                    orderId, userId, OrderStatus.CONFIRMED,
                    new BigDecimal("258.48"), "123 Main St, Suite 100, Tech City, 94043",
                    Instant.parse("2026-05-01T10:15:30Z"), items
            );
            STORE.put(orderId, order);
        }
    }

    public Order findById(String orderId) {
        return STORE.get(orderId);
    }
}
