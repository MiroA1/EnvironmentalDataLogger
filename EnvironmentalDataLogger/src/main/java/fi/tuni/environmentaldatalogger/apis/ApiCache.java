package fi.tuni.environmentaldatalogger.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.tuni.environmentaldatalogger.save.Loadable;
import fi.tuni.environmentaldatalogger.save.LocalDateTimeAdapter;
import fi.tuni.environmentaldatalogger.save.Saveable;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.util.TimeUtils;
import javafx.util.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

// TODO: separate forecast and history caches
// TODO: make sure data resolution is right for query

/**
 * A very crude caching solution for API data.
 */
public class ApiCache implements Saveable, Loadable {

    TreeMap<String, TreeMap<LocalDateTime, Double>> cache = new TreeMap<>();
    Coordinate location = null;

    public ApiCache() {

    }

    /**
     * Inserts data into cache.
     * @param location Location of the data.
     * @param data Data to be inserted.
     */
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

    /**
     * Returns data from cache. Returns null if the whole data is not in cache.
     * @param location
     * @param param
     * @param range
     * @param margin
     * @return
     */
    public TreeMap<LocalDateTime, Double> get(Coordinate location, String param, Pair<LocalDateTime, LocalDateTime> range, Duration margin) {

        if (!location.isCloseEnoughTo(this.location)) {
            System.out.println("Cache miss: wrong location: " + location + ", cache: " + this.location);
            return null;
        }

        var data = cache.get(param);

        if (data == null) {
            System.out.println("Cache miss: cache does not contain data for " + param);
            return null;
        }

        if (data.isEmpty()) {
            System.out.println("Cache miss: 0 entries for  " + param);
            return null;
        }

        if (data.firstKey().isAfter(range.getKey().plus(margin)) || data.lastKey().isBefore(range.getValue().minus(margin))) {
            System.out.println("Cache miss: not enough data for " + param + " " + data.firstKey() + " " + data.lastKey() + " " + range.getKey() + " " + range.getValue());
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

    @Override
    public String getJson() {

        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        return gson.toJson(new SaveData(cache, location));
    }

    @Override
    public boolean loadFromJson(String json) {

        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        SaveData data = gson.fromJson(json, SaveData.class);

        if (data == null) {
            return false;
        }

        cache = data.cache;
        location = data.location;

        return true;
    }

    private record SaveData(TreeMap<String, TreeMap<LocalDateTime, Double>> cache, Coordinate location) {
    }
}
