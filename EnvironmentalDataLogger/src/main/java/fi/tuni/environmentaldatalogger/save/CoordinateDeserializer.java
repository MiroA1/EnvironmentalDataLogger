package fi.tuni.environmentaldatalogger.save;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import fi.tuni.environmentaldatalogger.util.Coordinate;

import java.lang.reflect.Type;

public class CoordinateDeserializer implements JsonDeserializer<Coordinate> {

    @Override
    public Coordinate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        var lat = jsonElement.getAsJsonObject().get("lat");
        var lon = jsonElement.getAsJsonObject().get("lon");

        if (lat == null || lon == null) {
            return null;
        }

        double latitude = lat.getAsDouble();
        double longitude = lon.getAsDouble();

        return new Coordinate(latitude, longitude);
    }
}
