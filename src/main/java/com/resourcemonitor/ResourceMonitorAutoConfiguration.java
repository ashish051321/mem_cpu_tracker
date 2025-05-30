package com.resourcemonitor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ResourceMonitorProperties.class)
public class ResourceMonitorAutoConfiguration {

    private final ResourceMonitor resourceMonitor;
    private final ApplicationContext applicationContext;

    public ResourceMonitorAutoConfiguration(ResourceMonitorProperties properties, ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        List<DataSource> dataSources = getDataSources();
        this.resourceMonitor = ResourceMonitor.create(properties.getIntervalSeconds(), dataSources, properties);
    }

    private List<DataSource> getDataSources() {
        try {
            Map<String, DataSource> dataSourceMap = applicationContext.getBeansOfType(DataSource.class);
            return List.copyOf(dataSourceMap.values());
        } catch (Exception e) {
            return List.of();
        }
    }

    @PostConstruct
    public void startMonitoring() {
        resourceMonitor.start();
    }

    @PreDestroy
    public void stopMonitoring() {
        resourceMonitor.stop();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceMonitor resourceMonitor() {
        return resourceMonitor;
    }
} 