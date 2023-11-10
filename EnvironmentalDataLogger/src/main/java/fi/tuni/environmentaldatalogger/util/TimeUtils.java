package fi.tuni.environmentaldatalogger.util;

import java.time.LocalDateTime;

public class TimeUtils {

    public static LocalDateTime getStartOfDay(LocalDateTime date) {
        return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static LocalDateTime getEndOfDay(LocalDateTime date) {
        return date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }
}
