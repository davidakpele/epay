#!/bin/bash
# Scale down backend instances (minimum 2)

CURRENT=$(docker compose ps -q epay-server- | wc -l)
NEW=$((CURRENT - 1))

if [ $NEW -lt 2 ]; then
    echo "Cannot scale below 2 instances (minimum for HA)"
    exit 1
fi

echo "Scaling down to $NEW instances..."
docker compose up -d --scale epay-server-$NEW

echo "Scaled down to $NEW instances"