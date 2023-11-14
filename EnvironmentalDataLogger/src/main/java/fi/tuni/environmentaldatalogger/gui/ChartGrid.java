package fi.tuni.environmentaldatalogger.gui;

import com.google.gson.Gson;
import fi.tuni.environmentaldatalogger.save.Loadable;
import fi.tuni.environmentaldatalogger.save.Saveable;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ChartGrid extends GridPane implements Saveable, Loadable {

    private ArrayList<ArrayList<GridElement>> grid = new ArrayList<>();
    private boolean expandCharts = true;


    public ChartGrid() throws IOException {
        super();

        AnchorPane.setTopAnchor(this, 10.0);
        AnchorPane.setLeftAnchor(this, 10.0);
        AnchorPane.setRightAnchor(this, 10.0);
        AnchorPane.setBottomAnchor(this, 10.0);

        grid.add(new ArrayList<>());
        grid.get(0).add(null);

        addChart(new ChartViewerElement(0, 0, null));

        //this.setGridLinesVisible(true);

    }

    public int getGridColumnCount() {
        return grid.size();
    }

    public int getGridRowCount() {
        return grid.get(0).size();
    }

    private boolean columnIsEmpty(int column) {

        for (int i = 0; i < getGridRowCount(); ++i) {
            if (grid.get(column).get(i) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean rowIsEmpty(int row) {

        for (int i = 0; i < getGridColumnCount(); ++i) {
            if (grid.get(i).get(row) != null) {
                return false;
            }
        }

        return true;
    }


    public boolean removeChart(int column, int row) {

        if (!(grid.get(column).get(row) instanceof ChartViewerElement)) {
            return false;
        }

        AddChartButton addButton = new AddChartButton(column, row, this);
        boolean removed = replaceChild(addButton);

        trimButtons();
        trimEmpty();

        return removed;
    }

    private boolean removeChild(GridElement node) {

        if (node == null) {
            System.out.println("Node is null");
            return false;
        }

        int column = node.getColumn();
        int row = node.getRow();

        if (grid.get(column).get(row) == null) {
            System.out.println("No node in cell " + row + ", " + column);
            return false;
        }

        if (!this.getChildren().remove((Node) node)) {
            System.out.println("No node in children: " + node.toString());
            return false;
        }

        grid.get(column).set(row, null);

        return true;
    }

    private boolean removeChild(int column, int row) {

        return removeChild(grid.get(column).get(row));
    }

    private boolean removeButton(int column, int row) {

        if (!(grid.get(column).get(row) instanceof AddChartButton)) {
            return false;
        }

        return removeChild(column, row);
    }

    public void addChart(ChartViewerElement chart) {

        int column = chart.getColumn();
        int row = chart.getRow();

        replaceChild(chart);

        if (column == getGridColumnCount() - 1) {
            var list = new ArrayList<GridElement>(Collections.nCopies(getGridRowCount(), null));
            grid.add(list);
        }

        if (row == getGridRowCount() - 1) {
            grid.forEach(columnList -> columnList.add(null));
        }

        addButtons();
    }

    private boolean replaceChild(GridElement node) {

        int column = node.getColumn();
        int row = node.getRow();

        GridElement prev = grid.get(column).get(row);

        if (prev != null && !this.getChildren().remove((Node) prev)) {
            System.out.println("No node in children: " + node.toString());
            return false;
        }

        this.add((Node) node, column, row);
        grid.get(column).set(row, node);

        return true;
    }

    private void trimEmpty() {

        for (int i = getGridColumnCount() - 1; i >= 0; --i) {
            if (columnIsEmpty(i)) {
                grid.remove(i);
            }
        }

        for (int i = getGridRowCount() - 1; i >= 0; --i) {
            if (rowIsEmpty(i)) {
                // variable for use in lambda
                int finalI = i;
                grid.forEach(columnList -> columnList.remove(finalI));
            }
        }
    }

    private void trimButtons() {

        for (var pos : getExtraAddButtonPositions()) {
            removeButton(pos.getKey(), pos.getValue());
        }
    }

    private void addButtons() {

        for (var pos : getFreePositions()) {
            replaceChild(new AddChartButton(pos.getKey(), pos.getValue(), this));
        }
    }

    // get all positions in the grid that are null and have at least one neighbour that is a ChartViewerElement
    private ArrayList<Pair<Integer, Integer>> getFreePositions() {

        ArrayList<Pair<Integer, Integer>> freePositions = new ArrayList<>();

        for (int i = 0; i < getGridColumnCount(); ++i) {
            for (int j = 0; j < getGridRowCount(); ++j) {

                if (grid.get(i).get(j) != null) {
                    continue;
                }

                if (i > 0 && grid.get(i-1).get(j) instanceof ChartViewerElement) {
                    freePositions.add(new Pair<>(i, j));
                    continue;
                }

                if (j > 0 && grid.get(i).get(j-1) instanceof ChartViewerElement) {
                    freePositions.add(new Pair<>(i, j));
                    continue;
                }

                if (i < getGridColumnCount() - 1 && grid.get(i+1).get(j) instanceof ChartViewerElement) {
                    freePositions.add(new Pair<>(i, j));
                    continue;
                }

                if (j < getGridRowCount() - 1 && grid.get(i).get(j+1) instanceof ChartViewerElement) {
                    freePositions.add(new Pair<>(i, j));
                    continue;
                }
            }
        }

        return freePositions;
    }

    // get all positions in the grid that contain an AddChartButton that does not have a ChartViewerElement neighbour
    private ArrayList<Pair<Integer, Integer>> getExtraAddButtonPositions() {

        ArrayList<Pair<Integer, Integer>> addButtonPositions = new ArrayList<>();

        for (int i = 0; i < getGridColumnCount(); ++i) {
            for (int j = 0; j < getGridRowCount(); ++j) {

                if (grid.get(i).get(j) == null) {
                    continue;
                }

                if (!(grid.get(i).get(j) instanceof AddChartButton)) {
                    continue;
                }

                if (i > 0 && grid.get(i-1).get(j) instanceof ChartViewerElement) {
                    continue;
                }

                if (j > 0 && grid.get(i).get(j-1) instanceof ChartViewerElement) {
                    continue;
                }

                if (i < getGridColumnCount() - 1 && grid.get(i+1).get(j) instanceof ChartViewerElement) {
                    continue;
                }

                if (j < getGridRowCount() - 1 && grid.get(i).get(j+1) instanceof ChartViewerElement) {
                    continue;
                }

                addButtonPositions.add(new Pair<>(i, j));
            }
        }

        return addButtonPositions;
    }

    // Get all positions in the grid that contain a ChartViewerElement with an AddChartButton to the left. The ChartViewerElement must be located at least 2 columns away from the right edge of the grid.
    public ArrayList<Pair<Integer, Integer>> getDoubleChartPositions() {

        ArrayList<Pair<Integer, Integer>> chartPositions = new ArrayList<>();

        for (int i = 0; i < getGridColumnCount(); ++i) {
            for (int j = 0; j < getGridRowCount(); ++j) {

                if (grid.get(i).get(j) == null) {
                    continue;
                }

                if (!(grid.get(i).get(j) instanceof ChartViewerElement)) {
                    continue;
                }

                if (i > getGridColumnCount() - 3 || !(grid.get(i+1).get(j) instanceof AddChartButton)) {
                    continue;
                }

                chartPositions.add(new Pair<>(i, j));
            }
        }

        return chartPositions;
    }

    public void editMode() {
        showButtons();

        ObservableList<Node> children = this.getChildren();

        for(Node node : children) {
            if (node instanceof ChartViewerElement) {
                ((ChartViewerElement) node).editMode();
            }
        }

        for (var pos : getDoubleChartPositions()) {
            GridPane.setColumnSpan(((ChartViewerElement) grid.get(pos.getKey()).get(pos.getValue())), 1);
        }
    }

    public void viewMode() {
        hideButtons();

        ObservableList<Node> children = this.getChildren();

        for(Node node : children) {
            if (node instanceof ChartViewerElement) {
                ((ChartViewerElement) node).viewMode();
            }
        }

        if (expandCharts) {
            for (var pos : getDoubleChartPositions()) {
                GridPane.setColumnSpan(((ChartViewerElement) grid.get(pos.getKey()).get(pos.getValue())), 2);
            }
        }
    }

    private void hideButtons() {
        ObservableList<Node> children = this.getChildren();

        for(Node node : children) {
            if (node instanceof Button) {
                node.setVisible(false);
                node.setManaged(false);
            }
        }
    }

    private void showButtons() {
        ObservableList<Node> children = this.getChildren();

        for(Node node : children) {
            if (node instanceof Button) {
                node.setVisible(true);
                node.setManaged(true);
            }
        }
    }

    @Override
    public String getJson() {

        ArrayList<ArrayList<String>> saveGrid = new ArrayList<>();

        for (int i = 0; i < getGridColumnCount(); ++i) {
            saveGrid.add(new ArrayList<>());
            for (int j = 0; j < getGridRowCount(); ++j) {
                if (grid.get(i).get(j) == null) {
                    saveGrid.get(i).add(null);
                } else if (grid.get(i).get(j) instanceof AddChartButton) {
                    saveGrid.get(i).add("AddChartButton");
                } else if (grid.get(i).get(j) instanceof ChartViewerElement) {
                    saveGrid.get(i).add(((ChartViewerElement) grid.get(i).get(j)).getJson());
                } else {
                    saveGrid.get(i).add(null);
                }
            }
        }

        SaveData saveData = new SaveData(saveGrid, expandCharts);
        Gson gson = new Gson();

        return gson.toJson(saveData);
    }

    @Override
    public boolean loadFromJson(String json) {

            Gson gson = new Gson();
            SaveData saveData = gson.fromJson(json, SaveData.class);

            if (saveData == null) {
                return false;
            }

            ArrayList<ArrayList<String>> saveGrid = saveData.grid();

            int n = saveGrid.size();
            int m = saveGrid.get(0).size();

            ArrayList<ArrayList<GridElement>> emptyGrid = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                ArrayList<GridElement> row = new ArrayList<>(Collections.nCopies(m, null));
                emptyGrid.add(row);
            }

            this.grid = emptyGrid;
            this.getChildren().clear();

            for (int i = 0; i < saveGrid.size(); ++i) {
                for (int j = 0; j < saveGrid.get(i).size(); ++j) {
                    if (saveGrid.get(i).get(j) == null) {
                        continue;
                    } else if (saveGrid.get(i).get(j).equals("AddChartButton")) {
                        continue;
                    } else {

                        ChartViewerElement chart;

                        try {
                            if (i == 0 && j == 0) {
                                chart = new ChartViewerElement(i, j, null);
                            } else {
                                chart = new ChartViewerElement(i, j, new RemoveChartButton(i, j, this));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                        chart.loadFromJson(saveGrid.get(i).get(j));
                        replaceChild(chart);
                    }
                }
            }

            addButtons();

            expandCharts = saveData.expandCharts();

            return true;
    }

    private record SaveData(ArrayList<ArrayList<String>> grid, boolean expandCharts) {
    }
}
