package com.resourcemonitor.util;

/**
 * Utility class for formatting various values in the monitoring output.
 */
public final class FormatUtils {
    private FormatUtils() {} // Prevent instantiation

    /**
     * Formats bytes to megabytes with 2 decimal places.
     * @param bytes The number of bytes to format
     * @return Formatted string in MB
     */
    public static String formatMB(long bytes) {
        return String.format("%.2f", bytes / (1024.0 * 1024.0));
    }

    /**
     * Formats nanoseconds to milliseconds with 2 decimal places.
     * @param nanos The number of nanoseconds to format
     * @return Formatted string in milliseconds
     */
    public static String formatNanosToMillis(long nanos) {
        return String.format("%.2f", nanos / 1_000_000.0);
    }
} 