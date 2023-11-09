package fi.tuni.environmentaldatalogger;

import javafx.scene.Node;
import fi.tuni.environmentaldatalogger.apis.AirQualityDataExtractor;
import fi.tuni.environmentaldatalogger.apis.DataExtractor;
import fi.tuni.environmentaldatalogger.apis.WeatherDataExtractor;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.gui.CoordinateDialog;
import javafx.scene.chart.*;
import javafx.util.Pair;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

        TreeSet<String> params = new TreeSet<>();

        for (DataExtractor api : APIs) {
            params.addAll(api.getValidParameters());
        }

        return new ArrayList<>(params);
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
        // TODO: change this to something that makes sense
        return airQualityAPIs.get(0).getValidDataRange("");
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

    private void setSliceColor(PieChart.Data slice) {

        String sliceName = slice.getName();
        double sliceValue = slice.getPieValue();
        Node sliceNode = slice.getNode();

        if (sliceName.equals("S02")) {
            if (sliceValue < 20) {
                sliceNode.setStyle("-fx-pie-color: #33FF4C");
            } else if (20 <= sliceValue && sliceValue < 80){
                sliceNode.setStyle("-fx-pie-color: #FFF933");
            } else if (80 <= sliceValue && sliceValue < 250) {
                sliceNode.setStyle("-fx-pie-color: #FFA533");
            } else if (250 <= sliceValue && sliceValue < 350) {
                sliceNode.setStyle("-fx-pie-color: #EA3F1D");
            } else if (sliceValue >= 350) {
                sliceNode.setStyle("-fx-pie-color: #b5468b");
            }
        } else if (sliceName.equals("NO2")) {
            if (sliceValue < 40) {
                sliceNode.setStyle("-fx-pie-color: #33FF4C");
            } else if (40 <= sliceValue && sliceValue < 70){
                sliceNode.setStyle("-fx-pie-color: #FFF933");
            } else if (70 <= sliceValue && sliceValue < 150) {
                sliceNode.setStyle("-fx-pie-color: #FFA533");
            } else if (150 <= sliceValue && sliceValue < 200) {
                sliceNode.setStyle("-fx-pie-color: #EA3F1D");
            } else if (sliceValue >= 200) {
                sliceNode.setStyle("-fx-pie-color: #b5468b");
            }
        } else {
            sliceNode.setStyle("-fx-pie-color: #000000");
        }

    }


    /**
     * Returns a pie chart containing data of supplied parameters
     * @param params List of pollutants in the air
     * @param date date for the data
     * @param coordinates coordinates for the geographic location of data
     * @return pie chart containing data of supplied parameters
     */
    public PieChart getDataAsPieChart(ArrayList<String> params, LocalDateTime date, Coordinate coordinates) {

        Pair<LocalDateTime, LocalDateTime> range = new Pair<>(date, date);

        TreeMap<String, TreeMap<LocalDateTime, Double>> dataMap = AirQualityDataExtractor.getInstance().
                                                                        getData(params, range, coordinates);

        PieChart pieChart = new PieChart();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateString = date.format(formatter);
        pieChart.setTitle(dateString);

        for (String param : dataMap.keySet()) {
            TreeMap<LocalDateTime, Double> dateMap = dataMap.get(param);
            PieChart.Data slice = new PieChart.Data(param, dateMap.get(date));
            setSliceColor(slice);
            pieChart.getData().add(slice);
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
