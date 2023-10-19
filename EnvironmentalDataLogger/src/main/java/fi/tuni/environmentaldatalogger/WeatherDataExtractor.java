package fi.tuni.environmentaldatalogger;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import javafx.util.Pair;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public Pair<Date, Date> getValidDataRange(String param) {
        return null;
    }

    @Override
    public TreeMap<Date, Double> getData(String param, Pair<Date, Date> range, Coordinate coordinates) {
        String apiUrl = constructApiUrl(coordinates, range.getKey(), range.getValue());
        return fetchData(apiUrl, param);
    }

    @Override
    public TreeMap<Date, Double> getData(String param, Coordinate coordinates) {
        String apiUrl = constructApiUrl(coordinates, null, null);
        return fetchData(apiUrl, param);
    }

    private String constructApiUrl(Coordinate coordinates, Date startDate, Date endDate) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL + "London,UK");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (startDate != null && endDate != null) {
            apiUrl.append("/").append(dateFormat.format(startDate)).append("/").append(dateFormat.format(endDate));
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


    private TreeMap<Date, Double> fetchData(String apiUrl, String param) {
        TreeMap<Date, Double> weatherData = new TreeMap<>();
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

    private TreeMap<Date, Double> parseWeatherData(String json, String param) {
        TreeMap<Date, Double> weatherData = new TreeMap<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray daysArray = jsonObject.getJSONArray("days");

            for (int i = 0; i < daysArray.length(); i++) {
                JSONObject dayObject = daysArray.getJSONObject(i);
                String datetime = dayObject.getString("datetime");
                double value = dayObject.getDouble(param);

                Date date = parseDate(datetime);
                if (date != null) {
                    weatherData.put(date, value);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weatherData;
    }

    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}




