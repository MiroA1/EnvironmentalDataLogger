package fi.tuni.environmentaldatalogger;

import fi.tuni.environmentaldatalogger.apis.AirQualityDataExtractor;
import fi.tuni.environmentaldatalogger.apis.DataExtractor;
import fi.tuni.environmentaldatalogger.apis.WeatherDataExtractor;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import javafx.scene.chart.*;
import javafx.util.Pair;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;


public class Presenter {

    ArrayList<DataExtractor> APIs;
    ArrayList<DataExtractor> airQualityAPIs;
    ArrayList<DataExtractor> weatherAPIs;

    private static Presenter instance;

    public static synchronized Presenter getInstance() {
        if (instance == null) {
            instance = new Presenter();
        }
        return instance;
    }

    private Presenter() {
        APIs = new ArrayList<>();
        APIs.add(WeatherDataExtractor.getInstance());
        APIs.add(AirQualityDataExtractor.getInstance());

        airQualityAPIs = new ArrayList<>();
        airQualityAPIs.add(AirQualityDataExtractor.getInstance());

        weatherAPIs = new ArrayList<>();
        weatherAPIs.add(WeatherDataExtractor.getInstance());
    }

    public ArrayList<String> getValidParameters() {
        return new ArrayList<>();
    }

    public ArrayList<String> getValidWeatherParameters() {
        return new ArrayList<>();
    }

    public ArrayList<String> getValidAirQualityParameters() {
        return new ArrayList<>();
    }

    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(String param) {
        return null;
    }

    /**
     * Returns the maximum range of data available for the given set of parameters.
     * @param params
     * @return
     */
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(ArrayList<String> params) {
        return null;
    }

    /**
     * Returns a line chart containing data of supplied parameters
     *
     * @param params what data is used to create a line chart. Temperature and humidity for example.
     * @param range date or time range for the data
     * @param coordinates coordinates for the geographic location of data
     * @return line chart of the given parameters
     */
    public LineChart<String, Number> getDataAsLineChart(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) {

        TreeMap<String, TreeMap<LocalDateTime, Double>> datamap = new TreeMap<>();

        for (String param : params) {
            TreeMap<LocalDateTime, Double> result = WeatherDataExtractor.getInstance().getData(param, range, coordinates);
            datamap.put(param, result);
        }

        Duration duration = Duration.between(range.getKey(), range.getValue());
        DateTimeFormatter formatter = (duration.toHours() <= 24) ?
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH") :
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        NumberAxis yAxis = new NumberAxis();
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setGapStartAndEnd(false);
        xAxis.setStartMargin(10);
        xAxis.setEndMargin(50);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        //lineChart.setTitle("Weather statistics");

        var api = weatherAPIs.get(0);
        for (String param : datamap.keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            TreeMap<LocalDateTime, Double> dateMap = datamap.get(param);
            // Change the first letter of param to uppercase
            String paramFormatted = param.substring(0, 1).toUpperCase() + param.substring(1);

            for (LocalDateTime date : dateMap.keySet()) {
                String dateString = date.format(formatter);
                series.getData().add(new XYChart.Data<>(dateString, dateMap.get(date)));
            }
            series.setName(paramFormatted + " " + api.getUnit(param));
            lineChart.getData().add(series);
        }

        return lineChart;
    }


    /**
     * Returns a pie chart containing data of supplied parameters
     * @param params List of pollutants in the air
     * @param range date or time range for the data
     * @param coordinates coordinates for the geographic location of data
     * @return pie chart containing data of supplied parameters
     */
    public PieChart getDataAsPieChart(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) {

        TreeMap<String, TreeMap<LocalDateTime, Double>> datamap = new TreeMap<>();
        PieChart pieChart = new PieChart();

        for (String param : params) {
            TreeMap<LocalDateTime, Double> result = AirQualityDataExtractor.getInstance().getData(param, range, coordinates);
            datamap.put(param, result);
        }

        for (String param : datamap.keySet()) {
            TreeMap<LocalDateTime, Double> dateMap = datamap.get(param);
            for (LocalDateTime date : dateMap.keySet()) {
                PieChart.Data slice = new PieChart.Data(param, dateMap.get(date));
                pieChart.getData().add(slice);
            }
        }

        return pieChart;
    }

    /**
     * Return the current (or most recent available) values and units of supplied parameters as string.
     * @param params
     * @param coordinates
     * @return TreeMap where key: parameter, value: value + unit (e.g. "20.1 °C")
     */
    public TreeMap<String, String> getCurrentData(ArrayList<String> params, Coordinate coordinates) {

        var api = weatherAPIs.get(0);
        TreeMap<String, Double> currentData = api.getCurrentData(params, coordinates);

        TreeMap<String, String> result = new TreeMap<>();

        for (String param : currentData.keySet()) {
            result.put(param, currentData.get(param) + " " + api.getUnit(param));
        }

        return result;
    }

}
