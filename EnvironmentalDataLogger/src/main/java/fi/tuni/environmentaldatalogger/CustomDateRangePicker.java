package fi.tuni.environmentaldatalogger;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class CustomDateRangePicker extends HBox {

    DatePicker startDatePicker = new DatePicker();
    DatePicker endDatePicker = new DatePicker();

    public CustomDateRangePicker() {

        this.setSpacing(10);

        startDatePicker.setPrefWidth(120);
        endDatePicker.setPrefWidth(120);

        startDatePicker.getEditor().setDisable(true);
        startDatePicker.getEditor().setOpacity(1);

        endDatePicker.getEditor().setDisable(true);
        endDatePicker.getEditor().setOpacity(1);

        // Presenter.getValidDataRange
        Pair<Date, Date> range = new Pair<>(new Date(2023 - 1900, Calendar.SEPTEMBER, 15)
                , new Date(2023 - 1900, Calendar.OCTOBER, 10));

        //Pair<LocalDateTime, LocalDateTime> range1 = new Pair<>(LocalDateTime.of(2023, 9, 15, 0, 0),
        //        LocalDateTime.of(2023, 10, 10, 0, 0));

        LocalDate minDate = range.getKey().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate maxDate = range.getValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        //LocalDate minDate1 = range1.getKey().toLocalDate();
        //LocalDate maxDate1 = range1.getValue().toLocalDate();

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
}
