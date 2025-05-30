package com.resourcemonitor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.util.List;

/**
 * Auto-configuration for the ResourceMonitor.
 * Creates and configures the ResourceMonitor bean when enabled.
 */
@Configuration
@EnableConfigurationProperties(ResourceMonitorProperties.class)
@ConditionalOnProperty(prefix = "resource.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResourceMonitorAutoConfiguration {

    @Bean
    public ResourceMonitor resourceMonitor(List<DataSource> dataSources, ResourceMonitorProperties properties) {
        return new ResourceMonitor(dataSources, properties);
    }
} 