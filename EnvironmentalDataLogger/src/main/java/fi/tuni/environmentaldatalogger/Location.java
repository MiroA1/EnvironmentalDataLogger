package fi.tuni.environmentaldatalogger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class Location {

    private final String name;
    private final String countryCode;
    private final Coordinate coordinates;

    public Location(String name, String countryCode, Coordinate coordinates) {
        this.name = name;
        this.coordinates = coordinates;
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public String toString() {
        if (name.equals("")) {
            return coordinates.toString();
        } else {
            return name + ", " + countryCode;
        }
    }

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

    public static Location fromPlaceName(String placeName) throws IOException {
        String urlstring = "http://api.openweathermap.org/geo/1.0/direct?q=" + placeName + "&limit=1&appid=07d06bd23d2396085f4b5fc56085fe7b";

        URL url = new URL(urlstring);

        InputStreamReader reader = new InputStreamReader(url.openStream());
        Gson gson = new Gson();
        JsonArray arr = gson.fromJson(reader, JsonArray.class);

        JsonObject jo = arr.get(0).getAsJsonObject();

        double lat = jo.get("lat").getAsDouble();
        double lon = jo.get("lon").getAsDouble();

        return new Location(placeName, jo.get("country").getAsString(), new Coordinate(lat, lon));
    }

    // TODO: toteutetaan jos ehditään, esim. https://ip-api.com/
    public static Coordinate geoLocateByIP() {
        return null;
    }
}
