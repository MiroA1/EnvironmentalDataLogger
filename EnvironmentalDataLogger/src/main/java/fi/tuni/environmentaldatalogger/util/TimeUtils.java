package fi.tuni.environmentaldatalogger.util;

import java.time.Duration;
import java.time.LocalDateTime;
import javafx.util.Pair;

public class TimeUtils {

    public static LocalDateTime getStartOfDay(LocalDateTime date) {
        return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public static LocalDateTime getEndOfDay(LocalDateTime date) {
        return date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    public static Pair<LocalDateTime, LocalDateTime> dateRangeDifference(Pair<LocalDateTime, LocalDateTime> range1,
                                                                         Pair<LocalDateTime, LocalDateTime> range2) {

        LocalDateTime start = range1.getKey().isBefore(range2.getKey()) ? range1.getKey() : range2.getKey();
        LocalDateTime end = range1.getValue().isAfter(range2.getValue()) ? range1.getValue() : range2.getValue();

        return new Pair<>(start, end);
    }

    public static Duration rangeDuration(Pair<LocalDateTime, LocalDateTime> range) {
        return Duration.between(range.getKey(), range.getValue());
    }
}
