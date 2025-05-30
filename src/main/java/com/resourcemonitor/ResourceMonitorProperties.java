package com.resourcemonitor;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resource.monitor")
public class ResourceMonitorProperties {
    private long intervalSeconds = 60; // Default to 1 minute

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(long intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }
} 