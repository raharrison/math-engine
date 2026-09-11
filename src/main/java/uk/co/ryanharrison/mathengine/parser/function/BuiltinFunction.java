package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.ast.Node;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;

import java.util.List;

/**
 * The {@link MathFunction} implementations produced by {@link FunctionBuilder}:
 * shared metadata plus a body. Eager bodies receive evaluated values, lazy bodies
 * receive argument nodes.
 */
final class BuiltinFunction {

    private BuiltinFunction() {
    }

    /**
     * Everything a function exposes apart from its body.
     */
    record Metadata(String name,
                    List<String> aliases,
                    String description,
                    List<List<String>> parameterSets,
                    MathFunction.Category category,
                    int minArity,
                    int maxArity,
                    boolean broadcasts) {
    }

    static MathFunction eager(Metadata meta, AggregateFunction body) {
        return new Eager(meta, body);
    }

    static MathFunction lazy(Metadata meta, LazyFunction.Body body) {
        return new Lazy(meta, body);
    }

    /**
     * Metadata accessors shared by both kinds.
     */
    private static abstract class Base implements MathFunction {

        final Metadata meta;

        Base(Metadata meta) {
            this.meta = meta;
        }

        @Override
        public String name() {
            return meta.name();
        }

        @Override
        public List<String> aliases() {
            return meta.aliases();
        }

        @Override
        public String description() {
            return meta.description();
        }

        @Override
        public List<List<String>> parameterSets() {
            return meta.parameterSets();
        }

        @Override
        public MathFunction.Category category() {
            return meta.category();
        }

        @Override
        public int minArity() {
            return meta.minArity();
        }

        @Override
        public int maxArity() {
            return meta.maxArity();
        }

        @Override
        public String toString() {
            return meta.name() + "/" + meta.minArity() +
                    (meta.maxArity() == meta.minArity() ? "" : ".." + meta.maxArity());
        }
    }

    private static final class Eager extends Base {

        private final AggregateFunction body;

        Eager(Metadata meta, AggregateFunction body) {
            super(meta);
            this.body = body;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return meta.broadcasts();
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            return body.apply(args, ctx);
        }
    }

    private static final class Lazy extends Base implements LazyFunction {

        private final LazyFunction.Body body;

        Lazy(Metadata meta, LazyFunction.Body body) {
            super(meta);
            this.body = body;
        }

        @Override
        public NodeConstant applyLazy(List<Node> args, FunctionContext ctx, ArgumentEvaluator evaluate) {
            return body.apply(args, ctx, evaluate);
        }
    }
}
