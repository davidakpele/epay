#!/bin/bash
# Scale up backend instances

echo "Scaling up ePay backend instances..."

# Get current number of instances
CURRENT=$(docker compose ps -q epay-server- | wc -l)
NEW=$((CURRENT + 1))

echo "Current instances: $CURRENT"
echo "New instances: $NEW"

# Scale up
docker compose up -d --scale epay-server-$NEW

# Wait for health check
echo "Waiting for new instance to be healthy..."
sleep 10

# Verify
docker compose ps | grep epay-server

# Update NGINX if needed (you'll need to reload)
docker compose exec nginx nginx -s reload

echo "Scaled up to $NEW instances"