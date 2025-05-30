package com.resourcemonitor;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resource.monitor")
public class ResourceMonitorProperties {
    private long intervalSeconds = 60; // Default to 1 minute
    
    // Section toggles
    private boolean memoryMonitoring = true;
    private boolean cpuMonitoring = true;
    private boolean threadMonitoring = true;
    private boolean threadPoolMonitoring = true;
    private boolean databasePoolMonitoring = true;
    
    // Thread monitoring sub-sections
    private boolean threadStateDistribution = true;
    private boolean deadlockDetection = true;
    private boolean highCpuThreads = true;
    private boolean blockedThreads = true;

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public boolean isMemoryMonitoring() {
        return memoryMonitoring;
    }

    public void setMemoryMonitoring(boolean memoryMonitoring) {
        this.memoryMonitoring = memoryMonitoring;
    }

    public boolean isCpuMonitoring() {
        return cpuMonitoring;
    }

    public void setCpuMonitoring(boolean cpuMonitoring) {
        this.cpuMonitoring = cpuMonitoring;
    }

    public boolean isThreadMonitoring() {
        return threadMonitoring;
    }

    public void setThreadMonitoring(boolean threadMonitoring) {
        this.threadMonitoring = threadMonitoring;
    }

    public boolean isThreadPoolMonitoring() {
        return threadPoolMonitoring;
    }

    public void setThreadPoolMonitoring(boolean threadPoolMonitoring) {
        this.threadPoolMonitoring = threadPoolMonitoring;
    }

    public boolean isDatabasePoolMonitoring() {
        return databasePoolMonitoring;
    }

    public void setDatabasePoolMonitoring(boolean databasePoolMonitoring) {
        this.databasePoolMonitoring = databasePoolMonitoring;
    }

    public boolean isThreadStateDistribution() {
        return threadStateDistribution;
    }

    public void setThreadStateDistribution(boolean threadStateDistribution) {
        this.threadStateDistribution = threadStateDistribution;
    }

    public boolean isDeadlockDetection() {
        return deadlockDetection;
    }

    public void setDeadlockDetection(boolean deadlockDetection) {
        this.deadlockDetection = deadlockDetection;
    }

    public boolean isHighCpuThreads() {
        return highCpuThreads;
    }

    public void setHighCpuThreads(boolean highCpuThreads) {
        this.highCpuThreads = highCpuThreads;
    }

    public boolean isBlockedThreads() {
        return blockedThreads;
    }

    public void setBlockedThreads(boolean blockedThreads) {
        this.blockedThreads = blockedThreads;
    }
} 