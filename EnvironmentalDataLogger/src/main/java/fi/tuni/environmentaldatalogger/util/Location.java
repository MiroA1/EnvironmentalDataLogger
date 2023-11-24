package fi.tuni.environmentaldatalogger.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A class for representing a location on Earth.
 */
public class Location {

    private final String name;
    private final String countryCode;
    private final Coordinate coordinates;

    /**
     * Creates a new Location object.
     * @param name name of the location
     * @param countryCode country code of the location
     * @param coordinates coordinates of the location
     */
    public Location(String name, String countryCode, Coordinate coordinates) {
        this.name = name;
        this.coordinates = coordinates;
        this.countryCode = countryCode;
    }

    /**
     * Returns the name of the location.
     * @return name of the location
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the coordinates of the location.
     * @return coordinates of the location
     */
    public Coordinate getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        if (name.equals("")) {
            return coordinates.toString();
        } else {
            return name + ", " + countryCode;
        }
    }

    /**
     * Creates a Location object based on the given coordinates using a reverse geocoding API.
     * @param coordinates coordinates of the location
     * @return Location object
     * @throws IOException if an I/O error occurs
     */
    public static Location fromCoordinates(Coordinate coordinates) throws IOException {

        double lat = coordinates.latitude();
        double lon = coordinates.longitude();
        String urlstring = "http://api.openweathermap.org/geo/1.0/reverse?lat=" + lat + "&lon=" + lon + "&limit=1&appid=07d06bd23d2396085f4b5fc56085fe7b";

        URL url = new URL(urlstring);

        InputStreamReader reader = new InputStreamReader(url.openStream());
        Gson gson = new Gson();
        JsonArray arr = gson.fromJson(reader, JsonArray.class);

        JsonObject jo = arr.get(0).getAsJsonObject();

        String placeName = jo.get("name").getAsString();
        String countryCode = jo.get("country").getAsString();

        return new Location(placeName, countryCode, coordinates);
    }

    /**
     * Creates a Location object based on the given place name using a geocoding API.
     * @param placeName place name
     * @return Location object
     * @throws IOException if an I/O error occurs
     */
    public static Location fromPlaceName(String placeName) throws IOException {
        String urlstring = "http://api.openweathermap.org/geo/1.0/direct?q=" + placeName + "&limit=1&appid=07d06bd23d2396085f4b5fc56085fe7b";

        URL url = new URL(urlstring);

        InputStreamReader reader = new InputStreamReader(url.openStream());
        Gson gson = new Gson();
        JsonArray arr = gson.fromJson(reader, JsonArray.class);

        try {
            JsonObject jo = arr.get(0).getAsJsonObject();

            String name = jo.get("name").getAsString();

            double lat = jo.get("lat").getAsDouble();
            double lon = jo.get("lon").getAsDouble();

            return new Location(name, jo.get("country").getAsString(), new Coordinate(lat, lon));
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid place name");
        }

    }

    public static Location fromIP() throws IOException {
        String urlstring = "http://ip-api.com/json/";

        URL url = new URL(urlstring);

        InputStreamReader reader = new InputStreamReader(url.openStream());
        Gson gson = new Gson();
        JsonObject arr = gson.fromJson(reader, JsonObject.class);

        String name = arr.get("city").getAsString();
        String countryCode = arr.get("countryCode").getAsString();
        double lat = arr.get("lat").getAsDouble();
        double lon = arr.get("lon").getAsDouble();

        return new Location(name, countryCode, new Coordinate(lat, lon));
    }

    // TODO: toteutetaan jos ehditään, esim. https://ip-api.com/
    public static Coordinate geoLocateByIP() {
        return null;
    }
}
