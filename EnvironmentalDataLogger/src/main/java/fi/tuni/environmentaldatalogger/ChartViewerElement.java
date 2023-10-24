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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;

public class ChartViewerElement extends VBox implements Initializable {

    private final String DEFAULT_RANGE = "Last 7 days";
    private final List<String> DEFAULT_ENABLED_PARAMETERS = List.of("temperature");

    // TODO: remember to change this
    Presenter presenter = new Presenter();
    
    @FXML
    public AnchorPane chartBox;
    @FXML
    public MenuButton parameterSelector;
    @FXML
    public ComboBox<String> rangeSelector;
    @FXML
    public Button loadButton;
    @FXML
    public CustomDateRangePicker customRangePicker;

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

        //MenuButton menuButton = new MenuButton("Select Parameters");
        ContextMenu contextMenu = new ContextMenu();

        // TODO: change to presenter when implemented
        for (String param : WeatherDataExtractor.getInstance().getValidParameters()) {
            CheckMenuItem item = new CheckMenuItem(param);
            contextMenu.getItems().add(item);
        }

        parameterSelector.setContextMenu(contextMenu);

        for (var item : contextMenu.getItems()) {
            String text = item.getText();
            if (DEFAULT_ENABLED_PARAMETERS.contains(text)) {
                ((CheckMenuItem) item).setSelected(true);
            }
        }

        rangeSelector.getItems().addAll("Last 14 days", "Last 7 days", "Last 24 hours", "Next 24 hours", "Next 7 days", "Next 14 days", "Custom");
        rangeSelector.setValue(DEFAULT_RANGE);

        customRangePicker.setVisible(false);
        customRangePicker.setManaged(false);

        loadButton.setOnAction(actionEvent -> {
            ArrayList<String> params = new ArrayList<>();

            for (var item : contextMenu.getItems()) {
                if (((CheckMenuItem) item).isSelected()) {
                    params.add(item.getText());
                }
            }

            chartBox.getChildren().clear();

            var lc = presenter.getDataAsLineChart(params, getRange(), EnvironmentalDataLogger.getCurrentLocation());

            AnchorPane.setTopAnchor(lc, 0.0);
            AnchorPane.setLeftAnchor(lc, 0.0);
            AnchorPane.setRightAnchor(lc, 0.0);
            AnchorPane.setBottomAnchor(lc, 0.0);

            chartBox.getChildren().add(lc);
        });

        rangeSelector.setOnAction(event -> {
            if (rangeSelector.getValue().equals("Custom")) {
                customRangePicker.setVisible(true);
                customRangePicker.setManaged(true);
            } else {
                customRangePicker.setVisible(false);
                customRangePicker.setManaged(false);
            }
        });

        loadButton.fire();
    }

    private Pair<LocalDateTime, LocalDateTime> getRange() {

        LocalDate now = LocalDate.now();

        // TODO: change
        if (rangeSelector.getValue().equals("Last 14 days")) {
            LocalDate start = now.minusDays(14);
            Date startDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            return new Pair<>(start.atStartOfDay(), now.atStartOfDay());
        } else if (rangeSelector.getValue().equals("Last 7 days")) {
            LocalDate start = now.minusDays(7);
            Date startDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            return new Pair<>(start.atStartOfDay(), now.atStartOfDay());
        } else if (rangeSelector.getValue().equals("Last 24 hours")) {
            LocalDate start = now.minusDays(1);
            Date startDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            return new Pair<>(start.atStartOfDay(), now.atStartOfDay());
        } else if (rangeSelector.getValue().equals("Next 24 hours")) {
            LocalDate start = now.plusDays(1);
            Date endDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date startDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            return new Pair<>(now.atStartOfDay(), start.atStartOfDay());
        } else if (rangeSelector.getValue().equals("Next 7 days")) {
            LocalDate start = now.plusDays(7);
            Date endDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date startDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            return new Pair<>(now.atStartOfDay(), start.atStartOfDay());
        } else if (rangeSelector.getValue().equals("Next 14 days")) {
            LocalDate start = now.plusDays(14);
            Date endDate = Date.from(start.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            Date startDate = Date.from(now.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            return new Pair<>(now.atStartOfDay(), start.atStartOfDay());
        } else {
            return customRangePicker.getRange();
        }
    }
}
