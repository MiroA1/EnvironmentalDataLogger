package fi.tuni.environmentaldatalogger.visualization;

import fi.tuni.environmentaldatalogger.EnvironmentalDataLogger;
import fi.tuni.environmentaldatalogger.util.TimeUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Builder for a line chart with a time based x-axis. Supports multiple y-axes.
 */
public class TemporalLineChartBuilder {

    private final static ArrayList<String> baseColors = new ArrayList<>(List.of("#c90823", "#f43d25", "#fd8c3c", "#febf5a", "#ffe793"));
    private final static ArrayList<String> secondaryColors = new ArrayList<>(List.of("#1373b2", "#42a6cc", "#7accc4", "#b4e2ba", "#daf0d4"));
    private final TreeMap<String, TreeMap<LocalDateTime, Double>> data;
    private final TreeMap<String, String> units;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private final NumberAxis yAxisSecondary;
    private final NumberAxis xAxisSecondary;
    private String title;

    public TemporalLineChartBuilder() {
        data = new TreeMap<>();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        yAxisSecondary = new NumberAxis();
        xAxisSecondary = new NumberAxis();
        units = new TreeMap<>();

        yAxis.setForceZeroInRange(false);

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

    private ArrayList<TreeMap<String, TreeMap<LocalDateTime, Double>>> splitData() {

        ArrayList<DataSplitter.DataObject> dataObjects = new ArrayList<>();

        for (var entry : data.entrySet()) {
            dataObjects.add(new DataSplitter.DataObject(entry.getKey(), entry.getValue()));
        }

        var a = DataSplitter.kMeansCluster(dataObjects, 2);

        ArrayList<TreeMap<String, TreeMap<LocalDateTime, Double>>> result = new ArrayList<>();

        for (var cluster : a) {
            TreeMap<String, TreeMap<LocalDateTime, Double>> map = new TreeMap<>();

            for (var dataObject : cluster) {
                map.put(dataObject.getName(), data.get(dataObject.getName()));
            }

            result.add(map);
        }

        return result;
    }

    public Region getResult() {

        var chart = new TemporalLineChart(xAxis, yAxis, data, units);

        chart.setTitle(title);

        chart.setColors(baseColors);
        chart.setLegendVisible(false);

        chart.setPrefHeight(500);

        VBox res = new VBox();
        HBox legendBox = new HBox();

        Pane spacer1 = new Pane();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        legendBox.setPadding(new Insets(0, 0, 5, 0));

        legendBox.getChildren().addAll(spacer1, chart.getCustomLegend(), spacer2);

        res.getChildren().addAll(chart, legendBox);
        res.setAlignment(Pos.CENTER);

        return res;
    }

    private TemporalLineChart getBaseChart(TreeMap<String, TreeMap<LocalDateTime, Double>> baseData) {

        TemporalLineChart chart = new TemporalLineChart(xAxis, yAxis, baseData, units);

        chart.setColors(baseColors);
        chart.setTitle(title);
        chart.getXAxis().setVisible(false);

        return chart;
    }

    private TemporalLineChart getSecondaryChart(TreeMap<String, TreeMap<LocalDateTime, Double>> secondaryData) {

        setUpSecondaryAxes();

        TemporalLineChart chart = new TemporalLineChart(xAxisSecondary, yAxisSecondary, secondaryData, units);
        chart.getStyleClass().add("secondary-chart");

        chart.setColors(secondaryColors);
        chart.setTitle(title);
        chart.getXAxis().setVisible(false);

        styleSecondaryChart(chart);

        return chart;
    }

    public Region getDoubleAxisResult() {

        StackPane st = new StackPane();
        st.setMinSize(0, 0);

        var splitData = splitData();

        var baseData = splitData.get(0);
        var secondaryData = splitData.get(1);

        TemporalLineChart baseChart = getBaseChart(baseData);
        TemporalLineChart secondaryChart = getSecondaryChart(secondaryData);

        alignCharts(st, baseChart, secondaryChart);

        VBox res = new VBox();
        res.getChildren().addAll(st, getLegend(baseChart, secondaryChart));

        // A terrible workaround to have the charts aligned properly when the window is maximized
        Platform.runLater( () -> {
                EnvironmentalDataLogger.primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
                    Platform.runLater(() -> alignCharts(st, baseChart, secondaryChart));
                });
            }
        );

        st.setPrefHeight(500);

        return res;
    }

    private void setUpSecondaryAxes() {
        yAxisSecondary.setSide(Side.RIGHT);

        xAxisSecondary.setForceZeroInRange(false);
        xAxisSecondary.setAutoRanging(false);
        xAxisSecondary.setLowerBound(xAxis.getLowerBound());
        xAxisSecondary.setUpperBound(xAxis.getUpperBound());
        xAxisSecondary.setTickUnit(xAxis.getTickUnit());
        xAxisSecondary.setTickLabelFormatter(xAxis.getTickLabelFormatter());
    }

    private HBox getLegend(TemporalLineChart baseChart, TemporalLineChart secondaryChart) {

        HBox legend = new HBox();
        legend.setSpacing(10);
        legend.setAlignment(Pos.CENTER);

        Pane spacer1 = new Pane();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        VBox baseLegendBox = new VBox();
        Label baseLabel = new Label("Left y-axis");
        baseLabel.setPadding(new Insets(-4, 0, 0, 0));
        HBox baseLegend = baseChart.getCustomLegend();


        baseLegendBox.getChildren().addAll(baseLabel, baseLegend);
        baseLegendBox.setFillWidth(false);

        VBox secondaryLegendBox = new VBox();
        Label secondaryLabel = new Label("Right y-axis");
        secondaryLabel.setPadding(new Insets(-4, 0, 0, 0));
        Node secondaryLegend = secondaryChart.getCustomLegend();

        secondaryLegendBox.getChildren().addAll(secondaryLabel, secondaryLegend);

        legend.getChildren().addAll(spacer1, baseLegendBox, secondaryLegendBox, spacer2);
        secondaryLegendBox.setFillWidth(false);

        //addDebugBorder(legend, Color.RED, 1);
        legend.setPadding(new Insets(0, 0, 5, 0));

        return legend;
    }

    private static void styleSecondaryChart(LineChart<Number, Number> chart) {
        chart.setVerticalZeroLineVisible(false);
        chart.setHorizontalZeroLineVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalGridLinesVisible(false);

        chart.getXAxis().setOpacity(0);

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
