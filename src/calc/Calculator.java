package calc;

import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;

class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        } else {
            List<String> tokens = tokenize(expr);
            List<String> postfix = infix2Postfix(tokens);
            return evalPostfix(postfix);
        }
    }
    // Check for negativ integer
    private boolean isNegative(String prev,  String current) {
        if(current.equals("-") && (prev.equals("") || OPERATORS.contains(prev) || prev.equals("("))){
            return true;
        }
        return false;
    }

    // ------  Evaluate RPN expression -------------------

    double evalPostfix(List<String> postfix) {
        List<String> stack = new ArrayList<>();
        for (String ele : postfix) {
            if (OPERATORS.contains(ele)) {
                double d1;
                double d2;
                try {
                    d1 = Double.parseDouble(stack.get(stack.size() - 1));
                    d2 = Double.parseDouble(stack.get(stack.size() - 2));
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new IllegalArgumentException(MISSING_OPERAND);
                }
                stack.add(String.valueOf(applyOperator(ele, d1, d2)));
                stack.remove(stack.size() - 2);
                stack.remove(stack.size() - 2);
            } else {
                stack.add(ele);
            }
        }

        if (stack.size() > 1) {
            throw new IllegalArgumentException(MISSING_OPERATOR);
        }

        return Double.parseDouble(stack.get(0));
    }

    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    List<String> infix2Postfix(List<String> list) {
        List<String> result = new ArrayList<>();
        List<String> stack = new ArrayList<>();
        int index = 0;
        boolean skipNext = false;

        for (String ele : list) {
            if(skipNext){
                skipNext = false;
                index++;
                continue;
            }

            if (ele.equals(")")) {
                infix2postfixRightParantases(result, stack);
            }

            // Check for negative integer

            else if(isNegative( (index == 0) ?  ""  : list.get(index-1) ,ele)){
                skipNext = true;
                String negativeN = "-" + list.get(index+1);
                result.add(negativeN);
            }

            else if (OPERATORS.contains(ele)) {
                if (stack.size() > 0 && OPERATORS.contains(stack.get(stack.size() - 1))) {
                    infix2postfixOperators(result, stack, ele);
                    if (stack.size() <= 0) {
                        stack.add(ele);
                    }
                } else {
                    stack.add(ele);
                }
            } else {
                if (ele.equals("(")) {
                    stack.add(ele);
                } else {
                    result.add(ele);
                }

            }
            index++;
        }

        for (int i = 0; i < stack.size(); ) {
            if (!stack.get(stack.size() - 1).equals("(")) {
                result.add(stack.get(stack.size() - 1));
                stack.remove(stack.size() - 1);
            } else {
                stack.remove(stack.size() - 1);
            }
        }

        return result;
    }

    private void infix2postfixRightParantases(List<String> result, List<String> stack) {
        for (int i = stack.size() - 1; i > 0; i--) {
            if (stack.get(i).equals("(")) {
                stack.remove(stack.get(i));
                i = 0;
            } else {
                result.add(stack.get(i));
                stack.remove(stack.get(i));
            }
        }
    }

    private void infix2postfixOperators(List<String> result, List<String> stack, String ele) {
        boolean satisfied = Boolean.FALSE;
        while (!satisfied && stack.size() > 0) {
            if (stack.get(stack.size() - 1).equals("(")) {
                stack.add(ele);
                satisfied = Boolean.TRUE;
            } else if (getPrecedence(stack.get(stack.size() - 1)) == getPrecedence(ele) || getPrecedence(stack.get(stack.size() - 1)) > getPrecedence(ele)) {
                if (getAssociativity(ele) == Assoc.RIGHT && getPrecedence(stack.get(stack.size() - 1)) == getPrecedence(ele)) {
                    stack.add(ele);
                    satisfied = Boolean.TRUE;
                } else {
                    result.add(stack.get(stack.size() - 1));
                    stack.remove(stack.size() - 1);
                }
            } else {
                stack.add(ele);
                satisfied = Boolean.TRUE;
            }
        }
    }

    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }


    // ---------- Tokenize -----------------------

    List<String> tokenize(String expr) {
        List<String> list = Arrays.asList(expr.split(""));
        List<String> retlist = new ArrayList<>();
        String element = "";
        String parentheses = "()";

        for (int i = 0; i < list.size(); i++) {
            String ele = list.get(i);
            if (!isNegative(i == 0 ? "" : list.get(i - 1), ele) && (OPERATORS.contains(ele) || parentheses.contains(ele) || ele.equals(" "))) {
                if (!element.isEmpty()) {
                    retlist.add(element);
                    element = "";
                }

                if (OPERATORS.contains(ele) || parentheses.contains(ele)) {
                    retlist.add(ele);
                }
            }
            else {
                element += ele;
            }
        }
        if (!element.isEmpty()) {
            retlist.add(element);
        }

        return retlist;
    }

}
