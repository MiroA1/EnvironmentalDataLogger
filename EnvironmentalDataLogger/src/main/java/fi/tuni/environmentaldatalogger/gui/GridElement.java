package fi.tuni.environmentaldatalogger.gui;

/**
 * An interface for elements in a ChartGrid.
 */
public interface GridElement {

    /**
     * Returns the column index of the element.
     * @return column index of the element
     */
    int getColumn();

    /**
     * Returns the row index of the element.
     * @return row index of the element
     */
    int getRow();

}
