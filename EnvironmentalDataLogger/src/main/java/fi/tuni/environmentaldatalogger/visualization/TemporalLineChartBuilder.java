package fi.tuni.environmentaldatalogger.visualization;

import fi.tuni.environmentaldatalogger.EnvironmentalDataLogger;
import fi.tuni.environmentaldatalogger.util.TimeUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class TemporalLineChartBuilder {

    private final TreeMap<String, TreeMap<LocalDateTime, Double>> data;
    private final TreeMap<String, String> units;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final NumberAxis yAxisSecondary;
    private String title;

    public TemporalLineChartBuilder() {
        data = new TreeMap<>();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        yAxisSecondary = new NumberAxis();
        units = new TreeMap<>();

        yAxis.setForceZeroInRange(false);

        // necessary for time based charts
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
    }

    public void addData(String param, TreeMap<LocalDateTime, Double> data, String unit) {
        this.data.put(param, data);
        this.units.put(param, unit);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBounds(LocalDateTime startDate, LocalDateTime endDate) {

        Duration duration = Duration.between(startDate, endDate);

        if (duration.toHours() > 121) {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfDay(startDate.minusHours(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfDay(endDate.plusHours(1))));
            xAxis.setTickUnit(86400);
        } else if (duration.toHours() > 49) {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusMinutes(30))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusMinutes(30))));
            xAxis.setTickUnit(21600);
        } else if (duration.toHours() > 25) {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusMinutes(30))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusMinutes(30))));
            xAxis.setTickUnit(14400);
        } else {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusMinutes(30))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusMinutes(30))));
            xAxis.setTickUnit(3600);
        }

        DateTimeFormatter formatter;

        if (duration.toHours() > 121) {
            formatter = DateTimeFormatter.ofPattern("dd.MM");
        } else if (duration.toHours() > 25) {
            formatter = DateTimeFormatter.ofPattern("dd.MM HH:mm");
        } else {
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        }

        formatTickLabels(formatter);
    }

    public void setBoundsBasedOnSmallestDataset() {

        if (data.isEmpty()) {
            return;
        }

        LocalDateTime startDate = LocalDateTime.MIN;
        LocalDateTime endDate = LocalDateTime.MAX;

        for (var param : data.keySet()) {
            if (data.get(param).firstKey().isAfter(startDate)) {
                startDate = data.get(param).firstKey();
            }

            if (data.get(param).lastKey().isBefore(endDate)) {
                endDate = data.get(param).lastKey();
            }
        }

        setBounds(startDate, endDate);
    }

    public void setBoundsBasedOnLargestDataset() {

        if (data.isEmpty()) {
            return;
        }

        LocalDateTime startDate = LocalDateTime.MAX;
        LocalDateTime endDate = LocalDateTime.MIN;

        for (var param : data.keySet()) {
            if (data.get(param).firstKey().isBefore(startDate)) {
                startDate = data.get(param).firstKey();
            }

            if (data.get(param).lastKey().isAfter(endDate)) {
                endDate = data.get(param).lastKey();
            }
        }

        setBounds(startDate, endDate);
    }

    public void setAutoBounds() {
        xAxis.setAutoRanging(true);
        formatTickLabels(DateTimeFormatter.ofPattern("dd.MM HH:mm"));
    }

    private void formatTickLabels(DateTimeFormatter formatter) {
        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis, null, "s") {
            @Override
            public String toString(Number object) {
                // Convert back to LocalDateTime and format
                LocalDateTime date = LocalDateTime.ofEpochSecond(object.longValue(), 0, ZoneOffset.UTC);
                return date.format(formatter);
            }
        });
    }

    // TODO: no need for maps
    private ArrayList<TreeMap<String, TreeMap<LocalDateTime, Double>>> splitData() {

        ArrayList<TreeMap<String, TreeMap<LocalDateTime, Double>>> result = new ArrayList<>();

        ArrayList<String> params = new ArrayList<>(data.keySet());

        params.sort((o1, o2) -> {
            double diff1 = getDiff(data.get(o1));
            double diff2 = getDiff(data.get(o2));

            return Double.compare(diff1, diff2);
        });

        // split params in the middle
        int split = (params.size() + 1) / 2;

        TreeMap<String, TreeMap<LocalDateTime, Double>> first = new TreeMap<>();
        TreeMap<String, TreeMap<LocalDateTime, Double>> second = new TreeMap<>();

        for (int i = 0; i < split; i++) {
            first.put(params.get(i), data.get(params.get(i)));
        }

        for (int i = split; i < params.size(); i++) {
            second.put(params.get(i), data.get(params.get(i)));
        }

        result.add(first);
        result.add(second);

        return result;
    }

    private static Double getDiff(TreeMap<LocalDateTime, Double> data) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (var entry : data.entrySet()) {
            min = Math.min(min, entry.getValue());
            max = Math.max(max, entry.getValue());
        }

        return abs(max - min);
    }

    private static Pair<Double, Double> getExtremes(TreeMap<LocalDateTime, Double> data) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (var entry : data.entrySet()) {
            min = Math.min(min, entry.getValue());
            max = Math.max(max, entry.getValue());
        }

        return new Pair<>(min, max);
    }

    public LineChart<Number, Number> getResult() {
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

        chart.setTitle(title);

        for (var param : data.keySet()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();

            for (var entry : data.get(param).entrySet()) {
                series.getData().add(new XYChart.Data<>(TimeUtils.getEpochSecond(entry.getKey()), entry.getValue()));
            }

            series.setName(param + " (" + units.get(param) + ")");
            chart.getData().add(series);

            if (data.get(param).size() > 49) {
                for (XYChart.Data<Number, Number> dataPoint : series.getData()) {
                    Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                    lineSymbol.setStyle("-fx-background-color: transparent;");
                }
            }
        }

        chart.getXAxis().setVisible(false);

        return chart;
    }

    private LineChart<Number, Number> getBaseChart(TreeMap<String, TreeMap<LocalDateTime, Double>> baseData) {
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

        chart.setTitle(title);

        for (var param : baseData.keySet()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();

            for (var entry : baseData.get(param).entrySet()) {
                series.getData().add(new XYChart.Data<>(TimeUtils.getEpochSecond(entry.getKey()), entry.getValue()));
            }

            series.setName(param + " (" + units.get(param) + ")");
            chart.getData().add(series);

            if (baseData.get(param).size() > 49) {
                for (XYChart.Data<Number, Number> dataPoint : series.getData()) {
                    Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                    lineSymbol.setStyle("-fx-background-color: transparent;");
                }
            }
        }

        chart.getXAxis().setVisible(false);

        return chart;
    }

    public Region getDoubleAxisResult() {

        StackPane st = new StackPane();
        st.setMinSize(0, 0);


        yAxisSecondary.setSide(Side.RIGHT);

        NumberAxis xAxisSecondary = new NumberAxis();

        xAxisSecondary.setForceZeroInRange(false);
        xAxisSecondary.setAutoRanging(false);
        xAxisSecondary.setLowerBound(xAxis.getLowerBound());
        xAxisSecondary.setUpperBound(xAxis.getUpperBound());
        xAxisSecondary.setTickUnit(xAxis.getTickUnit());
        xAxisSecondary.setTickLabelFormatter(xAxis.getTickLabelFormatter());

        LineChart<Number, Number> chart = new LineChart<>(xAxisSecondary, yAxisSecondary);
        chart.getStyleClass().add("secondary-chart");

        var baseData = splitData().get(0);
        var secondaryData = splitData().get(1);

        for (var param : secondaryData.keySet()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();

            for (var entry : secondaryData.get(param).entrySet()) {
                series.getData().add(new XYChart.Data<>(TimeUtils.getEpochSecond(entry.getKey()), entry.getValue()));
            }

            series.setName(param + " (" + units.get(param) + ")");
            chart.getData().add(series);
        }


        var baseChart = getBaseChart(baseData);
        //baseChart.setLegendVisible(false);

        chart.setTitle(baseChart.getTitle());

        styleSecondaryChart(chart);
        alignCharts(st, baseChart, chart);

        VBox res = new VBox();
        res.getChildren().addAll(st, getLegend(baseChart, chart));

        // A terrible workaround to have the charts aligned properly when the window is maximized
        Platform.runLater( () -> {
                EnvironmentalDataLogger.primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                    Platform.runLater(() -> alignCharts(st, baseChart, chart));
                });
            }
        );


        st.setPrefHeight(500);

        return res;
    }

    private HBox getLegend(LineChart<Number, Number> baseChart, LineChart<Number, Number> secondaryChart) {

        HBox legend = new HBox();
        legend.setSpacing(10);
        legend.setAlignment(Pos.CENTER);

        Pane spacer1 = new Pane();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        VBox baseLegendBox = new VBox();
        Label baseLabel = new Label("Left y-axis");
        Node baseLegend = baseChart.lookup(".chart-legend");

        baseLegendBox.getChildren().addAll(baseLabel, baseLegend);
        baseLegendBox.setFillWidth(false);

        VBox secondaryLegendBox = new VBox();
        Label secondaryLabel = new Label("Right y-axis");
        Node secondaryLegend = secondaryChart.lookup(".chart-legend");

        secondaryLegendBox.getChildren().addAll(secondaryLabel, secondaryLegend);

        legend.getChildren().addAll(spacer1, baseLegendBox, secondaryLegendBox, spacer2);
        secondaryLegendBox.setFillWidth(false);

        return legend;
    }

    private static void styleSecondaryChart(LineChart<Number, Number> chart) {
        chart.setVerticalZeroLineVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        //chart.setCreateSymbols(false);

        //TODO: uncomment
        chart.getXAxis().setOpacity(0);

        //chart.setLegendVisible(false);

        Node contentBackground = chart.lookup(".chart-content").lookup(".chart-plot-background");
        contentBackground.setStyle("-fx-background-color: transparent;");

        chart.lookup(".chart-title").setOpacity(0);
    }

    private static void alignCharts(StackPane sp, LineChart<Number, Number> baseChart, LineChart<Number, Number> secondaryChart) {

        sp.getChildren().clear();

        HBox baseChartBox = new HBox(baseChart);
        baseChartBox.prefHeightProperty().bind(sp.heightProperty());
        baseChartBox.prefWidthProperty().bind(sp.widthProperty());
        baseChartBox.setAlignment(Pos.CENTER_LEFT);

        var sub = baseChart.getYAxis().widthProperty().add(secondaryChart.getYAxis().widthProperty());

        baseChart.minWidthProperty().bind(sp.widthProperty().subtract(sub).add(baseChart.getYAxis().widthProperty()));
        baseChart.maxWidthProperty().bind(sp.widthProperty().subtract(sub).add(baseChart.getYAxis().widthProperty()));
        baseChart.prefWidthProperty().bind(sp.widthProperty().subtract(sub).add(baseChart.getYAxis().widthProperty()));

        HBox chartBox = new HBox(secondaryChart);
        chartBox.prefHeightProperty().bind(sp.heightProperty());
        chartBox.prefWidthProperty().bind(sp.widthProperty());
        chartBox.setAlignment(Pos.CENTER_LEFT);

        secondaryChart.minWidthProperty().bind(sp.widthProperty().subtract(sub).add(secondaryChart.getYAxis().widthProperty()));
        secondaryChart.maxWidthProperty().bind(sp.widthProperty().subtract(sub).add(secondaryChart.getYAxis().widthProperty()));
        secondaryChart.prefWidthProperty().bind(sp.widthProperty().subtract(sub).add(secondaryChart.getYAxis().widthProperty()));

        secondaryChart.translateXProperty().bind(baseChart.getYAxis().widthProperty());

        sp.getChildren().addAll(baseChartBox, chartBox);
    }
}
