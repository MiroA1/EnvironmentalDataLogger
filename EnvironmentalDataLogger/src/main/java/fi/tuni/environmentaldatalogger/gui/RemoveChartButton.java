package fi.tuni.environmentaldatalogger.gui;

import javafx.scene.control.Button;

public class RemoveChartButton extends Button {

    private final int row;
    private final int column;
    private final ChartGrid parent;

    public RemoveChartButton(int column, int row, ChartGrid parent) {

        super("X");

        this.row = row;
        this.column = column;
        this.parent = parent;

        this.setStyle("-fx-background-color: transparent;");

        this.setOnAction(actionEvent -> {
            parent.removeChart(column, row);
        });
    }


    public int getColumn() {
        return column;
    }


    public int getRow() {
        return row;
    }
}
