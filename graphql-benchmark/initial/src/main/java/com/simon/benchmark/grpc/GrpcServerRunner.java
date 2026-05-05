package com.simon.benchmark.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerRunner {

    private final OrderDetailGrpcService orderDetailService;
    private Server server;

    public GrpcServerRunner(OrderDetailGrpcService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }

    @PostConstruct
    public void start() throws Exception {
        server = ServerBuilder.forPort(9090)
                .addService(orderDetailService)
                .build()
                .start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
