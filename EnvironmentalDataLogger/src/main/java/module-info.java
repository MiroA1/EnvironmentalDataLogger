module fi.tuni.environmentaldatalogger {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires okhttp3;
    requires org.json;

    opens fi.tuni.environmentaldatalogger to javafx.fxml;
    exports fi.tuni.environmentaldatalogger;
}