#!/bin/bash
# Auto-scaling based on metrics

# Get CPU usage from Prometheus
CPU_USAGE=$(curl -s 'http://localhost:9090/api/v1/query?query=avg(container_cpu_usage_seconds_total{container_name=~"epay-server.*"})' | jq -r '.data.result[0].value[1]' | awk '{print $1 * 100}')

if (( $(echo "$CPU_USAGE > 70" | bc -l) )); then
    CURRENT=$(docker compose ps -q epay-server- | wc -l)
    if [ $CURRENT -lt 5 ]; then
        echo "High CPU ($CPU_USAGE%). Scaling up..."
        ./scripts/scale-up.sh
    fi
elif (( $(echo "$CPU_USAGE < 30" | bc -l) )); then
    CURRENT=$(docker compose ps -q epay-server- | wc -l)
    if [ $CURRENT -gt 2 ]; then
        echo "Low CPU ($CPU_USAGE%). Scaling down..."
        ./scripts/scale-down.sh
    fi
fi