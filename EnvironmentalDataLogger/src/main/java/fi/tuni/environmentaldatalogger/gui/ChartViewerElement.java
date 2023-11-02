package fi.tuni.environmentaldatalogger.gui;

import fi.tuni.environmentaldatalogger.EnvironmentalDataLogger;
import fi.tuni.environmentaldatalogger.Presenter;
import fi.tuni.environmentaldatalogger.apis.WeatherDataExtractor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class ChartViewerElement extends VBox implements Initializable, GridElement {

    private final String DEFAULT_RANGE = "Last 7 days";
    private final List<String> DEFAULT_ENABLED_PARAMETERS = List.of("temperature");

    // TODO: remember to change this
    Presenter presenter = Presenter.getInstance();

    private int column;
    private int row;
    
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
    @FXML
    public HBox removeHBox;
    @FXML
    public HBox headerHBox;

    private final RemoveChartButton removeButton;

    public ChartViewerElement(int column, int row, RemoveChartButton removeButton) throws IOException {

        this.removeButton = removeButton;
        this.column = column;
        this.row = row;

        FXMLLoader fxmlLoader = new FXMLLoader(ChartViewerElement.class.getResource("/fi/tuni/environmentaldatalogger/chart_viewer_element.fxml"));
        fxmlLoader.setController(this);
        this.getChildren().add(fxmlLoader.load());
    }

    public void viewMode() {
        headerHBox.setVisible(false);
        headerHBox.setManaged(false);
        this.setStyle("-fx-border-color: #AAAAAA; -fx-border-width: 0.5px; -fx-border-style: solid;");
    }

    public void editMode() {
        headerHBox.setVisible(true);
        headerHBox.setManaged(true);
        this.setStyle("-fx-border-color: transparent;");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        AnchorPane.setTopAnchor(this, 10.0);
        AnchorPane.setLeftAnchor(this, 10.0);
        AnchorPane.setRightAnchor(this, 10.0);
        AnchorPane.setBottomAnchor(this, 10.0);

        this.setPrefWidth(4000);

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

            var lc = presenter.getDataAsLineChart(params, getRange(), EnvironmentalDataLogger.getCurrentCoords());


            AnchorPane.setTopAnchor(lc, 10.0);
            AnchorPane.setLeftAnchor(lc, 0.0);
            AnchorPane.setRightAnchor(lc, 0.0);
            AnchorPane.setBottomAnchor(lc, 0.0);

            lc.setPrefHeight(500);
            //lc.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> lc.getWidth() / 2, lc.widthProperty()));

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


        if (removeButton != null) {
            removeHBox.getChildren().add(removeButton);
        }

        loadButton.fire();
    }

    private Pair<LocalDateTime, LocalDateTime> getRange() {

        LocalDateTime now = LocalDateTime.now();

        if (rangeSelector.getValue().equals("Last 14 days")) {
            return new Pair<>(now.minusDays(14), now);

        } else if (rangeSelector.getValue().equals("Last 7 days")) {
            return new Pair<>(now.minusDays(7), now);

        } else if (rangeSelector.getValue().equals("Last 24 hours")) {
            return new Pair<>(now.minusDays(1), now);

        } else if (rangeSelector.getValue().equals("Next 24 hours")) {
            return new Pair<>(now, now.plusDays(1));

        } else if (rangeSelector.getValue().equals("Next 7 days")) {
            return new Pair<>(now, now.plusDays(7));

        } else if (rangeSelector.getValue().equals("Next 14 days")) {
            return new Pair<>(now, now.plusDays(14));

        } else {
            return customRangePicker.getRange();
        }
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getRow() {
        return row;
    }
}
