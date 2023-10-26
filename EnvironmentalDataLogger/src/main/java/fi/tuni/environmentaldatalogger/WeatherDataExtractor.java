package fi.tuni.environmentaldatalogger;

import okhttp3.*;
import org.json.JSONArray;
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
        validParameters.add("temperature");
        validParameters.add("humidity");
        return validParameters;
    }

    @Override
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(String param) {
        return new Pair<>(LocalDateTime.now().minusDays(30), LocalDateTime.now());
    }

    @Override
    public TreeMap<LocalDateTime, Double> getData(String param, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) {
        ArrayList<String> params = new ArrayList<>();
        params.add(param);
        TreeMap<String, TreeMap<LocalDateTime, Double>> combinedData = getData(params, range, coordinates);
        return combinedData.get(param);
    }

    @Override
    public TreeMap<LocalDateTime, Double> getData(String param, Coordinate coordinates) {
        Pair<LocalDateTime, LocalDateTime> range = getValidDataRange(param);
        return getData(param, range, coordinates);
    }

    @Override
    public TreeMap<String, TreeMap<LocalDateTime, Double>> getData(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) {
        String apiUrl = constructApiUrl(coordinates, range.getKey(), range.getValue(), params, false);
        TreeMap<LocalDateTime, TreeMap<String, Double>> fetchedData = fetchDataMultipleParams(apiUrl, params);

        TreeMap<String, TreeMap<LocalDateTime, Double>> resultMap = new TreeMap<>();
        for (String param : params) {
            TreeMap<LocalDateTime, Double> paramData = new TreeMap<>();
            for (Map.Entry<LocalDateTime, TreeMap<String, Double>> entry : fetchedData.entrySet()) {
                paramData.put(entry.getKey(), entry.getValue().get(param));
            }
            resultMap.put(param, paramData);
        }

        return resultMap;
    }

    @Override
    public TreeMap<String, Double> getCurrentData(ArrayList<String> params, Coordinate coordinates) {
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
            default:
                return "";
        }
    }

    private String constructApiUrl(Coordinate coordinates, LocalDateTime startDate, LocalDateTime endDate, ArrayList<String> params, boolean getCurrent) {
        StringBuilder apiUrl = new StringBuilder(API_BASE_URL + "London,UK");

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
            apiUrl.append("&include=days");
        }

        String elements = String.join(",", urlParams);
        apiUrl.append("&elements=datetime,").append(elements);
        apiUrl.append("&unitGroup=metric");

        return apiUrl.toString();
    }

    private TreeMap<LocalDateTime, TreeMap<String, Double>> fetchDataMultipleParams(String apiUrl, ArrayList<String> params) {
        TreeMap<LocalDateTime, TreeMap<String, Double>> weatherData = new TreeMap<>();
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                weatherData = parseWeatherDataMultipleParams(responseBody, params);
            } else {
                System.err.println("Unexpected response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weatherData;
    }

    private TreeMap<LocalDateTime, TreeMap<String, Double>> parseWeatherDataMultipleParams(String json, ArrayList<String> params) {
        TreeMap<LocalDateTime, TreeMap<String, Double>> weatherData = new TreeMap<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray daysArray = jsonObject.getJSONArray("days");

            for (int i = 0; i < daysArray.length(); i++) {
                JSONObject dayObject = daysArray.getJSONObject(i);
                String datetime = dayObject.getString("datetime");
                LocalDateTime date = parseDate(datetime);

                TreeMap<String, Double> dailyData = new TreeMap<>();

                for (String param : params) {
                    if (param.equals("temperature") && dayObject.has("temp")) {
                        dailyData.put("temperature", dayObject.getDouble("temp"));
                    } else if (dayObject.has(param)) {
                        dailyData.put(param, dayObject.getDouble(param));
                    }
                }

                weatherData.put(date, dailyData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherData;
    }


    private TreeMap<String, Double> fetchCurrentData(String apiUrl, ArrayList<String> params) {
        TreeMap<String, Double> currentData = new TreeMap<>();
        Request request = new Request.Builder().url(apiUrl).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println(responseBody);
                currentData = parseCurrentWeatherData(responseBody, params);
            } else {
                System.err.println("Unexpected response code: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentData;
    }


    private LocalDateTime parseDate(String datetime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(datetime);
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TreeMap<String, Double> parseCurrentWeatherData(String json, ArrayList<String> params) {
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
            e.printStackTrace();
        }
        return currentData;
    }

}









