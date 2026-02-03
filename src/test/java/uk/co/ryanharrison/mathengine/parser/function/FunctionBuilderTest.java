package uk.co.ryanharrison.mathengine.parser.function;

import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.RecursionTracker;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link FunctionBuilder} fluent API.
 */
class FunctionBuilderTest {

    // ==================== Metadata Tests ====================

    @Test
    void builderCreatesUnaryFunctionWithMetadata() {
        MathFunction func = FunctionBuilder
                .named("test")
                .describedAs("Test function")
                .inCategory(MathFunction.Category.UTILITY)
                .takingUnary()
                .implementedByDouble(x -> x * 2);

        assertThat(func.name()).isEqualTo("test");
        assertThat(func.description()).isEqualTo("Test function");
        assertThat(func.category()).isEqualTo(MathFunction.Category.UTILITY);
        assertThat(func.minArity()).isEqualTo(1);
        assertThat(func.maxArity()).isEqualTo(1);
    }

    @Test
    void builderCreatesUnaryFunctionWithAliases() {
        MathFunction func = FunctionBuilder
                .named("test")
                .alias("alias1", "alias2")
                .takingUnary()
                .implementedByDouble(x -> x);

        assertThat(func.aliases()).containsExactly("alias1", "alias2");
    }

    // ==================== Arity Tests ====================

    @Test
    void builderCreatesUnaryFunction() {
        MathFunction func = FunctionBuilder
                .named("double")
                .takingUnary()
                .implementedByDouble(x -> x * 2);

        assertThat(func.minArity()).isEqualTo(1);
        assertThat(func.maxArity()).isEqualTo(1);
    }

    @Test
    void builderCreatesBinaryFunction() {
        MathFunction func = FunctionBuilder
                .named("add")
                .takingBinary()
                .implementedBy((a, b, ctx) -> new NodeDouble(
                        ctx.toNumber(a).doubleValue() + ctx.toNumber(b).doubleValue()));

        assertThat(func.minArity()).isEqualTo(2);
        assertThat(func.maxArity()).isEqualTo(2);
    }

    @Test
    void builderCreatesVariadicFunction() {
        MathFunction func = FunctionBuilder
                .named("sum")
                .takingVariadic(1)
                .implementedByAggregate((args, ctx) -> {
                    double sum = args.stream()
                            .mapToDouble(arg -> ctx.toNumber(arg).doubleValue())
                            .sum();
                    return new NodeDouble(sum);
                });

        assertThat(func.minArity()).isEqualTo(1);
        assertThat(func.maxArity()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void builderCreatesRangeArityFunction() {
        MathFunction func = FunctionBuilder
                .named("clamp")
                .takingBetween(2, 3)
                .implementedByAggregate((args, ctx) -> args.get(0));

        assertThat(func.minArity()).isEqualTo(2);
        assertThat(func.maxArity()).isEqualTo(3);
    }

    // ==================== Broadcasting Tests ====================

    @Test
    void unaryFunctionSupportsVectorBroadcastingByDefault() {
        MathFunction func = FunctionBuilder
                .named("double")
                .takingUnary()
                .implementedByDouble(x -> x * 2);

        assertThat(func.supportsVectorBroadcasting()).isTrue();
    }

    @Test
    void binaryFunctionSupportsVectorBroadcastingWhenEnabled() {
        MathFunction func = FunctionBuilder
                .named("add")
                .takingBinary()
                .withBroadcasting()
                .implementedBy((a, b, ctx) -> new NodeDouble(
                        ctx.toNumber(a).doubleValue() + ctx.toNumber(b).doubleValue()));

        assertThat(func.supportsVectorBroadcasting()).isTrue();
    }

    @Test
    void functionCanDisableBroadcasting() {
        MathFunction func = FunctionBuilder
                .named("test")
                .takingUnary()
                .noBroadcasting()
                .implementedByDouble(x -> x);

        assertThat(func.supportsVectorBroadcasting()).isFalse();
    }

    // ==================== Implementation Tests ====================

    @Test
    void implementedByDoubleCreatesWorkingFunction() {
        MathFunction square = FunctionBuilder
                .named("square")
                .takingUnary()
                .implementedByDouble(x -> x * x);

        FunctionContext ctx = createContext();
        NodeConstant result = square.apply(List.of(new NodeDouble(3.0)), ctx);

        assertThat(result).isInstanceOf(NodeDouble.class);
        assertThat(((NodeDouble) result).doubleValue()).isEqualTo(9.0);
    }

    @Test
    void implementedByUnaryCreatesWorkingFunction() {
        MathFunction negate = FunctionBuilder
                .named("negate")
                .takingUnary()
                .implementedBy((arg, ctx) -> new NodeDouble(-ctx.toNumber(arg).doubleValue()));

        FunctionContext ctx = createContext();
        NodeConstant result = negate.apply(List.of(new NodeDouble(5.0)), ctx);

        assertThat(result).isInstanceOf(NodeDouble.class);
        assertThat(((NodeDouble) result).doubleValue()).isEqualTo(-5.0);
    }

    @Test
    void implementedByBinaryCreatesWorkingFunction() {
        MathFunction multiply = FunctionBuilder
                .named("multiply")
                .takingBinary()
                .implementedBy((a, b, ctx) -> new NodeDouble(
                        ctx.toNumber(a).doubleValue() * ctx.toNumber(b).doubleValue()));

        FunctionContext ctx = createContext();
        NodeConstant result = multiply.apply(List.of(new NodeDouble(3.0), new NodeDouble(4.0)), ctx);

        assertThat(result).isInstanceOf(NodeDouble.class);
        assertThat(((NodeDouble) result).doubleValue()).isEqualTo(12.0);
    }

    @Test
    void implementedByAggregateCreatesWorkingFunction() {
        MathFunction sum = FunctionBuilder
                .named("sum")
                .takingVariadic(1)
                .implementedByAggregate((args, ctx) -> {
                    double total = args.stream()
                            .mapToDouble(arg -> ctx.toNumber(arg).doubleValue())
                            .sum();
                    return new NodeDouble(total);
                });

        FunctionContext ctx = createContext();
        NodeConstant result = sum.apply(List.of(
                new NodeDouble(1.0),
                new NodeDouble(2.0),
                new NodeDouble(3.0)
        ), ctx);

        assertThat(result).isInstanceOf(NodeDouble.class);
        assertThat(((NodeDouble) result).doubleValue()).isEqualTo(6.0);
    }

    // ==================== Validation Tests ====================

    @Test
    void inlineValidationWorksForUnaryFunction() {
        MathFunction positive = FunctionBuilder
                .named("positive")
                .takingUnary()
                .implementedBy((arg, ctx) -> {
                    double value = ctx.toNumber(arg).doubleValue();
                    ctx.requirePositive(value);
                    return new NodeDouble(value);
                });

        FunctionContext ctx = createContext();

        // Valid input
        NodeConstant result = positive.apply(List.of(new NodeDouble(5.0)), ctx);
        assertThat(((NodeDouble) result).doubleValue()).isEqualTo(5.0);

        // Invalid input
        assertThatThrownBy(() -> positive.apply(List.of(new NodeDouble(-5.0)), ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires positive");
    }

    @Test
    void inlineValidationWorksForBinaryFunction() {
        MathFunction divide = FunctionBuilder
                .named("divide")
                .takingBinary()
                .implementedBy((a, b, ctx) -> {
                    double aVal = ctx.toNumber(a).doubleValue();
                    double bVal = ctx.toNumber(b).doubleValue();
                    ctx.requireNonZero(bVal);
                    return new NodeDouble(aVal / bVal);
                });

        FunctionContext ctx = createContext();

        // Valid input
        NodeConstant result = divide.apply(List.of(new NodeDouble(10.0), new NodeDouble(2.0)), ctx);
        assertThat(((NodeDouble) result).doubleValue()).isEqualTo(5.0);

        // Invalid input
        assertThatThrownBy(() -> divide.apply(List.of(new NodeDouble(10.0), new NodeDouble(0.0)), ctx))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-zero");
    }

    // ==================== Broadcasting Integration Tests ====================

    @Test
    void binaryFunctionWithBroadcastingWorksWithVectors() {
        MathFunction multiply = FunctionBuilder
                .named("multiply")
                .takingBinary()
                .withBroadcasting()
                .implementedBy((a, b, ctx) -> new NodeDouble(
                        ctx.toNumber(a).doubleValue() * ctx.toNumber(b).doubleValue()));

        FunctionContext ctx = createContext();

        // Vector op Scalar
        NodeVector vec = new NodeVector(List.of(new NodeDouble(2.0), new NodeDouble(3.0), new NodeDouble(4.0)));
        NodeConstant result = multiply.apply(List.of(vec, new NodeDouble(10.0)), ctx);

        assertThat(result).isInstanceOf(NodeVector.class);
        NodeVector resultVec = (NodeVector) result;
        assertThat(resultVec.size()).isEqualTo(3);
        assertThat(((NodeDouble) resultVec.getElement(0)).doubleValue()).isEqualTo(20.0);
        assertThat(((NodeDouble) resultVec.getElement(1)).doubleValue()).isEqualTo(30.0);
        assertThat(((NodeDouble) resultVec.getElement(2)).doubleValue()).isEqualTo(40.0);
    }

    // ==================== Error Handling Tests ====================

    @Test
    void builderRequiresFunctionName() {
        assertThatThrownBy(() ->
                FunctionBuilder.named("")
                        .takingUnary()
                        .implementedByDouble(x -> x))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    void unaryImplementationRequiresUnaryArity() {
        assertThatThrownBy(() ->
                FunctionBuilder.named("test")
                        .takingBinary()
                        .implementedByDouble(x -> x))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unary functions must have arity [1,1]");
    }

    @Test
    void binaryImplementationRequiresBinaryArity() {
        assertThatThrownBy(() ->
                FunctionBuilder.named("test")
                        .takingUnary()
                        .implementedBy((a, b, ctx) -> new NodeDouble(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Binary functions must have arity [2,2]");
    }

    // ==================== Helper Methods ====================

    private FunctionContext createContext() {
        return new FunctionContext(
                "test",
                new EvaluationContext(MathEngineConfig.basic(), new RecursionTracker()),
                null
        );
    }
}
