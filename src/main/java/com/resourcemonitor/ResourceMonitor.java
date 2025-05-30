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
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ResourceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);
    private final ScheduledExecutorService scheduler;
    private final long intervalSeconds;
    private final MemoryMXBean memoryMXBean;
    private final OperatingSystemMXBean operatingSystemMXBean;
    private final ThreadMXBean threadMXBean;
    private final List<DataSource> dataSources;

    // ANSI color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    private ResourceMonitor(long intervalSeconds, List<DataSource> dataSources) {
        this.intervalSeconds = intervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.dataSources = dataSources;
    }

    public static ResourceMonitor create(long intervalSeconds, List<DataSource> dataSources) {
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0");
        }
        return new ResourceMonitor(intervalSeconds, dataSources);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::logResources, 0, intervalSeconds, TimeUnit.SECONDS);
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
        // Memory metrics
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
        
        // CPU metrics
        double systemLoadAverage = operatingSystemMXBean.getSystemLoadAverage();
        int availableProcessors = operatingSystemMXBean.getAvailableProcessors();

        // Calculate percentages
        double heapUsagePercentage = (double) heapUsed / heapMax * 100;
        
        logger.info("{}Resource Usage Statistics:{}", BOLD + CYAN, RESET);
        
        // Memory Section
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

        // CPU Section
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

        // Thread Section
        logger.info("{}=== Thread Information ==={}", BOLD + PURPLE, RESET);
        int threadCount = threadMXBean.getThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        long totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
        int daemonThreadCount = threadMXBean.getDaemonThreadCount();

        logger.info("{}Active Threads:{} {}", BLUE, RESET, threadCount);
        logger.info("{}Peak Thread Count:{} {}", BLUE, RESET, peakThreadCount);
        logger.info("{}Total Started Threads:{} {}", BLUE, RESET, totalStartedThreadCount);
        logger.info("{}Daemon Threads:{} {}", BLUE, RESET, daemonThreadCount);

        // Thread Pool Section
        logger.info("{}=== Thread Pool Statistics ==={}", BOLD + PURPLE, RESET);
        if (scheduler instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) scheduler;
            logger.info("{}Pool Size:{} {}", BLUE, RESET, executor.getPoolSize());
            logger.info("{}Active Threads:{} {}", BLUE, RESET, executor.getActiveCount());
            logger.info("{}Queued Tasks:{} {}", BLUE, RESET, executor.getQueue().size());
            logger.info("{}Completed Tasks:{} {}", BLUE, RESET, executor.getCompletedTaskCount());
        }

        // Database Connection Pool Section
        if (!dataSources.isEmpty()) {
            logger.info("{}=== Database Connection Pools ==={}", BOLD + PURPLE, RESET);
            for (int i = 0; i < dataSources.size(); i++) {
                DataSource dataSource = dataSources.get(i);
                try {
                    if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
                        org.apache.tomcat.jdbc.pool.DataSource tomcatDS = (org.apache.tomcat.jdbc.pool.DataSource) dataSource;
                        logger.info("{}Pool {}:{}", BLUE, i + 1, RESET);
                        logger.info("  {}Active:{} {}", BLUE, RESET, tomcatDS.getActive());
                        logger.info("  {}Idle:{} {}", BLUE, RESET, tomcatDS.getIdle());
                        logger.info("  {}Max Active:{} {}", BLUE, RESET, tomcatDS.getMaxActive());
                        logger.info("  {}Max Idle:{} {}", BLUE, RESET, tomcatDS.getMaxIdle());
                    } else if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                        com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
                        logger.info("{}Pool {}:{}", BLUE, i + 1, RESET);
                        logger.info("  {}Active:{} {}", BLUE, RESET, hikariDS.getHikariPoolMXBean().getActiveConnections());
                        logger.info("  {}Idle:{} {}", BLUE, RESET, hikariDS.getHikariPoolMXBean().getIdleConnections());
                        logger.info("  {}Total:{} {}", BLUE, RESET, hikariDS.getHikariPoolMXBean().getTotalConnections());
                    }
                } catch (Exception e) {
                    logger.warn("{}Failed to get metrics for data source {}: {} {}", 
                        YELLOW, i + 1, e.getMessage(), RESET);
                }
            }
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
} 