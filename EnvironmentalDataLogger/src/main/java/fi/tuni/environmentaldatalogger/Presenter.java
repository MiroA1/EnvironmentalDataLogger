package fi.tuni.environmentaldatalogger;

import javafx.scene.chart.*;
import javafx.util.Pair;
import java.text.SimpleDateFormat;
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
     * Return a line chart containing data of supplied parameters.
     * (Return type not finalized)
     *
     * @param params
     * @param range
     * @return
     */
    public LineChart<String, Number> getDataAsLineChart(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) {

        TreeMap<String, TreeMap<LocalDateTime, Double>> datamap = new TreeMap<>();

        for (String param : params) {
            TreeMap<LocalDateTime, Double> result = WeatherDataExtractor.getInstance().getData(param, range, coordinates);
            datamap.put(param, result);
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();


        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        for (String param : datamap.keySet()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            TreeMap<LocalDateTime, Double> innerMap = datamap.get(param);

            for (LocalDateTime innerKey : innerMap.keySet()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                // TODO: do this properly
                Date innerKey1 = java.util.Date.from(innerKey.atZone(ZoneId.systemDefault()).toInstant());

                String dateString = dateFormat.format(innerKey1);
                series.getData().add(new XYChart.Data<>(dateString, innerMap.get(innerKey)));
            }
            series.setName(param);
            lineChart.getData().add(series);
        }

        lineChart.setTitle("Weather statistics");

        return lineChart;

    }


    // TODO: possibly other chart types
}
