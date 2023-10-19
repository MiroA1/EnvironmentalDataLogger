package fi.tuni.environmentaldatalogger;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javafx.util.Pair;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class WeatherDataExtractor implements DataExtractor {
    private static final String API_BASE_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/";
    private static final String API_KEY = "NAJZYGFDHA4GEA5QXD7S4TZSL";

    private OkHttpClient httpClient;

    private static WeatherDataExtractor instance;

    private WeatherDataExtractor() {
        this.httpClient = new OkHttpClient();
    }

    public static WeatherDataExtractor getInstance() {
        if (instance == null) {
            instance = new WeatherDataExtractor();
        }
        return instance;
    }

    @Override
    public ArrayList<String> getValidParameters() {
        ArrayList<String> validParameters = new ArrayList<>();
        validParameters.add("temp");
        validParameters.add("humidity");
        return validParameters;
    }

    @Override
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(String param) {
        return null;
    }

    @Override
    public TreeMap<LocalDateTime, Double> getData(String param, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) {
        String apiUrl = constructApiUrl(coordinates, range.getKey(), range.getValue());
        return fetchData(apiUrl, param);
    }

    @Override
    public TreeMap<LocalDateTime, Double> getData(String param, Coordinate coordinates) {
        String apiUrl = constructApiUrl(coordinates, null, null);
        return fetchData(apiUrl, param);
    }

    @Override
    public TreeMap<LocalDateTime, Double> getData(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) {
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

    private String constructApiUrl(Coordinate coordinates, LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL + "London,UK");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        // TODO: do this properly
        Date startDate1 = java.util.Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate1 = java.util.Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant());

        if (startDate != null && endDate != null) {
            apiUrl.append("/").append(dateFormat.format(startDate1)).append("/").append(dateFormat.format(endDate1));
        } else {
            apiUrl.append("/last30days");
        }

        apiUrl.append("?key=").append(API_KEY);
        apiUrl.append("&include=days");
        apiUrl.append("&elements=datetime,humidity,temp");
        apiUrl.append("&unitGroup=metric");

        System.out.println(apiUrl);
        return apiUrl.toString();
    }


    private TreeMap<LocalDateTime, Double> fetchData(String apiUrl, String param) {
        TreeMap<LocalDateTime, Double> weatherData = new TreeMap<>();
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                weatherData = parseWeatherData(responseBody, param);
            } else {
                System.err.println("Unexpected response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weatherData;
    }

    private TreeMap<LocalDateTime, Double> parseWeatherData(String json, String param) {
        TreeMap<LocalDateTime, Double> weatherData = new TreeMap<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray daysArray = jsonObject.getJSONArray("days");

            for (int i = 0; i < daysArray.length(); i++) {
                JSONObject dayObject = daysArray.getJSONObject(i);
                String datetime = dayObject.getString("datetime");
                double value = dayObject.getDouble(param);

                LocalDateTime date = parseDate(datetime);
                if (date != null) {
                    weatherData.put(date, value);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weatherData;
    }

    private LocalDateTime parseDate(String dateString) {
        try {
            // TODO: do this properly
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}




