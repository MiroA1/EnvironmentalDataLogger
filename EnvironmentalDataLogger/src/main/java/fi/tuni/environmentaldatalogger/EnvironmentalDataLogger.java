package fi.tuni.environmentaldatalogger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.util.Pair;

import java.util.*;


public class EnvironmentalDataLogger extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EnvironmentalDataLogger.class.getResource("gui_template.fxml"));


        Presenter presenter = new Presenter();
        ArrayList<String> params = new ArrayList<>(Collections.singletonList("temp"));
        Date startDate = new Date(2023-1900, Calendar.OCTOBER, 23);
        Date endDate = new Date(2023-1900, Calendar.NOVEMBER, 10);
        System.out.println(startDate);
        Pair<Date, Date> range = new Pair<Date, Date>(startDate, endDate);
        LineChart<String, Number> lineChart = presenter.getDataAsLineChart(params, range);
        Scene scene1 = new Scene(lineChart);

        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Environmental Data Logger");

        stage.setScene(scene1);
        stage.show();

        DataExtractor extractor = WeatherDataExtractor.getInstance();
        System.out.println("Running!!!!");

        System.out.println("Valid parameters: " + extractor.getValidParameters());


        // Get valid data range for a parameter
        //Pair<Date, Date> range = extractor.getValidDataRange("temperature");
        //System.out.println("Valid data range for temperature: " + range.getKey() + " to " + range.getValue());
        Coordinate coordinates = new Coordinate(0, 0);
        //Date startDate = new Date(); // replace with actual date
        //Date endDate = new Date(); // replace with actual date
        TreeMap<Date, Double> data = extractor.getData("temp", new Pair<>(startDate, endDate), coordinates);

        // Print the data
        for(Map.Entry<Date, Double> entry : data.entrySet()) {
            System.out.println("Date: " + entry.getKey() + ", Temperature: " + entry.getValue());
        }

    }



    public static void main(String[] args) {
        launch();
    }
}