package com.resourcemonitor.collectors;

import com.resourcemonitor.util.ColorCodes;
import com.resourcemonitor.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

/**
 * Collects and logs memory-related metrics including heap and non-heap memory usage.
 */
public class MemoryMetricsCollector implements MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(MemoryMetricsCollector.class);
    private final MemoryMXBean memoryMXBean;

    public MemoryMetricsCollector() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    @Override
    public void collectAndLog() {
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
        double heapUsagePercentage = (double) heapUsed / heapMax * 100;
        
        logger.info("{}=== Memory Usage ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
        logger.info("{}Memory - Heap:{} {}{}/{} MB ({}%){}", 
            ColorCodes.BLUE, ColorCodes.RESET,
            ColorCodes.getColorForPercentage(heapUsagePercentage),
            FormatUtils.formatMB(heapUsed), 
            FormatUtils.formatMB(heapMax), 
            String.format("%.2f", heapUsagePercentage),
            ColorCodes.RESET);
        logger.info("{}Memory - Non-Heap:{} {} MB", 
            ColorCodes.BLUE, ColorCodes.RESET,
            FormatUtils.formatMB(nonHeapUsed));
    }
} 