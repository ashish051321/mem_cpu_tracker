# Resource Monitor

A Java library that monitors and logs system resource usage (memory and CPU) at configurable intervals.

## Features

- Monitors heap and non-heap memory usage
- Tracks CPU system load
- Configurable logging interval
- Easy integration with Spring Boot applications
- Lightweight and non-intrusive

## Usage

### Maven Dependency

```xml
<dependency>
    <groupId>com.resourcemonitor</groupId>
    <artifactId>resource-monitor</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Spring Boot Integration

The library automatically configures itself in Spring Boot applications. You can customize the monitoring interval in your `application.properties` or `application.yml`:

```properties
resource.monitor.interval-seconds=60
```

### Manual Usage

If you're not using Spring Boot, you can use the library directly:

```java
ResourceMonitor monitor = ResourceMonitor.create(60); // 60 seconds interval
monitor.start();

// ... your application code ...

monitor.stop(); // When you want to stop monitoring
```

## Log Output

The library logs resource usage statistics in the following format:

```
Resource Usage Statistics:
Memory - Heap: 256.50/1024.00 MB (25.05%)
Memory - Non-Heap: 45.20 MB
CPU - System Load Average: 2.50/4 (62.50%)
```

## Requirements

- Java 11 or higher
- SLF4J logging implementation (provided by Spring Boot or add your own) 