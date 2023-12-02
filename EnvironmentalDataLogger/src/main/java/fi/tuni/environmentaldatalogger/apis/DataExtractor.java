package fi.tuni.environmentaldatalogger.apis;

import fi.tuni.environmentaldatalogger.util.Coordinate;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * An interface for extracting location-dependent temporal data from APIs.
 */
public interface DataExtractor {


    /**
     * Returns a list of valid parameters (e.g. temperature, humidity).
     *
     * @return list of valid parameters
     */
    ArrayList<String> getValidParameters();

    /**
     * Returns the time range available for data of given parameters.
     *
     * @param params list of parameters
     * @return time range available for data of given parameters
     */
    Pair<LocalDateTime, LocalDateTime> getValidDataRange(ArrayList<String> params);

    /**
     * Returns data for given parameters within specified time range.
     *
     * @param params list of parameters
     * @param range time range fro data
     * @param coordinates coordinates of the location to fetch data for
     * @return map(param, map(time, value))
     * @throws ApiException, if fetching data fails
     */
    TreeMap<String, TreeMap<LocalDateTime, Double>> getData(ArrayList<String> params, Pair<LocalDateTime,
            LocalDateTime> range, Coordinate coordinates) throws ApiException;

    /**
     * Returns real-time data of given parameters
     *
     * @param params list of parameters
     * @param coordinates coordinates of the location to fetch data for
     * @return map(param, value)
     * @throws ApiException, if fetching data fails
     */
    TreeMap<String, Double> getCurrentData(ArrayList<String> params, Coordinate coordinates) throws ApiException;

    /**
     * Returns the unit used for given parameter (e.g. "°C")
     *
     * @param param parameter
     * @return unit used for given parameter
     */
    String getUnit(String param);

    /**
     * Returns API name
     * @return String name of used API
     * /
    String getApiName();

    /**
     * Returns API base URL
     * @return String, URL of the used API
     */
    String getApiUrl();


}
