package fi.tuni.environmentaldatalogger;

import javafx.util.Pair;
import okhttp3.OkHttpClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

/**
 *  Data extractor class to fetch Air Quality data.
 */
public class AirQualityDataExtractor implements DataExtractor {

    /**
     * Returns an instance of the Air Quality data extractor
     * @return AirQualityDataExtractor instance
     */
    public static AirQualityDataExtractor getInstance() {
        if (instance == null) {
            instance = new AirQualityDataExtractor();
        }
        return instance;
    }

    /**
     * List parameters which are available for the API
     * @return a list of valid String parameters
     */
    @Override
    public ArrayList<String> getValidParameters() {
        return null;
    }

    /**
     * Return valid time range for the data
     * @param param the parameter of interest
     * @return valid time range as Date pair
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
     * @return map of data, <Date, Double>
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
     * @return the raw data as map structure; <Date, Double>
     */
    private TreeMap<Date, Double> fetchData(String apiUrl, String param){
        TreeMap<Date, Double> data = new TreeMap<>();
        return data;
    }

    /**
     * Constructor for API url. Currently uses place instead of coordinates.
     * @param place the name of the city or town
     * @param startDate the start date in ISO 8601 format (yyyy-MM-dd)
     * @param endDate the end date in form ISO 8601 format (yyyy-MM-dd)
     * @return url string which can be used for queries
     */
    private String constructApiUrl(String place, Date startDate, Date endDate) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL);
        String url = new String("Url");
        return url;
    }

    /**
     *  Creates http client for the extractor
     */
    private AirQualityDataExtractor() {
        this.httpClient = new OkHttpClient();
    }

    private final OkHttpClient httpClient;
    private static AirQualityDataExtractor instance;

    private static final String API_BASE_URL = "https://opendata.fmi.fi/wfs?s" +
            "ervice=WFS&version=2.0.0&request=GetFeature&storedquery_id=fmi::" +
            "observations::airquality::hourly::timevaluepair";
    private static final String STARTTIME = "&starttime=";
    private static final String ENDTIME = "&endtime=";
    private static final String MAX_LOCATIONS = "&maxlocations=";
    private static final String CRS = "&crs=";
    private static final String TIME_STEP = "×tep=";

    // air quality parameters
    private static final String SO2 = "SO2_PT1H_avg";                   // sulphur dioxide
    private static final String NO = "NO_PT1H_avg";                     // nitrogen oxide
    private static final String N02 = "NO2_PT1H_avg";                   // nitrogen dioxide
    private static final String O3 = "O3_PT1H_avg";                     // ozone
    private static final String TRSC = "TRSC_PT1H_avg";                 // total reduced sulphur compounds
    private static final String CO = "CO_PT1H_avg";                     // carbon monoxide
    private static final String PM10 = "PM10_PT1H_avg";                 // thoracic particles
    private static final String PM2_5 = "PM25_PT1H_avg";                // fine particles PM2.5
    private static final String AIR_QUALITY_INDEX = "AQINDEX_PT1H_avg"; // air quality index





}
