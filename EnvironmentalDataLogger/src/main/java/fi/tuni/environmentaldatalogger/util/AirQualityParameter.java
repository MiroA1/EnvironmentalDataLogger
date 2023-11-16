package fi.tuni.environmentaldatalogger.util;

/**
 * Possible util enum class?
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

    AirQualityParameter(String abbreviation, String name, String queryWord, String unit) {
        this.abbreviation = abbreviation;
        this.name = name;
        this.queryWord = queryWord;
        this.unit = unit;
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
    public String getUnit(){
        return unit;
    }

    public static AirQualityParameter fromName(String name) {
        for (AirQualityParameter parameter : values()) {
            if (parameter.name.equals(name)) {
                return parameter;
            }
        }
        return null; // name doesn't match any enum constant
    }

    public static AirQualityParameter fromQueryWord(String queryWord) {

        for (AirQualityParameter parameter : values()) {
            if (parameter.queryWord.equals(queryWord)) {
                return parameter;
            }
        }

        throw new IllegalArgumentException("No enum constant with query word " + queryWord);
    }
}



/*
    // Currently used air quality parameters, fmi
    SO2("SO2","Sulphur dioxide", "SO2_PT1H_avg"),
    NO("NO","Nitrogen oxide", "NO_PT1H_avg"),
    NO2("NO2","Nitrogen dioxide", "NO2_PT1H_avg"),
    O3("O3","Ozone", "O3_PT1H_avg"),
    TRSC("TRSC","Total Reduced Sulphur", "TRSC_PT1H_avg"),
    CO("CO","Carbon monoxide", "CO_PT1H_avg"),
    PM10("PM10","Particulate matter, diameter 10µm", "PM10_PT1H_avg"),
    PM2_5("PM2.5","Particulate matter, diameter 2.5µm", "PM25_PT1H_avg"),
    AIR_QUALITY_INDEX("AQI","European Air Quality Index (AQI)",
            "AQINDEX_PT1H_avg");
*/