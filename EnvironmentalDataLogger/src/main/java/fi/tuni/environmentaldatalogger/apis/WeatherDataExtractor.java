package fi.tuni.environmentaldatalogger.apis;

import fi.tuni.environmentaldatalogger.save.SaveLoad;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import javafx.util.Pair;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.time.format.DateTimeFormatter;

/**
 * WeatherDataExtractor class implements DataExtractor interface to fetch weather data from an API.
 * It handles fetching both current and historical weather data, cache management, and data formatting.
 */
public class WeatherDataExtractor implements DataExtractor {
    private static final String API_BASE_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
    private static final String API_KEY = "NAJZYGFDHA4GEA5QXD7S4TZSL";

    private OkHttpClient httpClient;
    private static WeatherDataExtractor instance;

    private final ApiCache cache;

    /**
     * Private constructor for WeatherDataExtractor.
     * Initializes the HTTP client and loads the cache from a saved state if available.
     */
    private WeatherDataExtractor() {
        this.httpClient = new OkHttpClient();
        this.cache = new ApiCache();

        SaveLoad.load(this.cache, "weather_cache.json");
    }

    /**
     * Gets the singleton instance of WeatherDataExtractor.
     * If the instance does not exist, it is created.
     *
     * @return The single instance of WeatherDataExtractor.
     */
    public static synchronized WeatherDataExtractor getInstance() {
        if (instance == null) {
            instance = new WeatherDataExtractor();
        }
        return instance;
    }

    @Override
    public ArrayList<String> getValidParameters() {
        ArrayList<String> validParameters = new ArrayList<>();
        validParameters.add("temperature");
        validParameters.add("humidity");
        validParameters.add("feelslike");
        validParameters.add("windspeed");
        return validParameters;
    }

    @Override
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(ArrayList<String> params) {
        return new Pair<>(LocalDateTime.now().minusDays(30), LocalDateTime.now().plusDays(15));
    }

    @Override
    public TreeMap<String, TreeMap<LocalDateTime, Double>> getData(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) throws ApiException {

        TreeMap<String, TreeMap<LocalDateTime, Double>> resultMap = new TreeMap<>();

        ArrayList<String> remainingParams = new ArrayList<>();

        // TODO: there's probably a better way of doing this
        Duration margin = Duration.ofDays(1);
        if (!range.getValue().isAfter(range.getKey().plusDays(1))) {
            margin = Duration.ofHours(1);
        }

        for (String param : params) {
            var cachedData = this.cache.get(coordinates, param, range, margin, margin);

            if (cachedData != null) {
                resultMap.put(param, cachedData);
            } else {
                remainingParams.add(param);
            }
        }

        if (remainingParams.isEmpty()) {
            return resultMap;
        }

        String apiUrl = constructApiUrl(coordinates, range.getKey(), range.getValue(), remainingParams, false);
        TreeMap<LocalDateTime, TreeMap<String, Double>> fetchedData = fetchDataMultipleParams(apiUrl, remainingParams);

        TreeMap<String, TreeMap<LocalDateTime, Double>> cacheInsertData = new TreeMap<>();

        for (String param : remainingParams) {
            TreeMap<LocalDateTime, Double> paramData = new TreeMap<>();
            for (Map.Entry<LocalDateTime, TreeMap<String, Double>> entry : fetchedData.entrySet()) {
                paramData.put(entry.getKey(), entry.getValue().get(param));
            }
            resultMap.put(param, paramData);
            cacheInsertData.put(param, paramData);
        }

        this.cache.insert(coordinates, cacheInsertData);
        SaveLoad.save(this.cache, "weather_cache.json");

        return resultMap;
    }

    @Override
    public TreeMap<String, Double> getCurrentData(ArrayList<String> params, Coordinate coordinates) throws ApiException {
        String apiUrl = constructApiUrl(coordinates, LocalDateTime.now(), LocalDateTime.now(), params, true);
        TreeMap<String, Double> currentData = fetchCurrentData(apiUrl, params);
        return currentData;
    }

    @Override
    public String getUnit(String param) {
        switch (param) {
            case "temperature":
                return "°C";
            case "humidity":
                return "%";
            case "feelslike":
                return "°C";
            case "windspeed":
                return "m/s";
            default:
                return "";
        }
    }

    /**
     * Constructs the API URL for the weather data request.
     *
     * @param coordinates The geographical coordinates for the weather data.
     * @param startDate The start date for the data range.
     * @param endDate The end date for the data range.
     * @param params The parameters for which data is requested.
     * @param getCurrent Flag to determine if current data is being fetched.
     * @return Constructed API URL as a String.
     */
    private String constructApiUrl(Coordinate coordinates, LocalDateTime startDate, LocalDateTime endDate, ArrayList<String> params, boolean getCurrent) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL);
        apiUrl.append(coordinates.latitude()).append(",").append(coordinates.longitude());

        ArrayList<String> urlParams = new ArrayList<>(params);
        if(urlParams.contains("temperature")) {
            urlParams.set(urlParams.indexOf("temperature"), "temp");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant());

        apiUrl.append("/").append(dateFormat.format(start)).append("/").append(dateFormat.format(end));
        apiUrl.append("?key=").append(API_KEY);
        if (getCurrent) {
            apiUrl.append("&include=current");
        } else {
            if (startDate != null && endDate != null && !endDate.isAfter(startDate.plusDays(1))) {
                apiUrl.append("&include=hours");
            } else {
                apiUrl.append("&include=days");
            }
        }

        String elements = String.join(",", urlParams);
        apiUrl.append("&elements=datetime,").append(elements);
        apiUrl.append("&unitGroup=metric");

        return apiUrl.toString();
    }

    /**
     * Fetches weather data for multiple parameters from the API.
     *
     * @param apiUrl The URL to fetch the weather data from.
     * @param params A list of parameters to fetch data for.
     * @return A TreeMap mapping LocalDateTime to another TreeMap mapping parameter names to their values.
     * @throws ApiException If there is an error in the API call.
     */
    private TreeMap<LocalDateTime, TreeMap<String, Double>> fetchDataMultipleParams(String apiUrl,
                                                                                    ArrayList<String> params)
                                                                                    throws ApiException {
        TreeMap<LocalDateTime, TreeMap<String, Double>> weatherData = new TreeMap<>();
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                weatherData = parseWeatherDataMultipleParams(responseBody, params);
            } else {
                throw new ApiException("Received unexpected response code " + response.code() +
                        " from weather server. Unable to fetch data.",
                        ApiException.ErrorCode.INVALID_RESPONSE);
            }
        } catch (IOException e) {
            throw new ApiException("Failed to connect to weather server. Check the internet connection.",
                    ApiException.ErrorCode.CONNECTION_ERROR);
        }
        return weatherData;
    }

    /**
     * Parses weather data for multiple parameters from a JSON response.
     *
     * @param json The JSON string containing weather data.
     * @param params The parameters included in the JSON data.
     * @return A TreeMap mapping LocalDateTime to another TreeMap mapping parameter names to their values.
     * @throws ApiException If there is an error in parsing the JSON data.
     */
    private TreeMap<LocalDateTime, TreeMap<String, Double>> parseWeatherDataMultipleParams(String json, ArrayList<String> params) throws ApiException {
        TreeMap<LocalDateTime, TreeMap<String, Double>> weatherData = new TreeMap<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray daysArray = jsonObject.getJSONArray("days");

            for (int i = 0; i < daysArray.length(); i++) {
                JSONObject dayObject = daysArray.getJSONObject(i);
                String dateStr = dayObject.getString("datetime");

                LocalDateTime date = LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toLocalDateTime();

                if (dayObject.has("hours")) {
                    JSONArray hoursArray = dayObject.getJSONArray("hours");
                    for (int j = 0; j < hoursArray.length(); j++) {
                        JSONObject hourObject = hoursArray.getJSONObject(j);
                        String time = hourObject.getString("datetime");
                        LocalDateTime dateTime = LocalDateTime.parse(dateStr + "T" + time, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                        TreeMap<String, Double> hourlyData = extractParams(hourObject, params);
                        weatherData.put(dateTime, hourlyData);
                    }
                } else {
                    TreeMap<String, Double> dailyData = extractParams(dayObject, params);
                    weatherData.put(date, dailyData);
                }
            }

        } catch (Exception e) {
            throw new ApiException("Error in parsing the weather data.", ApiException.ErrorCode.PARSE_ERROR);
        }
        return weatherData;
    }

    /**
     * Extracts parameter values from a JSONObject.
     *
     * @param dataObject The JSONObject containing weather data.
     * @param params The list of parameters to extract.
     * @return A TreeMap mapping parameter names to their values.
     */
    private TreeMap<String, Double> extractParams(JSONObject dataObject, ArrayList<String> params) {
        TreeMap<String, Double> data = new TreeMap<>();
        for (String param : params) {
            if (param.equals("temperature") && dataObject.has("temp")) {
                data.put("temperature", dataObject.getDouble("temp"));
            } else if (dataObject.has(param)) {
                data.put(param, dataObject.getDouble(param));
            }
        }
        return data;
    }

    /**
     * Fetches current weather data from the API.
     *
     * @param apiUrl The URL to fetch the current weather data from.
     * @param params A list of parameters to fetch data for.
     * @return A TreeMap mapping parameter names to their current values.
     * @throws ApiException If there is an error in the API call.
     */
    private TreeMap<String, Double> fetchCurrentData(String apiUrl, ArrayList<String> params) throws ApiException {
        TreeMap<String, Double> currentData = new TreeMap<>();
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                currentData = parseCurrentWeatherData(responseBody, params);
            } else {
                throw new ApiException("Received unexpected response code " + response.code() +
                        " from weather server. Unable to fetch data.",
                        ApiException.ErrorCode.INVALID_RESPONSE);
            }
        } catch (IOException e) {
            throw new ApiException("Failed to connect to weather server. Check the internet connection.",
                                    ApiException.ErrorCode.CONNECTION_ERROR);
        }
        return currentData;
    }

    /**
     * Parses a date from a string.
     *
     * @param datetime The date string to parse.
     * @return The parsed LocalDateTime object.
     * @throws ApiException If there is an error in parsing the date.
     */
    private LocalDateTime parseDate(String datetime) throws ApiException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(datetime);
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (ParseException e) {
            throw new ApiException("Error in parsing the data.", ApiException.ErrorCode.PARSE_ERROR);
        }
    }

    /**
     * Parses current weather data from a JSON string.
     *
     * @param json The JSON string containing current weather data.
     * @param params The parameters included in the JSON data.
     * @return A TreeMap mapping parameter names to their current values.
     * @throws ApiException If there is an error in parsing the JSON data.
     */
    private TreeMap<String, Double> parseCurrentWeatherData(String json, ArrayList<String> params) throws ApiException {
        TreeMap<String, Double> currentData = new TreeMap<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject currentConditions = jsonObject.getJSONObject("currentConditions");

            for (String param : params) {
                if (param.equals("temperature") && currentConditions.has("temp")) {
                    currentData.put("temperature", currentConditions.getDouble("temp"));
                } else if (currentConditions.has(param)) {
                    currentData.put(param, currentConditions.getDouble(param));
                }
            }

        } catch (Exception e) {
            throw new ApiException("Error in parsing the current weather data.", ApiException.ErrorCode.PARSE_ERROR);
        }
        return currentData;
    }
}









