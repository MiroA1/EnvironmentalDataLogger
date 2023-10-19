package fi.tuni.environmentaldatalogger;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class CoordinateDialog extends Dialog<Coordinate> {

    public CoordinateDialog() {
        super();
        this.setTitle("Coordinate Input");

        TextField latitudeField = new TextField();
        TextField longitudeField = new TextField();

        GridPane grid = new GridPane();
        grid.add(new Label("Latitude:"), 0, 0);
        grid.add(latitudeField, 1, 0);
        grid.add(new Label("Longitude:"), 0, 1);
        grid.add(longitudeField, 1, 1);

        grid.setVgap(5);
        grid.setHgap(5);

        this.getDialogPane().setContent(grid);

        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    double latitude = Double.parseDouble(latitudeField.getText());
                    double longitude = Double.parseDouble(longitudeField.getText());
                    return new Coordinate(latitude, longitude);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
            return null;
        });
    }
}
