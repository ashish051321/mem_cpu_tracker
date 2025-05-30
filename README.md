# Resource Monitor

A Spring Boot library for monitoring system resources, thread states, and database connection pools in real-time.

## Features

### 1. Memory Monitoring
- Heap memory usage tracking
- Non-heap memory monitoring
- Memory utilization percentage with color-coded alerts
- Visual representation of memory thresholds

### 2. CPU Monitoring
- System load average tracking
- CPU utilization percentage
- Available processors monitoring
- Color-coded CPU usage indicators

### 3. Thread Monitoring
- Active thread count tracking
- Peak thread count monitoring
- Total started threads count
- Daemon thread statistics
- Thread state distribution analysis
- Deadlock detection
- High CPU thread identification
- Blocked thread analysis

### 4. Thread Pool Monitoring
- Pool size tracking
- Active thread count
- Queued tasks monitoring
- Completed tasks count
- Thread pool utilization metrics

### 5. Database Connection Pool Monitoring
#### HikariCP Support
- Active connections tracking
- Idle connections monitoring
- Total connections count
- Connection utilization percentages
- Pool configuration details
- Connection timeout monitoring
- Idle timeout tracking
- Maximum lifetime settings
- Threads awaiting connection detection

#### Tomcat JDBC Pool Support
- Active and idle connection tracking
- Connection utilization metrics
- Wait count and time monitoring
- Pool configuration details
- Connection testing status
- Pool capacity alerts

#### Generic DataSource Support
- Basic connection testing
- Database product information
- Version details
- Connection status monitoring

## Configuration

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.resourcemonitor</groupId>
    <artifactId>resource-monitor</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Optional Dependencies
For database connection pool monitoring, add one or both of these optional dependencies:

```xml
<!-- For HikariCP monitoring -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <optional>true</optional>
</dependency>

<!-- For Tomcat JDBC Pool monitoring -->
<dependency>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>tomcat-jdbc</artifactId>
    <optional>true</optional>
</dependency>
```

### Properties Configuration

Configure the monitor in your `application.properties` or `application.yml`:

```yaml
resource:
  monitor:
    enabled: true
    interval-seconds: 60
    memory-monitoring: true
    cpu-monitoring: true
    thread-monitoring: true
    thread-pool-monitoring: true
    database-pool-monitoring: true
    thread-state-distribution: true
    deadlock-detection: true
    high-cpu-threads: true
    blocked-threads: true
```

## Output Format

The monitor provides color-coded, structured output for easy reading:

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
=== Thread Information ===
Active Threads: 25
Peak Thread Count: 30
Total Started Threads: 100
Daemon Threads: 15

=== Thread State Distribution ===
Threads in RUNNABLE state: 10
Threads in BLOCKED state: 2
Threads in WAITING state: 8
Threads in TIMED_WAITING state: 5
```

### Database Pool Section
```
=== Database Connection Pools ===
Pool 1 (HikariCP):
  Active Connections: 5/10 (50.00%)
  Idle Connections: 3/10 (30.00%)
  Total Connections: 8
  Min Idle: 2
  Pool Configuration:
    Connection Timeout: 30000 ms
    Idle Timeout: 600000 ms
    Max Lifetime: 1800000 ms
```

## Color Coding

The output uses ANSI color codes for better visibility:
- 🟢 Green: Normal/healthy state
- 🟡 Yellow: Warning state
- 🔴 Red: Critical state
- 🔵 Blue: Section headers
- 🟣 Purple: Main section headers

## Best Practices

1. **Interval Setting**
   - For production: 60-300 seconds
   - For development: 30-60 seconds
   - For debugging: 10-30 seconds

2. **Memory Monitoring**
   - Monitor heap usage regularly
   - Set up alerts for high memory usage
   - Watch for memory leaks

3. **Thread Monitoring**
   - Regularly check for deadlocks
   - Monitor blocked threads
   - Track thread pool utilization

4. **Database Pool Monitoring**
   - Monitor connection utilization
   - Watch for connection leaks
   - Track connection wait times

## Troubleshooting

1. **High Memory Usage**
   - Check for memory leaks
   - Monitor garbage collection
   - Review heap dump if necessary

2. **High CPU Usage**
   - Identify high CPU threads
   - Check for infinite loops
   - Monitor system load

3. **Thread Issues**
   - Check for deadlocks
   - Monitor blocked threads
   - Review thread pool settings

4. **Database Issues**
   - Monitor connection pool utilization
   - Check for connection leaks
   - Review pool configuration

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is licensed under the MIT License - see the LICENSE file for details. 