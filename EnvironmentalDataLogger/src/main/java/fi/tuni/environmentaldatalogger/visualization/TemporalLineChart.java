package fi.tuni.environmentaldatalogger.visualization;

import fi.tuni.environmentaldatalogger.util.TimeUtils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * A class for a temporal line chart.
 */
public class TemporalLineChart extends LineChart<Number, Number> {

    private TreeMap<String, String> colors = new TreeMap<>();
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    /**
     * Constructor.
     * @param xAxis The x-axis.
     * @param yAxis The y-axis.
     * @param data The data.
     * @param units units mapped to parameters.
     */
    public TemporalLineChart(NumberAxis xAxis, NumberAxis yAxis, TreeMap<String, TreeMap<LocalDateTime, Double>> data,
                             TreeMap<String, String> units) {
        super(xAxis, yAxis);
        this.xAxis = xAxis;
        this.yAxis = yAxis;

        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);

        yAxis.setForceZeroInRange(false);

        this.setLegendVisible(false);

        for (var param : data.keySet()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();

            for (var entry : data.get(param).entrySet()) {
                series.getData().add(new XYChart.Data<>(TimeUtils.getEpochSecond(entry.getKey()), entry.getValue()));
            }

            series.setName(param + " (" + units.get(param) + ")");
            this.getData().add(series);

            if (data.get(param).size() > 49) {
                for (XYChart.Data<Number, Number> dataPoint : series.getData()) {
                    Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                    lineSymbol.setStyle("-fx-background-color: transparent;");
                }
            }
        }
    }

    /**
     * Constructor.
     * @param data The data.
     * @param units units mapped to parameters.
     */
    public TemporalLineChart(TreeMap<String, TreeMap<LocalDateTime, Double>> data, TreeMap<String, String> units) {
        this(new NumberAxis(), new NumberAxis(), data, units);
    }

    /**
     * Sets the color palette for the chart.
     * @param colors The colors.
     */
    public void setColors(ArrayList<String> colors) {

        this.getData().forEach(series -> {
            int index = this.getData().indexOf(series) % colors.size();
            this.colors.put(series.getName(), colors.get(index));
            series.getNode().setStyle("-fx-stroke: " + colors.get(index) + ";");

            boolean hideSymbols = series.getData().size() >= 50;

            for (XYChart.Data<Number, Number> data : series.getData()) {

                Node symbol = data.getNode();
                if (symbol != null) {
                    symbol.setVisible(!hideSymbols);
                    symbol.setStyle("-fx-background-color: " + colors.get(index) + ", white;");
                }
            }
        });
    }

    /**
     * Returns the colors used.
     * @return The colors.
     */
    public TreeMap<String, String> getColors() {
        return colors;
    }

    /**
     * Returns the legend for the chart.
     * @return The legend.
     */
    public HBox getCustomLegend() {

        HBox legend = new HBox();
        legend.setSpacing(7);

        legend.setStyle("-fx-border-color: #DDDDDD; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 2; " + // Adjust the radius for roundness
                "-fx-padding: 3;");

        for (var colorData : colors.entrySet()) {
            Circle icon = new Circle(4, Color.valueOf("#FFFFFF"));
            icon.setStroke(Color.valueOf(colorData.getValue()));
            icon.setStrokeWidth(2);

            HBox seriesBox = new HBox();
            seriesBox.setSpacing(3);
            seriesBox.setAlignment(Pos.CENTER);

            Label label = new Label(colorData.getKey());
            label.setMinHeight(18);
            label.setMaxHeight(18);

            seriesBox.getChildren().addAll(icon, label);
            legend.getChildren().add(seriesBox);
        }

        return legend;
    }
}
