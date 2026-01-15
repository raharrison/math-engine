package uk.co.ryanharrison.mathengine.differential.symbolic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.core.Function;

public class SymbolicTest {
    @Test
    public void testSymbolicDifferentiation() {
        Differentiator d = new Differentiator();

        asserts("(x+3)+(x+2)", d.differentiate(new Function("(x+3)*(x+2)"), true).getEquation());

        asserts("sin(x)*x^(sin(x)-1)+x^sin(x)*ln(x)*cos(x)", d.differentiate(new Function("x^sin(x)"), true).getEquation());

        asserts("(cos(x)*x-sin(x))/x^2", d.differentiate(new Function("sin(x)/x"), true).getEquation());

        asserts("(x+3)*cos(x)+(sin(x)+2)", d.differentiate(new Function("(x+3)*(sin(x)+2)"), true).getEquation());

        asserts("2*x+8", d.differentiate(new Function("x^2 + 8*x + 12"), true).getEquation());

        asserts("(((1-2*3*2*(2*x)/abs(2*x)*cos(3*abs(2*x)))+4*2*x)*(((3*x)+4)-cos(2*(x^2)))-(((x+3)-(2*sin(3*abs(2*x))))+(4*(x^2)))*(3-2*2*x*-sin(2*(x^2))))/(((3*x)+4)-cos(2*(x^2)))^2", d.differentiate(new Function("((x+3)-2*sin(3*abs(2*x)) + 4*x^2)/(3*x + 4 - cos(2*x^2))"), true).getEquation());
    }

    private static void asserts(String expected, String actual) {
        if (!expected.equalsIgnoreCase(actual.replace(" ", ""))) {
            Assertions.fail("'" + expected + "' does not equal '" + actual + "'");
        }
    }
}
