package com.resourcemonitor.collectors;

import com.resourcemonitor.util.ColorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

/**
 * Collects and logs CPU-related metrics including system load average and utilization.
 */
public class CpuMetricsCollector implements MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(CpuMetricsCollector.class);
    private final OperatingSystemMXBean operatingSystemMXBean;

    public CpuMetricsCollector() {
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    public void collectAndLog() {
        double systemLoadAverage = operatingSystemMXBean.getSystemLoadAverage();
        int availableProcessors = operatingSystemMXBean.getAvailableProcessors();

        logger.info("{}=== CPU Usage ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
        if (systemLoadAverage < 0) {
            logger.info("{}CPU - System Load Average:{} {}Not available{}", 
                ColorCodes.BLUE, ColorCodes.RESET, ColorCodes.YELLOW, ColorCodes.RESET);
        } else {
            double cpuUsagePercentage = (systemLoadAverage / availableProcessors) * 100;
            logger.info("{}CPU - System Load Average:{} {}{}/{} ({}%){}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.getColorForPercentage(cpuUsagePercentage),
                String.format("%.2f", systemLoadAverage),
                availableProcessors,
                String.format("%.2f", cpuUsagePercentage),
                ColorCodes.RESET);
        }
    }
} 