package fi.tuni.environmentaldatalogger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class ChartViewerElement extends VBox implements Initializable {

    private final String DEFAULT_RANGE = "Last 7 days";
    private final List<String> DEFAULT_ENABLED_PARAMETERS = List.of("Option 1", "Option 2");
    @FXML
    public HBox optionsHBox;
    @FXML
    public HBox confirmHBox;
    @FXML
    public VBox testBox;

    Presenter presenter = new Presenter();

    public ChartViewerElement() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChartViewerElement.class.getResource("chartViewerElement.fxml"));
        fxmlLoader.setController(this);
        this.getChildren().add(fxmlLoader.load());
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ArrayList<String> params1 = new ArrayList<>(Collections.singletonList("temp"));

        Date startDate = new Date(2023-1900, Calendar.OCTOBER, 23);
        Date endDate = new Date(2023-1900, Calendar.NOVEMBER, 10);

        Pair<Date, Date> range = new Pair<>(startDate, endDate);

        LineChart<String, Number> lineChart = presenter.getDataAsLineChart(params1, range, new Coordinate(1, 1));
        testBox.getChildren().add(lineChart);

        MenuButton menuButton = new MenuButton("Select Options");
        ContextMenu contextMenu = new ContextMenu();

        // Create CheckMenuItem items and add them to the context menu
        CheckMenuItem option1 = new CheckMenuItem("Option 1");
        CheckMenuItem option2 = new CheckMenuItem("Option 2");
        CheckMenuItem option3 = new CheckMenuItem("Option 3");

        contextMenu.getItems().addAll(option1, option2, option3);

        menuButton.setContextMenu(contextMenu);

        for (var item : contextMenu.getItems()) {
            String text = item.getText();
            if (DEFAULT_ENABLED_PARAMETERS.contains(text)) {
                ((CheckMenuItem) item).setSelected(true);
            }
        }

        optionsHBox.getChildren().add(menuButton);

        ComboBox<String> comboBox = new ComboBox<>();

        comboBox.getItems().addAll("Last 14 days", "Last 7 days", "Last 24 hours", "Next 24 hours", "Next 7 days", "Next 14 days", "Custom");
        comboBox.setValue(DEFAULT_RANGE);

        HBox customRangePicker = getCustomRangePicker();

        customRangePicker.setVisible(false);
        customRangePicker.setManaged(false);

        Button loadButton = new Button("Load");

        loadButton.setOnAction(actionEvent -> {
            ArrayList<String> params = new ArrayList<>();

            for (var item : contextMenu.getItems()) {
                if (((CheckMenuItem) item).isSelected()) {
                    params.add(item.getText());
                }
            }

            params.forEach(System.out::println);
        });

        optionsHBox.getChildren().add(comboBox);
        optionsHBox.getChildren().add(customRangePicker);
        confirmHBox.getChildren().add(loadButton);

        comboBox.setOnAction(event -> {
            if (comboBox.getValue().equals("Custom")) {
                customRangePicker.setVisible(true);
                customRangePicker.setManaged(true);
            } else {
                customRangePicker.setVisible(false);
                customRangePicker.setManaged(false);
            }
        });
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

    private HBox getCustomRangePicker() {

        HBox res = new HBox();
        res.setSpacing(10);

        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        startDatePicker.setPrefWidth(120);
        endDatePicker.setPrefWidth(120);

        startDatePicker.getEditor().setDisable(true);
        startDatePicker.getEditor().setOpacity(1);

        endDatePicker.getEditor().setDisable(true);
        endDatePicker.getEditor().setOpacity(1);

        // Presenter.getValidDataRange
        Pair<Date, Date> range = new Pair<>(new Date(2023 - 1900, Calendar.SEPTEMBER, 15)
                , new Date(2023 - 1900, Calendar.OCTOBER, 10));



        LocalDate minDate = range.getKey().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate maxDate = range.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        startDatePicker.setDayCellFactory(getDayCellFactory(minDate, maxDate));
        endDatePicker.setDayCellFactory(getDayCellFactory(minDate, maxDate));

        res.getChildren().addAll(startDatePicker, endDatePicker);

        return res;
    }

    private Callback<DatePicker, DateCell> getDayCellFactory(LocalDate minDate, LocalDate maxDate) {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(minDate) || item.isAfter(maxDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #cccccc;"); // Change the style of disabled dates
                }
            }
        };
    }
}
