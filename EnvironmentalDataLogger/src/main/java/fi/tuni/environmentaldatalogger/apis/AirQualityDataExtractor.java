package fi.tuni.environmentaldatalogger.apis;

import fi.tuni.environmentaldatalogger.save.SaveLoad;
import fi.tuni.environmentaldatalogger.util.AirQualityParameter;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import javafx.util.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public static synchronized AirQualityDataExtractor getInstance() {
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
     * @param params the parameters of interest
     * @return valid time range as Date pair
     */
    @Override
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(ArrayList<String> params) {
        LocalDateTime upperLimit = LocalDateTime.now().plusDays(MAX_FORECAST_DAYS);
        return new Pair<>(OLDEST_ENTRY, upperLimit);
    }


    /**
     * Return data from the API
     * @param params the parameters of interest
     * @param range the time range of interest
     * @param coordinates the coordinates of interest
     * @return requested data
     */
    @Override
    public TreeMap<String, TreeMap<LocalDateTime, Double>> getData(ArrayList<String> params, Pair<LocalDateTime,
            LocalDateTime> range, Coordinate coordinates) throws ApiException {

        TreeMap<String, TreeMap<LocalDateTime, Double>> data = new TreeMap<>();

        String latitude = String.valueOf(coordinates.latitude());
        String longitude = String.valueOf(coordinates.longitude());

        StringBuilder url = new StringBuilder(constructApiUrl(latitude, longitude, range.getKey(), range.getValue()));

        ArrayList<String> queryWords = new ArrayList<>();

        url.append("&").append(PARAMETERS);

        boolean isFirst = true;
        Duration margin = Duration.ofHours(1);

        for (String param : params) {
            for (AirQualityParameter ap : AirQualityParameter.values()){
                if (ap.getAbbreviation().equals(param)){

                    var cachedData = this.cache.get(coordinates, param, range, margin, margin);

                    if (cachedData != null) {
                        data.put(param, cachedData);
                        break;
                    }

                    queryWords.add(ap.getQueryWord());
                    if (!isFirst) {
                        url.append(",");
                    } else {
                        isFirst = false;
                    }
                    url.append(ap.getQueryWord());
                    break;
                }
            }
        }

        if (!queryWords.isEmpty()) {
            var apiData = fetchData(url.toString(), queryWords);
            cache.insert(coordinates, apiData);
            SaveLoad.save(this.cache, "air_quality_cache.json");
            data.putAll(apiData);
        }

        return data;
    }

    /**
     * Return current data from the API
     * @param params list of parameters
     * @param coordinates coordinates of the location to fetch data for
     * @return current data
     * @throws ApiException if the API call fails
     */
    @Override
    public TreeMap<String, Double> getCurrentData(ArrayList<String> params, Coordinate coordinates) throws ApiException {

        String latitude = String.valueOf(coordinates.latitude());
        String longitude = String.valueOf(coordinates.longitude());

        StringBuilder url = new StringBuilder(constructApiUrl(latitude, longitude, null, null));

        ArrayList<String> queryWords = new ArrayList<>();

        url.append("&").append(CURRENT);

        boolean isFirst = true;

        for (String param : params) {
            for (AirQualityParameter ap : AirQualityParameter.values()){
                if (ap.getAbbreviation().equals(param)){
                    queryWords.add(ap.getQueryWord());
                    if (!isFirst) {
                        url.append(",");
                    } else {
                        isFirst = false;
                    }
                    url.append(ap.getQueryWord());
                    break;
                }
            }
        }

        System.out.println(url);

        return fetchCurrentData(url.toString(), queryWords);
    }

    /**
     * Get the unit for the given parameter
     * @param param the parameter of interest
     * @return the unit of the parameter
     */
    @Override
    public String getUnit(String param) {
        for (AirQualityParameter ap : AirQualityParameter.values()){
            if (ap.getAbbreviation().equals(param)){
                return ap.getUnit();
            }
        }
        return "";
    }

    /**
     * Get the name of the API
     * @return the name of the API
     */
    @Override
    public String getApiName() {
        return API_NAME;
    }

    /**
     * Get the url of the API
     * @return the url of the API
     */
    @Override
    public String getApiUrl() {
        return API_URL;
    }

    /**
     * Constructor for API query url.
     * @param latitude latitude for the query, for now a string element
     * @param longitude longitude for the query, for now a string element
     * @param startDate the start date in ISO 8601 format (yyyy-MM-dd)
     * @param endDate the end date in form ISO 8601 format (yyyy-MM-dd)
     * @return url string which can be used for queries
     */
    private String constructApiUrl(String latitude, String longitude, LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder apiUrl = new StringBuilder(API_QUERY_BASE_URL);
        apiUrl.append(LATITUDE).append(latitude);
        apiUrl.append("&" + LONGITUDE).append(longitude);

        if (startDate == null || endDate == null) {
            return apiUrl.toString();
        }

        if (startDate.isBefore(OLDEST_ENTRY)) {
            System.err.println("Start date cannot be before " + OLDEST_ENTRY);
            startDate = OLDEST_ENTRY;
        }

        if (endDate.isAfter(LocalDateTime.now().plusDays(MAX_FORECAST_DAYS))) {
            System.err.println("End date cannot be after " + endDate);
            endDate = LocalDateTime.now().plusDays(MAX_FORECAST_DAYS);
        }

        apiUrl.append("&" + START_DATE).append(startDate.toLocalDate().toString());
        apiUrl.append("&" + END_DATE).append(endDate.toLocalDate().toString());

        return apiUrl.toString();
    }

    /**
     * Fetch data for the given parameter
     * @param apiUrl constructed url for the query
     * @param params the parameter which is queried
     * @return data as a Treemap (Date,Double)
     */
    private TreeMap<String, TreeMap<LocalDateTime, Double>> fetchData(String apiUrl, ArrayList<String> params)
            throws ApiException {
        TreeMap<String, TreeMap<LocalDateTime, Double>> airQualityData;
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                airQualityData = parseAirQualityData(responseBody, params);
            } else {
                throw new ApiException("Received unexpected response code " + response.code() +
                        " from API server. Unable to fetch data.",
                          ApiException.ErrorCode.INVALID_RESPONSE);
            }
        } catch (IOException e) {
            throw new ApiException("Failed to connect to API server. Check internet connection.",
                      ApiException.ErrorCode.CONNECTION_ERROR);
        }
        return airQualityData;
    }

    /**
     * Fetch current data for the given parameter
     * @param apiUrl constructed url for the query
     * @param params the parameter which is queried
     * @return data as a Treemap (Date,Double)
     */
    private TreeMap<String, Double> fetchCurrentData(String apiUrl, ArrayList<String> params) throws ApiException {
        TreeMap<String, Double> airQualityData;
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                airQualityData = parseCurrentAirQualityData(responseBody, params);
            } else {
                throw new ApiException("Received unexpected response code " + response.code() +
                        " from air quality server. Unable to fetch data.",
                        ApiException.ErrorCode.INVALID_RESPONSE);
            }
        } catch (IOException e) {
            throw new ApiException("Failed to connect to air quality server. Check the internet connection.",
                    ApiException.ErrorCode.CONNECTION_ERROR);
        }

        return airQualityData;
    }

    /**
     * Parse the data from the API
     * @param json the json string to parse
     * @param params the parameters which are queried
     * @return data
     */
    private TreeMap<String, TreeMap<LocalDateTime, Double>> parseAirQualityData(String json, ArrayList<String> params)
            throws ApiException {

        TreeMap<String, TreeMap<LocalDateTime, Double>> airQualityData = new TreeMap<>();

        try {
            JSONArray dateArray = new JSONObject(json).getJSONObject("hourly").getJSONArray("time");

            for ( String param : params) {
                JSONArray dataArray = new JSONObject(json).getJSONObject("hourly").getJSONArray(param);

                String abbr = AirQualityParameter.fromQueryWord(param).getAbbreviation();
                airQualityData.put(abbr, new TreeMap<>());

                for ( int i = 0; i < dateArray.length(); i++) {
                    String dateObject = dateArray.getString(i);
                    double paramValue = dataArray.getDouble(i);
                    LocalDateTime date = LocalDateTime.parse(dateObject);
                    airQualityData.get(abbr).put(date, paramValue);
                }
            }
        } catch (JSONException e) {
            throw new ApiException("Error in parsing the data.", ApiException.ErrorCode.PARSE_ERROR);
        }
        return airQualityData;
    }

    /**
     * Parse the current data from the API
     * @param json the json string to parse
     * @param params the parameters which are queried
     * @return data
     */
    private TreeMap<String, Double> parseCurrentAirQualityData(String json, ArrayList<String> params)
            throws ApiException {

        TreeMap<String, Double> airQualityData = new TreeMap<>();

        try {
            JSONObject dataObject = new JSONObject(json).getJSONObject("current");

            for ( String param : params) {
                double paramValue = dataObject.getDouble(param);
                airQualityData.put(AirQualityParameter.fromQueryWord(param).getAbbreviation(), paramValue);
            }
        } catch (JSONException e) {
            throw new ApiException("Error in parsing the data.", ApiException.ErrorCode.PARSE_ERROR);
        }
        return airQualityData;
    }

    /**
     *  Creates http client for the extractor
     */
    private AirQualityDataExtractor() {
        this.httpClient = new OkHttpClient();
        this.cache = new ApiCache();

        SaveLoad.load(this.cache, "air_quality_cache.json");
    }

    private final OkHttpClient httpClient;
    private final ApiCache cache;
    private static AirQualityDataExtractor instance;

    private static final String API_NAME = "Open-Meteo";
    private static final String API_URL = "https://open-meteo.com/";
    private static final String API_QUERY_BASE_URL = "https://air-quality-api.open-meteo.com/v1/air-quality?";
    private static final String LATITUDE = "latitude=";
    private static final String LONGITUDE = "longitude=";
    private static final String PARAMETERS = "hourly=";
    private static final String PAST = "past_days=";
    private static final String START_DATE = "start_date=";
    private static final String END_DATE = "end_date=";
    private static final String CURRENT = "current=";
    private static final String DOMAINS = "domains=cams_europe";
    private static final int MAX_FORECAST_DAYS = 5;
    private static final LocalDateTime OLDEST_ENTRY = LocalDateTime.of(2022, 7, 29, 0, 0);

}
