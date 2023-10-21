package fi.tuni.environmentaldatalogger;

import fi.tuni.environmentaldatalogger.util.AirQualityParameter;
import javafx.util.Pair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
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
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(String param) {
        // TODO: TimeZone changes? Current API provides timestamps in GMT+0 -> https://open-meteo.com/en/docs/air-quality-api
        // For now allow only 3-day forecast; if longer period is allowed, json parsing must be improved
        LocalDateTime upperLimit = LocalDateTime.now().plusDays(5);
        return new Pair<>(OLDEST_ENTRY, upperLimit);
    }

    /**
     * Return the data in map form
     * @param param the parameter of interest
     * @param range the time range for the data
     * @param coordinates the coordinates of the place
     * @return map of data, <Date, Double>
     */
    @Override
    public TreeMap<LocalDateTime, Double> getData(String param, Pair<LocalDateTime, LocalDateTime> range,
                                         Coordinate coordinates) {
        String latitude = "61.29,56.8";     // placeholder for now. TODO: Replace with coordinates, once class is implemented
        String longitude = "23.47,13.63";   // placeholder for now
        String url = constructApiUrl(latitude,longitude, range.getKey(),range.getValue());
        System.out.print(url);
        boolean validParam = false;
        String queryWord = "";
        for (AirQualityParameter ap : AirQualityParameter.values()){
            if (ap.getAbbreviation().equals(param)){
                validParam = true;
                queryWord = ap.getQueryWord();
                url = url + "&" + PARAMETERS + ap.getQueryWord();
                break;
            }
        }
        if (!validParam) {
          //  throw new Exception("Invalid parameter!");
            return null;
        }
        return fetchData(url, queryWord);
    }

    /**
     * Return all available data within the given parameter
     * @param param parameter which is queried
     * @param coordinates coordinates of the place
     * @return data in map form <Date, Double>
     */
    @Override
    public TreeMap<LocalDateTime, Double> getData(String param, Coordinate coordinates) {
        return null;
    }

    @Override
    public TreeMap<LocalDateTime, Double> getData(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range,
                                                  Coordinate coordinates) {
        return null;
    }

    @Override
    public TreeMap<String, Double> getCurrentData(ArrayList<String> params, Coordinate coordinates) {
        return null;
    }

    @Override
    public String getUnit(String param) {
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
    public String constructApiUrl(String latitude, String longitude, LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL);
        apiUrl.append(LATITUDE).append(latitude);
        apiUrl.append("&" + LONGITUDE).append(longitude);

        if (startDate != null && endDate != null) {
            apiUrl.append("&" + START_DATE).append(startDate.toLocalDate().toString());
            apiUrl.append("&" + END_DATE).append(endDate.toLocalDate().toString());
        }
        return apiUrl.toString();
    }

    /**
     * Fetch data for the given parameter
     * @param apiUrl constructed url for the query
     * @param param the parameter which is queried
     * @return data as a Treemap (Date,Double)
     */
    private TreeMap<LocalDateTime, Double> fetchData(String apiUrl, String param) {
        TreeMap<LocalDateTime, Double> airQualityData = new TreeMap<>();
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                airQualityData = parseAirQualityData(responseBody, param);
            } else {
                System.err.println("Unexpected response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return airQualityData;
    }

    public TreeMap<LocalDateTime, Double> parseAirQualityData(String json, String param) {

        TreeMap<LocalDateTime, Double> airQualityData = new TreeMap<>();

        try {
            JSONArray dateArray = new JSONArray(json).getJSONObject(0).getJSONObject("hourly")
                                                     .getJSONArray("time");
            JSONArray dataArray = new JSONArray(json).getJSONObject(0).getJSONObject("hourly")
                                                     .getJSONArray(param);

            for ( int i = 0; i < dateArray.length(); i++) {
                String dateObject = dateArray.getString(i);
                double paramValue = dataArray.getDouble(i);
                LocalDateTime date = LocalDateTime.parse(dateObject);
                if (date != null) {
                    airQualityData.put(date, paramValue);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return airQualityData;
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
    private static final LocalDateTime OLDEST_ENTRY = LocalDateTime.of(2022, 7, 29, 0, 0);

    // json string for practising. TODO: Remove from the final version
    private static final String json = "[{\"latitude\":61.249996,\"longitude\":23.45,\"generationtime_ms\":0.08606910705566406,\"utc_offset_seconds\":0,\"timezone\":\"GMT\",\"timezone_abbreviation\":\"GMT\",\"elevation\":121.0,\"hourly_units\":{\"time\":\"iso8601\",\"carbon_monoxide\":\"μg/m³\"},\"hourly\":{\"time\":[\"2023-10-10T00:00\",\"2023-10-10T01:00\",\"2023-10-10T02:00\",\"2023-10-10T03:00\",\"2023-10-10T04:00\",\"2023-10-10T05:00\",\"2023-10-10T06:00\",\"2023-10-10T07:00\",\"2023-10-10T08:00\",\"2023-10-10T09:00\",\"2023-10-10T10:00\",\"2023-10-10T11:00\",\"2023-10-10T12:00\",\"2023-10-10T13:00\",\"2023-10-10T14:00\",\"2023-10-10T15:00\",\"2023-10-10T16:00\",\"2023-10-10T17:00\",\"2023-10-10T18:00\",\"2023-10-10T19:00\",\"2023-10-10T20:00\",\"2023-10-10T21:00\",\"2023-10-10T22:00\",\"2023-10-10T23:00\",\"2023-10-11T00:00\",\"2023-10-11T01:00\",\"2023-10-11T02:00\",\"2023-10-11T03:00\",\"2023-10-11T04:00\",\"2023-10-11T05:00\",\"2023-10-11T06:00\",\"2023-10-11T07:00\",\"2023-10-11T08:00\",\"2023-10-11T09:00\",\"2023-10-11T10:00\",\"2023-10-11T11:00\",\"2023-10-11T12:00\",\"2023-10-11T13:00\",\"2023-10-11T14:00\",\"2023-10-11T15:00\",\"2023-10-11T16:00\",\"2023-10-11T17:00\",\"2023-10-11T18:00\",\"2023-10-11T19:00\",\"2023-10-11T20:00\",\"2023-10-11T21:00\",\"2023-10-11T22:00\",\"2023-10-11T23:00\",\"2023-10-12T00:00\",\"2023-10-12T01:00\",\"2023-10-12T02:00\",\"2023-10-12T03:00\",\"2023-10-12T04:00\",\"2023-10-12T05:00\",\"2023-10-12T06:00\",\"2023-10-12T07:00\",\"2023-10-12T08:00\",\"2023-10-12T09:00\",\"2023-10-12T10:00\",\"2023-10-12T11:00\",\"2023-10-12T12:00\",\"2023-10-12T13:00\",\"2023-10-12T14:00\",\"2023-10-12T15:00\",\"2023-10-12T16:00\",\"2023-10-12T17:00\",\"2023-10-12T18:00\",\"2023-10-12T19:00\",\"2023-10-12T20:00\",\"2023-10-12T21:00\",\"2023-10-12T22:00\",\"2023-10-12T23:00\",\"2023-10-13T00:00\",\"2023-10-13T01:00\",\"2023-10-13T02:00\",\"2023-10-13T03:00\",\"2023-10-13T04:00\",\"2023-10-13T05:00\",\"2023-10-13T06:00\",\"2023-10-13T07:00\",\"2023-10-13T08:00\",\"2023-10-13T09:00\",\"2023-10-13T10:00\",\"2023-10-13T11:00\",\"2023-10-13T12:00\",\"2023-10-13T13:00\",\"2023-10-13T14:00\",\"2023-10-13T15:00\",\"2023-10-13T16:00\",\"2023-10-13T17:00\",\"2023-10-13T18:00\",\"2023-10-13T19:00\",\"2023-10-13T20:00\",\"2023-10-13T21:00\",\"2023-10-13T22:00\",\"2023-10-13T23:00\",\"2023-10-14T00:00\",\"2023-10-14T01:00\",\"2023-10-14T02:00\",\"2023-10-14T03:00\",\"2023-10-14T04:00\",\"2023-10-14T05:00\",\"2023-10-14T06:00\",\"2023-10-14T07:00\",\"2023-10-14T08:00\",\"2023-10-14T09:00\",\"2023-10-14T10:00\",\"2023-10-14T11:00\",\"2023-10-14T12:00\",\"2023-10-14T13:00\",\"2023-10-14T14:00\",\"2023-10-14T15:00\",\"2023-10-14T16:00\",\"2023-10-14T17:00\",\"2023-10-14T18:00\",\"2023-10-14T19:00\",\"2023-10-14T20:00\",\"2023-10-14T21:00\",\"2023-10-14T22:00\",\"2023-10-14T23:00\"],\"carbon_monoxide\":[193.0,197.0,203.0,201.0,203.0,209.0,205.0,205.0,198.0,197.0,196.0,190.0,189.0,187.0,189.0,190.0,192.0,194.0,192.0,189.0,185.0,185.0,185.0,178.0,180.0,181.0,179.0,178.0,179.0,177.0,173.0,169.0,164.0,156.0,149.0,144.0,146.0,148.0,149.0,148.0,143.0,142.0,149.0,161.0,168.0,173.0,177.0,181.0,184.0,183.0,180.0,178.0,178.0,179.0,178.0,178.0,180.0,182.0,179.0,175.0,177.0,183.0,182.0,184.0,181.0,176.0,178.0,181.0,184.0,185.0,184.0,184.0,181.0,182.0,184.0,185.0,185.0,185.0,184.0,184.0,182.0,180.0,181.0,180.0,181.0,181.0,181.0,182.0,184.0,184.0,180.0,176.0,174.0,171.0,170.0,169.0,169.0,171.0,167.0,166.0,141.0,140.0,143.0,144.0,147.0,145.0,149.0,155.0,162.0,168.0,173.0,174.0,172.0,173.0,175.0,177.0,176.0,175.0,176.0,176.0]}},{\"latitude\":56.85,\"longitude\":13.650002,\"generationtime_ms\":0.04303455352783203,\"utc_offset_seconds\":0,\"timezone\":\"GMT\",\"timezone_abbreviation\":\"GMT\",\"elevation\":149.0,\"hourly_units\":{\"time\":\"iso8601\",\"carbon_monoxide\":\"μg/m³\"},\"hourly\":{\"time\":[\"2023-10-10T00:00\",\"2023-10-10T01:00\",\"2023-10-10T02:00\",\"2023-10-10T03:00\",\"2023-10-10T04:00\",\"2023-10-10T05:00\",\"2023-10-10T06:00\",\"2023-10-10T07:00\",\"2023-10-10T08:00\",\"2023-10-10T09:00\",\"2023-10-10T10:00\",\"2023-10-10T11:00\",\"2023-10-10T12:00\",\"2023-10-10T13:00\",\"2023-10-10T14:00\",\"2023-10-10T15:00\",\"2023-10-10T16:00\",\"2023-10-10T17:00\",\"2023-10-10T18:00\",\"2023-10-10T19:00\",\"2023-10-10T20:00\",\"2023-10-10T21:00\",\"2023-10-10T22:00\",\"2023-10-10T23:00\",\"2023-10-11T00:00\",\"2023-10-11T01:00\",\"2023-10-11T02:00\",\"2023-10-11T03:00\",\"2023-10-11T04:00\",\"2023-10-11T05:00\",\"2023-10-11T06:00\",\"2023-10-11T07:00\",\"2023-10-11T08:00\",\"2023-10-11T09:00\",\"2023-10-11T10:00\",\"2023-10-11T11:00\",\"2023-10-11T12:00\",\"2023-10-11T13:00\",\"2023-10-11T14:00\",\"2023-10-11T15:00\",\"2023-10-11T16:00\",\"2023-10-11T17:00\",\"2023-10-11T18:00\",\"2023-10-11T19:00\",\"2023-10-11T20:00\",\"2023-10-11T21:00\",\"2023-10-11T22:00\",\"2023-10-11T23:00\",\"2023-10-12T00:00\",\"2023-10-12T01:00\",\"2023-10-12T02:00\",\"2023-10-12T03:00\",\"2023-10-12T04:00\",\"2023-10-12T05:00\",\"2023-10-12T06:00\",\"2023-10-12T07:00\",\"2023-10-12T08:00\",\"2023-10-12T09:00\",\"2023-10-12T10:00\",\"2023-10-12T11:00\",\"2023-10-12T12:00\",\"2023-10-12T13:00\",\"2023-10-12T14:00\",\"2023-10-12T15:00\",\"2023-10-12T16:00\",\"2023-10-12T17:00\",\"2023-10-12T18:00\",\"2023-10-12T19:00\",\"2023-10-12T20:00\",\"2023-10-12T21:00\",\"2023-10-12T22:00\",\"2023-10-12T23:00\",\"2023-10-13T00:00\",\"2023-10-13T01:00\",\"2023-10-13T02:00\",\"2023-10-13T03:00\",\"2023-10-13T04:00\",\"2023-10-13T05:00\",\"2023-10-13T06:00\",\"2023-10-13T07:00\",\"2023-10-13T08:00\",\"2023-10-13T09:00\",\"2023-10-13T10:00\",\"2023-10-13T11:00\",\"2023-10-13T12:00\",\"2023-10-13T13:00\",\"2023-10-13T14:00\",\"2023-10-13T15:00\",\"2023-10-13T16:00\",\"2023-10-13T17:00\",\"2023-10-13T18:00\",\"2023-10-13T19:00\",\"2023-10-13T20:00\",\"2023-10-13T21:00\",\"2023-10-13T22:00\",\"2023-10-13T23:00\",\"2023-10-14T00:00\",\"2023-10-14T01:00\",\"2023-10-14T02:00\",\"2023-10-14T03:00\",\"2023-10-14T04:00\",\"2023-10-14T05:00\",\"2023-10-14T06:00\",\"2023-10-14T07:00\",\"2023-10-14T08:00\",\"2023-10-14T09:00\",\"2023-10-14T10:00\",\"2023-10-14T11:00\",\"2023-10-14T12:00\",\"2023-10-14T13:00\",\"2023-10-14T14:00\",\"2023-10-14T15:00\",\"2023-10-14T16:00\",\"2023-10-14T17:00\",\"2023-10-14T18:00\",\"2023-10-14T19:00\",\"2023-10-14T20:00\",\"2023-10-14T21:00\",\"2023-10-14T22:00\",\"2023-10-14T23:00\"],\"carbon_monoxide\":[181.0,182.0,184.0,185.0,192.0,201.0,205.0,201.0,190.0,180.0,178.0,177.0,180.0,183.0,186.0,182.0,186.0,184.0,181.0,186.0,176.0,165.0,165.0,163.0,165.0,173.0,184.0,198.0,194.0,185.0,175.0,165.0,153.0,143.0,141.0,137.0,138.0,140.0,149.0,157.0,163.0,166.0,170.0,175.0,178.0,179.0,182.0,182.0,181.0,180.0,178.0,176.0,174.0,174.0,173.0,172.0,172.0,173.0,174.0,174.0,173.0,171.0,171.0,171.0,171.0,171.0,171.0,170.0,169.0,170.0,171.0,172.0,175.0,176.0,178.0,178.0,178.0,178.0,179.0,182.0,181.0,178.0,174.0,174.0,176.0,178.0,183.0,167.0,148.0,140.0,128.0,125.0,122.0,121.0,124.0,122.0,133.0,152.0,165.0,170.0,170.0,173.0,175.0,175.0,175.0,175.0,175.0,175.0,174.0,173.0,171.0,171.0,173.0,172.0,175.0,177.0,175.0,174.0,173.0,177.0]}}]";

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
