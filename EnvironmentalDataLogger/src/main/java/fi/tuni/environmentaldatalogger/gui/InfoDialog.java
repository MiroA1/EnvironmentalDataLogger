package fi.tuni.environmentaldatalogger.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import java.io.IOException;


/**
 * Class for presenting relevant information about the application
 */
public class InfoDialog extends Dialog<ButtonType>  {

    @FXML
    Label weatherApiLabel;
    @FXML
    Label airQualityApiLabel;
    @FXML
    Label iconLabel;

    /**
     * Constructor
     * @param weatherApiName the name of the used weather API
     * @param airQualityApiName the name of the used Air Quality API
     */
    public InfoDialog(String weatherApiName, String airQualityApiName) {
        super();
        this.setTitle("Information");

        // load the template
        FXMLLoader fxmlLoader = new FXMLLoader(ChartViewerElement.class
                                               .getResource("/fi/tuni/environmentaldatalogger/info_dialog.fxml"));
        fxmlLoader.setController(this);

        try {
            DialogPane dialogPane = fxmlLoader.load();
            this.setDialogPane(dialogPane);

            // set names for apis, other references
            weatherApiLabel.setText(weatherApiName);
            airQualityApiLabel.setText(airQualityApiName);
            iconLabel.setText("<placeholder>");

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

}
