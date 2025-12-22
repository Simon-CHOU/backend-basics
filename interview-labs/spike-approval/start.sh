#!/bin/sh

# Function to check if service is healthy
check_health() {
    local url=$1
    local max_attempts=30
    local attempt=1

    echo "Checking $url health..."
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "$url" > /dev/null; then
            echo "$url is healthy!"
            return 0
        fi
        echo "Attempt $attempt/$max_attempts: $url not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "$url failed to become healthy"
    return 1
}

# Start backend in background
echo "Starting backend..."
/app/backend &

# Wait for backend to be healthy
check_health "http://localhost:8089/actuator/health"

# Start nginx in foreground
echo "Starting nginx..."
nginx -g "daemon off;"