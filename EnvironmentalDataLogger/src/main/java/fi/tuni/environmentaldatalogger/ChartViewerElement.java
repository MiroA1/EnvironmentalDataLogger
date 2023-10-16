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
import javafx.scene.layout.AnchorPane;
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
        FXMLLoader fxmlLoader = new FXMLLoader(ChartViewerElement.class.getResource("chart_viewer_element.fxml"));
        fxmlLoader.setController(this);
        this.getChildren().add(fxmlLoader.load());
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        AnchorPane.setTopAnchor(this, 10.0);
        AnchorPane.setLeftAnchor(this, 10.0);
        AnchorPane.setRightAnchor(this, 10.0);
        AnchorPane.setBottomAnchor(this, 10.0);

        ArrayList<String> params1 = new ArrayList<>(Collections.singletonList("temp"));

        Date startDate = new Date(2023-1900, Calendar.OCTOBER, 23);
        Date endDate = new Date(2023-1900, Calendar.NOVEMBER, 10);

        Pair<Date, Date> range = new Pair<>(startDate, endDate);

        LineChart<String, Number> lineChart = presenter.getDataAsLineChart(params1, range);
        testBox.getChildren().add(lineChart);

        MenuButton menuButton = new MenuButton("Select Parameters");
        ContextMenu contextMenu = new ContextMenu();

        // TODO: change to presenter when implemented
        for (String param : WeatherDataExtractor.getInstance().getValidParameters()) {
            CheckMenuItem item = new CheckMenuItem(param);
            contextMenu.getItems().add(item);
        }

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
