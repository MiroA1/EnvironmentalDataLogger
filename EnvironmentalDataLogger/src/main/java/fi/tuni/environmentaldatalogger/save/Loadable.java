package fi.tuni.environmentaldatalogger.save;

/**
 * An interface for classes that can be loaded from a JSON save file.
 */
public interface Loadable {
    /**
     * Loads the object from a JSON string (edits existing instance).
     * @param json JSON string
     * @return true if successful, false otherwise
     */
    boolean loadFromJson(String json);
}
