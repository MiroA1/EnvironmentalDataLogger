package fi.tuni.environmentaldatalogger.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javafx.util.Pair;

/**
 * A class containing methods for various operations regarding time used throughout the application.
 */
public class TimeUtils {

    /**
     * Returns the start of the day of the given date.
     * @param date datetime
     * @return start of the day of the given date
     */
    public static LocalDateTime getStartOfDay(LocalDateTime date) {
        return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Returns the end of the day of the given date.
     * @param date datetime
     * @return end of the day of the given date
     */
    public static LocalDateTime getEndOfDay(LocalDateTime date) {
        return date.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Returns the start of the hour of the given date.
     * @param date datetime
     * @return start of the hour of the given date
     */
    public static LocalDateTime getStartOfHour(LocalDateTime date) {
        return date.withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Returns the end of the hour of the given date.
     * @param date datetime
     * @return end of the hour of the given date
     */
    public static LocalDateTime getEndOfHour(LocalDateTime date) {
        return date.plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Returns given datetime as Unix time.
     * @param date datetime
     * @return start of the minute of the given date
     */
    public static long getEpochSecond(LocalDateTime date) {
        return date.toEpochSecond(ZoneOffset.UTC);
    }
}
