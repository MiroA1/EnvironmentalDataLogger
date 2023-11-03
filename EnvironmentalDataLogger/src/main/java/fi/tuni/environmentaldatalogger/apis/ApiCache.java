package fi.tuni.environmentaldatalogger.apis;

import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.util.TimeUtils;
import javafx.util.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

public class ApiCache {

    TreeMap<String, TreeMap<LocalDateTime, Double>> cache = new TreeMap<>();
    Coordinate location = null;

    public ApiCache() {

    }

    public void insert(Coordinate location, TreeMap<String, TreeMap<LocalDateTime, Double>> data) {

        if (getCacheSize() > 10000) {
            cache.clear();
        }

        if (this.location != location) {
            cache.clear();
            this.location = location;
        }

        for (String param : data.keySet()) {
            if (cache.containsKey(param)) {
                cache.get(param).putAll(data.get(param));
            } else {
                cache.put(param, data.get(param));
            }
        }
    }

    public TreeMap<LocalDateTime, Double> get(Coordinate location, String param, Pair<LocalDateTime, LocalDateTime> range, Duration margin) {

        if (location != this.location) {
            return null;
        }

        var data = cache.get(param);

        if (data == null) {
            return null;
        }

        if (data.firstKey().isAfter(range.getKey().plus(margin)) || data.lastKey().isBefore(range.getValue().minus(margin))) {
            return null;
        }

        SortedMap<LocalDateTime, Double> subMap = data.subMap(TimeUtils.getStartOfDay(range.getKey()), TimeUtils.getEndOfDay(range.getValue()));

        System.out.println("Cache hit: " + param + " " + subMap.size() + " " + subMap.firstKey() + " " + subMap.lastKey());
        return new TreeMap<>(subMap);
    }

    private int getCacheSize() {

        int size = 0;

        for (String param : cache.keySet()) {
            size += cache.get(param).size();
        }

        return size;
    }
}
