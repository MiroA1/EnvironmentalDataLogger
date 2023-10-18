package fi.tuni.environmentaldatalogger;

import javafx.scene.chart.*;
import javafx.util.Pair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

public class Presenter {

    ArrayList<DataExtractor> APIs;

    public ArrayList<String> getValidParameters() {
        return new ArrayList<>();
    }

    public Pair<Date, Date> getValidDataRange(String param) {
        return new Pair<>(new Date(), new Date());
    }

    /**
     * Return a line chart containing data of supplied parameters.
     * (Return type not finalized)
     *
     * @param params
     * @param range
     * @return
     */
    public LineChart<String, Number> getDataAsLineChart(ArrayList<String> params, Pair<Date, Date> range, Coordinate coordinates) {

        TreeMap<String, TreeMap<Date, Double>> datamap = new TreeMap<>();

        for (String param : params) {
            TreeMap<Date, Double> result = WeatherDataExtractor.getInstance().getData(param, range, coordinates);
            datamap.put(param, result);
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();


        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        for (String param : datamap.keySet()) {

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            TreeMap<Date, Double> innerMap = datamap.get(param);

            for (Date innerKey : innerMap.keySet()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = dateFormat.format(innerKey);
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
