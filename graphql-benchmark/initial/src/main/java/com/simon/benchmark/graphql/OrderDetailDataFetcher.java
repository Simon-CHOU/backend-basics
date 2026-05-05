package com.simon.benchmark.graphql;

import com.simon.benchmark.application.OrderDetailUseCase;
import com.simon.benchmark.domain.OrderDetailView;
import org.springframework.stereotype.Component;

// EXERCISE: 实现 GraphQL DataFetcher
// 步骤：
// 1. 添加 @DgsComponent 注解
// 2. 注入 OrderDetailUseCase
// 3. 创建查询方法，添加 @DgsQuery 注解
// 4. 方法参数使用 @InputArgument String orderId
// 5. 返回 OrderDetailView
//
// 注意：确保方法的返回类型和参数名与 schema/orderdetail.graphqls 中定义一致

@Component
public class OrderDetailDataFetcher {

    // TODO: 注入 OrderDetailUseCase
    // private final OrderDetailUseCase useCase;

    // TODO: 实现 DGS Query
    // @DgsQuery
    // public OrderDetailView orderDetail(@InputArgument String orderId) {
    //     return useCase.getOrderDetail(orderId);
    // }
}
