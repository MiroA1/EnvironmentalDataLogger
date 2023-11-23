package fi.tuni.environmentaldatalogger.apis;

import com.opencagedata.jopencage.JOpenCageGeocoder;
import com.opencagedata.jopencage.model.JOpenCageForwardRequest;
import com.opencagedata.jopencage.model.JOpenCageLatLng;
import com.opencagedata.jopencage.model.JOpenCageResponse;
import fi.tuni.environmentaldatalogger.util.Coordinate;

/**
 * The GeocodingService class provides geocoding services by interacting with the JOpenCage Geocoding API.
 * It converts a location query (like an address) into geographical coordinates (latitude and longitude).
 */
public class GeocodingService {

    private static final String API_KEY = "5409ecbc4e384bddb83f0461b2234d9d";
    private static GeocodingService instance;
    private final JOpenCageGeocoder geocoder;

    /**
     * Private constructor for GeocodingService.
     * Initializes the JOpenCageGeocoder with the API key.
     */
    private GeocodingService() {
        this.geocoder = new JOpenCageGeocoder(API_KEY);
    }

    /**
     * Gets the singleton instance of GeocodingService.
     * If the instance does not exist, it is created.
     *
     * @return The single instance of GeocodingService.
     */
    public static synchronized GeocodingService getInstance() {
        if (instance == null) {
            instance = new GeocodingService();
        }
        return instance;
    }

    /**
     * Converts a location query string into geographical coordinates.
     * Only the first result from the geocoding service is returned.
     *
     * @param query The location query string, like an address or place name.
     * @return A Coordinate object containing the latitude and longitude of the location,
     *         or null if no result is found or in case of an error.
     */
    public Coordinate getCoordinates(String query) {
        JOpenCageForwardRequest request = new JOpenCageForwardRequest(query);
        request.setLimit(1); // get only first result

        JOpenCageResponse response = geocoder.forward(request);
        if (response != null && !response.getResults().isEmpty()) {
            JOpenCageLatLng latLng = response.getResults().get(0).getGeometry();
            return new Coordinate(latLng.getLat(), latLng.getLng());
        } else {
            // Handle errors / null
            return null;
        }
    }
}


