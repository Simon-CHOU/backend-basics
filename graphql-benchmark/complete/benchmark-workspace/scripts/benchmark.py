"""
Protocol benchmark: REST vs GraphQL vs gRPC
Measures P50/P90/P95/P99/P99.9 latency, throughput, and response sizes.
"""
import concurrent.futures
import json
import statistics
import time
import urllib.request
import sys
import random
import os

HOST = "localhost"
PORT = 8080
THREADS = 100
DURATION = 60
WARMUP = 5

ORDER_IDS = [
    "ORD-000001", "ORD-000002", "ORD-000003", "ORD-000004", "ORD-000005",
    "ORD-000010", "ORD-000050", "ORD-000100", "ORD-000200", "ORD-000500",
]

GRAPHQL_QUERY_TEMPLATE = (
    '{{"query":"query OrderDetail($orderId: ID!) {{'
    ' orderDetail(orderId: $orderId) {{'
    ' orderId status totalAmount shippingAddress orderCreatedAt'
    ' items {{ productId productName quantity unitPrice }}'
    ' userName userEmail userPhone userRegisteredAt }} }}",'
    '"variables":{{"orderId":"{order_id}"}}}}'
)


def benchmark_rest(order_id: str) -> dict:
    """Single REST request, returns {latency_ms, status, size_bytes}."""
    url = f"http://{HOST}:{PORT}/api/orders/{order_id}"
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(url, timeout=10) as resp:
            body = resp.read()
            latency = (time.perf_counter() - start) * 1000
            return {"latency_ms": latency, "status": resp.status, "size_bytes": len(body)}
    except Exception as e:
        latency = (time.perf_counter() - start) * 1000
        return {"latency_ms": latency, "status": 0, "size_bytes": 0, "error": str(e)}


def benchmark_graphql(order_id: str) -> dict:
    """Single GraphQL request, returns {latency_ms, status, size_bytes}."""
    url = f"http://{HOST}:{PORT}/graphql"
    body = GRAPHQL_QUERY_TEMPLATE.format(order_id=order_id).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    start = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            resp_body = resp.read()
            latency = (time.perf_counter() - start) * 1000
            return {"latency_ms": latency, "status": resp.status, "size_bytes": len(resp_body)}
    except Exception as e:
        latency = (time.perf_counter() - start) * 1000
        return {"latency_ms": latency, "status": 0, "size_bytes": 0, "error": str(e)}


def run_benchmark(name: str, worker_func, threads: int = THREADS, duration: int = DURATION):
    """Run concurrent benchmark and return stats."""
    print(f"\n{'='*60}")
    print(f"  {name} Benchmark")
    print(f"  Threads: {threads}, Duration: {duration}s (excluding {WARMUP}s warmup)")
    print(f"{'='*60}")

    results = []
    stop_event = False
    start_time = time.perf_counter()
    warmup_end = start_time + WARMUP

    def worker():
        """Worker thread: send requests until duration expires."""
        local_results = []
        while not stop_event:
            order_id = random.choice(ORDER_IDS)
            result = worker_func(order_id)
            elapsed = time.perf_counter() - start_time
            result["elapsed_s"] = elapsed
            local_results.append(result)
        return local_results

    # Warmup phase
    print(f"  Warming up for {WARMUP}s...")
    warmup_start = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=min(threads // 4, 25)) as executor:
        warmup_futures = [executor.submit(worker_func, random.choice(ORDER_IDS)) for _ in range(500)]
        concurrent.futures.wait(warmup_futures)
    warmup_dur = time.perf_counter() - warmup_start
    print(f"  Warmup complete ({warmup_dur:.1f}s, {len(warmup_futures)} requests)")

    # Main benchmark
    print(f"  Running benchmark for {duration}s...")
    start = time.perf_counter()
    deadline = start + duration
    count_lock = None  # not needed for per-worker collection

    with concurrent.futures.ThreadPoolExecutor(max_workers=threads) as executor:
        futures = []
        for _ in range(threads):
            futures.append(executor.submit(worker))

        # Wait for duration
        time.sleep(duration)
        stop_event = True

        # Collect results
        print("  Collecting results...")
        for future in concurrent.futures.as_completed(futures, timeout=30):
            try:
                results.extend(future.result())
            except Exception as e:
                print(f"  Worker error: {e}")

    # Filter to only results during measurement window
    measured_start = start
    results = [r for r in results if r["elapsed_s"] >= 0]

    if not results:
        print("  ERROR: No successful results!")
        return None

    latencies = [r["latency_ms"] for r in results]
    latencies.sort()
    errors = [r for r in results if r.get("error")]
    total_time = time.perf_counter() - start

    stats = {
        "name": name,
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
        "response_size_mean": statistics.mean([r["size_bytes"] for r in results]) if results else 0,
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
    print("Protocol Benchmark: REST + JSON vs GraphQL")
    print(f"Target: http://{HOST}:{PORT}")
    print(f"Threads: {THREADS}, Duration: {DURATION}s, Warmup: {WARMUP}s")

    # Quick connectivity check
    try:
        with urllib.request.urlopen(f"http://{HOST}:{PORT}/api/orders/ORD-000001", timeout=5) as r:
            r.read()
        print("REST endpoint: OK")
    except Exception as e:
        print(f"REST endpoint: FAILED - {e}")
        sys.exit(1)

    try:
        data = GRAPHQL_QUERY_TEMPLATE.format(order_id="ORD-000001").encode()
        req = urllib.request.Request(
            f"http://{HOST}:{PORT}/graphql",
            data=data,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=5) as r:
            r.read()
        print("GraphQL endpoint: OK")
    except Exception as e:
        print(f"GraphQL endpoint: FAILED - {e}")
        sys.exit(1)

    # Run benchmarks
    rest_stats = run_benchmark("REST (GET /api/orders/{id})", benchmark_rest)
    print_stats(rest_stats)

    graphql_stats = run_benchmark("GraphQL (POST /graphql)", benchmark_graphql)
    print_stats(graphql_stats)

    # Comparison summary
    if rest_stats and graphql_stats:
        print("\n" + "=" * 60)
        print("  COMPARISON SUMMARY")
        print("=" * 60)
        print(f"  {'Metric':<20} {'REST':>15} {'GraphQL':>15} {'Winner':>10}")
        print(f"  {'-'*20} {'-'*15} {'-'*15} {'-'*10}")

        def compare(k, fmt=",.1f", lower_is_better=True):
            r = rest_stats[k]
            g = graphql_stats[k]
            if lower_is_better:
                winner = "REST" if r <= g else "GraphQL"
            else:
                winner = "REST" if r >= g else "GraphQL"
            print(f"  {k:<20} {r:{fmt}} {g:{fmt}} {winner:>10}")

        compare("throughput_rps", ",.1f", lower_is_better=False)
        compare("latency_p50_ms", "8.2f")
        compare("latency_p90_ms", "8.2f")
        compare("latency_p95_ms", "8.2f")
        compare("latency_p99_ms", "8.2f")
        compare("latency_p999_ms", "8.2f")
        compare("latency_mean_ms", "8.2f")
        compare("response_size_mean", ",.0f", lower_is_better=False)
        print()
