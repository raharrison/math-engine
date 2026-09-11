package uk.co.ryanharrison.mathengine.parser.function;

import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.ast.NodeVector;
import uk.co.ryanharrison.mathengine.parser.evaluator.ArityException;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.RecursionTracker;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FunctionExecutorTest {

    private static final double TOLERANCE = 1e-9;

    private final FunctionExecutor executor = FunctionExecutor.of(StandardFunctions.all());

    private EvaluationContext context() {
        return new EvaluationContext(MathEngineConfig.defaults(), new RecursionTracker(100));
    }

    private NodeConstant execute(String name, NodeConstant... args) {
        return executor.execute(name, List.of(args), context(), null);
    }

    @Test
    void functionsAreFoundByNameAndByAlias() {
        assertThat(executor.hasFunction("sqrt")).isTrue();
        assertThat(executor.hasFunction("SQRT")).isTrue();
        assertThat(executor.hasFunction("strlen")).isTrue();
        assertThat(executor.hasFunction("not_a_function")).isFalse();
    }

    @Test
    void anUnknownNameIsReported() {
        assertThatThrownBy(() -> execute("not_a_function"))
                .isInstanceOf(EvaluationException.class)
                .hasMessageContaining("Unknown function");
    }

    @Test
    void tooFewArgumentsAreReported() {
        assertThatThrownBy(() -> execute("hypot", new NodeDouble(1)))
                .isInstanceOf(ArityException.class)
                .hasMessageContaining("at least 2");
    }

    @Test
    void tooManyArgumentsAreReported() {
        assertThatThrownBy(() -> execute("hypot", new NodeDouble(1), new NodeDouble(2), new NodeDouble(3)))
                .isInstanceOf(ArityException.class)
                .hasMessageContaining("at most 2");
    }

    @Test
    void severalArgumentsToAUnaryFunctionAreReadAsAVector() {
        NodeConstant result = execute("sqrt", new NodeDouble(4), new NodeDouble(9), new NodeDouble(16));

        assertThat(result).isInstanceOf(NodeVector.class);
        NodeVector vector = (NodeVector) result;
        assertThat(vector.size()).isEqualTo(3);
        assertThat(((NodeConstant) vector.getElement(2)).doubleValue()).isCloseTo(4.0, within(TOLERANCE));
    }

    @Test
    void registeringAClashingNameIsRejected() {
        MathFunction clashing = FunctionBuilder
                .named("sqrt")
                .takingUnary()
                .implementedByDouble(value -> value);

        assertThatThrownBy(() -> FunctionExecutor.builder().addAll(StandardFunctions.all()).add(clashing))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void registeringAClashingAliasIsRejected() {
        MathFunction clashing = FunctionBuilder
                .named("definitely_unique_name")
                .alias("ln")
                .takingUnary()
                .implementedByDouble(value -> value);

        assertThatThrownBy(() -> FunctionExecutor.builder().addAll(StandardFunctions.all()).add(clashing))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void everyStandardFunctionRegistersWithoutClashing() {
        assertThatCode(() -> FunctionExecutor.of(StandardFunctions.all())).doesNotThrowAnyException();
    }
}
