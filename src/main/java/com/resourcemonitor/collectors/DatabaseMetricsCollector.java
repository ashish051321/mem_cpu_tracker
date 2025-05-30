package com.resourcemonitor.collectors;

import com.resourcemonitor.util.ColorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.lang.reflect.Method;

/**
 * Collects and logs database connection pool metrics for various connection pool implementations.
 * 
 * This collector supports multiple connection pool implementations:
 * 1. HikariCP - A high-performance JDBC connection pool
 * 2. Tomcat JDBC Pool - Apache Tomcat's connection pool implementation
 * 3. Generic DataSource - Basic metrics for any DataSource implementation
 * 
 * The collector uses reflection to safely access pool-specific methods, allowing it to work
 * without direct dependencies on specific connection pool implementations. This makes the
 * collector more flexible and maintainable.
 * 
 * Metrics collected include:
 * - Active/Idle/Total connections
 * - Connection utilization percentages
 * - Pool configuration (timeouts, max size, etc.)
 * - Connection wait statistics
 * - Pool state and health indicators
 */
public class DatabaseMetricsCollector implements MetricsCollector {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMetricsCollector.class);
    private final List<DataSource> dataSources;

    /**
     * Creates a new DatabaseMetricsCollector.
     * 
     * @param dataSources List of DataSource instances to monitor. These can be of any
     *                    supported connection pool implementation.
     */
    public DatabaseMetricsCollector(List<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public void collectAndLog() {
        for (DataSource dataSource : dataSources) {
            if (isHikariPool(dataSource)) {
                logHikariPoolMetrics(dataSource);
            } else if (isTomcatPool(dataSource)) {
                logTomcatPoolMetrics(dataSource);
            } else {
                logGenericPoolMetrics(dataSource);
            }
        }
    }

    /**
     * Checks if a DataSource is a HikariCP implementation.
     * Uses class name check to avoid direct dependency.
     * 
     * @param dataSource The DataSource to check
     * @return true if the DataSource is a HikariCP implementation
     */
    private boolean isHikariPool(DataSource dataSource) {
        return dataSource.getClass().getName().contains("HikariDataSource");
    }

    /**
     * Checks if a DataSource is a Tomcat JDBC Pool implementation.
     * Uses class name check to avoid direct dependency.
     * 
     * @param dataSource The DataSource to check
     * @return true if the DataSource is a Tomcat JDBC Pool implementation
     */
    private boolean isTomcatPool(DataSource dataSource) {
        return dataSource.getClass().getName().contains("org.apache.tomcat.jdbc.pool.DataSource");
    }

    /**
     * Collects and logs metrics for a HikariCP connection pool.
     * 
     * Metrics collected:
     * - Active connections with utilization percentage
     * - Idle connections
     * - Total connections
     * - Pool configuration (timeouts, max lifetime)
     * 
     * Uses reflection to safely access HikariCP-specific methods.
     * 
     * @param dataSource The HikariCP DataSource to monitor
     */
    private void logHikariPoolMetrics(DataSource dataSource) {
        try {
            // Get the HikariPoolMXBean for runtime metrics
            Object pool = dataSource.getClass().getMethod("getHikariPoolMXBean").invoke(dataSource);
            logger.info("{}=== HikariCP Pool Metrics ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
            
            // Active connections with utilization percentage
            int activeConnections = (int) pool.getClass().getMethod("getActiveConnections").invoke(pool);
            logger.info("{}Active Connections:{} {}{}{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.getColorForPercentage(activeConnections * 100.0 / 10), // Assuming max pool size of 10
                activeConnections, ColorCodes.RESET);

            // Idle connections
            int idleConnections = (int) pool.getClass().getMethod("getIdleConnections").invoke(pool);
            logger.info("{}Idle Connections:{} {}{}{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.GREEN, idleConnections, ColorCodes.RESET);

            // Total connections
            int totalConnections = (int) pool.getClass().getMethod("getTotalConnections").invoke(pool);
            logger.info("{}Total Connections:{} {}{}{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.CYAN, totalConnections, ColorCodes.RESET);

            // Pool configuration - get from HikariDataSource
            logger.info("{}Pool Configuration:{}", ColorCodes.BLUE, ColorCodes.RESET);
            logger.info("{}  Connection Timeout:{} {}{}ms{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.CYAN, dataSource.getClass().getMethod("getConnectionTimeout").invoke(dataSource), ColorCodes.RESET);
            logger.info("{}  Idle Timeout:{} {}{}ms{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.CYAN, dataSource.getClass().getMethod("getIdleTimeout").invoke(dataSource), ColorCodes.RESET);
            logger.info("{}  Max Lifetime:{} {}{}ms{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.CYAN, dataSource.getClass().getMethod("getMaxLifetime").invoke(dataSource), ColorCodes.RESET);

        } catch (Exception e) {
            logger.warn("Failed to collect HikariCP metrics: {}", e.getMessage());
        }
    }

    /**
     * Collects and logs metrics for a Tomcat JDBC Pool.
     * 
     * Metrics collected:
     * - Active connections with utilization percentage
     * - Idle connections
     * - Maximum active connections
     * - Connection wait statistics (if any)
     * 
     * Uses reflection to safely access Tomcat pool-specific methods.
     * 
     * @param dataSource The Tomcat JDBC Pool DataSource to monitor
     */
    private void logTomcatPoolMetrics(DataSource dataSource) {
        try {
            logger.info("{}=== Tomcat JDBC Pool Metrics ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
            
            // Basic metrics
            int active = (int) dataSource.getClass().getMethod("getActive").invoke(dataSource);
            int idle = (int) dataSource.getClass().getMethod("getIdle").invoke(dataSource);
            int maxActive = (int) dataSource.getClass().getMethod("getMaxActive").invoke(dataSource);
            
            // Log connection metrics with utilization percentage
            logger.info("{}Active Connections:{} {}{}{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.getColorForPercentage(active * 100.0 / maxActive),
                active, ColorCodes.RESET);
            logger.info("{}Idle Connections:{} {}{}{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.GREEN, idle, ColorCodes.RESET);
            logger.info("{}Max Active:{} {}{}{}", 
                ColorCodes.BLUE, ColorCodes.RESET,
                ColorCodes.CYAN, maxActive, ColorCodes.RESET);

            // Connection wait statistics
            long waitCount = (long) dataSource.getClass().getMethod("getWaitCount").invoke(dataSource);
            long waitTime = (long) dataSource.getClass().getMethod("getWaitTime").invoke(dataSource);
            
            // Only log wait stats if there have been waits
            if (waitCount > 0) {
                logger.info("{}Connection Wait Stats:{}", ColorCodes.BLUE, ColorCodes.RESET);
                logger.info("{}  Total Wait Count:{} {}{}{}", 
                    ColorCodes.BLUE, ColorCodes.RESET,
                    ColorCodes.YELLOW, waitCount, ColorCodes.RESET);
                logger.info("{}  Average Wait Time:{} {}{}ms{}", 
                    ColorCodes.BLUE, ColorCodes.RESET,
                    ColorCodes.YELLOW, waitTime / waitCount, ColorCodes.RESET);
            }

        } catch (Exception e) {
            logger.warn("Failed to collect Tomcat JDBC Pool metrics: {}", e.getMessage());
        }
    }

    /**
     * Logs basic metrics for a generic DataSource implementation.
     * This is a fallback for unsupported connection pool implementations.
     * 
     * @param dataSource The generic DataSource to monitor
     */
    private void logGenericPoolMetrics(DataSource dataSource) {
        logger.info("{}=== Generic DataSource Metrics ==={}", ColorCodes.BOLD + ColorCodes.PURPLE, ColorCodes.RESET);
        logger.info("{}DataSource Type:{} {}{}{}", 
            ColorCodes.BLUE, ColorCodes.RESET,
            ColorCodes.CYAN, dataSource.getClass().getName(), ColorCodes.RESET);
        // Add more generic metrics if available
    }
} 