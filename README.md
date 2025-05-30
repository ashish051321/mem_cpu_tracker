# Resource Monitor

A Spring Boot library for monitoring system resources, thread states, and database connection pools in real-time.

## Features

### Memory Monitoring
- Heap memory usage with utilization percentage
- Non-heap memory usage
- Color-coded output based on memory utilization

### CPU Monitoring
- System load average
- CPU utilization percentage
- Available processors count

### Thread Monitoring
- Thread state distribution
- Blocked thread detection
- Thread pool statistics
- Deadlock detection

### Database Connection Pool Monitoring

The library provides comprehensive monitoring for various database connection pool implementations:

#### HikariCP
- Active connections with utilization percentage
- Idle connections
- Total connections
- Pool configuration:
  - Connection timeout
  - Idle timeout
  - Maximum lifetime
- Color-coded output for connection utilization

#### Tomcat JDBC Pool
- Active connections with utilization percentage
- Idle connections
- Maximum active connections
- Connection wait statistics:
  - Total wait count
  - Average wait time
- Color-coded output for connection utilization

#### Generic DataSource
- Basic DataSource type information
- Connection pool implementation details

## Configuration

### Maven Dependency
```xml
<dependency>
    <groupId>com.resourcemonitor</groupId>
    <artifactId>resource-monitor</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Properties Configuration

#### YAML Format
```yaml
resource:
  monitor:
    enabled: true
    interval-seconds: 60
    memory-enabled: true
    cpu-enabled: true
    thread-enabled: true
    database-enabled: true
```

#### Properties Format
```properties
resource.monitor.enabled=true
resource.monitor.interval-seconds=60
resource.monitor.memory-enabled=true
resource.monitor.cpu-enabled=true
resource.monitor.thread-enabled=true
resource.monitor.database-enabled=true
```

## Output Format

### Memory Section
```
=== Memory Usage ===
Memory - Heap: 512.50/1024.00 MB (50.00%)
Memory - Non-Heap: 128.25 MB
```

### CPU Section
```
=== CPU Usage ===
CPU - System Load Average: 2.50/4 (62.50%)
```

### Thread Section
```
=== Thread States ===
Threads in RUNNABLE state: 15
Threads in BLOCKED state: 2
Threads in WAITING state: 8

=== Blocked Threads ===
Thread: pool-1-thread-1
  Blocked Time: 1500ms
  Blocked by: pool-2-thread-1
```

### Database Pool Section

#### HikariCP
```
=== HikariCP Pool Metrics ===
Active Connections: 5/10 (50%)
Idle Connections: 3
Total Connections: 8
Pool Configuration:
  Connection Timeout: 30000ms
  Idle Timeout: 600000ms
  Max Lifetime: 1800000ms
```

#### Tomcat JDBC Pool
```
=== Tomcat JDBC Pool Metrics ===
Active Connections: 3/10 (30%)
Idle Connections: 5
Max Active: 10
Connection Wait Stats:
  Total Wait Count: 2
  Average Wait Time: 150ms
```

## Best Practices

### Monitoring Intervals
- For production: 60 seconds or more
- For development: 30 seconds
- For debugging: 10-15 seconds

### Memory Monitoring
- Monitor heap usage trends
- Set up alerts for high utilization (>80%)
- Watch for memory leaks

### Thread Monitoring
- Monitor blocked thread patterns
- Watch for thread pool saturation
- Track deadlock occurrences

### Database Pool Monitoring
- Monitor connection utilization
- Watch for connection wait times
- Track connection pool saturation
- Monitor connection timeouts
- Set up alerts for:
  - High connection utilization (>80%)
  - Long wait times (>1000ms)
  - Frequent connection timeouts

## Troubleshooting

### Memory Issues
- High heap usage: Check for memory leaks
- Growing non-heap: Monitor class loading

### CPU Issues
- High load average: Check for CPU-intensive operations
- Spikes: Monitor thread activity

### Thread Issues
- Blocked threads: Check for lock contention
- High thread count: Monitor thread creation

### Database Connection Issues
- Connection timeouts: Check pool size and network
- High wait times: Monitor pool utilization
- Connection leaks: Check connection handling

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 