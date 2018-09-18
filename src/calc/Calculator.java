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
            LinkedList<String> tokens = tokenize(expr);
            LinkedList<String> postfix = infix2Postfix(tokens);
            return evalPostfix(postfix);
        }
    }
    // Check for negative integer
    private boolean isNegative(String prev,  String current) {
        return current.equals("-") && (prev.equals("") || OPERATORS.contains(prev) || prev.equals("("));
    }

    // ------  Evaluate RPN expression -------------------

    double evalPostfix(LinkedList<String> postfix) {
        LinkedList<String> stack = new LinkedList<>();
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
                stack.remove(stack.get(stack.size() - 2));
                stack.remove(stack.get(stack.size() - 2));
            } else {
                stack.add(ele);
            }
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

    LinkedList<String> infix2Postfix(LinkedList<String> list) {
        LinkedList<String> result = new LinkedList<>();
        LinkedList<String> stack = new LinkedList<>();
        int index = 0;
        boolean skipNext = false;

        for (String ele : list) {
            if(skipNext){
                skipNext = false;
                index++;
                continue;
            }

            if (ele.equals(")")) {
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

            // Check for negative integer

            else if(isNegative( (index == 0) ?  ""  : list.get(index-1) ,ele)){
                skipNext = true;
                String negativeN = "-" + list.get(index+1);
                result.add(negativeN);
            }

            else if (OPERATORS.contains(ele)) {
                boolean satisfied = false;
                if (stack.size() > 0 && OPERATORS.contains(stack.getLast())) {
                    while (!satisfied && stack.size() > 0) {
                        if (stack.getLast().equals("(")) {
                            stack.add(ele);
                            satisfied = true;
                        } else if (getPrecedence(stack.getLast()) == getPrecedence(ele) || getPrecedence(stack.getLast()) > getPrecedence(ele)) {
                            if (getAssociativity(ele) == Assoc.RIGHT && getPrecedence(stack.getLast()) == getPrecedence(ele)) {
                                stack.add(ele);
                                satisfied = false;
                            } else {
                                result.add(stack.removeLast());
                            }
                        } else {
                            stack.add(ele);
                            satisfied = true;
                        }
                    }
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
            if (!stack.getLast().equals("(")) {
                result.add(stack.removeLast());
            } else {
                stack.removeLast();
            }
        }

        return result;
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

    LinkedList<String> tokenize(String expr) {
        LinkedList<String> list = new LinkedList<>(Arrays.asList(expr.split("")));
        LinkedList<String> retlist = new LinkedList<>();
        StringBuilder element = new StringBuilder();
        String parentheses = "()";
        int operators = 0;
        int operands = 0;
        int par = 0;
        int index = 0;
        for (String ele : list) {
            if (!isNegative((index==0) ? "" : list.get(index-1),ele) && (OPERATORS.contains(ele) || parentheses.contains(ele) || ele.equals(" "))) {
                if (!element.toString().equals("")) {
                    retlist.add(element.toString());
                    element = new StringBuilder();
                    operands++;
                }
                if (OPERATORS.contains(ele)) {
                    retlist.add(ele);
                    operators++;
                } else if (parentheses.contains(ele)) {
                    retlist.add(ele);
                    par++;
                }
            } else {
                element.append(ele);
            }
            index++;
        }
        if (!element.toString().equals("")) {
            retlist.add(element.toString());
            operands++;
        }
        if (operators < operands - 1 || par % 2 != 0) {

            throw new IllegalArgumentException(MISSING_OPERATOR);
        }
        return retlist;
    }

}
