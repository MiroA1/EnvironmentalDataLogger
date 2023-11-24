package fi.tuni.environmentaldatalogger.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.tuni.environmentaldatalogger.EnvironmentalDataLogger;
import fi.tuni.environmentaldatalogger.Presenter;
import fi.tuni.environmentaldatalogger.save.Loadable;
import fi.tuni.environmentaldatalogger.save.Saveable;
import fi.tuni.environmentaldatalogger.apis.ApiException;
import fi.tuni.environmentaldatalogger.util.Coordinate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class ChartViewerElement extends VBox implements Initializable, GridElement, Saveable, Loadable {

    private final String DEFAULT_RANGE = "Last 7 days";
    private final List<String> DEFAULT_ENABLED_PARAMETERS = List.of("temperature");
    private final String DEFAULT_CHART_TYPE = "Line chart";
    private final List<String> CHART_TYPES = List.of("Line chart", "Pie chart");
    private final List<String> RANGES = List.of("Last 14 days", "Last 7 days", "Last 3 days", "Last 24 hours",
            "Next 24 hours", "Next 3 days", "Next 7 days", "Next 14 days", "Custom");

    Presenter presenter = Presenter.getInstance();

    private final int column;
    private final int row;
    
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
    public VBox headerVBox;
    @FXML
    public ComboBox<String> chartTypeSelector;
    @FXML
    public Label coordinateLabel;
    private final RemoveChartButton removeButton;

    private Coordinate selectedCoordinates;
    @FXML
    public TextField locationTextField;


    /**
     * Constructor for ChartViewerElement.
     * @param column Column of the element in the ChartGrid.
     * @param row Row of the element in the ChartGrid.
     * @param removeButton Button that removes this element from the ChartGrid.
     * @throws IOException If the FXML file cannot be loaded.
     */
    public ChartViewerElement(int column, int row, RemoveChartButton removeButton) throws IOException {

        this.removeButton = removeButton;
        this.column = column;
        this.row = row;

        FXMLLoader fxmlLoader = new FXMLLoader(ChartViewerElement.class.getResource("/fi/tuni/environmentaldatalogger/chart_viewer_element.fxml"));
        fxmlLoader.setController(this);
        this.getChildren().add(fxmlLoader.load());
    }

    /**
     * Sets the element to view mode, hiding the options bar.
     */
    public void viewMode() {
        headerVBox.setVisible(false);
        headerVBox.setManaged(false);
        this.setStyle("-fx-border-color: #AAAAAA; -fx-border-width: 0.5px; -fx-border-style: solid;");
    }

    /**
     * Sets the element to edit mode, showing the options bar.
     */
    public void editMode() {
        headerVBox.setVisible(true);
        headerVBox.setManaged(true);
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

        contextMenu.setOnAction(event -> {
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
            } else if (chartTypeSelector.getValue().equals("Pie chart")) {
                pieChartSelected();
            }
        });

        lineChartSelected();

        loadButton.fire();
    }

    /**
     * Updates options bar to show the correct options for a line chart.
     */
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


    /**
     * Updates options bar to show the correct options for a pie chart.
     */
    private void pieChartSelected() {
        this.rangeSelector.setVisible(false);
        this.rangeSelector.setManaged(false);

        this.customRangePicker.setVisible(false);
        this.customRangePicker.setManaged(false);

        this.parameterSelector.setVisible(false);
        this.parameterSelector.setManaged(false);
    }

    /**
     * Updates the range picker to show options that valid for current parameters.
     */
    private void updateRangePicker() {

        var validRanges = getValidRanges();

        String currentSelection = this.rangeSelector.getValue();

        if (currentSelection == null) {
            currentSelection = DEFAULT_RANGE;
        }

        //this.rangeSelector.getItems().clear();
        //this.rangeSelector.getItems().addAll(validRanges);
        this.rangeSelector.setItems(FXCollections.observableList(validRanges));

        if (validRanges.contains(currentSelection)) {
            this.rangeSelector.setValue(currentSelection);
        } else {
            this.rangeSelector.setValue(validRanges.get(0));
        }

        rangeSelector.requestLayout();

        updateCustomRangePicker();
    }

    /**
     * Updates the custom range picker to be visible/invisible and to show a valid range.
     */
    private void updateCustomRangePicker() {
        if (Objects.equals(rangeSelector.getValue(), "Custom")) {
            customRangePicker.setVisible(true);
            customRangePicker.setManaged(true);
            customRangePicker.setRange(presenter.getValidDataRange(getSelectedParameters()));
        } else {
            customRangePicker.setVisible(false);
            customRangePicker.setManaged(false);
        }
    }

    /**
     * Returns a list of valid ranges for the current parameters as String.
     * @return List of valid ranges as String.
     */
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

    /**
     * Returns the selected parameters.
     * @return List of selected parameters.
     */
    private ArrayList<String> getSelectedParameters() {

        ArrayList<String> selectedParameters = new ArrayList<>();

        for (var item : parameterSelector.getContextMenu().getItems()) {
            if (((CheckMenuItem) item).isSelected()) {
                selectedParameters.add(item.getText());
            }
        }

        return selectedParameters;
    }

    /**
     * Returns the selected range.
     * @return Pair of LocalDateTime objects representing the selected range.
     */
    private Pair<LocalDateTime, LocalDateTime> getSelectedRange() {

        return getRange(rangeSelector.getValue());
    }

    /**
     * Returns a range as a Pair of LocalDateTime objects based on a String description.
     * @param rangeDescription String description of the range.
     * @return Pair of LocalDateTime objects representing the range.
     */
    private Pair<LocalDateTime, LocalDateTime> getRange(String rangeDescription) {

        LocalDateTime now = LocalDateTime.now();

        return switch (rangeDescription) {
            case "Last 14 days" -> new Pair<>(now.minusDays(14), now);
            case "Last 7 days" -> new Pair<>(now.minusDays(7), now);
            case "Last 3 days" -> new Pair<>(now.minusDays(3), now);
            case "Last 24 hours" -> new Pair<>(now.minusDays(1), now);
            case "Next 24 hours" -> new Pair<>(now, now.plusDays(1));
            case "Next 3 days" -> new Pair<>(now, now.plusDays(3));
            case "Next 7 days" -> new Pair<>(now, now.plusDays(7));
            case "Next 14 days" -> new Pair<>(now, now.plusDays(14));
            default -> customRangePicker.getRange();
        };
    }

    /**
     * Loads a chart based on the current options.
     */
    private void loadChart() {

        chartBox.getChildren().clear();

        if (chartTypeSelector.getValue().equals("Line chart")) {
            loadLineChart();
        } else if (chartTypeSelector.getValue().equals("Pie chart")) {
            loadPieChart();
        }
    }

    /**
     * Loads a line chart based on the current options.
     */
    private void loadLineChart() {

        String location = locationTextField.getText();
        Coordinate coords = null;
        if (!location.isEmpty()) {
            coords = presenter.getCoordinatesFromAddress(location);
            if (coords != null) {
                selectedCoordinates = coords;
                coordinateLabel.setText("Coordinates: " + coords.toString());
            } else {
                coordinateLabel.setText("Coordinates: Not found");
                return;
            }
        } else if (selectedCoordinates != null) {
            coords = selectedCoordinates;
        } else {
            coords = MainView.getCurrentCoords(); // Default coordinates or previously selected
        }

        ArrayList<String> params = getSelectedParameters();
        chartBox.getChildren().clear();

        try {
            Region lc = presenter.getDataAsLineChart(params, getSelectedRange(), coords);
            AnchorPane.setTopAnchor(lc, 10.0);
            AnchorPane.setLeftAnchor(lc, 0.0);
            AnchorPane.setRightAnchor(lc, 0.0);
            AnchorPane.setBottomAnchor(lc, 0.0);
            lc.setPrefHeight(500);
            chartBox.getChildren().add(lc);
        } catch (ApiException e) {
            MainView.notificationBar.pushAlertNotification(e.getMessage());
        }
    }

    /**
     * Loads a pie chart based on the current options.
     */
    private void loadPieChart() {

        Coordinate coords = selectedCoordinates != null ? selectedCoordinates : MainView.getCurrentCoords();

        try {
            var pc = presenter.getDataAsPieChart(presenter.getValidAirQualityParameters(), LocalDateTime.now(), coords);

            AnchorPane.setTopAnchor(pc, 10.0);
            AnchorPane.setLeftAnchor(pc, 0.0);
            AnchorPane.setRightAnchor(pc, 0.0);
            AnchorPane.setBottomAnchor(pc, 0.0);

            pc.setPrefHeight(500);
            pc.setPrefWidth(500);

            chartBox.getChildren().add(pc);
        } catch (ApiException e) {
            MainView.notificationBar.pushAlertNotification(e.getMessage());
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

    /**
     * Returns a JSON that can be used to save current options.
     * @return JSON as String.
     */
    public String getJson() {
        Gson gson = new Gson();
        SaveData saveData = new SaveData(chartTypeSelector.getValue(), rangeSelector.getValue(), getSelectedParameters(), selectedCoordinates);
        return gson.toJson(saveData);
    }

    /**
     * Loads options from a JSON.
     * @param json JSON as String.
     * @return True if successful, false otherwise.
     */
    public boolean loadFromJson(String json) {

        Gson gson = new Gson(); //new GsonBuilder().registerTypeAdapter(Coordinate.class, new CoordinateDeserializer()).create();

        SaveData saveData;

        try {
            saveData = gson.fromJson(json, SaveData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        chartTypeSelector.setValue(saveData.chartType);
        rangeSelector.setValue(saveData.range);

        for (var item : parameterSelector.getContextMenu().getItems()) {
            ((CheckMenuItem) item).setSelected(saveData.parameters.contains(item.getText()));
        }

        selectedCoordinates = saveData.coordinates;

        String selectedCoordinatesString = selectedCoordinates != null ? selectedCoordinates.toString() : "Not Set";
        coordinateLabel.setText("Coordinates: " + selectedCoordinatesString);

        if (chartTypeSelector.getValue().equals("Line chart")) {
            lineChartSelected();
        } else if (chartTypeSelector.getValue().equals("Pie chart")) {
            pieChartSelected();
        }

        loadButton.fire();
        updateRangePicker();

        return true;
    }

    /**
     * Private class used to save options.
     * @param chartType Type of the chart.
     * @param range Range used for data query.
     * @param parameters Parameters used for data query.
     * @param coordinates Coordinates used for data query.
     */
    private record SaveData(String chartType, String range, ArrayList<String> parameters, Coordinate coordinates) {
    }
}
