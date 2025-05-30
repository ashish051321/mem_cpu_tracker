package com.resourcemonitor.util;

/**
 * Utility class providing ANSI color codes for console output.
 * These colors are used to make the monitoring output more readable and meaningful.
 */
public final class ColorCodes {
    private ColorCodes() {} // Prevent instantiation

    public static final String RESET = "\u001B[0m";    // Reset color
    public static final String RED = "\u001B[31m";     // Error/Critical state
    public static final String GREEN = "\u001B[32m";   // Normal/Healthy state
    public static final String YELLOW = "\u001B[33m";  // Warning state
    public static final String BLUE = "\u001B[34m";    // Section headers
    public static final String PURPLE = "\u001B[35m";  // Main section headers
    public static final String CYAN = "\u001B[36m";    // Debug/Info messages
    public static final String BOLD = "\u001B[1m";     // Bold text

    /**
     * Returns appropriate color code based on percentage value.
     * @param percentage The percentage value to evaluate
     * @return Color code string
     */
    public static String getColorForPercentage(double percentage) {
        if (percentage >= 90) {
            return RED;
        } else if (percentage >= 70) {
            return YELLOW;
        } else {
            return GREEN;
        }
    }

    /**
     * Returns appropriate color code based on thread state.
     * @param state The thread state to evaluate
     * @return Color code string
     */
    public static String getColorForThreadState(Thread.State state) {
        switch (state) {
            case BLOCKED:
                return RED;
            case WAITING:
            case TIMED_WAITING:
                return YELLOW;
            case RUNNABLE:
                return GREEN;
            default:
                return RESET;
        }
    }
} 