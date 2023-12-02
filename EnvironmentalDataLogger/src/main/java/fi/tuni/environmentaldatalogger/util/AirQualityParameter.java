package fi.tuni.environmentaldatalogger.util;

/**
 * A class for handling air quality parameters.
 */
public enum AirQualityParameter {
    // Currently used air quality parameters, open-meteo
    SO2("SO2","Sulphur dioxide", "sulphur_dioxide", "µg/m³"),
    NO2("NO2","Nitrogen dioxide", "nitrogen_dioxide", "µg/m³"),
    O3("O3","Ozone", "ozone", "µg/m³"),
    CO("CO","Carbon monoxide", "carbon_monoxide", "µg/m³"),
    PM10("PM10","Particulate matter, diameter 10µm", "pm10", "µg/m³"),
    PM2_5("PM2.5","Particulate matter, diameter 2.5µm", "pm2_5", "µg/m³"),
    AIR_QUALITY_INDEX("AQI","European Air Quality Index (AQI)",
            "european_aqi", "");

    private final String abbreviation;
    private final String name;
    private final String queryWord;
    private final String unit;

    /**
     * Constructor.
     * @param abbreviation abbreviation of the parameter
     * @param name name of the parameter
     * @param queryWord query word used in the API
     * @param unit unit of the parameter
     */
    AirQualityParameter(String abbreviation, String name, String queryWord, String unit) {
        this.abbreviation = abbreviation;
        this.name = name;
        this.queryWord = queryWord;
        this.unit = unit;
    }

    /**
     * Returns the abbreviation of the parameter.
     * @return abbreviation
     */
    public String getAbbreviation(){
        return abbreviation;
    }

    /**
     * Returns the name of the parameter.
     * @return name
     */
    public String getName(){
        return name;
    }

    /**
     * Returns the query word used in the API.
     * @return query word
     */
    public String getQueryWord(){
        return queryWord;
    }

    /**
     * Returns the unit of the parameter.
     * @return unit
     */
    public String getUnit(){
        return unit;
    }

    /**
     * Returns the parameter with the given name.
     * @param name name of the parameter
     * @return parameter with the given name
     */
    public static AirQualityParameter fromName(String name) {
        for (AirQualityParameter parameter : values()) {
            if (parameter.name.equals(name)) {
                return parameter;
            }
        }
        return null; // name doesn't match any enum constant
    }

    /**
     * Returns the parameter with the given query word.
     * @param queryWord query word of the parameter
     * @return parameter with the given query word
     */
    public static AirQualityParameter fromQueryWord(String queryWord) {

        for (AirQualityParameter parameter : values()) {
            if (parameter.queryWord.equals(queryWord)) {
                return parameter;
            }
        }

        throw new IllegalArgumentException("No enum constant with query word " + queryWord);
    }
}
