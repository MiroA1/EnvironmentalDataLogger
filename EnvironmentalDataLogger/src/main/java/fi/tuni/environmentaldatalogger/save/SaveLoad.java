package fi.tuni.environmentaldatalogger.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.tuni.environmentaldatalogger.gui.ChartGrid;

import java.io.*;

/**
 * A class for saving and loading objects to/from JSON files.
 */
public class SaveLoad {

    private final static String saveFolderName = "saves";

    /**
     * Saves a Saveable object to a JSON file.
     * @param saveable object to save
     * @param filename name of the file
     */
    public static void save(Saveable saveable, String filename) {

        File folder = new File(saveFolderName);

        if (folder.mkdir()) {
            System.out.println("Created folder " + saveFolderName);
        }

        String filepath = folder.getAbsolutePath() + "/" + filename;

        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(saveable.getJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a Loadable object from a JSON file.
     * @param loadable object to load
     * @param filename name of the file
     * @return true if successful, false otherwise
     */
    public static boolean load(Loadable loadable, String filename) {

        String filePath = saveFolderName + "/" + filename;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator()); // Add line separator if needed
            }

            String json = content.toString();
            return loadable.loadFromJson(json);

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Deletes all save files.
     */
    public static void wipeSaves() {
        File folder = new File(saveFolderName);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }
}
