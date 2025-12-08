package com.simon.mybatis.mapper;

import com.simon.mybatis.domain.Order;
import com.simon.mybatis.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    void insertOrderItem(OrderItem item);
    void insertItem(OrderItem item);
    Order findOrderWithItemsById(@Param("id") Long id);
    List<Order> findOrdersWithItems();
    List<Order> findAllOrders();
    OrderItem findItemsByOrderId(@Param("orderId") Long orderId);
    List<OrderItem> findItemsByOrderIdList(@Param("orderId") Long orderId);
    long countOrders();
    void deleteAllOrders();
    void deleteAllItems();
    void deleteOrdersByIdRange(@Param("startId") long startId, @Param("endId") long endId);
    void deleteItemsByOrderIdRange(@Param("startId") long startId, @Param("endId") long endId);
}

