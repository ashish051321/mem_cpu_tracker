package com.resourcemonitor.collectors;

import com.resourcemonitor.util.ColorCodes;
import com.resourcemonitor.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Collects and logs thread-related metrics including thread states, blocked threads,
 * and thread pool statistics.
 */
public class ThreadMetricsCollector implements MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(ThreadMetricsCollector.class);
    private final ThreadMXBean threadMXBean;
    private final Map<Long, Long> threadStartTimes = new ConcurrentHashMap<>();

    public ThreadMetricsCollector() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public void collectAndLog() {
        logThreadStates();
        logBlockedThreads();
        logThreadPoolMetrics();
    }

    private void logThreadStates() {
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        Map<Thread.State, Long> stateCounts = Arrays.stream(threadInfos)
            .collect(java.util.stream.Collectors.groupingBy(
                ThreadInfo::getThreadState,
                java.util.stream.Collectors.counting()
            ));

        logger.info("{}=== Thread States ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
        stateCounts.forEach((state, count) -> {
            String color = ColorCodes.getColorForThreadState(state);
            logger.info("{}Threads in {} state:{} {}{}{}", 
                ColorCodes.BLUE, state, ColorCodes.RESET, color, count, ColorCodes.RESET);
        });
    }

    private void logBlockedThreads() {
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        boolean hasBlockedThreads = false;

        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo.getThreadState() == Thread.State.BLOCKED) {
                if (!hasBlockedThreads) {
                    logger.info("{}=== Blocked Threads ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
                    hasBlockedThreads = true;
                }

                long blockedTime = threadInfo.getBlockedTime();
                String blockedTimeStr = blockedTime > 0 ? 
                    FormatUtils.formatNanosToMillis(blockedTime * 1_000_000) + "ms" : "N/A";

                logger.info("{}Thread:{} {}{}{}", 
                    ColorCodes.BLUE, ColorCodes.RESET, ColorCodes.RED, threadInfo.getThreadName(), ColorCodes.RESET);
                logger.info("{}  Blocked Time:{} {}{}{}", 
                    ColorCodes.BLUE, ColorCodes.RESET, ColorCodes.RED, blockedTimeStr, ColorCodes.RESET);
                logger.info("{}  Blocked by:{} {}{}{}", 
                    ColorCodes.BLUE, ColorCodes.RESET, ColorCodes.YELLOW, threadInfo.getLockOwnerName(), ColorCodes.RESET);
            }
        }
    }

    private void logThreadPoolMetrics() {
        logger.info("{}=== Thread Pool Metrics ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
        // Add thread pool metrics collection here
        // This will be implemented when we have access to thread pool beans
    }
} 