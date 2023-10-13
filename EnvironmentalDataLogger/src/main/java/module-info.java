module fi.tuni.environmentaldatalogger {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires okhttp3;
    requires org.json;
    requires javafx.web;

    opens fi.tuni.environmentaldatalogger to javafx.fxml;
    exports fi.tuni.environmentaldatalogger;
    exports fi.tuni.environmentaldatalogger.util;
    opens fi.tuni.environmentaldatalogger.util to javafx.fxml;
}