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

    private int checkForm(String s) {
        if (s.length() < 2) return Ex2Utils.ERR_FORM_FORMAT;

        // Remove the '=' sign for checking
        s = s.substring(1).toUpperCase();

        // Empty formula
        if (s.isEmpty() || s.equals("()")) return Ex2Utils.ERR_FORM_FORMAT;

        Stack<Character> parentheses = new Stack<>();
        char prev = ' ';
        boolean hasOperand = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            // Check for valid characters
            if (!isValidChar(c)) return Ex2Utils.ERR_FORM_FORMAT;

            // Handle parentheses
            if (c == '(') {
                parentheses.push(c);
                if (isOperand(prev)) return Ex2Utils.ERR_FORM_FORMAT;
            }
            else if (c == ')') {
                if (parentheses.isEmpty()) return Ex2Utils.ERR_FORM_FORMAT;
                parentheses.pop();
                if (!hasOperand) return Ex2Utils.ERR_FORM_FORMAT;
            }
            // Handle operators
            else if (isOperator(c)) {
                if (i == 0 && c != '-') return Ex2Utils.ERR_FORM_FORMAT;
                if (i == s.length() - 1) return Ex2Utils.ERR_FORM_FORMAT;
                if (isOperator(prev) || prev == '(') return Ex2Utils.ERR_FORM_FORMAT;
            }
            // Handle digits
            else if (Character.isDigit(c) || c == '.') {
                if (prev == ')') return Ex2Utils.ERR_FORM_FORMAT;
                hasOperand = true;
            }
            // Handle cell references
            else if (Character.isLetter(c)) {
                if (prev == ')' || isOperand(prev)) return Ex2Utils.ERR_FORM_FORMAT;
                // Collect the full cell reference
                StringBuilder ref = new StringBuilder().append(c);
                while (i + 1 < s.length() && Character.isDigit(s.charAt(i + 1))) {
                    ref.append(s.charAt(++i));
                }
                hasOperand = true;
            }

            prev = c;
        }

        // Check for unmatched parentheses
        if (!parentheses.isEmpty()) return Ex2Utils.ERR_FORM_FORMAT;

        // Must have at least one operand
        if (!hasOperand) return Ex2Utils.ERR_FORM_FORMAT;

        return Ex2Utils.FORM;
    }

    private boolean isValidChar(char c) {
        return Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '*' || c == '/' ||
                c == '(' || c == ')' || c == '.';
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean isOperand(char c) {
        return Character.isLetterOrDigit(c) || c == ')';
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
        if (!formula.startsWith("=")) return new String[0];

        formula = formula.substring(1).toUpperCase();
        Set<String> deps = new HashSet<>();
        StringBuilder currentRef = new StringBuilder();

        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (Character.isLetter(c)) {
                currentRef = new StringBuilder().append(c);
                while (i + 1 < formula.length() && Character.isDigit(formula.charAt(i + 1))) {
                    currentRef.append(formula.charAt(++i));
                }
                deps.add(currentRef.toString());
            }
        }

        return deps.toArray(new String[0]);
    }

    private Cell getCellFromReference(String reference) {
        // This should be implemented based on your spreadsheet implementation
        return null;
    }

    @Override
    public String getData() {
        if (type == Ex2Utils.ERR_FORM_FORMAT || type == Ex2Utils.ERR_CYCLE_FORM) {
            return line;
        }
        return data;
    }

    @Override
    public String getContent() {
        return line;
    }

    @Override
    public String toString() {
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