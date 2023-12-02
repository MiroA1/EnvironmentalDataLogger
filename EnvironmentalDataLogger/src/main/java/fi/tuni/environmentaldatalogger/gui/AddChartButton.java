package fi.tuni.environmentaldatalogger.gui;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.io.IOException;

/**
 * Button for adding a chart to a ChartGrid.
 */
public class AddChartButton extends Button implements GridElement {

    private final int row;
    private final int column;
    private final ChartGrid parent;

    /**
     * Constructor for AddChartButton.
     * @param column column index
     * @param row row index
     * @param parent parent grid
     */
    public AddChartButton(int column, int row, ChartGrid parent) {

        super("+");

        this.row = row;
        this.column = column;
        this.parent = parent;

        this.setMinSize(25, 25);
        this.setMaxSize(4000, 4000);

        GridPane.setHalignment(this, HPos.CENTER);
        GridPane.setValignment(this, VPos.CENTER);

        this.setStyle("-fx-background-color: #EDEDED;");

        this.setOnAction(actionEvent -> {
            try {
                ChartViewerElement chart = new ChartViewerElement(column, row, new RemoveChartButton(column, row, parent));

                parent.addChart(chart);
                Platform.runLater(chart::pushLoadButton);
            } catch (IOException e) {
                // TODO: improve behaviour
                throw new RuntimeException(e);
            }
        });
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
