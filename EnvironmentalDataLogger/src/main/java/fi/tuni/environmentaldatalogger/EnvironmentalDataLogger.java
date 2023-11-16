package fi.tuni.environmentaldatalogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.tuni.environmentaldatalogger.apis.AirQualityDataExtractor;
import fi.tuni.environmentaldatalogger.gui.ChartGrid;
import fi.tuni.environmentaldatalogger.gui.CoordinateDialog;
import fi.tuni.environmentaldatalogger.gui.NotificationBar;
import fi.tuni.environmentaldatalogger.save.SaveLoad;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.util.Location;
import fi.tuni.environmentaldatalogger.gui.MainView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EnvironmentalDataLogger extends Application implements Initializable {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EnvironmentalDataLogger.class.getResource("gui_template.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Environmental Data Logger");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        locationButton.setOnAction(actionEvent -> launchCoordinateDialog());
        locationLabel.setText(currentLocation.toString());

        initExitButton();
        initInfoButton();
        initNotificationBar();

        try {
            var grid = new ChartGrid();
            this.chartGrid = grid;
            chartsPane.getChildren().add(grid);

            SaveLoad.load(grid, "save1.json");

            Button test = new Button("View");
            test.setOnAction(actionEvent -> {
                if (test.getText().equals("View")) {
                    grid.viewMode();
                    test.setText("Edit");
                } else {
                    grid.editMode();
                    test.setText("View");
                }
            });

            AnchorPane.setTopAnchor(test, 5.0);
            AnchorPane.setRightAnchor(test, 5.0);

            chartsPane.getChildren().add(test);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // update temperature label every 10 minutes
        temperatureTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTemperatureLabel();
            }
        }, 0, 600000);

        // update time and date labels every second
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        }, 0, 1000);

        currentDataPane.getChildren().add(Presenter.getInstance().getDataAsPieChart(AirQualityDataExtractor.getInstance().getValidParameters(), LocalDateTime.now().minusDays(5), getCurrentCoords()));

        MainView.getInstance().initialize();

    }
}
