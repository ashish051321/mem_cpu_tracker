package com.resourcemonitor;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the ResourceMonitor.
 * Controls which monitoring sections are enabled and the monitoring interval.
 */
@ConfigurationProperties(prefix = "resource.monitor")
public class ResourceMonitorProperties {
    private boolean memoryEnabled = true;
    private boolean cpuEnabled = true;
    private boolean threadEnabled = true;
    private boolean databaseEnabled = true;
    private long intervalSeconds = 60;

    public boolean isMemoryEnabled() {
        return memoryEnabled;
    }

    public void setMemoryEnabled(boolean memoryEnabled) {
        this.memoryEnabled = memoryEnabled;
    }

    public boolean isCpuEnabled() {
        return cpuEnabled;
    }

    public void setCpuEnabled(boolean cpuEnabled) {
        this.cpuEnabled = cpuEnabled;
    }

    public boolean isThreadEnabled() {
        return threadEnabled;
    }

    public void setThreadEnabled(boolean threadEnabled) {
        this.threadEnabled = threadEnabled;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    public void setDatabaseEnabled(boolean databaseEnabled) {
        this.databaseEnabled = databaseEnabled;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }
} 