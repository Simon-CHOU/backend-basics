package com.simon.benchmark.grpc;

import com.simon.benchmark.grpc.stub.OrderDetailRequest;
import com.simon.benchmark.grpc.stub.OrderDetailResponse;
import com.simon.benchmark.grpc.stub.OrderDetailServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailGrpcService extends OrderDetailServiceGrpc.OrderDetailServiceImplBase {

    // TODO: 注入 OrderDetailUseCase

    // EXERCISE: 实现 gRPC Service
    // 1. 注入 OrderDetailUseCase
    // 2. 调用 useCase.getOrderDetail(request.getOrderId())
    // 3. 将 OrderDetailView 映射到 OrderDetailResponse (protobuf)
    // 4. 调用 responseObserver.onNext(builder.build()) + onCompleted()
    //
    // 映射要点：
    // - BigDecimal → String (toPlainString)
    // - Instant → String (toString)
    // - Enum → String (name())
    // - List<OrderItemView> → List<OrderItemMsg>
    @Override
    public void getOrderDetail(OrderDetailRequest request,
                                StreamObserver<OrderDetailResponse> responseObserver) {
        // TODO: 实现 gRPC 服务逻辑
        throw new UnsupportedOperationException("TODO: 实现 getOrderDetail");
    }
}
