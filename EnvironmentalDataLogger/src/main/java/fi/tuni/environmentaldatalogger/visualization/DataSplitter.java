package fi.tuni.environmentaldatalogger.visualization;

import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import static java.lang.Math.*;

/**
 * Class for splitting data into clusters. A bit overkill for what it's used for. Mostly written by AI.
 */
public class DataSplitter {

    /**
     * Splits data into clusters using k-means clustering.
     * @param dataObjects list of data objects
     * @param k number of clusters
     * @return list of clusters
     */
    static List<List<DataObject>> kMeansCluster(List<DataObject> dataObjects, int k) {
        // Randomly initialize centroids
        List<DataObject> centroids = initializeCentroids(dataObjects, k);

        List<List<DataObject>> clusters;
        boolean centroidsChanged;

        do {
            // Assign each data object to the nearest centroid
            clusters = assignToClusters(dataObjects, centroids);

            // Update centroids
            List<DataObject> newCentroids = updateCentroids(clusters);

            // Check if centroids have changed
            centroidsChanged = !newCentroids.equals(centroids);
            centroids = newCentroids;

            // Handle any empty clusters
            handleEmptyClusters(clusters, centroids, dataObjects);

        } while (centroidsChanged); // Repeat until centroids do not change

        return clusters;
    }

    /**
     * Randomly initialize centroids.
     * @param dataObjects list of data objects
     * @param k number of clusters
     * @return list of centroids
     */
    private static List<DataObject> initializeCentroids(List<DataObject> dataObjects, int k) {
        Random random = new Random(1);
        List<DataObject> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            centroids.add(dataObjects.get(random.nextInt(dataObjects.size())));
        }
        return centroids;
    }

    /**
     * Assign each data object to the nearest centroid.
     * @param dataObjects list of data objects
     * @param centroids list of centroids
     * @return list of clusters
     */
    private static List<List<DataObject>> assignToClusters(List<DataObject> dataObjects, List<DataObject> centroids) {
        List<List<DataObject>> clusters = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ArrayList<>());
        }

        for (DataObject obj : dataObjects) {
            int nearestCentroidIndex = getNearestCentroidIndex(obj, centroids);
            clusters.get(nearestCentroidIndex).add(obj);
        }

        return clusters;
    }

    /**
     * Handle any empty clusters.
     * @param clusters list of clusters
     * @param centroids list of centroids
     * @param dataObjects list of data objects
     */
    private static void handleEmptyClusters(List<List<DataObject>> clusters, List<DataObject> centroids, List<DataObject> dataObjects) {
        Random random = new Random(1);
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).isEmpty()) {
                // If a cluster is empty, reinitialize its centroid
                centroids.set(i, dataObjects.get(random.nextInt(dataObjects.size())));
            }
        }
    }

    /**
     * Get the index of the nearest centroid.
     * @param obj data object
     * @param centroids list of centroids
     * @return index of the nearest centroid
     */
    private static int getNearestCentroidIndex(DataObject obj, List<DataObject> centroids) {
        int nearestIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < centroids.size(); i++) {
            double distance = obj.distanceTo(centroids.get(i)); // Implement distanceTo method in DataObject class
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    /**
     * Update centroids.
     * @param clusters list of clusters
     * @return list of centroids
     */
    private static List<DataObject> updateCentroids(List<List<DataObject>> clusters) {
        List<DataObject> newCentroids = new ArrayList<>();
        for (List<DataObject> cluster : clusters) {
            newCentroids.add(calculateMean(cluster)); // Implement calculateMean method to find the mean of a cluster
        }
        return newCentroids;
    }

    /**
     * Data object containing temporal data.
     */
    static class DataObject {

        private final String name;
        private final double diff;
        private final double min;
        private final double max;

        /**
         * Constructor for DataObject.
         * @param name name of the data object
         * @param data data
         */
        DataObject(String name, TreeMap<LocalDateTime, Double> data) {
            this.name = name;

            var extremes = extremes(data);

            this.min = extremes.getKey();
            this.max = extremes.getValue();

            this.diff = abs(max - min);
        }

        /**
         * Constructor for DataObject.
         * @param name name of the data object
         * @param diff difference between min and max values
         * @param min minimum value
         * @param max maximum value
         */
        DataObject(String name, double diff, double min, double max) {
            this.name = name;
            this.diff = diff;
            this.min = min;
            this.max = max;
        }

        /**
         * Calculate distance to another data object.
         * @param other other data object
         * @return distance
         */
        double distanceTo(DataObject other) {

            double diff1 = this.getDiff();
            double diff2 = other.getDiff();

            double min1 = this.getMin();
            double min2 = other.getMin();

            double max1 = this.getMax();
            double max2 = other.getMax();

            return sqrt(pow(diff1 - diff2, 2) + pow(min1 - min2, 2) + pow(max1 - max2, 2));
        }

        /**
         * Get name of the data object.
         * @return name
         */
        String getName() {
            return name;
        }

        /**
         * Get difference between min and max values.
         * @return difference
         */
        double getDiff() {
            return diff;
        }

        /**
         * Get minimum value.
         * @return minimum value
         */
        double getMin() {
            return min;
        }

        /**
         * Get maximum value.
         * @return maximum value
         */
        double getMax() {
            return max;
        }

        /**
         * Get minimum and maximum values.
         * @param data data
         * @return minimum and maximum values
         */
        private static Pair<Double, Double> extremes(TreeMap<LocalDateTime, Double> data) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            for (var entry : data.entrySet()) {
                min = Math.min(min, entry.getValue());
                max = Math.max(max, entry.getValue());
            }

            return new Pair<>(min, max);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (!(other instanceof DataObject otherDataObject)) {
                return false;
            }

            return this.getDiff() == otherDataObject.getDiff() &&
                    this.getMin() == otherDataObject.getMin() &&
                    this.getMax() == otherDataObject.getMax();
        }
    }

    /**
     * Calculate mean of a cluster.
     * @param cluster cluster
     * @return mean
     */
    private static DataObject calculateMean(List<DataObject> cluster) {

        double diff = 0;
        double min = 0;
        double max = 0;

        for (DataObject obj : cluster) {
            diff += obj.getDiff();
            min += obj.getMin();
            max += obj.getMax();
        }

        int n = cluster.size();
        return new DataObject("mean", diff / n, min / n, max / n);
    }
}
