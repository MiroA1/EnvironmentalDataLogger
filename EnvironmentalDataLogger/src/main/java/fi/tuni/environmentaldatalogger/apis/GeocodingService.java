package fi.tuni.environmentaldatalogger.apis;

import com.opencagedata.jopencage.JOpenCageGeocoder;
import com.opencagedata.jopencage.model.JOpenCageForwardRequest;
import com.opencagedata.jopencage.model.JOpenCageLatLng;
import com.opencagedata.jopencage.model.JOpenCageResponse;
import fi.tuni.environmentaldatalogger.util.Coordinate;

public class GeocodingService {

    private static final String API_KEY = "5409ecbc4e384bddb83f0461b2234d9d";
    private static GeocodingService instance;
    private final JOpenCageGeocoder geocoder;

    private GeocodingService() {
        this.geocoder = new JOpenCageGeocoder(API_KEY);
    }

    public static synchronized GeocodingService getInstance() {
        if (instance == null) {
            instance = new GeocodingService();
        }
        return instance;
    }

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

