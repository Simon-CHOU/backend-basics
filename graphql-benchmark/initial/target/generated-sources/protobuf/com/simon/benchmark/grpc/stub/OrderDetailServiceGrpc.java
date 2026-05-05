package com.simon.benchmark.grpc.stub;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.64.0)",
    comments = "Source: orderdetail.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class OrderDetailServiceGrpc {

  private OrderDetailServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "benchmark.OrderDetailService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.simon.benchmark.grpc.stub.OrderDetailRequest,
      com.simon.benchmark.grpc.stub.OrderDetailResponse> getGetOrderDetailMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetOrderDetail",
      requestType = com.simon.benchmark.grpc.stub.OrderDetailRequest.class,
      responseType = com.simon.benchmark.grpc.stub.OrderDetailResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.simon.benchmark.grpc.stub.OrderDetailRequest,
      com.simon.benchmark.grpc.stub.OrderDetailResponse> getGetOrderDetailMethod() {
    io.grpc.MethodDescriptor<com.simon.benchmark.grpc.stub.OrderDetailRequest, com.simon.benchmark.grpc.stub.OrderDetailResponse> getGetOrderDetailMethod;
    if ((getGetOrderDetailMethod = OrderDetailServiceGrpc.getGetOrderDetailMethod) == null) {
      synchronized (OrderDetailServiceGrpc.class) {
        if ((getGetOrderDetailMethod = OrderDetailServiceGrpc.getGetOrderDetailMethod) == null) {
          OrderDetailServiceGrpc.getGetOrderDetailMethod = getGetOrderDetailMethod =
              io.grpc.MethodDescriptor.<com.simon.benchmark.grpc.stub.OrderDetailRequest, com.simon.benchmark.grpc.stub.OrderDetailResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetOrderDetail"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.simon.benchmark.grpc.stub.OrderDetailRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.simon.benchmark.grpc.stub.OrderDetailResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OrderDetailServiceMethodDescriptorSupplier("GetOrderDetail"))
              .build();
        }
      }
    }
    return getGetOrderDetailMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OrderDetailServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderDetailServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderDetailServiceStub>() {
        @java.lang.Override
        public OrderDetailServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderDetailServiceStub(channel, callOptions);
        }
      };
    return OrderDetailServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OrderDetailServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderDetailServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderDetailServiceBlockingStub>() {
        @java.lang.Override
        public OrderDetailServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderDetailServiceBlockingStub(channel, callOptions);
        }
      };
    return OrderDetailServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OrderDetailServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderDetailServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderDetailServiceFutureStub>() {
        @java.lang.Override
        public OrderDetailServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderDetailServiceFutureStub(channel, callOptions);
        }
      };
    return OrderDetailServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getOrderDetail(com.simon.benchmark.grpc.stub.OrderDetailRequest request,
        io.grpc.stub.StreamObserver<com.simon.benchmark.grpc.stub.OrderDetailResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetOrderDetailMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service OrderDetailService.
   */
  public static abstract class OrderDetailServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return OrderDetailServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service OrderDetailService.
   */
  public static final class OrderDetailServiceStub
      extends io.grpc.stub.AbstractAsyncStub<OrderDetailServiceStub> {
    private OrderDetailServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderDetailServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderDetailServiceStub(channel, callOptions);
    }

    /**
     */
    public void getOrderDetail(com.simon.benchmark.grpc.stub.OrderDetailRequest request,
        io.grpc.stub.StreamObserver<com.simon.benchmark.grpc.stub.OrderDetailResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetOrderDetailMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service OrderDetailService.
   */
  public static final class OrderDetailServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<OrderDetailServiceBlockingStub> {
    private OrderDetailServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderDetailServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderDetailServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.simon.benchmark.grpc.stub.OrderDetailResponse getOrderDetail(com.simon.benchmark.grpc.stub.OrderDetailRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetOrderDetailMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service OrderDetailService.
   */
  public static final class OrderDetailServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<OrderDetailServiceFutureStub> {
    private OrderDetailServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderDetailServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderDetailServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.simon.benchmark.grpc.stub.OrderDetailResponse> getOrderDetail(
        com.simon.benchmark.grpc.stub.OrderDetailRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetOrderDetailMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_ORDER_DETAIL = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_ORDER_DETAIL:
          serviceImpl.getOrderDetail((com.simon.benchmark.grpc.stub.OrderDetailRequest) request,
              (io.grpc.stub.StreamObserver<com.simon.benchmark.grpc.stub.OrderDetailResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetOrderDetailMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.simon.benchmark.grpc.stub.OrderDetailRequest,
              com.simon.benchmark.grpc.stub.OrderDetailResponse>(
                service, METHODID_GET_ORDER_DETAIL)))
        .build();
  }

  private static abstract class OrderDetailServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OrderDetailServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.simon.benchmark.grpc.stub.OrderDetailProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OrderDetailService");
    }
  }

  private static final class OrderDetailServiceFileDescriptorSupplier
      extends OrderDetailServiceBaseDescriptorSupplier {
    OrderDetailServiceFileDescriptorSupplier() {}
  }

  private static final class OrderDetailServiceMethodDescriptorSupplier
      extends OrderDetailServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    OrderDetailServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OrderDetailServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OrderDetailServiceFileDescriptorSupplier())
              .addMethod(getGetOrderDetailMethod())
              .build();
        }
      }
    }
    return result;
  }
}
