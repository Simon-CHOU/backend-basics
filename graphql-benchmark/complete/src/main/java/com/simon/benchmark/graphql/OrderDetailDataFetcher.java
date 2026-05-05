package com.simon.benchmark.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.simon.benchmark.application.OrderDetailUseCase;
import com.simon.benchmark.domain.OrderDetailView;

@DgsComponent
public class OrderDetailDataFetcher {

    private final OrderDetailUseCase useCase;

    public OrderDetailDataFetcher(OrderDetailUseCase useCase) {
        this.useCase = useCase;
    }

    @DgsQuery
    public OrderDetailView orderDetail(@InputArgument String orderId) {
        return useCase.getOrderDetail(orderId);
    }
}
