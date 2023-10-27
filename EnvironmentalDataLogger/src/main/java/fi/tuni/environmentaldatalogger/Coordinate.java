package fi.tuni.environmentaldatalogger;

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

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude: " + latitude);
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude: " + longitude);
        }
    }

    public String toString() {
        return String.format("%.4f° N; %.4f° E", latitude, longitude);
    }
}
