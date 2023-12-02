package fi.tuni.environmentaldatalogger.visualization;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.TreeMap;

public class TemporalLineChart extends LineChart<Number, Number> {

    private TreeMap<String, String> colors = new TreeMap<>();
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    public TemporalLineChart(NumberAxis xAxis, NumberAxis yAxis) {
        super(xAxis, yAxis);
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        init();
    }

    public TemporalLineChart() {
        this(new NumberAxis(), new NumberAxis());
    }

    private void init() {
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);

        yAxis.setForceZeroInRange(false);

        this.setLegendVisible(false);
    }

    public void setColors(ArrayList<String> colors) {

        // TODO: make sure no overflow
        this.getData().forEach(series -> {
            int index = this.getData().indexOf(series);
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

    public TreeMap<String, String> getColors() {
        return colors;
    }

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

            seriesBox.getChildren().addAll(icon, new Label(colorData.getKey()));
            legend.getChildren().add(seriesBox);
        }

        return legend;
    }
}
