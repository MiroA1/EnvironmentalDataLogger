package fi.tuni.environmentaldatalogger;

import javafx.scene.chart.*;
import javafx.util.Pair;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;


public class Presenter {

    ArrayList<DataExtractor> APIs;

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

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setGapStartAndEnd(false);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        //lineChart.setTitle("Weather statistics");

        for (String param : datamap.keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            TreeMap<LocalDateTime, Double> dateMap = datamap.get(param);

            for (LocalDateTime date : dateMap.keySet()) {
                String dateString = date.format(formatter);
                series.getData().add(new XYChart.Data<>(dateString, dateMap.get(date)));
            }
            series.setName(param);
            lineChart.getData().add(series);
        }

        return lineChart;
    }

    /***
     * Returns a pie chart containing data of supplied parameters
     *
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


}
