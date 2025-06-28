package com.pino.intellijcallgraph.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {

    private TimeUtils() {
    }

    public static String getSpendTime(LocalDateTime startTime, LocalDateTime endTime) {
        var duration = Duration.between(startTime, endTime);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
