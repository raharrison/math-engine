package uk.co.ryanharrison.mathengine.plotting;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages coordinate transformations between screen space and Cartesian graph space.
 * <p>
 * This class handles the viewport transformation, including origin position, zoom level,
 * and conversions between pixel coordinates (screen space) and mathematical coordinates
 * (Cartesian space). The coordinate system uses the following conventions:
 * </p>
 * <ul>
 *     <li>Screen space: Origin at top-left, +x right, +y down</li>
 *     <li>Cartesian space: Origin configurable, +x right, +y up</li>
 * </ul>
 *
 * <h2>Zoom Levels:</h2>
 * <p>
 * The zoom system uses predefined scale units that determine how many graph units
 * are displayed per fixed pixel distance. Lower zoom index = more zoomed in.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * GraphCoordinateSystem coords = new GraphCoordinateSystem(800, 600);
 * coords.setOrigin(0, 0);  // Center at mathematical origin
 * coords.setZoom(14);       // Default zoom level
 *
 * // Convert mouse click to graph coordinates
 * Point2D.Double graphPoint = coords.toCartesian(mouseX, mouseY);
 *
 * // Convert graph point to screen for rendering
 * Point2D.Double screenPoint = coords.toScreen(x, y);
 * }</pre>
 */
public final class GraphCoordinateSystem {
    // Predefined zoom levels - graph units per 80 pixels
    // Much finer increments for smooth, Google Maps-like zooming
    private static final double[] UNITS = generateZoomLevels();

    // Fixed pixel distance for unit scaling
    private static final double PIXELS_PER_UNIT_STEP = 80.0;

    /**
     * Generates fine-grained zoom levels using 1.15x increments.
     * This creates smooth zoom transitions like Google Maps.
     * Includes exact "nice" values like 0.1, 1, 10, 100 for clean defaults.
     */
    private static double[] generateZoomLevels() {
        List<Double> levels = new ArrayList<>();

        // Generate smooth levels from 0.00001 to 10000
        double current = 0.00001;
        while (current <= 10000.0) {
            levels.add(current);
            current *= 1.15; // ~15% increase per level for smooth transitions
        }

        // Add exact "nice" values if not already present
        double[] niceValues = {0.00001, 0.0001, 0.001, 0.01, 0.1, 1.0, 10.0, 100.0, 1000.0, 10000.0};
        for (double nice : niceValues) {
            if (!levels.contains(nice)) {
                levels.add(nice);
            }
        }

        // Sort and remove duplicates
        return levels.stream()
                .distinct()
                .sorted()
                .mapToDouble(Double::doubleValue)
                .toArray();
    }

    private final double width;
    private final double height;

    // Cartesian origin position (center of viewport in graph coordinates)
    private double originX;
    private double originY;

    // Current zoom level (index into UNITS array)
    private int zoomLevel;

    // Derived value: pixels per graph unit at current zoom
    private double scale;


    /**
     * Creates a coordinate system for a viewport of the given dimensions.
     *
     * @param width  the viewport width in pixels
     * @param height the viewport height in pixels
     * @throws IllegalArgumentException if width or height are not positive
     */
    public GraphCoordinateSystem(double width, double height) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width must be positive, got: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive, got: " + height);
        }

        this.width = width;
        this.height = height;
        this.originX = 0.0;
        this.originY = 0.0;
        this.zoomLevel = findDefaultZoomLevel(); // Default zoom showing ~1 unit per 80 pixels
        updateScale();
    }

    /**
     * Finds the zoom level for exactly 1.0 units per 80 pixels.
     */
    private static int findDefaultZoomLevel() {
        // Find exact match for 1.0 first
        for (int i = 0; i < UNITS.length; i++) {
            if (Math.abs(UNITS[i] - 1.0) < 0.001) {
                return i;
            }
        }
        // If not found, find closest to 1.0
        int bestIndex = 0;
        double bestDiff = Double.MAX_VALUE;
        for (int i = 0; i < UNITS.length; i++) {
            double diff = Math.abs(UNITS[i] - 1.0);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /**
     * Updates the viewport dimensions.
     * <p>
     * This should be called when the viewport is resized.
     * </p>
     *
     * @param width  the new width in pixels
     * @param height the new height in pixels
     * @return a new GraphCoordinateSystem with updated dimensions
     */
    public GraphCoordinateSystem withDimensions(double width, double height) {
        GraphCoordinateSystem newSystem = new GraphCoordinateSystem(width, height);
        newSystem.originX = this.originX;
        newSystem.originY = this.originY;
        newSystem.zoomLevel = this.zoomLevel;
        newSystem.updateScale();
        return newSystem;
    }

    /**
     * Converts screen coordinates to Cartesian graph coordinates.
     *
     * @param screenX the x-coordinate in pixels (origin at left edge)
     * @param screenY the y-coordinate in pixels (origin at top edge)
     * @return the corresponding point in Cartesian space
     */
    public Point2D.Double toCartesian(double screenX, double screenY) {
        double cartesianX = originX + (screenX - width / 2.0) / scale;
        double cartesianY = originY - (screenY - height / 2.0) / scale;
        return new Point2D.Double(cartesianX, cartesianY);
    }

    /**
     * Converts Cartesian graph coordinates to screen coordinates.
     *
     * @param cartesianX the x-coordinate in graph space
     * @param cartesianY the y-coordinate in graph space
     * @return the corresponding point in screen space (pixels)
     */
    public Point2D.Double toScreen(double cartesianX, double cartesianY) {
        double screenX = width / 2.0 + (cartesianX - originX) * scale;
        double screenY = height / 2.0 - (cartesianY - originY) * scale;
        return new Point2D.Double(screenX, screenY);
    }

    /**
     * Converts a screen x-coordinate to the corresponding Cartesian x-coordinate.
     *
     * @param screenX the x-coordinate in pixels
     * @return the corresponding x-coordinate in graph space
     */
    public double toCartesianX(double screenX) {
        return originX + (screenX - width / 2.0) / scale;
    }

    /**
     * Pans the viewport by the given screen delta.
     * <p>
     * This is typically called during mouse drag operations. The delta values
     * represent the movement in screen pixels.
     * </p>
     *
     * @param screenDeltaX the horizontal movement in pixels
     * @param screenDeltaY the vertical movement in pixels
     */
    public void pan(double screenDeltaX, double screenDeltaY) {
        originX -= screenDeltaX / scale;
        originY += screenDeltaY / scale;  // Invert y because screen +y is down
    }

    /**
     * Zooms in by one level.
     * <p>
     * Decreases the zoom index, showing a smaller range with more detail.
     * </p>
     *
     * @return true if zoom changed, false if already at maximum zoom
     */
    public boolean zoomIn() {
        if (zoomLevel > 0) {
            zoomLevel--;
            updateScale();
            return true;
        }
        return false;
    }

    /**
     * Zooms out by one level.
     * <p>
     * Increases the zoom index, showing a larger range with less detail.
     * </p>
     *
     * @return true if zoom changed, false if already at minimum zoom
     */
    public boolean zoomOut() {
        if (zoomLevel < UNITS.length - 1) {
            zoomLevel++;
            updateScale();
            return true;
        }
        return false;
    }

    /**
     * Zooms toward a specific point in screen coordinates.
     * <p>
     * This keeps the point under the cursor at the same position after zooming,
     * providing intuitive zoom behavior for mouse wheel operations.
     * </p>
     *
     * @param screenX the x-coordinate to zoom toward
     * @param screenY the y-coordinate to zoom toward
     * @param zoomIn  true to zoom in, false to zoom out
     * @return true if zoom changed, false if already at limit
     */
    public boolean zoomToward(double screenX, double screenY, boolean zoomIn) {
        // Convert to Cartesian before zoom
        Point2D.Double pointBefore = toCartesian(screenX, screenY);

        // Perform zoom
        boolean changed = zoomIn ? zoomIn() : zoomOut();
        if (!changed) {
            return false;
        }

        // Adjust origin so the point stays at the same screen position
        Point2D.Double pointAfter = toCartesian(screenX, screenY);
        originX += pointBefore.x - pointAfter.x;
        originY += pointBefore.y - pointAfter.y;

        return true;
    }

    /**
     * Resets the viewport to the default view.
     * <p>
     * Centers the origin at (0, 0) and resets to default zoom level.
     * </p>
     */
    public void resetView() {
        originX = 0.0;
        originY = 0.0;
        zoomLevel = findDefaultZoomLevel();
        updateScale();
    }

    /**
     * Sets the origin (center of viewport) in Cartesian coordinates.
     *
     * @param x the x-coordinate of the new origin
     * @param y the y-coordinate of the new origin
     */
    public void setOrigin(double x, double y) {
        this.originX = x;
        this.originY = y;
    }

    /**
     * Sets the zoom level by index.
     *
     * @param zoomLevel the zoom level (0 = most zoomed in, higher = more zoomed out)
     * @throws IllegalArgumentException if zoom level is out of valid range
     */
    public void setZoom(int zoomLevel) {
        if (zoomLevel < 0 || zoomLevel >= UNITS.length) {
            // Clamp instead of throwing to handle edge cases gracefully
            this.zoomLevel = Math.max(0, Math.min(UNITS.length - 1, zoomLevel));
        } else {
            this.zoomLevel = zoomLevel;
        }
        updateScale();
    }

    /**
     * Gets the current zoom level index.
     *
     * @return the zoom level (0 = most zoomed in)
     */
    public int getZoomLevel() {
        return zoomLevel;
    }

    /**
     * Gets the graph unit size at the current zoom level.
     * <p>
     * This represents how many graph units are displayed per fixed pixel step.
     * </p>
     *
     * @return the unit size in graph coordinates
     */
    public double getUnitSize() {
        return UNITS[zoomLevel];
    }

    /**
     * Gets the current scale factor (pixels per graph unit).
     *
     * @return the scale factor
     */
    public double getScale() {
        return scale;
    }

    /**
     * Gets the x-coordinate of the origin in Cartesian space.
     *
     * @return the origin x-coordinate
     */
    public double getOriginX() {
        return originX;
    }

    /**
     * Gets the y-coordinate of the origin in Cartesian space.
     *
     * @return the origin y-coordinate
     */
    public double getOriginY() {
        return originY;
    }

    /**
     * Gets the viewport width in pixels.
     *
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the viewport height in pixels.
     *
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Gets the left edge of the visible area in Cartesian coordinates.
     *
     * @return the minimum visible x-coordinate
     */
    public double getMinVisibleX() {
        return toCartesianX(0);
    }

    /**
     * Gets the right edge of the visible area in Cartesian coordinates.
     *
     * @return the maximum visible x-coordinate
     */
    public double getMaxVisibleX() {
        return toCartesianX(width);
    }

    /**
     * Gets the bottom edge of the visible area in Cartesian coordinates.
     *
     * @return the minimum visible y-coordinate
     */
    public double getMinVisibleY() {
        return toCartesian(0, height).y;
    }

    /**
     * Gets the top edge of the visible area in Cartesian coordinates.
     *
     * @return the maximum visible y-coordinate
     */
    public double getMaxVisibleY() {
        return toCartesian(0, 0).y;
    }

    /**
     * Checks if a point in screen space is within the viewport bounds.
     *
     * @param screenX the x-coordinate in pixels
     * @param screenY the y-coordinate in pixels
     * @return true if the point is within bounds
     */
    public boolean isInBounds(double screenX, double screenY) {
        return screenX >= 0 && screenX <= width && screenY >= 0 && screenY <= height;
    }

    private void updateScale() {
        this.scale = PIXELS_PER_UNIT_STEP / UNITS[zoomLevel];
    }

    @Override
    public String toString() {
        return String.format("GraphCoordinateSystem(origin=(%.2f, %.2f), zoom=%d, unit=%.4f, scale=%.2f)",
                originX, originY, zoomLevel, UNITS[zoomLevel], scale);
    }
}
