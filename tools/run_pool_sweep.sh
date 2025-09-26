#!/usr/bin/env bash
set -euo pipefail

OUT_CSV=tools/pool_latency.csv
LOG_DIR=tools/_logs
mkdir -p "$LOG_DIR"

echo "pool,avg_ms,p95_ms,p99_ms" > "$OUT_CSV"
POOLS=(2 3 5 10 20 30)

for p in "${POOLS[@]}"; do
  echo "\n=== Running pool=$p ==="
  LOG="$LOG_DIR/run_pool_${p}.log"
  # run gradle task and capture output
  ./gradlew runLoadTestReuse --no-daemon --quiet --args="127.0.0.1 6379 20 100 30 700 $p" > "$LOG" 2>&1 || true

  # extract last 'Reuse-Final stats' line
  LINE=$(grep "Reuse-Final stats" "$LOG" | tail -n 1 || true)
  if [ -z "$LINE" ]; then
    echo "No final stats found for pool $p, see $LOG" >&2
    continue
  fi

  # parse avg, p95, p99 (strip trailing 'ms')
  AVG=$(echo "$LINE" | sed -n 's/.*avg=\([^ ]*\).*/\1/p' | sed 's/ms$//')
  P95=$(echo "$LINE" | sed -n 's/.*p95=\([^ ]*\).*/\1/p' | sed 's/ms$//')
  P99=$(echo "$LINE" | sed -n 's/.*p99=\([^ ]*\).*/\1/p' | sed 's/ms$//')

  if [ -z "$AVG" ] || [ -z "$P95" ] || [ -z "$P99" ]; then
    echo "Parsing failed for pool $p, line: $LINE" >&2
    continue
  fi

  echo "$p,$AVG,$P95,$P99" >> "$OUT_CSV"
  echo "Recorded: $p,$AVG,$P95,$P99"

  sleep 2
done

# generate plot
python tools/plot_poolsize_latency.py "$OUT_CSV" tools/pool_latency.png

echo "Sweep finished. CSV=$OUT_CSV, PNG=tools/pool_latency.png"
