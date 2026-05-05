package com.simon.benchmark.rest;

import com.simon.benchmark.application.OrderDetailUseCase;
import com.simon.benchmark.domain.OrderDetailView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderDetailController {

    private final OrderDetailUseCase useCase;

    public OrderDetailController(OrderDetailUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailView> getOrderDetail(@PathVariable String orderId) {
        OrderDetailView view = useCase.getOrderDetail(orderId);
        if (view == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(view);
    }
}
