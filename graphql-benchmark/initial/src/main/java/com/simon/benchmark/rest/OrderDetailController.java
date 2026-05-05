package com.simon.benchmark.rest;

import com.simon.benchmark.application.OrderDetailUseCase;
import com.simon.benchmark.domain.OrderDetailView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

// EXERCISE: 实现 REST 端点
// 步骤：
// 1. 添加 @GetMapping 注解，路径为 /api/orders/{orderId}
// 2. 注入 OrderDetailUseCase
// 3. 调用 useCase.getOrderDetail(orderId)
// 4. 如果返回 null，返回 404；否则返回 200 + OrderDetailView

@RestController
public class OrderDetailController {

    // TODO: 注入 OrderDetailUseCase
    // private final OrderDetailUseCase useCase;

    // TODO: 实现 GET /api/orders/{orderId}
    // @GetMapping("/api/orders/{orderId}")
    // public ResponseEntity<OrderDetailView> getOrderDetail(@PathVariable String orderId) {
    //     ...
    // }
}
