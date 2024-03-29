package fi.tuni.environmentaldatalogger;

import fi.tuni.environmentaldatalogger.gui.MainView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Application to showcase graphical representations of weather and air quality data.
 */
public class EnvironmentalDataLogger extends Application implements Initializable {

    // needed to fix a very specific bug with double y-axis line charts
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EnvironmentalDataLogger.class.getResource("gui_template.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("SkyCast");
        stage.setMinHeight(680);
        stage.setMinWidth(992);

        primaryStage = stage;

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        MainView.getInstance().initialize();

    }
}
