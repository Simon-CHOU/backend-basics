"""
gRPC benchmark using generated Python stubs.
Tests the same OrderDetailService/GetOrderDetail endpoint.
"""
import sys
import os

# Add the stubs directory to path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "grpc_stubs"))

import concurrent.futures
import grpc
import orderdetail_pb2
import orderdetail_pb2_grpc
import random
import statistics
import time

HOST = "localhost"
PORT = 9090
THREADS = 100
DURATION = 60
WARMUP = 5

ORDER_IDS = [
    "ORD-000001", "ORD-000002", "ORD-000003", "ORD-000004", "ORD-000005",
    "ORD-000010", "ORD-000050", "ORD-000100", "ORD-000200", "ORD-000500",
]


def benchmark_grpc():
    """Run full gRPC benchmark and return stats."""
    print(f"\n{'='*60}")
    print(f"  gRPC (OrderDetailService/GetOrderDetail) Benchmark")
    print(f"  Threads: {THREADS}, Duration: {DURATION}s (excluding {WARMUP}s warmup)")
    print(f"{'='*60}")

    # Create channel pool (one per thread)
    channels = []
    stubs = []
    print("  Creating connection pool...")
    for i in range(THREADS):
        channel = grpc.insecure_channel(f"{HOST}:{PORT}")
        stub = orderdetail_pb2_grpc.OrderDetailServiceStub(channel)
        channels.append(channel)
        stubs.append(stub)
    print(f"  Created {len(channels)} channels")

    results = []
    stop_event = False
    start_time = time.perf_counter()

    def worker(worker_id: int):
        stub = stubs[worker_id]
        local_results = []
        while not stop_event:
            order_id = random.choice(ORDER_IDS)
            request = orderdetail_pb2.OrderDetailRequest(order_id=order_id)
            start = time.perf_counter()
            try:
                response = stub.GetOrderDetail(request, timeout=10)
                latency = (time.perf_counter() - start) * 1000
                size = response.ByteSize()
                elapsed = time.perf_counter() - start_time
                local_results.append({
                    "latency_ms": latency,
                    "status": 0,  # gRPC OK
                    "size_bytes": size,
                    "elapsed_s": elapsed,
                })
            except Exception as e:
                latency = (time.perf_counter() - start) * 1000
                elapsed = time.perf_counter() - start_time
                local_results.append({
                    "latency_ms": latency,
                    "status": -1,
                    "size_bytes": 0,
                    "elapsed_s": elapsed,
                    "error": str(e),
                })
        return local_results

    # Warmup phase
    print(f"  Warming up for {WARMUP}s...")
    warmup_start = time.perf_counter()
    warmup_stub = stubs[0]
    for _ in range(500):
        request = orderdetail_pb2.OrderDetailRequest(order_id=random.choice(ORDER_IDS))
        try:
            warmup_stub.GetOrderDetail(request, timeout=10)
        except Exception:
            pass
    warmup_dur = time.perf_counter() - warmup_start
    print(f"  Warmup complete ({warmup_dur:.1f}s, 500 requests)")

    # Connectivity check
    print("  Checking connectivity...")
    try:
        request = orderdetail_pb2.OrderDetailRequest(order_id="ORD-000001")
        response = warmup_stub.GetOrderDetail(request, timeout=10)
        print(f"  gRPC endpoint: OK (orderId={response.order_id}, status={response.status})")
    except Exception as e:
        print(f"  gRPC endpoint: FAILED - {e}")
        return None

    # Main benchmark
    print(f"  Running benchmark for {DURATION}s...")
    start = time.perf_counter()

    with concurrent.futures.ThreadPoolExecutor(max_workers=THREADS) as executor:
        futures = [executor.submit(worker, i) for i in range(THREADS)]

        time.sleep(DURATION)
        stop_event = True

        print("  Collecting results...")
        for future in concurrent.futures.as_completed(futures, timeout=30):
            try:
                results.extend(future.result())
            except Exception as e:
                print(f"  Worker error: {e}")

    # Cleanup
    for channel in channels:
        channel.close()

    if not results:
        print("  ERROR: No results!")
        return None

    latencies = [r["latency_ms"] for r in results if r["status"] == 0]
    latencies.sort()
    errors = [r for r in results if r["status"] != 0]
    total_time = time.perf_counter() - start

    stats = {
        "name": "gRPC (OrderDetailService/GetOrderDetail)",
        "total_requests": len(results),
        "errors": len(errors),
        "error_rate": len(errors) / len(results) * 100 if results else 0,
        "duration_s": total_time,
        "throughput_rps": len(results) / total_time,
        "latency_p50_ms": latencies[int(len(latencies) * 0.50)] if latencies else 0,
        "latency_p90_ms": latencies[int(len(latencies) * 0.90)] if latencies else 0,
        "latency_p95_ms": latencies[int(len(latencies) * 0.95)] if latencies else 0,
        "latency_p99_ms": latencies[int(len(latencies) * 0.99)] if latencies else 0,
        "latency_p999_ms": latencies[int(len(latencies) * 0.999)] if latencies else 0,
        "latency_min_ms": min(latencies) if latencies else 0,
        "latency_max_ms": max(latencies) if latencies else 0,
        "latency_mean_ms": statistics.mean(latencies) if latencies else 0,
        "latency_stdev_ms": statistics.stdev(latencies) if len(latencies) > 1 else 0,
        "response_size_mean": statistics.mean([r["size_bytes"] for r in results if r["status"] == 0]) if results else 0,
    }

    return stats


def print_stats(stats):
    """Pretty print benchmark stats."""
    if stats is None:
        return
    print(f"\n  ----- {stats['name']} Results -----")
    print(f"  Total Requests:   {stats['total_requests']:,}")
    print(f"  Errors:           {stats['errors']} ({stats['error_rate']:.2f}%)")
    print(f"  Duration:         {stats['duration_s']:.1f}s")
    print(f"  Throughput:       {stats['throughput_rps']:,.1f} req/s")
    print(f"  Response Size:    {stats['response_size_mean']:,.0f} bytes (mean)")
    print()
    print(f"  Latency (ms):")
    print(f"    Min:     {stats['latency_min_ms']:8.2f}")
    print(f"    Mean:    {stats['latency_mean_ms']:8.2f}")
    print(f"    P50:     {stats['latency_p50_ms']:8.2f}")
    print(f"    P90:     {stats['latency_p90_ms']:8.2f}")
    print(f"    P95:     {stats['latency_p95_ms']:8.2f}")
    print(f"    P99:     {stats['latency_p99_ms']:8.2f}")
    print(f"    P99.9:   {stats['latency_p999_ms']:8.2f}")
    print(f"    Max:     {stats['latency_max_ms']:8.2f}")
    print(f"    StdDev:  {stats['latency_stdev_ms']:8.2f}")
    print()


if __name__ == "__main__":
    print("gRPC Benchmark")
    print(f"Target: {HOST}:{PORT}")
    print(f"Threads: {THREADS}, Duration: {DURATION}s, Warmup: {WARMUP}s")
    print()

    stats = benchmark_grpc()
    print_stats(stats)

    if stats:
        # Print JSON for easy machine reading
        import json
        print("\n--- RAW JSON ---")
        print(json.dumps(stats, indent=2))
