package fi.tuni.environmentaldatalogger.gui;

import fi.tuni.environmentaldatalogger.Settings;
import fi.tuni.environmentaldatalogger.save.SaveLoad;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class SettingsDialog extends Dialog<Boolean> {

    private final CheckBox enableDoubleYAxisCheckBox;
    private final Button resetButton;

    public SettingsDialog() {
        setTitle("Settings");

        // Create UI components
        enableDoubleYAxisCheckBox = new CheckBox("Enable double Y-axis for line charts");
        enableDoubleYAxisCheckBox.setSelected(Settings.getInstance().useDoubleAxis.get());

        resetButton = new Button("Restore default state");

        resetButton.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Restore default state");
            alert.setHeaderText("Restore default state of the program?");
            alert.setContentText("All saved data will be lost.");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    SaveLoad.wipeSaves();
                }
            });
        });

        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

        VBox vbox = new VBox(20);
        vbox.getChildren().addAll(enableDoubleYAxisCheckBox, resetButton);
        getDialogPane().setContent(vbox);

        setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Settings.getInstance().useDoubleAxis.set(enableDoubleYAxisCheckBox.isSelected());
            }
            return true;
        });
    }

    // Method to show the dialog and get the result
    public Boolean showDialog() {
        return showAndWait().orElse(false);
    }
}
