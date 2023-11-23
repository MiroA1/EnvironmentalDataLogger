package fi.tuni.environmentaldatalogger.visualization;

import fi.tuni.environmentaldatalogger.util.TimeUtils;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

public class TemporalLineChartBuilder {

    private final TreeMap<String, TreeMap<LocalDateTime, Double>> data;
    private final TreeMap<String, String> units;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private String title;

    public TemporalLineChartBuilder() {
        data = new TreeMap<>();
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        units = new TreeMap<>();

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
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusHours(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusHours(1))));
            xAxis.setTickUnit(21600);
        } else if (duration.toHours() > 25) {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusHours(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusHours(1))));
            xAxis.setTickUnit(14400);
        } else {
            xAxis.setLowerBound(TimeUtils.getEpochSecond(TimeUtils.getStartOfHour(startDate.minusHours(1))));
            xAxis.setUpperBound(TimeUtils.getEpochSecond(TimeUtils.getEndOfHour(endDate.plusHours(1))));
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

        return chart;
    }
}
