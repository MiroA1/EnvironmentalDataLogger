package fi.tuni.environmentaldatalogger;

import fi.tuni.environmentaldatalogger.util.AirQualityParameter;
import javafx.util.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return Stream
                .of(AirQualityParameter.values())
                .map(AirQualityParameter::getAbbreviation).collect(Collectors.toCollection(ArrayList::new));

    }

    /**
     * Return valid time range for the data
     * @param param the parameter of interest
     * @return valid time range as Date pair
     */
    @Override
    public Pair<Date, Date> getValidDataRange(String param) {
        Date now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, 5);
        Date newestEntry = c.getTime();

        return new Pair<>(OLDEST_ENTRY, newestEntry);
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
        String latitude = "61.29,56.8";     // placeholder for now
        String longitude = "23.47,13.63";   // placeholder for now
        String url = constructApiUrl(latitude,longitude, range.getKey(),range.getValue());
        boolean validParam = false;
        for (AirQualityParameter ap : AirQualityParameter.values()){
            if (ap.getAbbreviation().equals(param)){
                validParam = true;
                url = url + "&" + PARAMETERS + ap.getQueryWord();
                break;
            }
        }
        if (!validParam) {
          //  throw new Exception("Invalid parameter!");
            return null;
        }
        return null; //fetchData(url, param);
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
     * Constructor for API query url.
     * @param latitude latitude for the query, for now a string element
     * @param longitude longitude for the query, for now a string element
     * @param startDate the start date in ISO 8601 format (yyyy-MM-dd)
     * @param endDate the end date in form ISO 8601 format (yyyy-MM-dd)
     * @return url string which can be used for queries
     */
    public String constructApiUrl(String latitude, String longitude, Date startDate, Date endDate) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL);
        apiUrl.append(LATITUDE).append(latitude);
        apiUrl.append("&" + LONGITUDE).append(longitude);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
            apiUrl.append("&" + START_DATE).append(dateFormat.format(startDate));
            apiUrl.append("&" + END_DATE).append(dateFormat.format(endDate));
        }

        apiUrl.append("&" + DOMAINS);
        System.out.println(apiUrl);
        return apiUrl.toString();

    }

    /**
     * Fetch data for the given parameter
     * @param apiUrl constructed url for the query
     * @param param the parameter which is queried
     * @return data as a Treemap (Date,Double)
     */
    private TreeMap<Date, Double> fetchData(String apiUrl, String param) {
        TreeMap<Date, Double> airQualityData = new TreeMap<>();
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                airQualityData = parseAirQualityData(responseBody, param);
            } else {
                System.err.println("Unexpected response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return airQualityData;
    }

    private TreeMap<Date, Double> parseAirQualityData(String json, String param) {
        return null;
    }


    /**
     *  Creates http client for the extractor
     */
    private AirQualityDataExtractor() {
        this.httpClient = new OkHttpClient();
    }

    private final OkHttpClient httpClient;
    private static AirQualityDataExtractor instance;

    private static final String API_BASE_URL = "https://air-quality-api.open-meteo.com/v1/air-quality?";
    private static final String LATITUDE = "latitude=";
    private static final String LONGITUDE = "longitude=";
    private static final String PARAMETERS = "hourly=";
    private static final String PAST = "past_days=";
    private static final String START_DATE = "start_date=";
    private static final String END_DATE = "end_date=";
    private static final String DOMAINS = "domains=cams_europe";
    private static final Date OLDEST_ENTRY = new Date(2022-1900,Calendar.JULY,29);

    /*  Ilmatieteen laitoksen API query elementit:
    ================================================

    private static final String API_BASE_URL = "https://opendata.fmi.fi/wfs?s" +
    "ervice=WFS&version=2.0.0&request=GetFeature&storedquery_id=fmi::" +
    "observations::airquality::hourly::timevaluepair";
    private static final String START = "&starttime=";
    private static final String ENDTIME = "&endtime=";
    private static final String PLACE = "&place=";
    private static final String MAX_LOCATIONS = "&maxlocations=";
    private static final String CRS = "&crs=";
    private static final String TIME_STEP = "×tep=";

    */

}
