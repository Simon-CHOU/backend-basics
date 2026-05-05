# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project purpose

Protocol comparison benchmark: the same BFF "order detail + user info" endpoint implemented three ways — REST+JSON, GraphQL (Netflix DGS), gRPC+protobuf — to measure P99 latency, memory, and serialization CPU overhead. Conclusions must come from measurement data, not pre-formed opinions.

```
complete/   # Full working reference implementation
initial/    # Exercise skeleton with TODOs for deliberate practice
```

## Build & run

There is no Maven wrapper. Use the system `mvn`.

```bash
# Build (compile + run protobuf plugin to generate gRPC stubs)
cd complete
mvn clean package -DskipTests

# Run
java -jar target/protocol-benchmark-1.0.0.jar

# Run with controlled JVM for benchmarking
java -Xms256m -Xmx256m -XX:+UseG1GC -jar target/protocol-benchmark-1.0.0.jar
```

## Test

```bash
mvn test          # runs ProtocolBenchmarkApplicationTests (contextLoads smoke test)
```

## Verify endpoints

```bash
# REST
curl -s http://localhost:8080/api/orders/ORD-000001

# GraphQL (also browser: http://localhost:8080/graphiql)
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ orderDetail(orderId:\"ORD-000500\") { orderId status totalAmount userName userEmail } }"}'

# gRPC (requires ghz tool)
ghz --insecure --proto src/main/proto/orderdetail.proto \
  --call benchmark.OrderDetailService/GetOrderDetail \
  -d '{"order_id":"ORD-000500"}' -n 100 -c 10 localhost:9090
```

## Benchmark

```bash
# REST (JMeter)
jmeter -n -t jmeter/rest-benchmark.jmx -Jthreads=100 -Jduration=60 -l results/rest.csv -e -o results/rest-report

# GraphQL (JMeter)
jmeter -n -t jmeter/graphql-benchmark.jmx -Jthreads=100 -Jduration=60 -l results/graphql.csv -e -o results/graphql-report

# gRPC (ghz)
ghz --insecure --proto src/main/proto/orderdetail.proto \
  --call benchmark.OrderDetailService/GetOrderDetail \
  -d '{"order_id":"ORD-000500"}' -n 100000 -c 100 localhost:9090
```

## Architecture

The core design principle: **shared application logic, protocol-specific adapters**.

```
ProtocolBenchmarkApplication (Spring Boot, port 8080)
├── rest/OrderDetailController      @RestController  → GET /api/orders/{id}
├── graphql/OrderDetailDataFetcher  @DgsQuery        → Query.orderDetail
├── grpc/OrderDetailGrpcService     gRPC Service     → port 9090
└── grpc/GrpcServerRunner           starts gRPC server on @PostConstruct

All three adapters call:
  → application/OrderDetailUseCase.getOrderDetail(orderId)
    → infra/OrderRepository (ConcurrentHashMap, 1000 orders)
    → infra/UserRepository  (ConcurrentHashMap, 200 users)
    → returns domain/OrderDetailView (DTO)
```

The `OrderDetailUseCase` is the single aggregation point — it fetches Order + User from in-memory repos and maps them into an `OrderDetailView`. Each protocol adapter only handles its own wire format:
- **REST**: Jackson auto-serializes `OrderDetailView` → JSON
- **GraphQL**: DGS matches `OrderDetailView` fields against `schema/orderdetail.graphqls`
- **gRPC**: Manual `Builder` mapping from `OrderDetailView` → protobuf `OrderDetailResponse`

## Protobuf stub generation

gRPC stubs are generated from `src/main/proto/orderdetail.proto` into `target/generated-sources/protobuf/`. After `mvn compile`, IDE must mark that directory as Generated Sources. The generated Java package is `com.simon.benchmark.grpc.stub`.

## Data model

- `Order`: orderId, userId, status (enum), totalAmount, shippingAddress, createdAt, items
- `User`: id, name, email, phone, registeredAt
- `OrderDetailView`: flattened aggregation of Order fields + User fields, with nested `OrderItemView` list
- All data is pre-populated in static initializers — no database, no external dependencies

## Port allocations

- `8080`: Spring Boot (REST + GraphQL + GraphiQL)
- `9090`: gRPC server (separate Netty server, starts via `@PostConstruct`)

## Logging

Set to `WARN` globally in `application.yml` to avoid I/O noise in benchmarks. For debugging, temporarily lower to `DEBUG` for `com.simon.benchmark`.

## TDD discipline (from .trae rules)

- Red-Green-Refactor cycle
- McCabe cyclomatic complexity ≥ 20 → refactor
- JaCoCo coverage thresholds: line ≥ 85%, branch ≥ 80%, method ≥ 90%
- No watered-down tests or faking coverage
- Format before commit; follow Google Code Style Guide
- All unit tests pass before e2e testing
- Use Playwright for e2e tests
