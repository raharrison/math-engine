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
        Deque<ExpressionItem> stack = new ArrayDeque<>();
        convert(tree, stack);
        return stack;
    }

    private static void convert(Node tree, Deque<ExpressionItem> stack) {
        // Handle numeric constants
        if (tree instanceof NodeDouble || tree instanceof NodeRational) {
            stack.add(new ExpressionItem(tree.toString()));
        }
        // Handle variables and constants
        else if (tree instanceof NodeVariable var) {
            stack.add(new ExpressionItem(var.getName()));
        }
        // Handle binary operators
        else if (tree instanceof NodeBinary binary) {
            String treeStr = tree.toString();
            TokenType opType = binary.getOperator().type();

            // Map token type to operator character
            char op = mapTokenToOperator(opType);

            // Stack format: operator first, then operands (for processing with getFirst/pop)
            stack.add(ExpressionItem.operator(treeStr, op));
            convert(binary.getLeft(), stack);
            convert(binary.getRight(), stack);
        }
        // Handle unary operators (negation)
        else if (tree instanceof NodeUnary unary) {
            TokenType opType = unary.getOperator().type();

            if (opType.equals(TokenType.MINUS)) {
                // Handle as multiplication by -1
                stack.add(ExpressionItem.operator(tree.toString(), '*'));
                stack.add(new ExpressionItem("-1"));
                convert(unary.getOperand(), stack);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported unary operator: " + opType.getName());
            }
        }
        // Handle function calls (sin, cos, sqrt, etc.)
        else if (tree instanceof NodeCall call) {
            // Extract function name from the function node (should be a NodeVariable)
            Node funcNode = call.getFunction();
            if (!(funcNode instanceof NodeVariable funcVar)) {
                throw new UnsupportedOperationException(
                        "Symbolic computation only supports simple function names");
            }

            // Only single-argument functions are supported
            if (call.getArguments().size() != 1) {
                throw new UnsupportedOperationException(
                        "Symbolic computation only supports single-argument functions");
            }

            stack.add(ExpressionItem.function(tree.toString(), funcVar.getName()));
        } else {
            throw new UnsupportedOperationException("Unsupported node: "
                    + tree.getClass().getCanonicalName());
        }
    }

    private static char mapTokenToOperator(TokenType opType) {
        if (opType.equals(TokenType.PLUS)) {
            return '+';
        } else if (opType.equals(TokenType.MINUS)) {
            return '-';
        } else if (opType.equals(TokenType.MULTIPLY)) {
            return '*';
        } else if (opType.equals(TokenType.DIVIDE)) {
            return '/';
        } else if (opType.equals(TokenType.POWER)) {
            return '^';
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported binary operator: " + opType.getName());
        }
    }
}
