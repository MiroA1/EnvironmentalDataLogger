package fi.tuni.environmentaldatalogger.gui;

import fi.tuni.environmentaldatalogger.Presenter;
import fi.tuni.environmentaldatalogger.apis.AirQualityDataExtractor;
import fi.tuni.environmentaldatalogger.apis.ApiException;
import fi.tuni.environmentaldatalogger.save.SaveLoad;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.util.Location;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class MainView {


    public AnchorPane mapPane;
    public Label locationLabel;
    public HBox locationHBox;
    public AnchorPane chartsPane;
    public Button exitButton;
    public Button locationButton;
    public Button infoButton;
    public Label temperatureLabel;
    public Label timeLabel;
    public Label dateLabel;
    public AnchorPane currentDataPane;
    public Label notificationLabel;
    public static NotificationBar notificationBar;

    private static MainView instance;
    public static MainView getInstance() {
        if (instance == null) {
            instance = new MainView();
        }
        return instance;
    }

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

    private static final Location DEFAULT_LOCATION = new Location("Tampere", "FI", new Coordinate(61.4978, 23.7610));
    private static Location currentLocation = DEFAULT_LOCATION;
    private ChartGrid chartGrid;

    Timer temperatureTimer = new Timer(true);
    Timer clockTimer = new Timer(true);

    public void initialize(){
        locationButton.setOnAction(actionEvent -> launchCoordinateDialog());
        locationLabel.setText(currentLocation.toString());

        initExitButton();
        initInfoButton();
        initNotificationBar();

        // TODO: add an own init function for the chart grid, to improve readability
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
        } catch ( IOException e) {
            MainView.notificationBar.pushAlertNotification("Failed to initialize charts");
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

        try {
            currentDataPane.getChildren().add(Presenter.getInstance().getDataAsPieChart(AirQualityDataExtractor.getInstance().getValidParameters(), LocalDateTime.now().minusDays(5), getCurrentCoords()));
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     *  Initializes a notification bar
     */
    private void initNotificationBar() {
        notificationBar = new NotificationBar(notificationLabel);
    }

    public static Coordinate getCurrentCoords() {
        return currentLocation.getCoordinates();
    }

    private static SVGPath createPath(String d, String fill, String hoverFill) {
        SVGPath path = new SVGPath();
        path.getStyleClass().add("svg");
        path.setContent(d);
        path.setStyle("-fill:" + fill + ";-hover-fill:" + hoverFill + ';');
        return path;
    }

    private void launchCoordinateDialog() {

        CoordinateDialog dialog = new CoordinateDialog();

        dialog.showAndWait().ifPresent(result -> {
            try {
                currentLocation = Location.fromCoordinates(result);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            locationLabel.setText(currentLocation.toString());
        });
    }

    private void initExitButton() {
        exitButton.setOnAction(actionEvent -> {
            Platform.exit();
        });


        Group svg = new Group(
                createPath(EXIT_ARROW_PATH, "black", "gray"),
                createPath(EXIT_RECTANGLE_PATH, "black", "black")
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

    private void initInfoButton() {

        // Uncomment to test the notification bar!
        //    infoButton.setOnAction(actionEvent -> {
        //        notificationBar.pushAlertNotification("Info button pressed!");
        //    });

        Group svg = new Group(
                createPath(INFO_CIRCLE_PATH, "black", "black"),
                createPath(INFO_I_PATH, "black", "gray")
        );

        Bounds bounds = svg.getBoundsInParent();
        double scale = Math.min(32 / bounds.getWidth(), 32 / bounds.getHeight());
        svg.setScaleX(scale);
        svg.setScaleY(scale);

        infoButton.setGraphic(svg);
        infoButton.setMaxSize(48, 48);
        infoButton.setMinSize(48, 48);
        infoButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    private void updateTemperatureLabel() {
        try {
            String str = Presenter.getInstance().getCurrentData(new ArrayList<>(List.of("temperature")),
                            getCurrentCoords()).get("temperature");
            Platform.runLater(() -> temperatureLabel.setText(str));
        } catch (ApiException e) {
            notificationBar.pushAlertNotification(e.getMessage());
        }
    }

    private void updateTime() {
        // TODO: change to time of location
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

}
