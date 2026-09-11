package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.List;

/**
 * The statistics that can be stated as arithmetic on the values themselves.
 * <p>
 * Working in {@link NodeConstant} rather than in {@code double[]} is what keeps units,
 * percentages and exact rationals: {@code range({1 m, 5 m})} is 4 meters because it is
 * literally {@code 5 m - 1 m}, and {@code variance({1, 2})} is 1/2 rather than 0.5.
 * <p>
 * Anything needing a root or a logarithm still ends in a double, but the label survives
 * because the magnitude is mapped rather than extracted.
 */
final class Statistics {

    private Statistics() {
    }

    static List<NodeConstant> sorted(List<NodeConstant> elements) {
        return elements.stream().sorted(NodeConstant::compareTo).toList();
    }

    static NodeConstant sum(List<NodeConstant> elements) {
        NodeConstant total = elements.getFirst();
        for (int i = 1; i < elements.size(); i++) {
            total = total.add(elements.get(i));
        }
        return total;
    }

    static NodeConstant mean(List<NodeConstant> elements) {
        return sum(elements).divide(new NodeRational(elements.size()));
    }

    /**
     * Sample variance, dividing by n-1.
     * <p>
     * The label is the one the inputs wore, which is the best the engine can do: the
     * dimension is really that label squared, and there is no way to say so.
     */
    static NodeConstant variance(List<NodeConstant> elements) {
        NodeConstant mean = mean(elements);
        NodeConstant total = null;
        for (NodeConstant element : elements) {
            NodeConstant deviation = element.subtract(mean);
            NodeConstant square = deviation.multiply(deviation);
            total = total == null ? square : total.add(square);
        }
        return total.divide(new NodeRational(elements.size() - 1));
    }

    /**
     * Sample standard deviation. A root is not rational, so the magnitude becomes a double
     * while the label rides along.
     */
    static NodeConstant standardDeviation(List<NodeConstant> elements) {
        return variance(elements).mapMagnitude(Math::sqrt);
    }

    /**
     * The p-th percentile, interpolating between the two neighbouring elements.
     * <p>
     * When p lands on an element exactly, that element comes back untouched, so
     * {@code percentile({1 m, 2 m, 3 m}, 50)} is 2 meters and not a bare 2.
     *
     * @param sorted   the elements in ascending order
     * @param fraction the percentile as a fraction in [0, 1]
     */
    static NodeConstant percentile(List<NodeConstant> sorted, double fraction) {
        int size = sorted.size();
        double position = (size - 1) * fraction + 1;
        int lowerIndex = (int) Math.floor(position) - 1;
        double weight = position % 1;

        if (lowerIndex < 0) {
            return sorted.getFirst();
        }
        if (lowerIndex >= size - 1) {
            return sorted.getLast();
        }
        if (weight == 0.0) {
            return sorted.get(lowerIndex);
        }

        NodeConstant lower = sorted.get(lowerIndex);
        NodeConstant upper = sorted.get(lowerIndex + 1);
        return lower.add(upper.subtract(lower).multiply(TypeCoercion.toNumber(weight)));
    }
}
