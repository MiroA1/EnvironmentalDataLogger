package fi.tuni.environmentaldatalogger;

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

    // TODO: toteutetaan jos ehditään, esim. https://developers.google.com/maps/documentation/geocoding/overview
    public static Coordinate searchCoordinates(String placeName) {
        return null;
    }

    // TODO: toteutetaan jos ehditään, esim. https://ip-api.com/
    public static Coordinate geoLocateByIP() {
        return null;
    }
}
