module fi.tuni.environmentaldatalogger {
    requires javafx.controls;
    requires javafx.fxml;


    opens fi.tuni.environmentaldatalogger to javafx.fxml;
    exports fi.tuni.environmentaldatalogger;
}