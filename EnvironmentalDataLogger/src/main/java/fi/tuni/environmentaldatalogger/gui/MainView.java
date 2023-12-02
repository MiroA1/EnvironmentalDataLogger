package fi.tuni.environmentaldatalogger.gui;

import com.google.gson.Gson;
import fi.tuni.environmentaldatalogger.EnvironmentalDataLogger;
import fi.tuni.environmentaldatalogger.Presenter;
import fi.tuni.environmentaldatalogger.Settings;
import fi.tuni.environmentaldatalogger.apis.ApiCache;
import fi.tuni.environmentaldatalogger.save.Loadable;
import fi.tuni.environmentaldatalogger.save.Saveable;
import fi.tuni.environmentaldatalogger.util.ViewUtils;
import fi.tuni.environmentaldatalogger.apis.ApiException;
import fi.tuni.environmentaldatalogger.save.SaveLoad;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.util.Location;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;


import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class MainView implements Saveable, Loadable {

    public AnchorPane mapPane;
    public Label locationLabel;
    public HBox locationHBox;
    public AnchorPane chartsPane;
    public Button exitButton;
    public Button locationButton;
    public Button infoButton;
    public Button saveButton;
    public Button settingsButton;
    public Label temperatureLabel;
    public Label timeLabel;
    public Label dateLabel;
    public AnchorPane currentDataPane;
    public Label notificationLabel;
    public ImageView iconFrame;
    public static NotificationBar notificationBar;
    private static final String RESOURCE_PATH = "images/icons/";
    private static final String SAVE_BUTTON_IMG = "save.png";
    private static final String SETTINGS_BUTTON_IMG = "settings.png";
    private static MainView instance;

    public static MainView getInstance() {
        if (instance == null) {
            instance = new MainView();
        }
        return instance;
    }

    private static final Map<String,String> ICON_PATHS = Map.of(
            "snow","snowy.png",
            "rain", "rainy.png",
            "fog", "foggy.png",
            "wind", "windy.png",
            "cloudy", "cloudy.png",
            "partly-cloudy-day", "partly_cloud_day.png",
            "partly-cloudy-night", "partly_cloudy_night.png",
            "clear-day", "clear_day.png",
            "clear-night", "clear_night.png"
    );
    private static final String EXIT_RECTANGLE_PATH = "M36.501,33c-0.552,0-1,0.447-1,1v20h-32V2h32v20c0,0.553," +
            "0.448,1,1,1s1-0.447,1-1V1c0-0.553-0.448-1-1-1h-34c-0.552,0-1,0.447-1,1v54c0,0.553,0.448,1,1,1h34c0.552" +
            ",0,1-0.447,1-1V34C37.501,33.447,37.053,33,36.501,33z";
    private static final String EXIT_ARROW_PATH = "M54.424,28.382c0.101-0.244,0.101-0.519,0-0.764c-0.051-0.123-" +
            "0.125-0.234-0.217-0.327L42.208,15.293c-0.391-0.391-1.023-0.391-1.414,0s-0.391,1.023,0,1.414L51.087," +
            "27H20.501c-0.552,0-1,0.447-1,1s0.448,1,1,1h30.586L40.794,39.293c-0.391,0.391-0.391,1.023,0," +
            "1.414C40.989,40.902,41.245,41,41.501,41s0.512-0.098,0.707-0.293l11.999-11.999C54.299,28.616," +
            "54.373,28.505,54.424,28.382z";

    private static final String INFO_CIRCLE_PATH = "M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8" +
            " 8 0 0 0 0 16z";
    private static final String INFO_I_PATH = "m8.93 6.588-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 " +
            "3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275" +
            " 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z";

    private static final Location DEFAULT_LOCATION = new Location(
            "Tampere", "FI", new Coordinate(61.4978, 23.7610));
    private static Location currentLocation = DEFAULT_LOCATION;
    private ChartGrid chartGrid;

    Timer temperatureTimer = new Timer(true);
    Timer iconTimer = new Timer(true);
    Timer clockTimer = new Timer(true);

    public void initialize(){

        initLocationButton();
        initExitButton();
        initInfoButton();
        initSaveButton();
        initSettingButton();

        initNotificationBar();

        initChartGrid();

        if (!SaveLoad.load(this, "user_save.json")) {
            try {
                currentLocation = Location.fromIP();
                locationChanged();
            } catch (IOException ignored) {}
        }

        chartGrid.loadAllCharts();

        iconTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateWeatherIcon();
            }
        },0,600000);

        // update temperature label every 10 minutes
        temperatureTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTemperatureLabel();
                updateCurrentDataPane();
            }
            }, 600000, 600000);

        // update time and date labels every second
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        }, 0, 1000);

    }

    /**
     *  Initializes a notification bar
     */
    private void initNotificationBar() {
        notificationBar = new NotificationBar(notificationLabel);
    }

    /**
     * Returns the current location as coordinates.
     * @return current location as coordinates
     */
    public static Coordinate getCurrentCoords() {
        return currentLocation.getCoordinates();
    }

    /**
     * Launches a dialog for selecting a location.
     */
    private void launchCoordinateDialog() {

        CoordinateDialog dialog = new CoordinateDialog();

        dialog.showAndWait().ifPresent(result -> {
            if (result == null) {
                System.out.println("No location selected");
                return;
            }

            currentLocation = result;
            locationChanged();
        });
    }

    /**
     * Updates the view when the location is changed.
     */
    private void locationChanged() {
        locationLabel.setText(currentLocation.toString());
        updateCurrentDataPane();
        updateTemperatureLabel();
        updateWeatherIcon();
    }

    private void launchInfoDialog() {
        try {
            // Fetch information about the APIs
            ArrayList<String> weatherApiInfo = Presenter.getInstance().getWeatherApiInformation();
            ArrayList<String> airQualityApiInfo = Presenter.getInstance().getAirQualityApiInformation();

            InfoDialog infoDialog = new InfoDialog(weatherApiInfo, airQualityApiInfo);
            infoDialog.showAndWait();

        } catch (RuntimeException e) {
            notificationBar.pushAlertNotification("Error in showing application info");
        }
    }

    /**
     * Initializes the location button (and label).
     */
    private void initLocationButton() {

        locationButton.setOnAction(actionEvent -> launchCoordinateDialog());
        locationLabel.setOnMouseClicked(mouseEvent -> launchCoordinateDialog());
        locationLabel.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                locationLabel.setTextFill(Color.GRAY);
            } else {
                locationLabel.setTextFill(Color.BLACK);
            }
        });
    }

    /**
     * Initializes the exit button.
     */
    private void initExitButton() {
        exitButton.setOnAction(actionEvent -> {
            Platform.exit();
        });


        Group svg = new Group(
                ViewUtils.createPath(EXIT_ARROW_PATH, "black", "gray"),
                ViewUtils.createPath(EXIT_RECTANGLE_PATH, "black", "black")
        );

        Bounds bounds = svg.getBoundsInParent();
        double scale = Math.min(32 / bounds.getWidth(), 32 / bounds.getHeight());
        svg.setScaleX(scale);
        svg.setScaleY(scale);

        svg.setTranslateX(4.5);

        exitButton.setGraphic(svg);
        exitButton.setMaxSize(48, 48);
        exitButton.setMinSize(48, 48);
        exitButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    /**
     * Initializes the save button
     */
    private void initSaveButton() {
        URL _url = EnvironmentalDataLogger.class.getResource(RESOURCE_PATH + SAVE_BUTTON_IMG);
        Image img = new Image(Objects.requireNonNull(_url).toExternalForm());
        ImageView imgFrame = new ImageView(img);
        imgFrame.setFitWidth(32);
        imgFrame.setFitHeight(32);
        saveButton.setGraphic(imgFrame);

        saveButton.setMaxSize(48, 48);
        saveButton.setMinSize(48, 48);
        saveButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        saveButton.setOnAction(actionEvent -> {
            SaveLoad.save(this, "user_save.json");
        });
    }

    /**
     * Initializes the settings button
     */
    private void initSettingButton() {
        URL _url = EnvironmentalDataLogger.class.getResource(RESOURCE_PATH + SETTINGS_BUTTON_IMG);
        Image img = new Image(Objects.requireNonNull(_url).toExternalForm());
        ImageView imgFrame = new ImageView(img);
        imgFrame.setFitWidth(32);
        imgFrame.setFitHeight(32);
        settingsButton.setGraphic(imgFrame);

        settingsButton.setMaxSize(48, 48);
        settingsButton.setMinSize(48, 48);
        settingsButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        settingsButton.setOnAction(actionEvent -> {
            SettingsDialog dialog = new SettingsDialog();
            dialog.showDialog();
        });

    }

    /**
     * Initializes the info button.
     */
    private void initInfoButton() {

        Group svg = new Group(
                ViewUtils.createPath(INFO_CIRCLE_PATH, "black", "black"),
                ViewUtils.createPath(INFO_I_PATH, "black", "gray")
        );

        Bounds bounds = svg.getBoundsInParent();
        double scale = Math.min(32 / bounds.getWidth(), 32 / bounds.getHeight());
        svg.setScaleX(scale);
        svg.setScaleY(scale);

        infoButton.setGraphic(svg);
        infoButton.setMaxSize(48, 48);
        infoButton.setMinSize(48, 48);
        infoButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        infoButton.setOnAction(actionEvent -> launchInfoDialog());
    }

    /**
     * Initializes the Chart pane view
     */
    private void initChartGrid(){
        try {
            var grid = new ChartGrid();
            this.chartGrid = grid;
            chartsPane.getChildren().add(grid);

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
        } catch ( IOException e) {
            MainView.notificationBar.pushAlertNotification("Failed to initialize charts");
        }
    }


    /**
     * Updates the temperature label with current data.
     */
    private void updateTemperatureLabel() {
        try {
            String str = Presenter.getInstance().getCurrentData(new ArrayList<>(List.of("temperature")),
                            getCurrentCoords()).get("Temperature");
            Platform.runLater(() -> temperatureLabel.setText(str));
        } catch (ApiException e) {
            notificationBar.pushAlertNotification(e.getMessage());
        }
    }



    /**
     * Updates the weather icon
     */
    private void updateWeatherIcon() {

        String weatherStatus;
        try {
            weatherStatus = Presenter.getInstance().getWeatherStatusIcon(getCurrentCoords());
            URL _url = EnvironmentalDataLogger.class.getResource("images/icons/"+ICON_PATHS.get(weatherStatus));
            Image img = new Image(Objects.requireNonNull(_url).toExternalForm());
            Platform.runLater(() -> iconFrame.setImage(img));

        } catch (ApiException e) {
            notificationBar.pushAlertNotification("Unable to fetch weather status icon");
        } catch (Exception e) {
            notificationBar.pushAlertNotification("Unable to load weather status icon");
        }
    }


    /**
     * Updates the time and date labels.
     */
    private void updateTime() {
        // TODO: change to time of location ?
        // clock format to hh:mm
        Platform.runLater(() -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            timeLabel.setText(now.format(clockFormatter));
            dateLabel.setText(now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +
                    " " + now.format(dateFormatter));
        });
    }

    private void updateCurrentDataPane() {
        Platform.runLater(() -> {
            currentDataPane.getChildren().clear();
            currentDataPane.getChildren().add(CurrentDataPane.getInstance());
        });
    }

    @Override
    public boolean loadFromJson(String json) {

        Gson gson = new Gson();
        SaveData saveData = gson.fromJson(json, SaveData.class);

        if (saveData == null) {
            return false;
        }

        if (saveData.settings != null) {
            Settings.getInstance().loadFromJson(saveData.settings);
        }

        if (saveData.currentLocation != null) {
            currentLocation = saveData.currentLocation;
            locationChanged();
        }

        if (saveData.grid == null) {
            return false;
        }

        return chartGrid.loadFromJson(saveData.grid);
    }

    @Override
    public String getJson() {
        SaveData saveData = new SaveData(chartGrid.getJson(), Settings.getInstance().getJson(), currentLocation);
        Gson gson = new Gson();

        return gson.toJson(saveData);
    }

    private record SaveData(String grid, String settings, Location currentLocation) {
    }
}
