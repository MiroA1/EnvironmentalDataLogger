package fi.tuni.environmentaldatalogger.gui;

import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.util.Location;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class CoordinateDialog extends Dialog<Location> {

    public CoordinateDialog() {
        super();

        this.setTitle("Coordinate Input");

        TextField locationNameField = new TextField();

        TextField latitudeField = new TextField();
        TextField longitudeField = new TextField();

        GridPane grid = new GridPane();
        grid.add(new Label("Location name:"), 0, 0);
        grid.add(locationNameField, 1, 0);

        Label orLabel = new Label("---------------------- or ----------------------");
        grid.add(orLabel, 0, 1);
        GridPane.setColumnSpan(orLabel, 2);

        grid.add(new Label("Latitude:"), 0, 2);
        grid.add(latitudeField, 1, 2);
        grid.add(new Label("Longitude:"), 0, 3);
        grid.add(longitudeField, 1, 3);

        grid.setVgap(5);
        grid.setHgap(5);

        this.getDialogPane().setContent(grid);

        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    String locationName = locationNameField.getText();
                    if (!locationName.equals("")) {
                        return Location.fromPlaceName(locationName);
                    }
                } catch (IllegalArgumentException | IOException e) {
                    return null;
                }

                try {
                    double latitude = Double.parseDouble(latitudeField.getText());
                    double longitude = Double.parseDouble(longitudeField.getText());
                    return Location.fromCoordinates(new Coordinate(latitude, longitude));
                } catch (IllegalArgumentException | IOException e) {
                    return null;
                }
            }
            return null;
        });
    }
}
