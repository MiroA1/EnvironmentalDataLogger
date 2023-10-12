package fi.tuni.environmentaldatalogger;

import javafx.util.Pair;
import okhttp3.OkHttpClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;


public class AirQualityDataExtractor implements DataExtractor {

    private OkHttpClient httpClient;

    private static AirQualityDataExtractor instance;

    private AirQualityDataExtractor() {
        this.httpClient = new OkHttpClient();
    }

    public static AirQualityDataExtractor getInstance() {
        if (instance == null) {
            instance = new AirQualityDataExtractor();
        }
        return instance;
    }

    /**
     * List parameters which are available for the API
     * @return
     */
    @Override
    public ArrayList<String> getValidParameters() {
        return null;
    }

    /**
     * Return valid time range for the data
     * @param param
     * @return
     */
    @Override
    public Pair<Date, Date> getValidDataRange(String param) {
        return null;
    }

    /**
     * Return the data in map form
     * @param param the parameter of interest
     * @param range the time range for the data
     * @param coordinates the coordinates of the place
     * @return
     */
    @Override
    public TreeMap<Date, Double> getData(String param, Pair<Date, Date> range,
                                         Coordinate coordinates) {
        return null;
    }

    /**
     * Return all available data within the given parameter
     * @param param parameter which is queried
     * @param coordinates coordinates of the place
     * @return data in map form <Date, Double>
     */
    @Override
    public TreeMap<Date, Double> getData(String param, Coordinate coordinates) {
        return null;
    }

    /**
     * Function to fetch raw parameter data from the given url
     * @param apiUrl url to the API
     * @param param the parameter of interest
     * @return
     */
    private TreeMap<Date, Double> fetchData(String apiUrl, String param){
        TreeMap<Date, Double> data = new TreeMap<Date,Double>();
        return data;
    }

    /**
     * Constructor for API url. Currently uses place instead of coordinates.
     * @param place the name of the city or town
     * @param place the name of the city or town
     * @param startDate the start date in ISO 8601 format (yyyy-MM-dd)
     * @param endDate the end date in form ISO 8601 format (yyyy-MM-dd)
     * @return
     */
    private String constructApiUrl(String place, Date startDate, Date endDate) {
        String url = new String("Url");
        return url;
    }

}
