package fi.tuni.environmentaldatalogger.util;

/**
 * Possible util enum class?
 */
public enum AirQualityParameter {
    // Currently used air quality parameters
    SO2("SO2","Sulphur dioxide", "SO2_PT1H_avg"),
    NO("NO","Nitrogen oxide", "NO_PT1H_avg"),
    NO2("NO2","Nitrogen dioxide", "NO2_PT1H_avg"),
    O3("O3","Carbon dioxide", "O3_PT1H_avg"),
    TRSC("TRSC","Nitrogen oxide", "TRSC_PT1H_avg"),
    CO("CO","Nitrogen dioxide", "CO_PT1H_avg"),
    PM10("PM10","Carbon dioxide", "PM10_PT1H_avg"),
    PM2_5("PM2.5","Nitrogen oxide", "PM25_PT1H_avg"),
    AIR_QUALITY_INDEX("AQI","Nitrogen dioxide",
            "AQINDEX_PT1H_avg");

    private final String abbreviation;
    private final String name;
    private final String queryWord;

    AirQualityParameter(String abbreviation, String name, String queryWord) {
        this.abbreviation = abbreviation;
        this.name = name;
        this.queryWord = queryWord;
    }
    public String getAbbreviation(){
        return abbreviation;
    }
    public String getName(){
        return name;
    }
    public String getQueryWord(){
        return queryWord;
    }

    public static AirQualityParameter fromName(String name) {
        for (AirQualityParameter parameter : values()) {
            if (parameter.name.equals(name)) {
                return parameter;
            }
        }
        return null; // name doesn't match any enum constant
    }
}
