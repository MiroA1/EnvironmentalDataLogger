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
import java.util.SortedMap;
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
     * @param params the parameter of interest
     * @return valid time range as Date pair
     */
    @Override
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(ArrayList<String> params) {
        // TODO: TimeZone changes? Current API provides timestamps in GMT+0 -> https://open-meteo.com/en/docs/air-quality-api
        // For now allow only 3-day forecast; if longer period is allowed, json parsing must be improved
        LocalDateTime upperLimit = LocalDateTime.now().plusDays(MAX_FORECAST_DAYS);
        return new Pair<>(OLDEST_ENTRY, upperLimit);
    }


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
     * Constructor for API query url.
     * @param latitude latitude for the query, for now a string element
     * @param longitude longitude for the query, for now a string element
     * @param startDate the start date in ISO 8601 format (yyyy-MM-dd)
     * @param endDate the end date in form ISO 8601 format (yyyy-MM-dd)
     * @return url string which can be used for queries
     */
    private String constructApiUrl(String latitude, String longitude, LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL);
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
                    // TODO: make sure program can continue operations even if date cannot be parsed
                    LocalDateTime date = LocalDateTime.parse(dateObject);
                    airQualityData.get(abbr).put(date, paramValue);
                }
            }
        } catch (JSONException e) {
            throw new ApiException("Error in parsing the data.", ApiException.ErrorCode.PARSE_ERROR);
        }
        return airQualityData;
    }

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

    private static final String API_BASE_URL = "https://air-quality-api.open-meteo.com/v1/air-quality?";
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

    // json string for practising. TODO: Remove from the final version
    private static final String json = "[{\"latitude\":61.249996,\"longitude\":23.45,\"generationtime_ms\":0.08606910705566406,\"utc_offset_seconds\":0,\"timezone\":\"GMT\",\"timezone_abbreviation\":\"GMT\",\"elevation\":121.0,\"hourly_units\":{\"time\":\"iso8601\",\"carbon_monoxide\":\"μg/m³\"},\"hourly\":{\"time\":[\"2023-10-10T00:00\",\"2023-10-10T01:00\",\"2023-10-10T02:00\",\"2023-10-10T03:00\",\"2023-10-10T04:00\",\"2023-10-10T05:00\",\"2023-10-10T06:00\",\"2023-10-10T07:00\",\"2023-10-10T08:00\",\"2023-10-10T09:00\",\"2023-10-10T10:00\",\"2023-10-10T11:00\",\"2023-10-10T12:00\",\"2023-10-10T13:00\",\"2023-10-10T14:00\",\"2023-10-10T15:00\",\"2023-10-10T16:00\",\"2023-10-10T17:00\",\"2023-10-10T18:00\",\"2023-10-10T19:00\",\"2023-10-10T20:00\",\"2023-10-10T21:00\",\"2023-10-10T22:00\",\"2023-10-10T23:00\",\"2023-10-11T00:00\",\"2023-10-11T01:00\",\"2023-10-11T02:00\",\"2023-10-11T03:00\",\"2023-10-11T04:00\",\"2023-10-11T05:00\",\"2023-10-11T06:00\",\"2023-10-11T07:00\",\"2023-10-11T08:00\",\"2023-10-11T09:00\",\"2023-10-11T10:00\",\"2023-10-11T11:00\",\"2023-10-11T12:00\",\"2023-10-11T13:00\",\"2023-10-11T14:00\",\"2023-10-11T15:00\",\"2023-10-11T16:00\",\"2023-10-11T17:00\",\"2023-10-11T18:00\",\"2023-10-11T19:00\",\"2023-10-11T20:00\",\"2023-10-11T21:00\",\"2023-10-11T22:00\",\"2023-10-11T23:00\",\"2023-10-12T00:00\",\"2023-10-12T01:00\",\"2023-10-12T02:00\",\"2023-10-12T03:00\",\"2023-10-12T04:00\",\"2023-10-12T05:00\",\"2023-10-12T06:00\",\"2023-10-12T07:00\",\"2023-10-12T08:00\",\"2023-10-12T09:00\",\"2023-10-12T10:00\",\"2023-10-12T11:00\",\"2023-10-12T12:00\",\"2023-10-12T13:00\",\"2023-10-12T14:00\",\"2023-10-12T15:00\",\"2023-10-12T16:00\",\"2023-10-12T17:00\",\"2023-10-12T18:00\",\"2023-10-12T19:00\",\"2023-10-12T20:00\",\"2023-10-12T21:00\",\"2023-10-12T22:00\",\"2023-10-12T23:00\",\"2023-10-13T00:00\",\"2023-10-13T01:00\",\"2023-10-13T02:00\",\"2023-10-13T03:00\",\"2023-10-13T04:00\",\"2023-10-13T05:00\",\"2023-10-13T06:00\",\"2023-10-13T07:00\",\"2023-10-13T08:00\",\"2023-10-13T09:00\",\"2023-10-13T10:00\",\"2023-10-13T11:00\",\"2023-10-13T12:00\",\"2023-10-13T13:00\",\"2023-10-13T14:00\",\"2023-10-13T15:00\",\"2023-10-13T16:00\",\"2023-10-13T17:00\",\"2023-10-13T18:00\",\"2023-10-13T19:00\",\"2023-10-13T20:00\",\"2023-10-13T21:00\",\"2023-10-13T22:00\",\"2023-10-13T23:00\",\"2023-10-14T00:00\",\"2023-10-14T01:00\",\"2023-10-14T02:00\",\"2023-10-14T03:00\",\"2023-10-14T04:00\",\"2023-10-14T05:00\",\"2023-10-14T06:00\",\"2023-10-14T07:00\",\"2023-10-14T08:00\",\"2023-10-14T09:00\",\"2023-10-14T10:00\",\"2023-10-14T11:00\",\"2023-10-14T12:00\",\"2023-10-14T13:00\",\"2023-10-14T14:00\",\"2023-10-14T15:00\",\"2023-10-14T16:00\",\"2023-10-14T17:00\",\"2023-10-14T18:00\",\"2023-10-14T19:00\",\"2023-10-14T20:00\",\"2023-10-14T21:00\",\"2023-10-14T22:00\",\"2023-10-14T23:00\"],\"carbon_monoxide\":[193.0,197.0,203.0,201.0,203.0,209.0,205.0,205.0,198.0,197.0,196.0,190.0,189.0,187.0,189.0,190.0,192.0,194.0,192.0,189.0,185.0,185.0,185.0,178.0,180.0,181.0,179.0,178.0,179.0,177.0,173.0,169.0,164.0,156.0,149.0,144.0,146.0,148.0,149.0,148.0,143.0,142.0,149.0,161.0,168.0,173.0,177.0,181.0,184.0,183.0,180.0,178.0,178.0,179.0,178.0,178.0,180.0,182.0,179.0,175.0,177.0,183.0,182.0,184.0,181.0,176.0,178.0,181.0,184.0,185.0,184.0,184.0,181.0,182.0,184.0,185.0,185.0,185.0,184.0,184.0,182.0,180.0,181.0,180.0,181.0,181.0,181.0,182.0,184.0,184.0,180.0,176.0,174.0,171.0,170.0,169.0,169.0,171.0,167.0,166.0,141.0,140.0,143.0,144.0,147.0,145.0,149.0,155.0,162.0,168.0,173.0,174.0,172.0,173.0,175.0,177.0,176.0,175.0,176.0,176.0]}},{\"latitude\":56.85,\"longitude\":13.650002,\"generationtime_ms\":0.04303455352783203,\"utc_offset_seconds\":0,\"timezone\":\"GMT\",\"timezone_abbreviation\":\"GMT\",\"elevation\":149.0,\"hourly_units\":{\"time\":\"iso8601\",\"carbon_monoxide\":\"μg/m³\"},\"hourly\":{\"time\":[\"2023-10-10T00:00\",\"2023-10-10T01:00\",\"2023-10-10T02:00\",\"2023-10-10T03:00\",\"2023-10-10T04:00\",\"2023-10-10T05:00\",\"2023-10-10T06:00\",\"2023-10-10T07:00\",\"2023-10-10T08:00\",\"2023-10-10T09:00\",\"2023-10-10T10:00\",\"2023-10-10T11:00\",\"2023-10-10T12:00\",\"2023-10-10T13:00\",\"2023-10-10T14:00\",\"2023-10-10T15:00\",\"2023-10-10T16:00\",\"2023-10-10T17:00\",\"2023-10-10T18:00\",\"2023-10-10T19:00\",\"2023-10-10T20:00\",\"2023-10-10T21:00\",\"2023-10-10T22:00\",\"2023-10-10T23:00\",\"2023-10-11T00:00\",\"2023-10-11T01:00\",\"2023-10-11T02:00\",\"2023-10-11T03:00\",\"2023-10-11T04:00\",\"2023-10-11T05:00\",\"2023-10-11T06:00\",\"2023-10-11T07:00\",\"2023-10-11T08:00\",\"2023-10-11T09:00\",\"2023-10-11T10:00\",\"2023-10-11T11:00\",\"2023-10-11T12:00\",\"2023-10-11T13:00\",\"2023-10-11T14:00\",\"2023-10-11T15:00\",\"2023-10-11T16:00\",\"2023-10-11T17:00\",\"2023-10-11T18:00\",\"2023-10-11T19:00\",\"2023-10-11T20:00\",\"2023-10-11T21:00\",\"2023-10-11T22:00\",\"2023-10-11T23:00\",\"2023-10-12T00:00\",\"2023-10-12T01:00\",\"2023-10-12T02:00\",\"2023-10-12T03:00\",\"2023-10-12T04:00\",\"2023-10-12T05:00\",\"2023-10-12T06:00\",\"2023-10-12T07:00\",\"2023-10-12T08:00\",\"2023-10-12T09:00\",\"2023-10-12T10:00\",\"2023-10-12T11:00\",\"2023-10-12T12:00\",\"2023-10-12T13:00\",\"2023-10-12T14:00\",\"2023-10-12T15:00\",\"2023-10-12T16:00\",\"2023-10-12T17:00\",\"2023-10-12T18:00\",\"2023-10-12T19:00\",\"2023-10-12T20:00\",\"2023-10-12T21:00\",\"2023-10-12T22:00\",\"2023-10-12T23:00\",\"2023-10-13T00:00\",\"2023-10-13T01:00\",\"2023-10-13T02:00\",\"2023-10-13T03:00\",\"2023-10-13T04:00\",\"2023-10-13T05:00\",\"2023-10-13T06:00\",\"2023-10-13T07:00\",\"2023-10-13T08:00\",\"2023-10-13T09:00\",\"2023-10-13T10:00\",\"2023-10-13T11:00\",\"2023-10-13T12:00\",\"2023-10-13T13:00\",\"2023-10-13T14:00\",\"2023-10-13T15:00\",\"2023-10-13T16:00\",\"2023-10-13T17:00\",\"2023-10-13T18:00\",\"2023-10-13T19:00\",\"2023-10-13T20:00\",\"2023-10-13T21:00\",\"2023-10-13T22:00\",\"2023-10-13T23:00\",\"2023-10-14T00:00\",\"2023-10-14T01:00\",\"2023-10-14T02:00\",\"2023-10-14T03:00\",\"2023-10-14T04:00\",\"2023-10-14T05:00\",\"2023-10-14T06:00\",\"2023-10-14T07:00\",\"2023-10-14T08:00\",\"2023-10-14T09:00\",\"2023-10-14T10:00\",\"2023-10-14T11:00\",\"2023-10-14T12:00\",\"2023-10-14T13:00\",\"2023-10-14T14:00\",\"2023-10-14T15:00\",\"2023-10-14T16:00\",\"2023-10-14T17:00\",\"2023-10-14T18:00\",\"2023-10-14T19:00\",\"2023-10-14T20:00\",\"2023-10-14T21:00\",\"2023-10-14T22:00\",\"2023-10-14T23:00\"],\"carbon_monoxide\":[181.0,182.0,184.0,185.0,192.0,201.0,205.0,201.0,190.0,180.0,178.0,177.0,180.0,183.0,186.0,182.0,186.0,184.0,181.0,186.0,176.0,165.0,165.0,163.0,165.0,173.0,184.0,198.0,194.0,185.0,175.0,165.0,153.0,143.0,141.0,137.0,138.0,140.0,149.0,157.0,163.0,166.0,170.0,175.0,178.0,179.0,182.0,182.0,181.0,180.0,178.0,176.0,174.0,174.0,173.0,172.0,172.0,173.0,174.0,174.0,173.0,171.0,171.0,171.0,171.0,171.0,171.0,170.0,169.0,170.0,171.0,172.0,175.0,176.0,178.0,178.0,178.0,178.0,179.0,182.0,181.0,178.0,174.0,174.0,176.0,178.0,183.0,167.0,148.0,140.0,128.0,125.0,122.0,121.0,124.0,122.0,133.0,152.0,165.0,170.0,170.0,173.0,175.0,175.0,175.0,175.0,175.0,175.0,174.0,173.0,171.0,171.0,173.0,172.0,175.0,177.0,175.0,174.0,173.0,177.0]}}]";

    /*  Ilmatieteen laitoksen API query elementit:
    ================================================

    private static final String API_BASE_URL = "https://opendata.fmi.fi/wfs?s" +
    "service=WFS&version=2.0.0&request=GetFeature&storedquery_id=fmi::" +
    "observations::airquality::hourly::timevaluepair";
    private static final String START = "&starttime=";
    private static final String ENDTIME = "&endtime=";
    private static final String PLACE = "&place=";
    private static final String MAX_LOCATIONS = "&maxlocations=";
    private static final String CRS = "&crs=";
    private static final String TIME_STEP = "×tep=";

    */

}
