package com.almeneses.exam.utils;

public final class TimeUtils {
    public static String toClockFormat(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        String formattedMinutes = minutes < 10 ? "0" : "";
        String formattedSeconds = seconds < 10 ? "0" : "";

        return formattedMinutes + minutes + " : " + formattedSeconds + seconds;
    }
}
