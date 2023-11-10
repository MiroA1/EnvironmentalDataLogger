package fi.tuni.environmentaldatalogger.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.tuni.environmentaldatalogger.EnvironmentalDataLogger;
import fi.tuni.environmentaldatalogger.Presenter;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import fi.tuni.environmentaldatalogger.save.CoordinateDeserializer;
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
    private final String DEFAULT_CHART_TYPE = "Line chart";
    private final List<String> CHART_TYPES = List.of("Line chart");
    private final List<String> RANGES = List.of("Last 14 days", "Last 7 days", "Last 24 hours",
            "Next 24 hours", "Next 7 days", "Next 14 days", "Custom");

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
    @FXML
    public ComboBox<String> chartTypeSelector;
    public Button coordinateDialogButton;
    @FXML
    public Label coordinateLabel;
    private final RemoveChartButton removeButton;

    private Coordinate selectedCoordinates;

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

        if (removeButton != null) {
            removeHBox.getChildren().add(removeButton);
        }

        ContextMenu contextMenu = new ContextMenu();

        for (String param : presenter.getValidParameters()) {
            CheckMenuItem item = new CheckMenuItem(param);
            contextMenu.getItems().add(item);
        }

        parameterSelector.setContextMenu(contextMenu);

        parameterSelector.setOnAction(event -> {
            updateRangePicker();
        });

        loadButton.setOnAction(actionEvent -> {
            loadChart();
        });

        rangeSelector.setOnAction(event -> {
            updateCustomRangePicker();
        });

        chartTypeSelector.getItems().addAll(CHART_TYPES);
        chartTypeSelector.setValue(DEFAULT_CHART_TYPE);

        chartTypeSelector.setOnAction(event -> {
            if (chartTypeSelector.getValue().equals("Line chart")) {
                lineChartSelected();
            }
        });

        lineChartSelected();

        coordinateDialogButton.setOnAction(event -> {
            //load();
            //save();
            launchCoordinateDialog();
        });

        loadButton.fire();
    }

    private void lineChartSelected() {

        this.rangeSelector.setVisible(true);
        this.rangeSelector.setManaged(true);

        this.parameterSelector.setVisible(true);
        this.parameterSelector.setManaged(true);

        // enable default params
        for (var item : parameterSelector.getContextMenu().getItems()) {
            String text = item.getText();
            if (DEFAULT_ENABLED_PARAMETERS.contains(text)) {
                ((CheckMenuItem) item).setSelected(true);
            }
        }

        updateRangePicker();
    }

    private void updateRangePicker() {

        var validRanges = getValidRanges();

        this.rangeSelector.getItems().clear();
        this.rangeSelector.getItems().addAll(validRanges);

        if (validRanges.contains(DEFAULT_RANGE)) {
            this.rangeSelector.setValue(DEFAULT_RANGE);
        } else {
            this.rangeSelector.setValue(validRanges.get(0));
        }

        updateCustomRangePicker();
    }

    private void updateCustomRangePicker() {
        if (rangeSelector.getValue().equals("Custom")) {
            customRangePicker.setVisible(true);
            customRangePicker.setManaged(true);
            customRangePicker.setRange(presenter.getValidDataRange(getSelectedParameters()));
        } else {
            customRangePicker.setVisible(false);
            customRangePicker.setManaged(false);
        }
    }

    private List<String> getValidRanges() {

        List<String> validRanges = new ArrayList<>(RANGES);

        Pair<LocalDateTime, LocalDateTime> validRange = presenter.getValidDataRange(getSelectedParameters());

        for (var range : RANGES) {
            if (range.equals("Custom")) {
                continue;
            }

            Pair<LocalDateTime, LocalDateTime> rangePair = getRange(range);

            if (rangePair.getKey().isBefore(validRange.getKey()) || rangePair.getValue().isAfter(validRange.getValue())) {
                validRanges.remove(range);
            }
        }

        return validRanges;
    }

    private ArrayList<String> getSelectedParameters() {

        ArrayList<String> selectedParameters = new ArrayList<>();

        for (var item : parameterSelector.getContextMenu().getItems()) {
            if (((CheckMenuItem) item).isSelected()) {
                selectedParameters.add(item.getText());
            }
        }

        return selectedParameters;
    }

    private Pair<LocalDateTime, LocalDateTime> getSelectedRange() {

        return getRange(rangeSelector.getValue());
    }

    private Pair<LocalDateTime, LocalDateTime> getRange(String rangeDescription) {

        LocalDateTime now = LocalDateTime.now();

        return switch (rangeDescription) {
            case "Last 14 days" -> new Pair<>(now.minusDays(14), now);
            case "Last 7 days" -> new Pair<>(now.minusDays(7), now);
            case "Last 24 hours" -> new Pair<>(now.minusDays(1), now);
            case "Next 24 hours" -> new Pair<>(now, now.plusDays(1));
            case "Next 7 days" -> new Pair<>(now, now.plusDays(7));
            case "Next 14 days" -> new Pair<>(now, now.plusDays(14));
            default -> customRangePicker.getRange();
        };
    }

    private void loadChart() {
        ArrayList<String> params = new ArrayList<>();

        for (var item : parameterSelector.getContextMenu().getItems()) {
            if (((CheckMenuItem) item).isSelected()) {
                params.add(item.getText());
            }
        }

        chartBox.getChildren().clear();

        Coordinate coords = selectedCoordinates != null ? selectedCoordinates : EnvironmentalDataLogger.getCurrentCoords();
        var lc = presenter.getDataAsLineChart(params, getSelectedRange(), coords);


        AnchorPane.setTopAnchor(lc, 10.0);
        AnchorPane.setLeftAnchor(lc, 0.0);
        AnchorPane.setRightAnchor(lc, 0.0);
        AnchorPane.setBottomAnchor(lc, 0.0);

        lc.setPrefHeight(500);
        //lc.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> lc.getWidth() / 2, lc.widthProperty()));

        chartBox.getChildren().add(lc);
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getRow() {
        return row;
    }

    private void launchCoordinateDialog() {
        CoordinateDialog dialog = new CoordinateDialog();
        dialog.showAndWait().ifPresent(coordinate -> {
            System.out.println("Selected Coordinate: " + coordinate);
            selectedCoordinates = coordinate;
            coordinateLabel.setText("Coordinates: " + coordinate.toString());
        });
    }

    public String getJson() {
        Gson gson = new Gson();
        SaveData saveData = new SaveData(chartTypeSelector.getValue(), rangeSelector.getValue(), getSelectedParameters(), selectedCoordinates);
        return gson.toJson(saveData);
    }

    public void loadFromJson(String json) {

        Gson gson = new GsonBuilder().registerTypeAdapter(Coordinate.class, new CoordinateDeserializer()).create();
        SaveData saveData = gson.fromJson(json, SaveData.class);

        chartTypeSelector.setValue(saveData.chartType);
        rangeSelector.setValue(saveData.range);

        for (var item : parameterSelector.getContextMenu().getItems()) {
            ((CheckMenuItem) item).setSelected(saveData.parameters.contains(item.getText()));
        }

        selectedCoordinates = saveData.coordinates;

        String selectedCoordinatesString = selectedCoordinates != null ? selectedCoordinates.toString() : "Not Set";
        coordinateLabel.setText("Coordinates: " + selectedCoordinatesString);

        loadButton.fire();
    }

    private class SaveData {
        private final String chartType;
        private final String range;
        private final ArrayList<String> parameters;
        private final Coordinate coordinates;

        public SaveData(String chartType, String range, ArrayList<String> parameters, Coordinate coordinates) {
            this.chartType = chartType;
            this.range = range;
            this.parameters = parameters;
            this.coordinates = coordinates;
        }
    }
}
