package fi.tuni.environmentaldatalogger.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Class for presenting relevant information about the application.
 * Shown when info button is clicked from the main view.
 */
public class InfoDialog extends Dialog<ButtonType>  {

    @FXML
    Label weatherApiInfoLabel;
    @FXML
    Label airQualityApiInfoLabel;
    @FXML
    Label iconInfoLabel;

    /**
     * Constructor
     * @param weatherApiInfo the weather API related information. Index 0 reserved for API name.
     * @param airQualityApiInfo the air quality API related information. Index 0 reserved for API name.
     */
    public InfoDialog(ArrayList<String> weatherApiInfo, ArrayList<String> airQualityApiInfo) {
        super();
        this.setTitle("Information");

        // load the template
        FXMLLoader fxmlLoader = new FXMLLoader(ChartViewerElement.class
                                               .getResource("/fi/tuni/environmentaldatalogger/info_dialog.fxml"));
        fxmlLoader.setController(this);

        // lines for icon info defined separately here
        ArrayList<String> iconInfo = new ArrayList<>(Arrays.asList("Author: Laura Reen", "License: CC BY-NC 3.0",
                                                                    "https://www.iconfinder.com/laurareen"));
        try {
            DialogPane dialogPane = fxmlLoader.load();
            this.setDialogPane(dialogPane);

            // set names for apis, other references
            addInformation(weatherApiInfo,weatherApiInfoLabel);
            addInformation(airQualityApiInfo,airQualityApiInfoLabel);
            addInformation(iconInfo,iconInfoLabel);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // simply return null, close the window
        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.CLOSE) {
                return null;
            }
            return null;
        });
    }

    /**
     * Add information to label elements to info dialogue
     * @param lines array of string elements, relevant information
     * @param infoLabel label placed in the dialogue
     */
    private void addInformation(ArrayList<String> lines, Label infoLabel){
        StringBuilder infoText = new StringBuilder();
        for (String line : lines){
            infoText.append(line).append("\n");
        }
        infoLabel.setText(infoText.toString());
    }
}
