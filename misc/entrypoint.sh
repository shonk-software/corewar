#!/bin/sh

# Function to start nginx
start_nginx() {
    echo "Starting nginx..."
    nginx -g 'daemon off;' >> /log/nginx.log 2>&1 &
    nginx_pid=$!
}

# Function to start Java application
start_java() {
    echo "Unpacking JRE"
    zstd --decompress /opt/jre-minimal.tar.zst
    tar -xf /opt/jre-minimal.tar -C /opt
    rm /opt/jre-minimal.tar /opt/jre-minimal.tar.zst
    echo "Starting Java application..."
    java -jar /backend/app.jar >> /log/backend.log 2>&1 &
    java_pid=$!
}

# Function to stop services
stop_services() {
    echo "Stopping services..."
    if [ ! -z "$nginx_pid" ]; then
        kill $nginx_pid
    fi
    if [ ! -z "$java_pid" ]; then
        kill $java_pid
    fi
}

# Trap SIGINT to stop services on exit
trap stop_services SIGINT
trap stop_services SIGTERM

# Start services
start_nginx
start_java

echo "Services running"


# Wait for services to exit
wait $nginx_pid
wait $java_pid

