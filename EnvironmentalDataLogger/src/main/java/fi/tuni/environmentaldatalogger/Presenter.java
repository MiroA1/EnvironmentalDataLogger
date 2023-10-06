package fi.tuni.environmentaldatalogger;

import javafx.scene.chart.LineChart;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;

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
    public LineChart<Date, Double> getDataAsLineChart(ArrayList<String> params, Pair<Date, Date> range) {
        return null;
    }

    // TODO: possibly other chart types
}
