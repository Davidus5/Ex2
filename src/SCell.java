// Add your documentation below:

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SCell implements Cell {
    private String line;
    private String data;
    private int type;
    private int order;

    public SCell(String s) {
        setData(s);
        setContent(s);
    }

    public boolean isNumber(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isText(String text) {
        return !text.startsWith("=") && !isNumber(text);
    }

    public boolean isForm(String text) {
        return text.startsWith("=");
    }

    public Double computeForm(String form) {
        try {
            if (form.startsWith("=")) {
                form = form.substring(1); // Removes '=' to evaluate the expression
            }
            // Basic expression evaluation
            return evaluateExpression(form);
        } catch (Exception e) {
            return null; // Invalid formula
        }
    }

    private Double evaluateExpression(String expression) {
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);

            if (Character.isDigit(current) || current == '.') {
                // Extract full number (could be multi-digit or a float)
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    sb.append(expression.charAt(i++));
                }
                values.push(Double.parseDouble(sb.toString()));
                i--; // Step back because the loop will increment i
            } else if (current == '(') {
                ops.push(current);
            } else if (current == ')') {
                while (ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.pop(); // pop the '('
            } else if (isOperator(current)) {
                while (!ops.isEmpty() && hasPrecedence(current, ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(current);
            } else if (Character.isLetter(current)) {
                // Handle references like A1, B2, etc.
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && (Character.isLetter(expression.charAt(i)) || Character.isDigit(expression.charAt(i)))) {
                    sb.append(expression.charAt(i++));
                }
                values.push(handleReference(sb.toString())); // Placeholder for handling cell references
                i--; // Step back because the loop will increment i
            }
        }

        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private Double handleReference(String ref) {
        // Placeholder method for handling cell references like A3
        // In a real implementation, this should look up the value of the referenced cell
        return 1.0; // Returning a dummy value for now
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        return (op1 != '*' && op1 != '/') || (op2 != '+' && op2 != '-');
    }

    private Double applyOp(char op, Double b, Double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
        }
        return 0.0;
    }

    public String getContent() {
        return line; // Returns the cell's content as a string
    }

    public void setContent(String s) {
        if (type == Ex2Utils.ERR_FORM_FORMAT) {
            line = Ex2Utils.ERR_FORM;
            data = Ex2Utils.ERR_FORM;
        } else if (type == Ex2Utils.ERR_CYCLE_FORM) {
            line = Ex2Utils.ERR_CYCLE;
            data = Ex2Utils.ERR_CYCLE;
        } else {
            line = s;
            data = s.toUpperCase();
        }
    }

    @Override
    public void setData(String s) {
        if (isNumber(s)) {
            type = Ex2Utils.NUMBER;
            data = s;
            line = s;
        } else if (isText(s)) {
            type = Ex2Utils.TEXT;
            data = s.toUpperCase();
            line = s;
        } else if (isForm(s)) {
            type = checkForm(s);
            if (type == Ex2Utils.ERR_FORM_FORMAT) {
                data = Ex2Utils.ERR_FORM;
                line = Ex2Utils.ERR_FORM;
                return;
            }
            if (hasCycle(new HashSet<>())) {
                type = Ex2Utils.ERR_CYCLE_FORM;
                data = Ex2Utils.ERR_CYCLE;
                line = Ex2Utils.ERR_CYCLE;
                return;
            }
            data = s.toUpperCase();
            line = s;
        } else {
            type = Ex2Utils.ERR_FORM_FORMAT;
            data = Ex2Utils.ERR_FORM;
            line = Ex2Utils.ERR_FORM;
        }
    }

    private int checkForm(String s) {
        s = s.toUpperCase();
        char[] arr = s.toCharArray();
        String ops = "+-*/";
        String abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String nums = "0123456789";
        int paren = 0;

        if (arr.length < 2 || arr[0] != '=') {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        char prev = arr[1];
        if (!(abc.indexOf(prev) != -1 || nums.indexOf(prev) != -1 || prev == '(')) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        for (int i = 2; i < arr.length; i++) {
            char current = arr[i];

            if (!(ops.indexOf(current) != -1 || abc.indexOf(current) != -1 ||
                    nums.indexOf(current) != -1 || current == '.' ||
                    current == '(' || current == ')')) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (prev == '(' && (ops.indexOf(current) != -1 || current == ')')) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }
            if (ops.indexOf(prev) != -1 && (ops.indexOf(current) != -1 || current == ')')) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (current == '(') {
                paren++;
            }
            if (current == ')') {
                paren--;
                if (paren < 0) {
                    return Ex2Utils.ERR_FORM_FORMAT;
                }
            }

            prev = current;
        }

        if (paren != 0 || ops.indexOf(prev) != -1) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        return Ex2Utils.FORM;
    }

    private boolean hasCycle(Set<Cell> visitedCells) {
        if (visitedCells.contains(this)) {
            return true;
        }
        visitedCells.add(this);

        if (type == Ex2Utils.FORM) {
            String[] dependencies = parseDependencies(line);
            for (String dep : dependencies) {
                Cell refCell = getCellFromReference(dep);
                if (refCell != null && ((SCell) refCell).hasCycle(new HashSet<>(visitedCells))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] parseDependencies(String formula) {
        return new String[0];
    }

    private Cell getCellFromReference(String reference) {
        return null;
    }

    @Override
    public String toString() {
        if (type == Ex2Utils.ERR_FORM_FORMAT || type == Ex2Utils.ERR_CYCLE_FORM) {
            return line;
        }
        return data;
    }

    @Override
    public String getData() {
        if (type == Ex2Utils.ERR_FORM_FORMAT || type == Ex2Utils.ERR_CYCLE_FORM) {
            return line;
        }
        return data;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        this.order = t;
    }

    @Override
    public int getOrder() {
        if (type == Ex2Utils.NUMBER || type == Ex2Utils.TEXT) {
            return 0;
        }
        if (type == Ex2Utils.FORM) {
            return order;
        }
        return -1;
    }
}

