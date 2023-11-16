package fi.tuni.environmentaldatalogger.apis;

import fi.tuni.environmentaldatalogger.util.Coordinate;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
     * @param params
     * @return
     */
    Pair<LocalDateTime, LocalDateTime> getValidDataRange(ArrayList<String> params);

    /**
     * Returns data for a givens parameters within specified time range.
     *
     * @param params
     * @param range
     * @param coordinates
     * @return
     */
    TreeMap<String, TreeMap<LocalDateTime, Double>> getData(ArrayList<String> params, Pair<LocalDateTime,
            LocalDateTime> range, Coordinate coordinates) throws ApiException;

    /**
     * Returns real-time data of given parameters
     *
     * @param params
     * @param coordinates
     * @return map(param, value)
     */
    TreeMap<String, Double> getCurrentData(ArrayList<String> params, Coordinate coordinates) throws ApiException;

    /**
     * Returns the unit used for given parameter (e.g. "°C")
     *
     * @param param
     * @return
     */
    String getUnit(String param);
}
