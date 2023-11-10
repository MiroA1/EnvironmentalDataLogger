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

public record Coordinate(double latitude, double longitude) {

    public Coordinate {
        validateLatitude(latitude);
        validateLongitude(longitude);
    }

    private static void validateLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees.");
        }
    }

    private static void validateLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees.");
        }
    }

    public boolean isCloseEnoughTo(Coordinate another) {

        if (another == null) {
            return false;
        }

        return Math.abs(latitude - another.latitude) < 0.001 && Math.abs(longitude - another.longitude) < 0.001;
    }

    public String toString() {
        return String.format("%.4f° N; %.4f° E", latitude, longitude);
    }
}
