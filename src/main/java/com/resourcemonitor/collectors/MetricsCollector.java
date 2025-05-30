package com.resourcemonitor.collectors;

/**
 * Interface for all metrics collectors in the resource monitoring system.
 * Each collector is responsible for gathering specific types of metrics.
 */
public interface MetricsCollector {
    /**
     * Collects and logs the metrics for this collector.
     * The implementation should handle all the logging internally.
     */
    void collectAndLog();
} 