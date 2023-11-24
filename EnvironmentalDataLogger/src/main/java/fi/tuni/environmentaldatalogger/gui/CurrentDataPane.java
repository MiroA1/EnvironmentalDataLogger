package fi.tuni.environmentaldatalogger.gui;

import fi.tuni.environmentaldatalogger.Presenter;
import fi.tuni.environmentaldatalogger.apis.AirQualityDataExtractor;
import fi.tuni.environmentaldatalogger.apis.ApiException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import static fi.tuni.environmentaldatalogger.gui.MainView.getCurrentCoords;

public class CurrentDataPane extends GridPane {

    private GridPane pieChartPane;
    private GridPane dataPane;
    private HBox hbox;


    public CurrentDataPane() throws ApiException {

        initPieChartPane();
        initHBox();
        initDataPane();

        this.setPadding(new Insets(30, 0, 0, 0));

        this.add(pieChartPane, 0, 0);
        this.add(hbox, 1, 0);
        this.add(dataPane, 2, 0);
    }

    public static GridPane getInstance() {

        try {
            return new CurrentDataPane();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }


    private void initPieChartPane() throws ApiException {

        pieChartPane = new GridPane();

        TableView<DataItem> colorTable = getColorTable();
        GridPane.setConstraints(colorTable, 0, 0);

        PieChart pieChart = Presenter.getInstance().getDataAsPieChart(AirQualityDataExtractor.getInstance().getValidParameters(), LocalDateTime.now().minusDays(5), getCurrentCoords());
        GridPane.setConstraints(pieChart, 1, 0);
        pieChartPane.getChildren().add(pieChart);
        pieChartPane.getChildren().add(colorTable);

    }

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
        colorTable.setPrefWidth(133);
        colorTable.setEditable(false);

        return colorTable;
    }

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


    private void initHBox()  {

        hbox = new HBox();
        hbox.setPrefWidth(200);

    }

    private void initDataPane() throws ApiException {

        dataPane = new GridPane();

        dataPane.setHgap(20);

        Presenter presenter = Presenter.getInstance();

        TreeMap<String, String> currentDataMap = presenter.getCurrentData(presenter.getValidParameters(), getCurrentCoords());

        int rowIndex = 0;

        for (Map.Entry<String, String> entry : currentDataMap.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            Label keyLabel = new Label(key);
            Label valueLabel = new Label(value);

            keyLabel.setFont(new Font("Arial", 16));
            valueLabel.setFont(new Font("Arial", 16));

            dataPane.add(keyLabel, 0, rowIndex);
            dataPane.add(valueLabel, 1, rowIndex);

            rowIndex++;
        }


    }
}






