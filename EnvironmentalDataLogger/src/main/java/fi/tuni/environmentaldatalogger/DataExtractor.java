package fi.tuni.environmentaldatalogger;

import javafx.util.Pair;

import java.time.LocalDateTime;
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
    Pair<LocalDateTime, LocalDateTime> getValidDataRange(String param);

    /**
     * Returns data for a given parameter within specified time range.
     *
     * @param param
     * @param range
     * @return
     */
    TreeMap<LocalDateTime, Double> getData(String param, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates);

    /**
     * Returns all available data for a given parameter.
     *
     * @param param
     * @return
     */
    TreeMap<LocalDateTime, Double> getData(String param, Coordinate coordinates);

    /**
     * Returns data for a givens parameters within specified time range.
     *
     * @param params
     * @param range
     * @param coordinates
     * @return
     */
    TreeMap<LocalDateTime, Double> getData(ArrayList<String> params, Pair<LocalDateTime, LocalDateTime> range, Coordinate coordinates);

    /**
     * Returns real-time data of given parameters
     *
     * @param params
     * @param coordinates
     * @return map(param, value)
     */
    TreeMap<String, Double> getCurrentData(ArrayList<String> params, Coordinate coordinates);

    /**
     * Returns the unit used for given parameter (e.g. "°C")
     *
     * @param param
     * @return
     */
    String getUnit(String param);
}
