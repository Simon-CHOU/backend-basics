package com.simon.benchmark.application;

import com.simon.benchmark.domain.OrderDetailView;

public interface OrderDetailUseCase {
    OrderDetailView getOrderDetail(String orderId);
}
