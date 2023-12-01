/*

package fi.tuni.environmentaldatalogger.gui;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CustomMapLayer extends MapLayer {

    private final Node marker;

    public CustomMapLayer() {
        marker = new Circle(20, Color.color(0,0,1.00,0.5));
        getChildren().add(marker);
    }
    @Override
    protected void layoutLayer() {
        MapPoint point = new MapPoint(61.4978, 23.7610);
        Point2D mapPoint = getMapPoint(point.getLatitude(), point.getLongitude());
        marker.setVisible(true);
        marker.setTranslateX(mapPoint.getX());
        marker.setTranslateY(mapPoint.getY());
    }
}
*/