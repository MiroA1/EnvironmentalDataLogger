package fi.tuni.environmentaldatalogger.save;

public interface Saveable {
    String getJson();
    boolean loadFromJson(String json);
}
