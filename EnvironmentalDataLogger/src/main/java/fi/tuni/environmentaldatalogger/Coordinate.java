package fi.tuni.environmentaldatalogger;

public record Coordinate(double latitude, double longitude) {

    // TODO: toteutetaan jos ehditään, esim. https://developers.google.com/maps/documentation/geocoding/overview
    public static Coordinate searchCoordinates(String placeName) {
        return null;
    }

    // TODO: toteutetaan jos ehditään, esim. https://ip-api.com/
    public static Coordinate geoLocateByIP() {
        return null;
    }
}
