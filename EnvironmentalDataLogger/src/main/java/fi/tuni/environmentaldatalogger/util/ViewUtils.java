package fi.tuni.environmentaldatalogger.util;

import javafx.scene.shape.SVGPath;

/**
 * Utils class for view related functions
 */
public class ViewUtils {

    /**
     * Creates an SVGPath with the given parameters.
     * @param pathString SVG path string
     * @param fill fill color
     * @param hoverFill hover fill color
     * @return SVGPath
     */
    public static SVGPath createPath(String pathString, String fill, String hoverFill) {
        SVGPath path = new SVGPath();
        path.getStyleClass().add("svg");
        path.setContent(pathString);
        path.setStyle("-fill:" + fill + ";-hover-fill:" + hoverFill + ';');
        return path;
    }

}
