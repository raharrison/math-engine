package uk.co.ryanharrison.mathengine.parser.symbolic;

import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Converts AST nodes to postfix notation stack for symbolic computation.
 * Used by both symbolic differentiator and symbolic integrator.
 */
public final class TreeToStack {

    private TreeToStack() {
        throw new AssertionError("Utility class");
    }

    /**
     * Converts an AST node tree to a postfix notation stack.
     *
     * @param tree the root node of the expression tree
     * @return a stack of expression items in postfix order
     */
    public static Deque<ExpressionItem> treeToStack(Node tree) {
        var stack = new ArrayDeque<ExpressionItem>();
        convert(tree, stack);
        return stack;
    }

    private static void convert(Node tree, Deque<ExpressionItem> stack) {
        switch (tree) {
            // Handle numeric constants
            case NodeDouble _, NodeRational _ -> stack.add(new ExpressionItem(tree.toString()));

            // Handle variables
            case NodeVariable var -> stack.add(new ExpressionItem(var.getName()));

            // Handle binary operators
            case NodeBinary binary -> {
                char op = mapTokenToOperator(binary.getOperator().type());
                stack.add(ExpressionItem.operator(tree.toString(), op));
                convert(binary.getLeft(), stack);
                convert(binary.getRight(), stack);
            }

            // Handle unary operators (negation)
            case NodeUnary unary -> {
                TokenType opType = unary.getOperator().type();
                if (opType.equals(TokenType.MINUS)) {
                    // Handle as multiplication by -1
                    stack.add(ExpressionItem.operator(tree.toString(), '*'));
                    stack.add(new ExpressionItem("-1"));
                    convert(unary.getOperand(), stack);
                } else {
                    throw new UnsupportedOperationException(
                            "Unsupported unary operator: " + opType.name());
                }
            }

            // Handle function calls (sin, cos, sqrt, etc.)
            case NodeCall call -> {
                Node funcNode = call.getFunction();
                if (!(funcNode instanceof NodeVariable funcVar)) {
                    throw new UnsupportedOperationException(
                            "Symbolic computation only supports simple function names");
                }
                if (call.getArguments().size() != 1) {
                    throw new UnsupportedOperationException(
                            "Symbolic computation only supports single-argument functions");
                }
                stack.add(ExpressionItem.function(tree.toString(), funcVar.getName()));
            }

            default -> throw new UnsupportedOperationException(
                    "Unsupported node: " + tree.typeName());
        }
    }

    private static char mapTokenToOperator(TokenType type) {
        return switch (type) {
            case TokenType.PLUS -> '+';
            case TokenType.MINUS -> '-';
            case TokenType.MULTIPLY -> '*';
            case TokenType.DIVIDE -> '/';
            case TokenType.POWER -> '^';
            default -> throw new UnsupportedOperationException(
                    "Unsupported binary operator: " + type.name()
            );
        };
    }
}
