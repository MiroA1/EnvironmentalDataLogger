package fi.tuni.environmentaldatalogger.gui;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A class for picking a custom date range.
 */
public class CustomDateRangePicker extends HBox {

    DatePicker startDatePicker = new DatePicker();
    DatePicker endDatePicker = new DatePicker();

    private final Pair<LocalDate, LocalDate> DEFAULT_RANGE = new Pair<>(LocalDate.now().minusDays(7), LocalDate.now().plusDays(7));

    /**
     * Constructor for CustomDateRangePicker.
     */
    public CustomDateRangePicker() {

        this.setSpacing(10);

        startDatePicker.setPrefWidth(120);
        endDatePicker.setPrefWidth(120);

        startDatePicker.getEditor().setDisable(true);
        startDatePicker.getEditor().setOpacity(1);

        endDatePicker.getEditor().setDisable(true);
        endDatePicker.getEditor().setOpacity(1);

        LocalDate minDate = DEFAULT_RANGE.getKey();
        LocalDate maxDate = DEFAULT_RANGE.getValue();

        startDatePicker.setValue(minDate);
        endDatePicker.setValue(LocalDate.now());

        startDatePicker.setDayCellFactory(getDayCellFactory(minDate, maxDate));
        endDatePicker.setDayCellFactory(getDayCellFactory(minDate, maxDate));

        this.getChildren().addAll(startDatePicker, endDatePicker);
    }

    /**
     * Returns the start date of the date picker.
     * @return start date of the date picker
     */
    public LocalDateTime getStartDate() {
        return startDatePicker.getValue().atStartOfDay();
    }

    /**
     * Returns the end date of the date picker.
     * @return end date of the date picker
     */
    public LocalDateTime getEndDate() {
        return endDatePicker.getValue().atStartOfDay();
    }

    /**
     * Returns a Callback for DatePicker day cell factory that disables dates not in range.
     * @param minDate minimum date
     * @param maxDate maximum date
     * @return Callback for DatePicker day cell factory
     */
    private Callback<DatePicker, DateCell> getDayCellFactory(LocalDate minDate, LocalDate maxDate) {
        return datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item.isBefore(minDate) || item.isAfter(maxDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #cccccc;"); // Change the style of disabled dates
                }
            }
        };
    }

    /**
     * Returns the selected range of the date picker.
     * @return range of the date picker
     */
    public Pair<LocalDateTime, LocalDateTime> getRange() {
        return new Pair<>(this.getStartDate(), this.getEndDate());
    }

    /**
     * Forcibly sets the selected range of the date picker.
     * @param range range to set
     */
    public void setRange(Pair<LocalDateTime, LocalDateTime> range) {

        Pair<LocalDate, LocalDate> dayRange = new Pair<>(range.getKey().toLocalDate(), range.getValue().toLocalDate());

        startDatePicker.setDayCellFactory(getDayCellFactory(dayRange.getKey(), dayRange.getValue()));
        endDatePicker.setDayCellFactory(getDayCellFactory(dayRange.getKey(), dayRange.getValue()));

        if (startDatePicker.getValue().isBefore(dayRange.getKey())) {
            startDatePicker.setValue(dayRange.getKey());
        }

        if (startDatePicker.getValue().isAfter(dayRange.getValue())) {
            startDatePicker.setValue(dayRange.getValue());
        }

        if (endDatePicker.getValue().isBefore(dayRange.getKey())) {
            endDatePicker.setValue(dayRange.getKey());
        }

        if (endDatePicker.getValue().isAfter(dayRange.getValue())) {
            endDatePicker.setValue(dayRange.getValue());
        }

    }
}
