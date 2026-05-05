package com.simon.benchmark.grpc;

import com.simon.benchmark.application.OrderDetailUseCase;
import com.simon.benchmark.domain.OrderDetailView;
import com.simon.benchmark.grpc.stub.OrderDetailRequest;
import com.simon.benchmark.grpc.stub.OrderDetailResponse;
import com.simon.benchmark.grpc.stub.OrderDetailServiceGrpc;
import com.simon.benchmark.grpc.stub.OrderItemMsg;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailGrpcService extends OrderDetailServiceGrpc.OrderDetailServiceImplBase {

    private final OrderDetailUseCase useCase;

    public OrderDetailGrpcService(OrderDetailUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public void getOrderDetail(OrderDetailRequest request, StreamObserver<OrderDetailResponse> responseObserver) {
        OrderDetailView view = useCase.getOrderDetail(request.getOrderId());

        OrderDetailResponse.Builder builder = OrderDetailResponse.newBuilder();
        if (view != null) {
            builder.setOrderId(view.getOrderId())
                    .setStatus(view.getStatus().name())
                    .setTotalAmount(view.getTotalAmount().toPlainString())
                    .setShippingAddress(view.getShippingAddress())
                    .setOrderCreatedAt(view.getOrderCreatedAt().toString());

            if (view.getItems() != null) {
                List<OrderItemMsg> items = view.getItems().stream()
                        .map(item -> OrderItemMsg.newBuilder()
                                .setProductId(item.getProductId())
                                .setProductName(item.getProductName())
                                .setQuantity(item.getQuantity())
                                .setUnitPrice(item.getUnitPrice().toPlainString())
                                .build())
                        .toList();
                builder.addAllItems(items);
            }

            if (view.getUserName() != null) {
                builder.setUserName(view.getUserName());
            }
            if (view.getUserEmail() != null) {
                builder.setUserEmail(view.getUserEmail());
            }
            if (view.getUserPhone() != null) {
                builder.setUserPhone(view.getUserPhone());
            }
            if (view.getUserRegisteredAt() != null) {
                builder.setUserRegisteredAt(view.getUserRegisteredAt().toString());
            }
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
