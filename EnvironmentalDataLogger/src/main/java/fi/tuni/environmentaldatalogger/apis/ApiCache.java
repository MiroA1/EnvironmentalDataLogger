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
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// TODO: separate forecast and history caches

/**
 * A fairly crude caching solution for API data. Thread-safe.
 */
public class ApiCache implements Saveable, Loadable {

    HashMap<Coordinate, TreeMap<String, TreeMap<LocalDateTime, Double>>> cache = new HashMap<>();
    Lock lock = new ReentrantLock();


    /**
     * Inserts data into cache.
     * @param location Location of the data.
     * @param data Data to be inserted.
     */
    public void insert(Coordinate location, TreeMap<String, TreeMap<LocalDateTime, Double>> data) {

        if (!getLock()) {
            return;
        }

        // just nuke the entire thing if it gets too big, partial deletion also has problems, not worth the time
        if (getCacheSize() > 20000) {
            System.out.println("Cache deleted");
            cache.clear();
        }

        Coordinate locationKey = getLocationKey(location);

        if (locationKey == null) {
            cache.put(location, new TreeMap<>());
        }

        var cacheForLocation = cache.get(location);

        // TODO: investigate, if time
        // it seems this can happen in rare cases, Gson probably turns an empty map to null after save + load
        if (cacheForLocation == null) {
            System.out.println("Cache miss: cacheForLocation null");
            cache.put(locationKey, new TreeMap<>());
            cacheForLocation = cache.get(locationKey);
        }

        for (String param : data.keySet()) {
            if (cacheForLocation.containsKey(param)) {
                cacheForLocation.get(param).putAll(data.get(param));
            } else {
                cacheForLocation.put(param, data.get(param));
            }
        }

        lock.unlock();
    }

    /**
     * Returns data from cache.
     * Returns null if the cache does not contain data for the whole range (even if part of it is available).
     * @param location location to get data for
     * @param param parameter to get data for
     * @param range range of data to get
     * @param margin margin include in addition to the range
     * @param resolution resolution required for the data
     * @return data from cache
     */
    public TreeMap<LocalDateTime, Double> get(Coordinate location, String param, Pair<LocalDateTime, LocalDateTime> range, Duration margin, Duration resolution) {

        if (!getLock()) {
            return null;
        }

        Coordinate locationKey = getLocationKey(location);

        if (locationKey == null) {
            System.out.println("Cache miss: no cache for location " + location);
            lock.unlock();
            return null;
        }

        var data = cache.get(locationKey).get(param);

        if (data == null) {
            System.out.println("Cache miss: cache does not contain data for " + param);
            lock.unlock();
            return null;
        }

        if (data.isEmpty()) {
            System.out.println("Cache miss: 0 entries for  " + param);
            lock.unlock();
            return null;
        }

        if (data.firstKey().isAfter(range.getKey().plus(margin)) ||
                data.lastKey().isBefore(range.getValue().minus(margin))) {
            System.out.println("Cache miss: not enough data for " + param + " " + data.firstKey() + " " + data.lastKey() + " " + range.getKey() + " " + range.getValue());
            lock.unlock();
            return null;
        }

        SortedMap<LocalDateTime, Double> subMap = data.subMap(TimeUtils.getStartOfDay(range.getKey()), TimeUtils.getEndOfDay(range.getValue()));

        // this is in case there is a gap in the cached data
        if (subMap.firstKey().isAfter(range.getKey().plus(margin))) {
            System.out.println("Cache miss: not enough data for " + param + " " + subMap.firstKey() + " " + range.getKey());
            lock.unlock();
            return null;
        }

        TreeMap<LocalDateTime, Double> result = new TreeMap<>();

        for (var key : subMap.keySet()) {

            if (result.isEmpty()) {
                result.put(key, subMap.get(key));
                continue;
            }

            long resolutionDiff = Duration.between(result.lastKey(), key).minus(resolution).toMinutes();
            double minDiff = -10;
            double maxDiff = resolution.toMinutes() * 0.1;

            if (resolutionDiff < minDiff) {
                continue;
            }

            if (resolutionDiff > maxDiff) {
                System.out.println("Cache miss: resolution too low for " + param + " " + resolutionDiff + " " + minDiff + " " + maxDiff);
                lock.unlock();
                return null;
            }

            result.put(key, subMap.get(key));
        }

        if (result.isEmpty()) {
            System.out.println("Cache miss: final result empty for " + location + " " + param + " " + range.getKey() + " " + range.getValue() + " " + margin + " " + resolution);
            lock.unlock();
            return null;
        }

        System.out.println("Cache hit: " + param + " " + result.size() + " " + result.firstKey() + " " + result.lastKey());

        lock.unlock();
        return result;
    }

    /**
     * Returns the location key for the given location if it exists.
     * Coordinates with only slight differences are considered identical.
     * @param location location as coordinates
     * @return location key if it exists, null otherwise
     */
    private Coordinate getLocationKey(Coordinate location) {

        if (!cache.containsKey(location)) {
            for (Coordinate coord : cache.keySet()) {
                if (coord.isCloseEnoughTo(location)) {
                    return coord;
                }
            }
        } else {
            return location;
        }

        return null;
    }

    /**
     * Returns the size of the cache.
     * @return size of the cache
     */
    private int getCacheSize() {

        int size = 0;

        for(Coordinate location : cache.keySet()) {
            for (String param : cache.get(location).keySet()) {
                size += cache.get(location).get(param).size();
            }
        }

        return size;
    }

    /**
     * Returns cache as JSON string.
     * @return cache as JSON string
     */
    @Override
    public String getJson() {

        if (!getLock()) {
            return null;
        }

        String result = gson.toJson(new SaveData(cache));

        lock.unlock();

        return result;
    }

    /**
     * Loads cache from JSON string.
     * @param json JSON string
     * @return true if successful, false otherwise
     */
    @Override
    public boolean loadFromJson(String json) {

        if (!getLock()) {
            return false;
        }

        try {
            SaveData data = gson.fromJson(json, SaveData.class);

            if (data == null) {
                lock.unlock();
                return false;
            }

            cache = data.cache;

        } catch (Exception e) {
            e.printStackTrace();
            lock.unlock();
            return false;
        }

        lock.unlock();
        return true;
    }

    /**
     * Tries to acquire the cache lock, gives up after one second.
     * @return true if the lock was acquired, false otherwise
     */
    private boolean getLock() {

        try {
            if (!lock.tryLock(1, TimeUnit.SECONDS)) {
                System.out.println("Thread failed to acquire cache lock");
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(
            LocalDateTime.class, new LocalDateTimeAdapter()).enableComplexMapKeySerialization().create();

    /**
     * A class for saving the cache.
     * @param cache cache to be saved
     */
    private record SaveData(HashMap<Coordinate, TreeMap<String, TreeMap<LocalDateTime, Double>>> cache) {
    }
}
