package fi.tuni.environmentaldatalogger.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A class for representing coordinates on Earth.
 */
public record Coordinate(double latitude, double longitude) {

    /**
     * Constructor
     * @param latitude given as double value
     * @param longitude given as double value
     */
    public Coordinate {
        validateLatitude(latitude);
        validateLongitude(longitude);
    }

    /**
     * Checks whether the given latitude is valid.
     * @param latitude latitude to check
     */
    private static void validateLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees.");
        }
    }

    /**
     * Checks whether the given longitude is valid.
     * @param longitude longitude to check
     */
    private static void validateLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees.");
        }
    }

    /**
     * Returns whether the coordinate is close enough to another coordinate to be considered
     * identical for the purposes of this application.
     * @param another another coordinate
     * @return true if the coordinates are close enough, false otherwise
     */
    public boolean isCloseEnoughTo(Coordinate another) {

        if (another == null) {
            return false;
        }

        return Math.abs(latitude - another.latitude) < 0.01 && Math.abs(longitude - another.longitude) < 0.01;
    }

    @Override
    public String toString() {
        return String.format("%.4f° N; %.4f° E", latitude, longitude);
    }

    /**
     * Returns a string representation of the coordinate with the given precision.
     * @param precision number of decimal places
     * @return string representation of the coordinate
     */
    public String toString(int precision) {
        return String.format("%." + precision + "f° N; %." + precision + "f° E", latitude, longitude);
    }
}
