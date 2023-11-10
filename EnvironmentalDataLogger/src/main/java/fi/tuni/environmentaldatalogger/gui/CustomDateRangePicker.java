package fi.tuni.environmentaldatalogger.gui;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomDateRangePicker extends HBox {

    DatePicker startDatePicker = new DatePicker();
    DatePicker endDatePicker = new DatePicker();

    private final Pair<LocalDate, LocalDate> DEFAULT_RANGE = new Pair<>(LocalDate.now().minusDays(7), LocalDate.now().plusDays(7));

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

    public LocalDateTime getStartDate() {
        return startDatePicker.getValue().atStartOfDay();
    }

    public LocalDateTime getEndDate() {
        return endDatePicker.getValue().atStartOfDay();
    }

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

    public Pair<LocalDateTime, LocalDateTime> getRange() {
        return new Pair<>(this.getStartDate(), this.getEndDate());
    }

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
