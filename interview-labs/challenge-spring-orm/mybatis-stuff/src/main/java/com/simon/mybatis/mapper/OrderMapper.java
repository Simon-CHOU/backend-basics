package com.simon.mybatis.mapper;

import com.simon.mybatis.domain.Order;
import com.simon.mybatis.domain.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    void insertOrder(Order order);
    void insertItem(OrderItem item);
    Order findOrderWithItemsById(@Param("id") Long id);
}

