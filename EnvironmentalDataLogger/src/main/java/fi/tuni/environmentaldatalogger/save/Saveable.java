package fi.tuni.environmentaldatalogger.save;

/**
 * An interface for classes that can be saved to a JSON save file.
 */
public interface Saveable {
    /**
     * Returns a JSON string that can be used to save the object.
     * @return JSON string
     */
    String getJson();
}
