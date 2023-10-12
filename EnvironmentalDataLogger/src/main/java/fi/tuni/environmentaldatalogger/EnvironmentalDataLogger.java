package fi.tuni.environmentaldatalogger;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class EnvironmentalDataLogger extends Application implements Initializable {

    @FXML
    public Tab weatherTab;
    public HBox weatherHBox;
    public VBox weatherVBox;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EnvironmentalDataLogger.class.getResource("gui_template.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Environmental Data Logger");
        stage.setScene(scene);
        stage.show();
    }

    private Callback<DatePicker, DateCell> getDayCellFactory(LocalDate minDate, LocalDate maxDate) {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(minDate) || item.isAfter(maxDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ccc0cb;"); // Change the style of disabled dates
                }
            }
        };
    }



    public static void main(String[] args) {
        launch();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            weatherTab.setContent(new ChartViewerElement());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*
        weatherVBox.getChildren().add(testChart());

        MenuButton menuButton = new MenuButton("Select Options");
        ContextMenu contextMenu = new ContextMenu();

        // Create CheckMenuItem items and add them to the context menu
        CheckMenuItem option1 = new CheckMenuItem("Option 1");
        CheckMenuItem option2 = new CheckMenuItem("Option 2");
        CheckMenuItem option3 = new CheckMenuItem("Option 3");

        option1.setOnAction(event -> {
            // Handle option1 selection
            System.out.println("Option 1 selected: " + option1.isSelected());
        });

        option2.setOnAction(event -> {
            // Handle option2 selection
            System.out.println("Option 2 selected: " + option2.isSelected());
        });

        option3.setOnAction(event -> {
            // Handle option3 selection
            System.out.println("Option 3 selected: " + option3.isSelected());
        });

        contextMenu.getItems().addAll(option1, option2, option3);

        menuButton.setContextMenu(contextMenu);

        weatherHBox.getChildren().add(menuButton);

        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.getItems().addAll("Last 24h", "Last 7 days", "Last 14 days", "Next 24h", "Next 7 days", "Next 14 days", "custom");
        comboBox.setValue("Last 7 days");

        comboBox.setOnAction(event -> {
            // TODO: add/remove date picker
            return;
        });

        weatherHBox.getChildren().add(comboBox);

        DatePicker datePicker = new DatePicker();

        weatherHBox.getChildren().add(datePicker);

        LocalDate minDate = LocalDate.of(2023, 9, 15);
        LocalDate maxDate = LocalDate.of(2023, 10, 10);

        datePicker.setDayCellFactory(getDayCellFactory(minDate, maxDate));
        */
    }

    private LineChart<String, Number> testChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        // Create a NumberAxis for the Y-axis (used for numeric data)
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Sales");

        // Create a LineChart with String (Month) on the X-axis and Number (Sales) on the Y-axis
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Sales Chart");

        // Create a data series with sample data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("2023");

        series.getData().add(new XYChart.Data<>("Jan", 1200));
        series.getData().add(new XYChart.Data<>("Feb", 1400));
        series.getData().add(new XYChart.Data<>("Mar", 900));
        series.getData().add(new XYChart.Data<>("Apr", 1600));
        series.getData().add(new XYChart.Data<>("May", 1100));
        series.getData().add(new XYChart.Data<>("Jun", 1800));

        // Add the data series to the chart
        lineChart.getData().add(series);

        return lineChart;
    }
}