package com.resourcemonitor;

import com.resourcemonitor.collectors.*;
import com.resourcemonitor.util.ColorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main class for monitoring system resources, thread states, and database connection pools.
 * Uses separate collectors for different types of metrics to maintain separation of concerns.
 */
@Component
public class ResourceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ResourceMonitor.class);
    private final ScheduledExecutorService scheduler;
    private final List<DataSource> dataSources;
    private final ResourceMonitorProperties properties;
    private final MemoryMetricsCollector memoryCollector;
    private final CpuMetricsCollector cpuCollector;
    private final ThreadMetricsCollector threadCollector;
    private final DatabaseMetricsCollector databaseCollector;

    @Autowired
    public ResourceMonitor(List<DataSource> dataSources, ResourceMonitorProperties properties) {
        this.dataSources = dataSources;
        this.properties = properties;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.memoryCollector = new MemoryMetricsCollector();
        this.cpuCollector = new CpuMetricsCollector();
        this.threadCollector = new ThreadMetricsCollector();
        this.databaseCollector = new DatabaseMetricsCollector(dataSources);
    }

    @PostConstruct
    public void start() {
        logger.info("{}Starting Resource Monitor{}", ColorCodes.BOLD + ColorCodes.GREEN, ColorCodes.RESET);
        scheduler.scheduleAtFixedRate(this::collectMetrics, 0, properties.getIntervalSeconds(), TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        logger.info("{}Stopping Resource Monitor{}", ColorCodes.BOLD + ColorCodes.YELLOW, ColorCodes.RESET);
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void collectMetrics() {
        long startTime = System.nanoTime();
        logger.info("{}=== Resource Monitor Check ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
        
        try {
            if (properties.isMemoryEnabled()) {
                memoryCollector.collectAndLog();
            }
            
            if (properties.isCpuEnabled()) {
                cpuCollector.collectAndLog();
            }
            
            if (properties.isThreadEnabled()) {
                threadCollector.collectAndLog();
            }
            
            if (properties.isDatabaseEnabled()) {
                databaseCollector.collectAndLog();
            }
        } catch (Exception e) {
            logger.error("{}Error collecting metrics: {}{}", ColorCodes.RED, e.getMessage(), ColorCodes.RESET, e);
        }

        long duration = System.nanoTime() - startTime;
        logger.info("{}Metrics collection completed in {}ms{}", 
            ColorCodes.CYAN, 
            String.format("%.2f", duration / 1_000_000.0), 
            ColorCodes.RESET);
    }
} 