package com.resourcemonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Comparator;

public class ResourceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);
    private final ScheduledExecutorService scheduler;
    private final long intervalSeconds;
    private final MemoryMXBean memoryMXBean;
    private final OperatingSystemMXBean operatingSystemMXBean;
    private final ThreadMXBean threadMXBean;
    private final List<DataSource> dataSources;
    private final ResourceMonitorProperties properties;

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    private ResourceMonitor(long intervalSeconds, List<DataSource> dataSources, ResourceMonitorProperties properties) {
        this.intervalSeconds = intervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.dataSources = dataSources;
        this.properties = properties;
    }

    public static ResourceMonitor create(long intervalSeconds, List<DataSource> dataSources, ResourceMonitorProperties properties) {
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0");
        }
        return new ResourceMonitor(intervalSeconds, dataSources, properties);
    }

    public void start() {
        logger.info("{}Starting resource monitor with interval of {} seconds{}", GREEN, intervalSeconds, RESET);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.debug("{}Executing scheduled resource check{}", CYAN, RESET);
                logResources();
            } catch (Exception e) {
                logger.error("{}Error during resource monitoring: {} {}", RED, e.getMessage(), RESET, e);
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
        logger.info("{}Resource monitoring started with interval of {} seconds{}", GREEN, intervalSeconds, RESET);
    }

    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("{}Resource monitoring stopped{}", RED, RESET);
    }

    private void logResources() {
        logger.debug("{}Starting resource metrics collection{}", CYAN, RESET);
        
        logger.info("{}Resource Usage Statistics:{}", BOLD + CYAN, RESET);
        
        // Memory Section
        if (properties.isMemoryMonitoring()) {
            logMemoryMetrics();
        }

        // CPU Section
        if (properties.isCpuMonitoring()) {
            logCpuMetrics();
        }

        // Thread Section
        if (properties.isThreadMonitoring()) {
            logThreadMetrics();
        }

        // Thread Pool Section
        if (properties.isThreadPoolMonitoring()) {
            logThreadPoolMetrics();
        }

        // Database Connection Pool Section
        if (properties.isDatabasePoolMonitoring()) {
            logDatabasePoolMetrics();
        }
    }

    private void logMemoryMetrics() {
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
        double heapUsagePercentage = (double) heapUsed / heapMax * 100;
        
        logger.info("{}=== Memory Usage ==={}", BOLD + PURPLE, RESET);
        logger.info("{}Memory - Heap:{} {}{}/{} MB ({}%){}", 
            BLUE, RESET,
            getColorForPercentage(heapUsagePercentage),
            formatMB(heapUsed), 
            formatMB(heapMax), 
            String.format("%.2f", heapUsagePercentage),
            RESET);
        logger.info("{}Memory - Non-Heap:{} {} MB", 
            BLUE, RESET,
            formatMB(nonHeapUsed));
    }

    private void logCpuMetrics() {
        double systemLoadAverage = operatingSystemMXBean.getSystemLoadAverage();
        int availableProcessors = operatingSystemMXBean.getAvailableProcessors();

        logger.info("{}=== CPU Usage ==={}", BOLD + PURPLE, RESET);
        if (systemLoadAverage < 0) {
            logger.info("{}CPU - System Load Average:{} {}Not available{}", 
                BLUE, RESET, YELLOW, RESET);
        } else {
            double cpuUsagePercentage = (systemLoadAverage / availableProcessors) * 100;
            logger.info("{}CPU - System Load Average:{} {}{}/{} ({}%){}", 
                BLUE, RESET,
                getColorForPercentage(cpuUsagePercentage),
                String.format("%.2f", systemLoadAverage),
                availableProcessors,
                String.format("%.2f", cpuUsagePercentage),
                RESET);
        }
    }

    private void logThreadMetrics() {
        logger.info("{}=== Thread Information ==={}", BOLD + PURPLE, RESET);
        int threadCount = threadMXBean.getThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
        int daemonThreadCount = threadMXBean.getDaemonThreadCount();

        // Basic Thread Stats
        logger.info("{}Active Threads:{} {}", BLUE, RESET, threadCount);
        logger.info("{}Peak Thread Count:{} {}", BLUE, RESET, peakThreadCount);
        logger.info("{}Total Started Threads:{} {}", BLUE, RESET, totalStartedThreadCount);
        logger.info("{}Daemon Threads:{} {}", BLUE, RESET, daemonThreadCount);

        // Thread State Distribution
        if (properties.isThreadStateDistribution()) {
            logThreadStateDistribution();
        }

        // Deadlock Detection
        if (properties.isDeadlockDetection()) {
            logDeadlockDetection();
        }

        // High CPU Threads
        if (properties.isHighCpuThreads()) {
            logHighCpuThreads();
        }

        // Blocked Threads
        if (properties.isBlockedThreads()) {
            logBlockedThreads();
        }
    }

    private void logThreadStateDistribution() {
        Map<Thread.State, Long> threadStates = Arrays.stream(threadMXBean.getAllThreadIds())
            .mapToObj(threadMXBean::getThreadInfo)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                ThreadInfo::getThreadState,
                Collectors.counting()
            ));

        logger.info("{}=== Thread State Distribution ==={}", BOLD + PURPLE, RESET);
        threadStates.forEach((state, count) -> {
            String color = getColorForThreadState(state);
            logger.info("{}Threads in {} state:{} {}{}", BLUE, state, RESET, color, count);
        });
    }

    private void logDeadlockDetection() {
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            logger.error("{}=== DEADLOCK DETECTED ==={}", BOLD + RED, RESET);
            logger.error("{}Number of threads involved in deadlock:{} {}", RED, RESET, deadlockedThreads.length);
            for (long threadId : deadlockedThreads) {
                ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
                if (threadInfo != null) {
                    logger.error("{}Deadlocked Thread:{} {} (ID: {})", RED, RESET, 
                        threadInfo.getThreadName(), threadId);
                    logger.error("{}Blocked on:{} {}", RED, RESET, 
                        threadInfo.getLockName() != null ? threadInfo.getLockName() : "Unknown");
                    logger.error("{}Blocked by:{} {}", RED, RESET, 
                        threadInfo.getLockOwnerName() != null ? threadInfo.getLockOwnerName() : "Unknown");
                }
            }
        }
    }

    private void logHighCpuThreads() {
        if (threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeEnabled()) {
            logger.info("{}=== High CPU Threads ==={}", BOLD + PURPLE, RESET);
            Arrays.stream(threadMXBean.getAllThreadIds())
                .mapToObj(threadMXBean::getThreadInfo)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(threadInfo -> 
                    threadMXBean.getThreadCpuTime(threadInfo.getThreadId())))
                .limit(5)
                .forEach(threadInfo -> {
                    long cpuTime = threadMXBean.getThreadCpuTime(threadInfo.getThreadId());
                    logger.info("{}Thread:{} {} (CPU Time: {} ms)", BLUE, RESET,
                        threadInfo.getThreadName(),
                        formatNanosToMillis(cpuTime));
                });
        }
    }

    private void logBlockedThreads() {
        logger.info("{}=== Blocked Threads ==={}", BOLD + PURPLE, RESET);
        Arrays.stream(threadMXBean.getAllThreadIds())
            .mapToObj(threadMXBean::getThreadInfo)
            .filter(Objects::nonNull)
            .filter(info -> info.getThreadState() == Thread.State.BLOCKED)
            .forEach(threadInfo -> {
                logger.warn("{}Blocked Thread:{} {} (Blocked for: {} ms)", YELLOW, RESET,
                    threadInfo.getThreadName(),
                    threadInfo.getBlockedTime() > 0 ? threadInfo.getBlockedTime() : "Unknown");
                if (threadInfo.getLockName() != null) {
                    logger.warn("  {}Blocked on:{} {}", YELLOW, RESET, threadInfo.getLockName());
                }
                if (threadInfo.getLockOwnerName() != null) {
                    logger.warn("  {}Blocked by:{} {}", YELLOW, RESET, threadInfo.getLockOwnerName());
                }
            });
    }

    private void logThreadPoolMetrics() {
        logger.info("{}=== Thread Pool Statistics ==={}", BOLD + PURPLE, RESET);
        if (scheduler instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) scheduler;
            logger.info("{}Pool Size:{} {}", BLUE, RESET, executor.getPoolSize());
            logger.info("{}Active Threads:{} {}", BLUE, RESET, executor.getActiveCount());
            logger.info("{}Queued Tasks:{} {}", BLUE, RESET, executor.getQueue().size());
            logger.info("{}Completed Tasks:{} {}", BLUE, RESET, executor.getCompletedTaskCount());
        }
    }

    private void logDatabasePoolMetrics() {
        if (!dataSources.isEmpty()) {
            logger.info("{}=== Database Connection Pools ==={}", BOLD + PURPLE, RESET);
            for (int i = 0; i < dataSources.size(); i++) {
                DataSource dataSource = dataSources.get(i);
                try {
                    if (isTomcatPool(dataSource)) {
                        logTomcatPoolMetrics(dataSource, i + 1);
                    } else if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                        logHikariPoolMetrics((com.zaxxer.hikari.HikariDataSource) dataSource, i + 1);
                    } else {
                        logGenericPoolMetrics(dataSource, i + 1);
                    }
                } catch (Exception e) {
                    logger.warn("{}Failed to get metrics for data source {}: {} {}", 
                        YELLOW, i + 1, e.getMessage(), RESET);
                }
            }
        }
    }

    private boolean isTomcatPool(DataSource dataSource) {
        try {
            return dataSource.getClass().getName().equals("org.apache.tomcat.jdbc.pool.DataSource");
        } catch (Exception e) {
            return false;
        }
    }

    private void logTomcatPoolMetrics(DataSource dataSource, int poolNumber) {
        try {
            Class<?> tomcatDSClass = Class.forName("org.apache.tomcat.jdbc.pool.DataSource");
            if (!tomcatDSClass.isInstance(dataSource)) {
                logGenericPoolMetrics(dataSource, poolNumber);
                return;
            }

            logger.info("{}Pool {} (Tomcat JDBC Pool):{}", BLUE, poolNumber, RESET);
            
            // Use reflection to safely access Tomcat pool methods
            int active = (int) tomcatDSClass.getMethod("getActive").invoke(dataSource);
            int idle = (int) tomcatDSClass.getMethod("getIdle").invoke(dataSource);
            int maxActive = (int) tomcatDSClass.getMethod("getMaxActive").invoke(dataSource);
            int maxIdle = (int) tomcatDSClass.getMethod("getMaxIdle").invoke(dataSource);
            int minIdle = (int) tomcatDSClass.getMethod("getMinIdle").invoke(dataSource);
            long waitCount = (long) tomcatDSClass.getMethod("getWaitCount").invoke(dataSource);
            long maxWait = (long) tomcatDSClass.getMethod("getMaxWait").invoke(dataSource);
            
            // Calculate utilization
            double activeUtilization = (double) active / maxActive * 100;
            double idleUtilization = (double) idle / maxIdle * 100;
            
            // Log basic metrics with utilization
            logger.info("  {}Active Connections:{} {}{}/{} ({}%){}", 
                BLUE, RESET,
                getColorForPercentage(activeUtilization),
                active, maxActive,
                String.format("%.2f", activeUtilization),
                RESET);
            logger.info("  {}Idle Connections:{} {}{}/{} ({}%){}", 
                BLUE, RESET,
                getColorForPercentage(idleUtilization),
                idle, maxIdle,
                String.format("%.2f", idleUtilization),
                RESET);
            logger.info("  {}Min Idle:{} {}", BLUE, RESET, minIdle);
            
            // Log connection metrics
            logger.info("  {}Connection Wait Stats:{}", BLUE, RESET);
            logger.info("    {}Total Wait Count:{} {}", BLUE, RESET, waitCount);
            logger.info("    {}Max Wait Time:{} {} ms", BLUE, RESET, maxWait);
            
            // Log pool configuration
            logger.info("  {}Pool Configuration:{}", BLUE, RESET);
            logger.info("    {}Initial Size:{} {}", BLUE, RESET, 
                tomcatDSClass.getMethod("getInitialSize").invoke(dataSource));
            logger.info("    {}Test While Idle:{} {}", BLUE, RESET, 
                tomcatDSClass.getMethod("isTestWhileIdle").invoke(dataSource));
            logger.info("    {}Test On Borrow:{} {}", BLUE, RESET, 
                tomcatDSClass.getMethod("isTestOnBorrow").invoke(dataSource));
            logger.info("    {}Test On Return:{} {}", BLUE, RESET, 
                tomcatDSClass.getMethod("isTestOnReturn").invoke(dataSource));
            
            // Log pool state
            if (active >= maxActive) {
                logger.warn("  {}Pool at maximum capacity!{}", YELLOW, RESET);
            }
            if (waitCount > 0) {
                logger.warn("  {}Connection wait detected!{}", YELLOW, RESET);
            }
        } catch (Exception e) {
            logger.warn("{}Failed to get Tomcat pool metrics: {} {}", YELLOW, e.getMessage(), RESET);
            logGenericPoolMetrics(dataSource, poolNumber);
        }
    }

    private void logGenericPoolMetrics(DataSource dataSource, int poolNumber) {
        logger.info("{}Pool {} (Generic DataSource):{}", BLUE, poolNumber, RESET);
        logger.info("  {}Class:{} {}", BLUE, RESET, dataSource.getClass().getName());
        try {
            // Try to get basic connection info
            try (java.sql.Connection conn = dataSource.getConnection()) {
                logger.info("  {}Connection Test:{} Successful", GREEN, RESET);
                logger.info("  {}Database:{} {}", BLUE, RESET, conn.getMetaData().getDatabaseProductName());
                logger.info("  {}Version:{} {}", BLUE, RESET, conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (Exception e) {
            logger.warn("  {}Connection Test:{} Failed - {}", YELLOW, RESET, e.getMessage());
        }
    }

    private void logHikariPoolMetrics(com.zaxxer.hikari.HikariDataSource ds, int poolNumber) {
        logger.info("{}Pool {} (HikariCP):{}", BLUE, poolNumber, RESET);
        
        // Get pool metrics
        com.zaxxer.hikari.HikariPoolMXBean poolMXBean = ds.getHikariPoolMXBean();
        
        // Basic metrics
        int active = poolMXBean.getActiveConnections();
        int idle = poolMXBean.getIdleConnections();
        int total = poolMXBean.getTotalConnections();
        int maxPoolSize = ds.getMaximumPoolSize();
        int minIdle = ds.getMinimumIdle();
        
        // Calculate utilization
        double activeUtilization = (double) active / maxPoolSize * 100;
        double idleUtilization = (double) idle / maxPoolSize * 100;
        
        // Get pool stats
        long threadsAwaitingConnection = poolMXBean.getThreadsAwaitingConnection();
        long maxLifetime = ds.getMaxLifetime();
        long connectionTimeout = ds.getConnectionTimeout();
        long idleTimeout = ds.getIdleTimeout();
        
        // Log basic metrics with utilization
        logger.info("  {}Active Connections:{} {}{}/{} ({}%){}", 
            BLUE, RESET,
            getColorForPercentage(activeUtilization),
            active, maxPoolSize,
            String.format("%.2f", activeUtilization),
            RESET);
        logger.info("  {}Idle Connections:{} {}{}/{} ({}%){}", 
            BLUE, RESET,
            getColorForPercentage(idleUtilization),
            idle, maxPoolSize,
            String.format("%.2f", idleUtilization),
            RESET);
        logger.info("  {}Total Connections:{} {}", BLUE, RESET, total);
        logger.info("  {}Min Idle:{} {}", BLUE, RESET, minIdle);
        
        // Log pool configuration
        logger.info("  {}Pool Configuration:{}", BLUE, RESET);
        logger.info("    {}Connection Timeout:{} {} ms", BLUE, RESET, connectionTimeout);
        logger.info("    {}Idle Timeout:{} {} ms", BLUE, RESET, idleTimeout);
        logger.info("    {}Max Lifetime:{} {} ms", BLUE, RESET, maxLifetime);
        logger.info("    {}Validation Timeout:{} {} ms", BLUE, RESET, ds.getValidationTimeout());
        logger.info("    {}Leak Detection Threshold:{} {} ms", BLUE, RESET, ds.getLeakDetectionThreshold());
        
        // Log waiting threads
        if (threadsAwaitingConnection > 0) {
            logger.warn("  {}Threads Awaiting Connection:{} {}", YELLOW, RESET, threadsAwaitingConnection);
        }
        
        // Log pool state
        if (active >= maxPoolSize) {
            logger.warn("  {}Pool at maximum capacity!{}", YELLOW, RESET);
        }
        if (threadsAwaitingConnection > 0) {
            logger.warn("  {}Connection wait detected!{}", YELLOW, RESET);
        }
    }

    private String getColorForPercentage(double percentage) {
        if (percentage >= 90) {
            return RED;
        } else if (percentage >= 70) {
            return YELLOW;
        } else {
            return GREEN;
        }
    }

    private String formatMB(long bytes) {
        return String.format("%.2f", bytes / (1024.0 * 1024.0));
    }

    private String getColorForThreadState(Thread.State state) {
        switch (state) {
            case BLOCKED:
                return RED;
            case WAITING:
            case TIMED_WAITING:
                return YELLOW;
            case RUNNABLE:
                return GREEN;
            default:
                return RESET;
        }
    }

    private String formatNanosToMillis(long nanos) {
        return String.format("%.2f", nanos / 1_000_000.0);
    }
} 