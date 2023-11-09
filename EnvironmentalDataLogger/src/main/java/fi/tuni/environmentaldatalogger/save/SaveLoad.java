package fi.tuni.environmentaldatalogger.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.tuni.environmentaldatalogger.gui.ChartGrid;

import java.io.*;

public class SaveLoad {

    public static void save(Saveable saveable, String filename) {
        String folderName = "saves";
        File folder = new File(folderName);
        folder.mkdir();

        String filepath = folder.getAbsolutePath() + "/" + filename;

        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(saveable.getJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean load(Saveable saveable, String filename) {

        String filePath = "saves/" + filename;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator()); // Add line separator if needed
            }

            String json = content.toString();
            return saveable.loadFromJson(json);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
