import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    // Add your code here

    // ///////////////////
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    public int xCell(String c) {
        if (c.length() < 2) return -1;
        char colChar = c.charAt(0);
        return Character.isLetter(colChar) ? colChar - 'A' : -1;
    }

    public int yCell(String c) {
        try {
            return Integer.parseInt(c.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        // Add your code here

        Cell c = get(x,y);
        if(c!=null) {ans = c.toString();}


        /////////////////////
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        // Check if the input string is valid
        if (cords != null && cords.length() >= 2) {
            // Get the column index (A, B, C, ...) and row index (1, 2, 3, ...)
            int x = xCell(cords);
            int y = yCell(cords);

            // Check if the indices are valid
            if (x >= 0 && y >= 0 && isIn(x, y)) {
                ans = get(x, y); // Get the cell from the table
            }
        }
        return ans;
    }


    @Override
    public int width() {
        return table.length;
    }
    @Override
    public int height() {
        return table[0].length;
    }
    @Override
    public void set(int x, int y, String s) {
        Cell c = new SCell(s);
        table[x][y] = c; // Update the cell content
        eval(); // Recompute the values of all cells
    }


    public void set(int x, int y, Cell cell) {
        table[x][y] = cell;
    }

    @Override
    public void eval() {
        int[][] dd = depth();
        // Add your code here

        // ///////////////////
    }

    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx>=0 && yy>=0;
        // Add your code here

        /////////////////////
        return ans;
    }

    @Override
    public int[][] depth() {
        int rows = width();
        int cols = height();
        int[][] result = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = computeDepth(i, j, new HashSet<>());
            }
        }
        return result;
    }

    private int computeDepth(int x, int y, Set<String> visited) {
        SCell cell = new SCell(get(x, y));
        String content = cell.getContent();
        if (cell.isNumber(content) || cell.isText(content)) {
            return 0;
        } else if (cell.isForm(content)) {
            if (!visited.add(x + "," + y)) return -1; // Cycle detected
            return 1; // Placeholder
        }
        return -1;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Add your code here

        /////////////////////
    }

    @Override
    public void save(String fileName) throws IOException {
        // Add your code here

        /////////////////////
    }
    @Override
    public Double computeForm(String form) {
        if (form == null || !form.startsWith("=")) {
            return null; // Invalid formula if it doesn't start with "="
        }

        try {
            // Remove the "=" and evaluate the expression
            String expression = form.substring(1).trim();

            // Check if the expression is a cell reference
            if (isCellReference(expression)) {
                return evaluateCellReference(expression); // Handle cell references
            }

            // Evaluate arithmetic expressions
            return evaluateExpression(expression);
        } catch (Exception e) {
            return null; // Invalid formula
        }
    }


    // Helper method to evaluate basic arithmetic expressions
    private Double evaluateExpression(String expression) {
        // Handle formulas with operators
        for (String operator : Ex2Utils.M_OPS) {
            int opIndex = findOperatorIndex(expression, operator);
            if (opIndex != -1) {
                String leftExpr = expression.substring(0, opIndex).trim();
                String rightExpr = expression.substring(opIndex + operator.length()).trim();
                Double leftValue = evaluateExpression(leftExpr);
                Double rightValue = evaluateExpression(rightExpr);

                if (leftValue != null && rightValue != null) {
                    switch (operator) {
                        case "+": return leftValue + rightValue;
                        case "-": return leftValue - rightValue;
                        case "*": return leftValue * rightValue;
                        case "/": return rightValue != 0 ? leftValue / rightValue : null;
                    }
                }
            }
        }

        // If no operator is found, check for parentheses or a simple number
        if (expression.startsWith("(") && expression.endsWith(")")) {
            return evaluateExpression(expression.substring(1, expression.length() - 1));
        }

        try {
            return Double.parseDouble(expression); // Parse numbers
        } catch (NumberFormatException e) {
            return null; // Not a valid number
        }
    }


    // Helper method to evaluate cell references like A1, B2, etc.
    private Double evaluateCellReference(String ref) {
        int x = xCell(ref);
        int y = yCell(ref);

        if (x != -1 && y != -1 && isIn(x, y)) {
            Cell cell = get(x, y);
            if (cell instanceof SCell) {
                SCell sCell = (SCell) cell;
                String content = sCell.getContent();

                if (sCell.isNumber(content)) {
                    return Double.parseDouble(content);
                } else if (sCell.isForm(content)) {
                    return computeForm(content);
                }
            }
        }
        return null; // Invalid cell reference
    }


    // Helper to determine if a string is a valid cell reference
    private boolean isCellReference(String s) {
        if (s.length() < 2) return false;
        char col = s.charAt(0);
        String row = s.substring(1);
        return Character.isLetter(col) && Character.isDigit(row.charAt(0));
    }

    // Helper to find the operator index in an expression
    private int findOperatorIndex(String expression, String operator) {
        int depth = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (depth == 0 && expression.startsWith(operator, i)) {
                return i;
            }
        }
        return -1;

    }

    @Override
    public String[][] evalAll() {
        int rows = width();
        int cols = height();
        String[][] result = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = eval(i, j); // Compute and store the evaluated value
            }
        }
        return result;
    }





    /*
    public String eval(int x, int y) {
        String ans = null;
        if(get(x,y) != null) {
            ans = get(x,y).toString();
        }
        // Add your code here

        /////////////////////
        return ans;
    }*/

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell instanceof SCell) {
            SCell sCell = (SCell) cell;
            String content = sCell.getContent();

            if (sCell.isNumber(content)) {
                return content; // Return the number as a string
            } else if (sCell.isForm(content)) {
                Double result = computeForm(content);
                return (result == null) ? Ex2Utils.ERR_FORM : result.toString();
            } else if (sCell.isText(content)) {
                return content; // Return the text as is
            }
        }
        return Ex2Utils.EMPTY_CELL; // Default for empty or invalid cells
    }


}
