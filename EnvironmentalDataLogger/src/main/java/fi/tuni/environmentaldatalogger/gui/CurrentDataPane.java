package fi.tuni.environmentaldatalogger.gui;

import fi.tuni.environmentaldatalogger.Presenter;
import fi.tuni.environmentaldatalogger.apis.AirQualityDataExtractor;
import fi.tuni.environmentaldatalogger.apis.ApiException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import static fi.tuni.environmentaldatalogger.gui.MainView.getCurrentCoords;

/**
 * A class for the current data pane.
 *
 * The current data pane displays the current data for the selected parameters.
 */
public class CurrentDataPane extends GridPane {

    private GridPane pieChartPane;
    private GridPane dataPane;
    private HBox hbox;
    private HBox hbox2;

    /**
     * Constructor.
     *
     * @throws ApiException if an error occurs while getting the data.
     */
    public CurrentDataPane() throws ApiException {

        initPieChartPane();
        initHBox();
        initHBox2();
        initDataPane();

        this.setPadding(new Insets(30, 10, 10, 10));

        this.add(pieChartPane, 3, 0);
        this.add(hbox, 2, 0);
        this.add(hbox2, 0,0);
        this.add(dataPane, 1, 0);

        this.setStyle("-fx-background-color: #A1DEF6 ;");

        AnchorPane.setTopAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);


    }

    /**
     * Returns an instance of the current data pane.
     */
    public static GridPane getInstance() {

        try {
            return new CurrentDataPane();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the pie chart pane.
     *
     * @throws ApiException if an error occurs while getting the data.
     */
    private void initPieChartPane() throws ApiException {

        try {
            PieChart pieChart = Presenter.getInstance().getDataAsPieChart(AirQualityDataExtractor.getInstance().getValidParameters(), LocalDateTime.now(), getCurrentCoords());
            pieChartPane = new GridPane();
            pieChartPane.setHgap(50);

            TableView<DataItem> colorTable = getColorTable();
            GridPane.setConstraints(colorTable, 1, 0);

            GridPane.setConstraints(pieChart, 0, 0);
            pieChartPane.getChildren().add(pieChart);
            pieChartPane.getChildren().add(colorTable);

        } catch (ApiException e) {
            throw new ApiException("Error while getting data for pie chart", ApiException.ErrorCode.INVALID_RESPONSE);
        }
    }

    /**
     * Returns a table view of the colors.
     */
    private TableView<DataItem> getColorTable() {

        TableView<DataItem> colorTable = new TableView<>();

        TableColumn<DataItem, ImageView> imageColumn = new TableColumn<>("Color");
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("imageView"));
        imageColumn.setSortable(false);

        TableColumn<DataItem, String> stringColumn = new TableColumn<>("Quality");
        stringColumn.setCellValueFactory(new PropertyValueFactory<>("stringValue"));
        stringColumn.setSortable(false);

        colorTable.getColumns().add(imageColumn);
        colorTable.getColumns().add(stringColumn);

        String folderName = "images";
        File folder = new File(folderName);
        folder.mkdir();

        ObservableList<DataItem> data = FXCollections.observableArrayList(
                new DataItem(new ImageView(new Image(new File(folder.getAbsolutePath() + "/" + "green.jpg").toURI().toString())), "Good"),
                new DataItem(new ImageView(new Image(new File(folder.getAbsolutePath() + "/" + "yellow.jpg").toURI().toString())), "Satisfactory"),
                new DataItem(new ImageView(new Image(new File(folder.getAbsolutePath() + "/" + "orange.jpg").toURI().toString())), "Fair"),
                new DataItem(new ImageView(new Image(new File(folder.getAbsolutePath() + "/" + "red.jpg").toURI().toString())), "Poor"),
                new DataItem(new ImageView(new Image(new File(folder.getAbsolutePath() + "/" + "purple.jpg").toURI().toString())), "Very poor")
        );

        colorTable.setItems(data);
        colorTable.setMaxHeight(253);
        colorTable.setMinHeight(253);
        colorTable.setMaxWidth(133);
        colorTable.setMinWidth(133);
        colorTable.setEditable(false);

        return colorTable;
    }

    /**
     * A static class for the data item used in color table.
     */
    public static class DataItem {
        private final ImageView imageView;
        private final String stringValue;

        public DataItem(ImageView imageView, String stringValue) {
            this.imageView = imageView;
            this.stringValue = stringValue;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public String getStringValue() {
            return stringValue;
        }
    }

    /**
     * Initializes the HBox between the pie chart and the data pane.
     */
    private void initHBox()  {

        hbox = new HBox();
        hbox.setPrefWidth(150);

    }

    /**
     * Initializes the HBox between the left border and data pane.
     */
    private void initHBox2()  {

        hbox2 = new HBox();
        hbox2.setPrefWidth(50);
    }

    /**
     * Adds a label pair to the data pane.
     *
     * @param param the parameter.
     * @param value the value of the parameter.
     * @param rowIndex the row index.
     */
    private void addLabelPair(String param, String value, int rowIndex) {
        Label keyLabel = new Label(param);
        Label valueLabel = new Label(value);

        keyLabel.setFont(new Font("Verdana", 20));
        valueLabel.setFont(new Font("Verdana", 20));

        dataPane.add(keyLabel, 0, rowIndex);
        dataPane.add(valueLabel, 1, rowIndex);
    }

    /**
     * Initializes the data pane.
     *
     * @throws ApiException if an error occurs while getting the data.
     */
    private void initDataPane() throws ApiException {

        dataPane = new GridPane();

        dataPane.setHgap(20);
        dataPane.setVgap(10);

        Presenter presenter = Presenter.getInstance();
        TreeMap<String, String> currentDataMap = presenter.getCurrentData(presenter.getValidParameters(), getCurrentCoords());

        int rowIndex = 0;

        String temperatureKey = "Temperature";
        String temperatureValue = currentDataMap.get(temperatureKey);
        addLabelPair(temperatureKey, temperatureValue, rowIndex);
        rowIndex++;

        String feelsLikeKey = "Feels like";
        String feelsLikeValue = currentDataMap.get(feelsLikeKey);
        addLabelPair(feelsLikeKey, feelsLikeValue, rowIndex);
        rowIndex++;

        for (Map.Entry<String, String> entry : currentDataMap.entrySet()) {
            String param = entry.getKey();
            String value = entry.getValue();

            if (!param.equals(temperatureKey) && !param.equals(feelsLikeKey)) {
                addLabelPair(param, value, rowIndex);
                rowIndex++;
            }
        }

    }
}






