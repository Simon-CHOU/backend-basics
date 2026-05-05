package com.simon.benchmark.application;

import com.simon.benchmark.domain.Order;
import com.simon.benchmark.domain.OrderDetailView;
import com.simon.benchmark.domain.OrderItem;
import com.simon.benchmark.domain.User;
import com.simon.benchmark.infra.OrderRepository;
import com.simon.benchmark.infra.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailUseCase {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderDetailUseCase(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public OrderDetailView getOrderDetail(String orderId) {
        Order order = orderRepository.findById(orderId);
        if (order == null) {
            return null;
        }

        User user = userRepository.findById(order.getUserId());

        OrderDetailView view = new OrderDetailView();
        view.setOrderId(order.getOrderId());
        view.setStatus(order.getStatus());
        view.setTotalAmount(order.getTotalAmount());
        view.setShippingAddress(order.getShippingAddress());
        view.setOrderCreatedAt(order.getCreatedAt());

        if (order.getItems() != null) {
            List<OrderDetailView.OrderItemView> itemViews = order.getItems().stream()
                    .map(item -> new OrderDetailView.OrderItemView(
                            item.getProductId(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getUnitPrice()))
                    .toList();
            view.setItems(itemViews);
        }

        if (user != null) {
            view.setUserName(user.getName());
            view.setUserEmail(user.getEmail());
            view.setUserPhone(user.getPhone());
            view.setUserRegisteredAt(user.getRegisteredAt());
        }

        return view;
    }
}
