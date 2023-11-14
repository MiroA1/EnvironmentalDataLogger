package fi.tuni.environmentaldatalogger.save;

public interface Loadable {
    boolean loadFromJson(String json);
}
