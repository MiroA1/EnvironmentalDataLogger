package fi.tuni.environmentaldatalogger;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.scene.web.WebView;
import javafx.util.Pair;

import java.util.*;
public class EnvironmentalDataLogger extends Application implements Initializable {

    @FXML
    public Tab weatherTab;
    public HBox weatherHBox;
    public VBox weatherVBox;
    public AnchorPane testPane;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EnvironmentalDataLogger.class.getResource("gui_template.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Environmental Data Logger");

        stage.setScene(scene);
        stage.show();


        /*
        DataExtractor extractor = WeatherDataExtractor.getInstance();
        System.out.println("Running!!!!");

        System.out.println("Valid parameters: " + extractor.getValidParameters());

        // Get valid data range for a parameter
        //Pair<Date, Date> range = extractor.getValidDataRange("temperature");
        //System.out.println("Valid data range for temperature: " + range.getKey() + " to " + range.getValue());
        Coordinate coordinates = new Coordinate(0, 0);
        //Date startDate = new Date(); // replace with actual date
        //Date endDate = new Date(); // replace with actual date
        Date startDate = new Date(2023-1900, Calendar.OCTOBER, 23);
        Date endDate = new Date(2023-1900, Calendar.NOVEMBER, 10);
        TreeMap<Date, Double> data = extractor.getData("temp", new Pair<>(startDate, endDate), coordinates);

        // Print the data
        for(Map.Entry<Date, Double> entry : data.entrySet()) {
            System.out.println("Date: " + entry.getKey() + ", Temperature: " + entry.getValue());
        }
        */




    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            weatherTab.setContent(new ChartViewerElement());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
        WebView view = new WebView();
        view.getEngine().loadContent("<div style=\"overflow:hidden;max-width:100%;width:500px;height:500px;\">" +
                "<div id=\"embed-ded-map-canvas\" style=\"height:100%; width:100%;max-width:100%;\">" +
                "<iframe style=\"height:100%;width:100%;border:0;\" frameborder=\"0\" " +
                "src=\"https://www.google.com/maps/embed/v1/place?q=Tampere,+Suomi&key=AIzaSyBFw0Qbyq9zTFTd-tUY6dZWTgaQzuU17R8\">" +
                "</iframe></div><a class=\"our-googlemap-code\" href=\"https://www.bootstrapskins.com/themes\" " +
                "id=\"get-map-data\">premium bootstrap themes</a><style>#embed-ded-map-canvas " +
                "img{max-width:none!important;background:none!important;font-size: inherit;font-weight:inherit;}" +
                "</style></div>");
        view.setStyle("");

        testPane.getChildren().add(view);

         */


    }
}