package fi.tuni.environmentaldatalogger;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

public interface DataExtractor {

    /**
     * Returns a list of valid parameters (e.g. temperature, humidity).
     *
     * @return
     */
    ArrayList<String> getValidParameters();

    /**
     * Returns the time range available for data of given parameter.
     *
     * @param param
     * @return
     */
    Pair<Date, Date> getValidDataRange(String param);

    /**
     * Returns data for a given parameter within specified time range.
     *
     * @param param
     * @param range
     * @return
     */
    TreeMap<Date, Double> getData(String param, Pair<Date, Date> range, Coordinate coordinates);

    /**
     * Returns all available data for a given parameter.
     *
     * @param param
     * @return
     */
    TreeMap<Date, Double> getData(String param, Coordinate coordinates);


}
