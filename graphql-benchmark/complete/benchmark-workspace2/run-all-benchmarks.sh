#!/usr/bin/env bash
# =============================================================================
# Protocol Benchmark — One-Click Runner (benchmark-workspace2)
# =============================================================================
# Usage:
#   ./run-all-benchmarks.sh              # build → start → benchmark → stop → clean → report
#   ./run-all-benchmarks.sh --skip-build # skip mvn package
#   ./run-all-benchmarks.sh --clean      # only stop server and clean intermediates
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WORKSPACE_DIR="$SCRIPT_DIR"
COMPLETE_DIR="$(dirname "$SCRIPT_DIR")"
JMETER_DIR="$WORKSPACE_DIR/jmeter"
SCRIPTS_DIR="$WORKSPACE_DIR/scripts"
RESULTS_DIR="$WORKSPACE_DIR/results"
JAVA_HOME="${JAVA_HOME:-/c/Program Files/Java/jdk-21}"
JAR_FILE="$COMPLETE_DIR/target/protocol-benchmark-1.0.0.jar"

THREADS=100
DURATION=60
WARMUP=5
JVM_OPTS="-Xms256m -Xmx256m -XX:+UseG1GC"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ---------- helpers ----------
banner()  { echo -e "\n${BOLD}${CYAN}==== $* ====${NC}"; }
ok()      { echo -e "${GREEN}[OK]${NC} $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC} $*"; }
fail()    { echo -e "${RED}[FAIL]${NC} $*"; exit 1; }
step()    { echo -e "\n${BOLD}[$1/$STEPS]${NC} $2"; }
info()    { echo "  $*"; }

# ---------- parse args ----------
SKIP_BUILD=false
CLEAN_ONLY=false
for arg in "$@"; do
  case $arg in
    --skip-build) SKIP_BUILD=true ;;
    --clean)      CLEAN_ONLY=true ;;
    *)            echo "Unknown arg: $arg"; exit 1 ;;
  esac
done

# ---------- cleanup handler ----------
cleanup() {
  echo ""
  warn "Interrupted — stopping server and cleaning up..."
  stop_server
  clean_intermediates
  exit 130
}
trap cleanup INT TERM

# ---------- stop server ----------
stop_server() {
  local pid
  pid=$(ps aux 2>/dev/null | grep '[p]rotocol-benchmark' | awk '{print $1}' || true)
  if [ -n "$pid" ]; then
    kill "$pid" 2>/dev/null || true
    sleep 2
    kill -9 "$pid" 2>/dev/null || true
    ok "Server stopped (PID $pid)"
  fi
  local p8080 p9090
  p8080=$(netstat -ano 2>/dev/null | grep ':8080.*LISTENING' | awk '{print $NF}' || true)
  p9090=$(netstat -ano 2>/dev/null | grep ':9090.*LISTENING' | awk '{print $NF}' || true)
  for p in $p8080 $p9090; do
    taskkill //F //PID "$p" 2>/dev/null || true
  done
}

# ---------- clean intermediate files ----------
clean_intermediates() {
  banner "Cleaning intermediate files"
  rm -f "$RESULTS_DIR"/*.csv
  rm -f "$RESULTS_DIR"/grpc-output.txt
  rm -f "$RESULTS_DIR"/*.stats
  rm -f "$WORKSPACE_DIR"/jmeter.log
  ok "Cleaned raw CSVs, jmeter.log"
  ok "Kept: server-*.log, jstat.log"
}

# ---------- wait for server ----------
wait_for_server() {
  local max_wait=30 waited=0
  echo -n "  Waiting for server..."
  while [ $waited -lt $max_wait ]; do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/orders/ORD-000001 2>/dev/null | grep -q 200; then
      echo ""
      ok "Server ready (port 8080)"
      return 0
    fi
    sleep 1
    waited=$((waited + 1))
    echo -n "."
  done
  echo ""
  fail "Server did not start within ${max_wait}s"
}

# ---------- verify endpoints ----------
verify_endpoints() {
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/orders/ORD-000001 2>/dev/null || echo "000")
  [ "$code" = "200" ] || fail "REST endpoint not ready (HTTP $code)"
  ok "REST endpoint OK"

  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/graphql \
    -H "Content-Type: application/json" \
    -d '{"query":"{ orderDetail(orderId:\"ORD-000001\") { orderId } }"}' 2>/dev/null || echo "000")
  [ "$code" = "200" ] || warn "GraphQL endpoint returned HTTP $code"
  ok "GraphQL endpoint OK"
}

# ---------- parse JMeter CSV → stats file ----------
parse_jmeter_csv_to_file() {
  local csv_file=$1 stats_file=$2 label=$3
  python -c "
import csv, statistics, sys, json
latencies = []
errors = 0
total = 0
sizes = []
with open(r'$csv_file', 'r') as f:
    reader = csv.DictReader(f)
    for row in reader:
        total += 1
        if row['success'] == 'true':
            latencies.append(int(row['elapsed']))
            sizes.append(int(row['bytes']))
        else:
            errors += 1
if not latencies:
    print('ERROR: no successful requests', file=sys.stderr)
    sys.exit(1)
latencies.sort()
n = len(latencies)
stats = {
    'label': '$label',
    'total_requests': total,
    'errors': errors,
    'error_rate': errors/total*100 if total else 0,
    'throughput_rps': total / (${DURATION} + ${WARMUP}),
    'response_size_mean': statistics.mean(sizes),
    'latency_min': min(latencies),
    'latency_mean': statistics.mean(latencies),
    'latency_p50': latencies[int(n*0.50)],
    'latency_p90': latencies[int(n*0.90)],
    'latency_p95': latencies[int(n*0.95)],
    'latency_p99': latencies[int(n*0.99)],
    'latency_p999': latencies[int(n*0.999)],
    'latency_max': max(latencies),
    'latency_stdev': statistics.stdev(latencies) if n > 1 else 0,
}
with open(r'$stats_file', 'w') as out:
    json.dump(stats, out)
" 2>&1
}

# ---------- run JMeter benchmark ----------
run_jmeter() {
  local label=$1 jmx_file=$2 key=$3
  local csv_file="$RESULTS_DIR/${key}.csv"
  local report_dir="$RESULTS_DIR/${key}-report"
  local stats_file="$RESULTS_DIR/${key}.stats"

  banner "$label"
  rm -f "$csv_file"
  rm -rf "$report_dir"

  export JAVA_HOME
  cd "$JMETER_DIR"
  jmeter -n \
    -t "$jmx_file" \
    -Jthreads="$THREADS" \
    -Jduration="$DURATION" \
    -l "$csv_file" \
    -e -o "$report_dir" \
    2>&1 | grep -E "summary|Err:|Tidying" || true

  [ -f "$csv_file" ] || fail "$label: JMeter did not produce a CSV file"

  parse_jmeter_csv_to_file "$csv_file" "$stats_file" "$label"
  ok "Report: $report_dir/index.html"
}

# ---------- run gRPC benchmark ----------
run_grpc() {
  local stats_file="$RESULTS_DIR/grpc.stats"

  banner "gRPC (OrderDetailService/GetOrderDetail)"

  cd "$SCRIPTS_DIR"
  python grpc_benchmark.py 2>&1 | tee "$RESULTS_DIR/grpc-output.txt"

  python -c "
import json, sys
text = open(r'$RESULTS_DIR/grpc-output.txt').read()
start = text.find('--- RAW JSON ---')
if start == -1:
    print('ERROR: no JSON block in gRPC output', file=sys.stderr)
    sys.exit(1)
json_str = text[start:].split('\n', 1)[1] if '\n' in text[start:] else text[start:]
data = json.loads(json_str)
stats = {
    'label': 'gRPC (OrderDetailService/GetOrderDetail)',
    'total_requests': data['total_requests'],
    'errors': data['errors'],
    'error_rate': data['error_rate'],
    'throughput_rps': data['throughput_rps'],
    'response_size_mean': data['response_size_mean'],
    'latency_min': data['latency_min_ms'],
    'latency_mean': data['latency_mean_ms'],
    'latency_p50': data['latency_p50_ms'],
    'latency_p90': data['latency_p90_ms'],
    'latency_p95': data['latency_p95_ms'],
    'latency_p99': data['latency_p99_ms'],
    'latency_p999': data['latency_p999_ms'],
    'latency_max': data['latency_max_ms'],
    'latency_stdev': data['latency_stdev_ms'],
}
with open(r'$stats_file', 'w') as out:
    json.dump(stats, out)
" 2>&1
  ok "Stats written to $stats_file"
}

# ---------- print comparison table ----------
print_comparison() {
  banner "Benchmark Results"
  echo ""
  info "Configuration: $THREADS threads, ${DURATION}s duration, ${WARMUP}s warmup, $JVM_OPTS"
  echo ""

  python -c "
import json, os

results_dir = r'$RESULTS_DIR'
files = {
    'REST':    os.path.join(results_dir, 'rest.stats'),
    'GraphQL': os.path.join(results_dir, 'graphql.stats'),
    'gRPC':    os.path.join(results_dir, 'grpc.stats'),
}

data = {}
for name, f in files.items():
    try:
        with open(f) as fh:
            data[name] = json.load(fh)
    except Exception:
        data[name] = {}

metrics = [
    ('total_requests',     'Requests (total)',   'count'),
    ('throughput_rps',     'Throughput (/s)',    'float1'),
    ('response_size_mean', 'Resp size (bytes)',  'int'),
    ('latency_p50',        'P50 (ms)',           'float2'),
    ('latency_p90',        'P90 (ms)',           'float2'),
    ('latency_p95',        'P95 (ms)',           'float2'),
    ('latency_p99',        'P99 (ms)',           'float2'),
    ('latency_p999',       'P99.9 (ms)',         'float2'),
    ('latency_mean',       'Mean (ms)',          'float2'),
    ('latency_max',        'Max (ms)',           'float2'),
    ('error_rate',         'Error rate (%)',     'pct'),
]

def fmt(v, kind):
    if v is None or v == 'N/A':
        return 'N/A'
    if kind == 'count':
        return f'{int(v):,}'
    elif kind == 'float1':
        return f'{float(v):,.1f}'
    elif kind == 'float2':
        return f'{float(v):.2f}'
    elif kind == 'int':
        return f'{int(float(v)):,}'
    elif kind == 'pct':
        return f'{float(v):.2f}%'
    return str(v)

header = f'  {\"Metric\":<22} {\"REST\":>15} {\"GraphQL\":>15} {\"gRPC\":>15}'
sep = f'  {\"-\"*22} {\"-\"*15} {\"-\"*15} {\"-\"*15}'
print(header)
print(sep)

for key, label, kind in metrics:
    r = fmt(data.get('REST', {}).get(key), kind) if data.get('REST') else 'N/A'
    g = fmt(data.get('GraphQL', {}).get(key), kind) if data.get('GraphQL') else 'N/A'
    p = fmt(data.get('gRPC', {}).get(key), kind) if data.get('gRPC') else 'N/A'
    print(f'  {label:<22} {r:>15} {g:>15} {p:>15}')
" 2>&1

  echo ""
  echo "  Server logs:"
  echo "    stdout:  $COMPLETE_DIR/server-out.log"
  echo "    stderr:  $COMPLETE_DIR/server-err.log"
  echo ""
}

# =============================================================================
# MAIN
# =============================================================================

if $CLEAN_ONLY; then
  stop_server
  clean_intermediates
  exit 0
fi

STEPS=6
if $SKIP_BUILD; then STEPS=5; fi

echo ""
echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${CYAN}║     Protocol Benchmark — One-Click Runner               ║${NC}"
echo -e "${BOLD}${CYAN}║     benchmark-workspace2 — Claude Code edition           ║${NC}"
echo -e "${BOLD}${CYAN}║     REST + JSON  |  GraphQL  |  gRPC + protobuf         ║${NC}"
echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"

# ---- Step 1: Build ----
if ! $SKIP_BUILD; then
  step 1 "Building project..."
  cd "$COMPLETE_DIR"
  JAVA_HOME="$JAVA_HOME" ./mvnw clean package -DskipTests -q 2>&1 || fail "Build failed"
  ok "Build complete"
else
  step 1 "Build skipped"
  [ -f "$JAR_FILE" ] || fail "JAR not found at $JAR_FILE — run without --skip-build first"
  ok "JAR found"
fi
STEP_NUM=2

# ---- Step 2: Start server ----
step $STEP_NUM "Starting benchmark server..."
STEP_NUM=$((STEP_NUM + 1))
stop_server
sleep 1

cd "$COMPLETE_DIR"
java $JVM_OPTS -jar "$JAR_FILE" > server-out.log 2> server-err.log &
SERVER_PID=$!
info "Server PID: $SERVER_PID"

wait_for_server
verify_endpoints

# ---- Step 3: REST benchmark ----
step $STEP_NUM "Running REST benchmark (JMeter)..."
STEP_NUM=$((STEP_NUM + 1))
mkdir -p "$RESULTS_DIR"
rm -f "$RESULTS_DIR"/*.stats "$RESULTS_DIR"/grpc-output.txt
run_jmeter "REST (GET /api/orders/{id})" "rest-benchmark.jmx" "rest"

# ---- Step 4: GraphQL benchmark ----
step $STEP_NUM "Running GraphQL benchmark (JMeter)..."
STEP_NUM=$((STEP_NUM + 1))
run_jmeter "GraphQL (POST /graphql)" "graphql-benchmark.jmx" "graphql"

# ---- Step 5: gRPC benchmark ----
step $STEP_NUM "Running gRPC benchmark (Python)..."
STEP_NUM=$((STEP_NUM + 1))
run_grpc

# ---- Step 6: Stop server ----
step $STEP_NUM "Stopping server..."
stop_server
ok "Server stopped"

# ---- Cleanup ----
clean_intermediates

# ---- Print final comparison ----
print_comparison
