package fi.tuni.environmentaldatalogger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import java.time.LocalDateTime;

import java.util.*;
public class EnvironmentalDataLogger extends Application implements Initializable {
    
    public AnchorPane mapPane;
    public Label locationLabel;
    public HBox locationHBox;
    public AnchorPane chartsPane;
    public Button exitButton;
    public Button locationButton;
    public Button infoButton;

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

    private static final Coordinate DEFAULT_LOCATION = new Coordinate(61.4978, 23.7610);
    private static Coordinate currentLocation = DEFAULT_LOCATION;

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

        try {
            var grid = new ChartGrid();
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Coordinate getCurrentLocation() {
        return currentLocation;
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
            currentLocation = result;
            locationLabel.setText(currentLocation.toString());
        });
    }

    private void initExitButton() {
        exitButton.setOnAction(actionEvent -> Platform.exit());


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
}