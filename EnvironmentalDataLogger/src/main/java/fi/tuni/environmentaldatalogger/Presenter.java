package fi.tuni.environmentaldatalogger;

import fi.tuni.environmentaldatalogger.util.TimeUtils;
import fi.tuni.environmentaldatalogger.apis.ApiException;
import fi.tuni.environmentaldatalogger.gui.MainView;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import fi.tuni.environmentaldatalogger.apis.AirQualityDataExtractor;
import fi.tuni.environmentaldatalogger.apis.DataExtractor;
import fi.tuni.environmentaldatalogger.apis.WeatherDataExtractor;
import fi.tuni.environmentaldatalogger.apis.GeocodingService;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import javafx.scene.chart.*;
import javafx.scene.text.Font;
import javafx.util.Pair;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

        MainView.getInstance();
    }

    /**
     * Returns all valid parameters available in the APIs.
     * @return list of valid parameters
     */
    public ArrayList<String> getValidParameters() {

        TreeSet<String> params = new TreeSet<>();

        for (DataExtractor api : APIs) {
            params.addAll(api.getValidParameters());
        }

        return new ArrayList<>(params);
    }

    /**
     * Returns all valid parameters available in the weather APIs.
     * @return list of valid parameters
     */
    public ArrayList<String> getValidWeatherParameters() {

        HashSet<String> params = new HashSet<>();

        for (DataExtractor api : weatherAPIs) {
            params.addAll(api.getValidParameters());
        }

        return new ArrayList<>(params);
    }

    /**
     * Returns all valid parameters available in the air quality APIs.
     * @return list of valid parameters
     */
    public ArrayList<String> getValidAirQualityParameters() {

        HashSet<String> params = new HashSet<>();

        for (DataExtractor api : airQualityAPIs) {
            params.addAll(api.getValidParameters());
        }

        return new ArrayList<>(params);
    }

    /**
     * Returns the maximum range of data available for the given set of parameters.
     * @param params parameters to be checked
     * @return Pair where key: start date, value: end date
     */
    public Pair<LocalDateTime, LocalDateTime> getValidDataRange(ArrayList<String> params) {

        var apiMap = matchParamsAndAPIs(params);

        Pair<LocalDateTime, LocalDateTime> result = null;

        for (DataExtractor api : apiMap.keySet()) {
            Pair<LocalDateTime, LocalDateTime> range = api.getValidDataRange(apiMap.get(api));

            if (result == null) {
                result = range;
            } else {
                var start = range.getKey().isBefore(result.getKey()) ? result.getKey() : range.getKey();
                var end = range.getValue().isAfter(result.getValue()) ? result.getValue() : range.getValue();
                result = new Pair<>(start, end);
            }
        }

        if (result == null) {
            return new Pair<>(LocalDateTime.now(), LocalDateTime.now());
        }

        return result;
    }

    /***
     * Adjusts the bounds of the x-axis of a line chart based on the duration of the data.
     * @param xAxis x-axis of the line chart
     * @param startDate start date of the data
     * @param endDate end date of the data
     */
    private void adjustBounds (NumberAxis xAxis, LocalDateTime startDate, LocalDateTime endDate) {

        Duration duration = Duration.between(startDate, endDate);
        DateTimeFormatter formatter;

        if (duration.toHours() > 120) {
            formatter = DateTimeFormatter.ofPattern("dd.MM");
        } else if (duration.toHours() > 24) {
            formatter = DateTimeFormatter.ofPattern("dd.MM HH:mm");
        } else {
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        }

        if (duration.toHours() > 120) {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfDay(startDate.minusDays(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfDay(endDate.plusDays(1))));
            xAxis.setTickUnit(86400);
        } else if (duration.toHours() > 48) {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusHours(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusHours(1))));
            xAxis.setTickUnit(21600);
        } else if (duration.toHours() > 24) {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusHours(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusHours(1))));
            xAxis.setTickUnit(14400);
        } else {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusHours(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusHours(1))));
            xAxis.setTickUnit(3600);
        }

        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis, null, "s") {
            @Override
            public String toString(Number object) {
                // Convert back to LocalDateTime and format
                LocalDateTime date = LocalDateTime.ofEpochSecond(object.longValue(), 0, ZoneOffset.UTC);
                return date.format(formatter);
            }
        });


    }

    /**
     * Returns a line chart containing data of supplied parameters
     *
     * @param params what data is used to create a line chart. Temperature and humidity for example.
     * @param range date or time range for the data
     * @param coordinates coordinates for the geographic location of data
     * @return line chart of the given parameters
     */

    public LineChart<Number, Number> getDataAsLineChart(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates) throws ApiException {

        TreeMap<String, TreeMap<LocalDateTime, Double>> dataMap = new TreeMap<>();

        var apiMap = matchParamsAndAPIs(params);
        TreeMap<String, String> units = new TreeMap<>();

        for (DataExtractor api : apiMap.keySet()) {
            TreeMap<String, TreeMap<LocalDateTime, Double>> result = api.getData(apiMap.get(api), range, coordinates);
            dataMap.putAll(result);

            for (String param : apiMap.get(api)) {
                units.put(param, api.getUnit(param));
            }
        }

        NumberAxis yAxis = new NumberAxis();
        NumberAxis xAxis = new NumberAxis();

        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);

        LocalDateTime startDate = range.getKey();
        LocalDateTime endDate = range.getValue();

        adjustBounds(xAxis, startDate, endDate);

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        for (Map.Entry<String, TreeMap<LocalDateTime, Double>> entry : dataMap.entrySet()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            TreeMap<LocalDateTime, Double> dateMap = entry.getValue();
            String param = entry.getKey();

            if (dateMap == null || dateMap.isEmpty()) {
                continue;
            }

            // discard data outside of range
            SortedMap<LocalDateTime, Double> subMap = dateMap.subMap(range.getKey().minusHours(1), range.getValue().plusHours(1));
            dateMap = new TreeMap<>(subMap);

            // Convert the first letter of param to uppercase
            String paramFormatted = param.substring(0, 1).toUpperCase() + param.substring(1);

            for (Map.Entry<LocalDateTime, Double> innerEntry : dateMap.entrySet()) {
                series.getData().add(new XYChart.Data<>(TimeUtils.getEpochSecond(innerEntry.getKey()), innerEntry.getValue()));
            }

            if (dateMap.size() > 49) {
                lineChart.setCreateSymbols(false);
            }

            series.setName(paramFormatted + " " + units.get(param));
            lineChart.getData().add(series);
        }

        return lineChart;
    }

    /***
     * Sets the color of a slice in a pie chart based on the value of the slice.
     * @param slice slice of the pie chart
     */
    private void setSliceColor(PieChart.Data slice) {

        String sliceName = slice.getName();
        double sliceValue = slice.getPieValue();
        Node sliceNode = slice.getNode();

        if (sliceName.equals("SO2")) {
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
            } else if (40 <= sliceValue && sliceValue < 70) {
                sliceNode.setStyle("-fx-pie-color: #FFF933");
            } else if (70 <= sliceValue && sliceValue < 150) {
                sliceNode.setStyle("-fx-pie-color: #FFA533");
            } else if (150 <= sliceValue && sliceValue < 200) {
                sliceNode.setStyle("-fx-pie-color: #EA3F1D");
            } else if (sliceValue >= 200) {
                sliceNode.setStyle("-fx-pie-color: #b5468b");
            }
        } else if (sliceName.equals("O3")) {
            if (sliceValue < 60) {
                sliceNode.setStyle("-fx-pie-color: #33FF4C");
            } else if (60 <= sliceValue && sliceValue < 100) {
                sliceNode.setStyle("-fx-pie-color: #FFF933");
            } else if (100 <= sliceValue && sliceValue < 140) {
                sliceNode.setStyle("-fx-pie-color: #FFA533");
            } else if (140 <= sliceValue && sliceValue < 180) {
                sliceNode.setStyle("-fx-pie-color: #EA3F1D");
            } else if (sliceValue >= 180) {
                sliceNode.setStyle("-fx-pie-color: #b5468b");
            }
        } else if (sliceName.equals("PM10")) {
            if (sliceValue < 20) {
                sliceNode.setStyle("-fx-pie-color: #33FF4C");
            } else if (20 <= sliceValue && sliceValue < 50) {
                sliceNode.setStyle("-fx-pie-color: #FFF933");
            } else if (50 <= sliceValue && sliceValue < 100) {
                sliceNode.setStyle("-fx-pie-color: #FFA533");
            } else if (100 <= sliceValue && sliceValue < 200) {
                sliceNode.setStyle("-fx-pie-color: #EA3F1D");
            } else if (sliceValue >= 200) {
                sliceNode.setStyle("-fx-pie-color: #b5468b");
            }
        } else if (sliceName.equals("PM2.5")) {
            if (sliceValue < 10) {
                sliceNode.setStyle("-fx-pie-color: #33FF4C");
            } else if (10 <= sliceValue && sliceValue < 25) {
                sliceNode.setStyle("-fx-pie-color: #FFF933");
            } else if (25 <= sliceValue && sliceValue < 50) {
                sliceNode.setStyle("-fx-pie-color: #FFA533");
            } else if (50 <= sliceValue && sliceValue < 75) {
                sliceNode.setStyle("-fx-pie-color: #EA3F1D");
            } else if (sliceValue >= 75) {
                sliceNode.setStyle("-fx-pie-color: #b5468b");
            }
        }
    }


    /**
     * Returns a pie chart containing data of supplied parameters
     * @param params List of pollutants in the air
     * @param date date for the data
     * @param coordinates coordinates for the geographic location of data
     * @return pie chart containing data of supplied parameters
     */
    public PieChart getDataAsPieChart(ArrayList<String> params, LocalDateTime date, Coordinate coordinates) throws
            ApiException {


        Pair<LocalDateTime, LocalDateTime> range = new Pair<>(date, date);
        TreeMap<String, TreeMap<LocalDateTime, Double>> dataMap;

        dataMap = AirQualityDataExtractor.getInstance().getData(params, range, coordinates);

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Distribution of common air pollutants");

        for (String param : dataMap.keySet()) {
            if (!param.equals("AQI") && !param.equals("CO")) {
                TreeMap<LocalDateTime, Double> dateMap = dataMap.get(param);
                double paramValue = dateMap.firstEntry().getValue();
                PieChart.Data slice = new PieChart.Data(param, paramValue);
                pieChart.getData().add(slice);

            }
        }

        for (PieChart.Data slice : pieChart.getData()) {
            setSliceColor(slice);
        }

        // Add value and unit to the name of the slice
        pieChart.getData().forEach(data ->
                data.nameProperty().bind(Bindings.concat(
                        data.getName(), "   ", data.pieValueProperty(), " µg/m³")));

        pieChart.setLegendVisible(false);

        return pieChart;
    }


    /**
     * Return the current (or most recent available) values and units of supplied parameters as string.
     * @param params parameters to fetch data for
     * @param coordinates coordinates for the geographic location of data
     * @return TreeMap where key: parameter, value: value + unit (e.g. "20.1 °C")
     */
    public TreeMap<String, String> getCurrentData(ArrayList<String> params, Coordinate coordinates)
            throws ApiException {

        var api = weatherAPIs.get(0);
        TreeMap<String, Double> currentData;
        currentData = api.getCurrentData(params, coordinates);

        TreeMap<String, String> result = new TreeMap<>();

        for (String param : currentData.keySet()) {
            String paramFormatted = param.substring(0, 1).toUpperCase() + param.substring(1);
            result.put(paramFormatted, currentData.get(param) + " " + api.getUnit(param));
        }

        return result;
    }

    /**
     * Distributes parameters to the APIs that can provide data for them.
     * Used for deciding which API to use for each parameter.
     * @param params parameters to be distributed
     * @return HashMap where key: API, value: list of parameters
     */
    private HashMap<DataExtractor, ArrayList<String>> matchParamsAndAPIs(ArrayList<String> params) {

            HashMap<DataExtractor, ArrayList<String>> result = new HashMap<>();

            for (String param : params) {
                for (DataExtractor api : APIs) {
                    if (api.getValidParameters().contains(param)) {
                        if (result.containsKey(api)) {
                            result.get(api).add(param);
                        } else {
                            ArrayList<String> list = new ArrayList<>();
                            list.add(param);
                            result.put(api, list);
                        }
                    }
                }
            }
            return result;
    }

    /**
     * Converts an address to coordinates.
     *
     * @param address The address to convert.
     * @return The coordinates of the address, or null if unable to convert.
     */
    public Coordinate getCoordinatesFromAddress(String address) {
        try {
            return GeocodingService.getInstance().getCoordinates(address);
        } catch (Exception e) {
            //Exceptions handled in GeocodingService class already
            return null;
        }
    }
}
