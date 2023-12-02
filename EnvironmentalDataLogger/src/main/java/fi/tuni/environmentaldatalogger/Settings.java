package fi.tuni.environmentaldatalogger;

import com.google.gson.Gson;
import fi.tuni.environmentaldatalogger.save.Loadable;
import fi.tuni.environmentaldatalogger.save.Saveable;

import java.util.concurrent.atomic.AtomicBoolean;

public class Settings implements Saveable, Loadable {

    private static Settings instance = null;

    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public AtomicBoolean useDoubleAxis = new AtomicBoolean(true);

    @Override
    public boolean loadFromJson(String json) {

        Gson gson = new Gson();
        SaveData loadedSettings = gson.fromJson(json, SaveData.class);

        if (loadedSettings == null) {
            return false;
        }

        useDoubleAxis.set(loadedSettings.useDoubleAxis);
        return true;
    }

    @Override
    public String getJson() {
        Gson gson = new Gson();
        SaveData saveData = new SaveData(useDoubleAxis.get());
        return gson.toJson(saveData);
    }

    private record SaveData(boolean useDoubleAxis) {
    }
}
