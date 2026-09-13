package uk.co.ryanharrison.mathengine.plotting;

import uk.co.ryanharrison.mathengine.core.Function;

import java.awt.*;
import java.util.Objects;

/**
 * Immutable representation of a function to be plotted on a graph.
 * <p>
 * Encapsulates the mathematical function, its visual properties (color, stroke width),
 * visibility state, and display name. This class is designed to be thread-safe and
 * can be safely shared across multiple graph rendering operations.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * PlottedFunction quadratic = PlottedFunction.builder()
 *     .function(new Function("x^2 + 2*x + 1"))
 *     .name("Quadratic")
 *     .color(Color.BLUE)
 *     .visible(true)
 *     .build();
 * }</pre>
 */
public record PlottedFunction(Function function, Color color, String name, boolean visible, float strokeWidth) {
    public PlottedFunction(Function function, Color color, String name, boolean visible, float strokeWidth) {
        this.function = Objects.requireNonNull(function, "Function cannot be null");
        this.color = Objects.requireNonNull(color, "Color cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.visible = visible;
        this.strokeWidth = strokeWidth;

        if (strokeWidth <= 0) {
            throw new IllegalArgumentException("Stroke width must be positive, got: " + strokeWidth);
        }
    }

    /**
     * Creates a new builder for constructing PlottedFunction instances.
     *
     * @return a new builder with default values
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Evaluates the function at the given x-coordinate.
     * <p>
     * This method handles potential evaluation errors by catching exceptions
     * and returning NaN for invalid inputs (e.g., sqrt of negative numbers,
     * division by zero).
     * </p>
     *
     * @param x the x-coordinate at which to evaluate the function
     * @return the y-value of the function at x, or NaN if evaluation fails
     */
    public double evaluateAt(double x) {
        try {
            double result = function.evaluateAt(x);
            // Return NaN for infinite or invalid results
            if (Double.isInfinite(result)) {
                return Double.NaN;
            }
            return result;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public String getEquation() {
        return function.getEquation();
    }

    /**
     * Creates a copy of this PlottedFunction with a different visibility state.
     *
     * @param visible the new visibility state
     * @return a new PlottedFunction with updated visibility
     */
    public PlottedFunction withVisible(boolean visible) {
        return new Builder()
                .function(this.function)
                .color(this.color)
                .name(this.name)
                .strokeWidth(this.strokeWidth)
                .visible(visible)
                .build();
    }

    /**
     * Builder for creating PlottedFunction instances with fluent API.
     */
    public static final class Builder {
        private Function function;
        private Color color = Color.BLACK;
        private String name = "f(x)";
        private boolean visible = true;
        private float strokeWidth = 2.0f;

        private Builder() {
        }

        /**
         * Sets the mathematical function to plot.
         *
         * @param function the function (must not be null)
         * @return this builder
         * @throws NullPointerException if function is null
         */
        public Builder function(Function function) {
            this.function = Objects.requireNonNull(function, "Function cannot be null");
            return this;
        }

        /**
         * Sets the function from an equation string.
         *
         * @param equation the equation string (e.g., "x^2 + 2*x + 1")
         * @return this builder
         */
        public Builder equation(String equation) {
            this.function = new Function(equation);
            return this;
        }

        /**
         * Sets the color for rendering the function curve.
         *
         * @param color the color (must not be null)
         * @return this builder
         */
        public Builder color(Color color) {
            this.color = Objects.requireNonNull(color, "Color cannot be null");
            return this;
        }

        /**
         * Sets the display name for the function.
         *
         * @param name the name (must not be null)
         * @return this builder
         */
        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "Name cannot be null");
            return this;
        }

        /**
         * Sets whether the function should be visible in the graph.
         *
         * @param visible true if visible, false otherwise
         * @return this builder
         */
        public Builder visible(boolean visible) {
            this.visible = visible;
            return this;
        }

        /**
         * Sets the stroke width for rendering the function curve.
         *
         * @param strokeWidth the width in pixels (must be positive)
         * @return this builder
         * @throws IllegalArgumentException if strokeWidth is not positive
         */
        public Builder strokeWidth(float strokeWidth) {
            if (strokeWidth <= 0) {
                throw new IllegalArgumentException("Stroke width must be positive, got: " + strokeWidth);
            }
            this.strokeWidth = strokeWidth;
            return this;
        }

        /**
         * Builds the PlottedFunction instance.
         *
         * @return a new immutable PlottedFunction
         * @throws NullPointerException if required fields are not set
         */
        public PlottedFunction build() {
            if (function == null) {
                throw new IllegalStateException("Function must be set before building");
            }
            return new PlottedFunction(function, color, name, visible, strokeWidth);
        }
    }
}
