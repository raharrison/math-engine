package uk.co.ryanharrison.mathengine.unitconversion.units;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.MathEngine;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.unitconversion.ConversionResult;

import java.util.List;
import java.util.Optional;

/**
 * A unit group for complex units with formula-based conversions.
 * <p>
 * Complex units use mathematical equations to perform conversions. For example,
 * temperature conversions between Celsius and Fahrenheit use the formula:
 * {@code F = C * 9/5 + 32}
 * </p>
 * <p>
 * Conversion equations are evaluated using the expression parser. Each complex unit
 * defines conversion formulas to other units in its group.
 * </p>
 *
 * @see ComplexUnit
 * @see SimpleUnitGroup
 */
public class ComplexUnitGroup extends AbstractUnitGroup<ComplexUnit> {
    private final MathEngine engine;

    private ComplexUnitGroup(String name, List<ComplexUnit> units) {
        super(name, units);
        this.engine = MathEngine.create();
    }

    /**
     * Creates a new complex unit group with the specified name and units.
     *
     * @param name  the display name for this unit group
     * @param units the units to include in this group
     * @return a new complex unit group
     * @throws NullPointerException if name or units is null
     */
    static ComplexUnitGroup of(String name, List<ComplexUnit> units) {
        return new ComplexUnitGroup(name, units);
    }

    @Override
    public ConversionResult convert(BigRational amount, String from, String to) {
        ComplexUnit fromUnit = findUnit(from);
        if (fromUnit == null) {
            return ConversionResult.partial(null, null, amount);
        }

        ComplexUnit toUnit = findUnit(to);
        if (toUnit == null) {
            return ConversionResult.partial(fromUnit, null, amount);
        }

        Optional<String> equation = fromUnit.getConversionEquationFor(toUnit);
        if (equation.isPresent()) {
            // Define the variable and evaluate the equation
            String varName = fromUnit.getVariable();
            engine.getContext().define(varName, new NodeDouble(amount.doubleValue()));
            double resultValue = engine.evaluateDouble(equation.get());
            BigRational result = BigRational.of(resultValue);
            return ConversionResult.success(fromUnit, toUnit, amount, result, getName());
        }

        // No conversion equation found - units are the same or incompatible
        return ConversionResult.success(fromUnit, toUnit, amount, amount, getName());
    }
}
