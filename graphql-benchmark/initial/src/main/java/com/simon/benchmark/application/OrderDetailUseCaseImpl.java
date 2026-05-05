package com.simon.benchmark.application;

import com.simon.benchmark.domain.Order;
import com.simon.benchmark.domain.OrderDetailView;
import com.simon.benchmark.domain.User;
import com.simon.benchmark.infra.OrderRepository;
import com.simon.benchmark.infra.UserRepository;
import org.springframework.stereotype.Service;

// EXERCISE: 实现聚合逻辑，将 Order + User 合并为 OrderDetailView
// 步骤：
// 1. 通过 orderId 查询 Order
// 2. 通过 order.userId 查询 User
// 3. 将 Order 的字段映射到 OrderDetailView
// 4. 将 Order.items 映射为 OrderDetailView.OrderItemView 列表
// 5. 将 User 信息映射到 OrderDetailView 的用户字段
// 6. 如果 order 不存在，返回 null

@Service
public class OrderDetailUseCaseImpl implements OrderDetailUseCase {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderDetailUseCaseImpl(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OrderDetailView getOrderDetail(String orderId) {
        // TODO: 实现聚合逻辑
        // Order order = orderRepository.findById(orderId);
        // if (order == null) return null;
        // User user = userRepository.findById(order.getUserId());
        // OrderDetailView view = new OrderDetailView();
        // ... 映射字段 ...
        // return view;
        throw new UnsupportedOperationException("TODO: 实现 getOrderDetail");
    }
}
