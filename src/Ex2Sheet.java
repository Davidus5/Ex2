import java.util.Stack;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
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
            return Integer.parseInt(c.substring(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        // Add your code here

        Cell c = get(x,y);
        if(c!=null) {
            ans = c.toString();
            if(c.getType() == Ex2Utils.FORM){
                ans = eval(x, y);
            }
        }


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

        table[x][y].setData(s);

        // Update the cell content
        // eval(); // Recompute the values of all cells
    }

    @Override
    public void eval() {
        int[][] depths = depth(); // Calculate computational depths
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (depths[x][y] == -1) {
                    // Handle circular reference
                    table[x][y].setType(Ex2Utils.ERR_CYCLE_FORM);
                } else {
                    // Recompute the cell value
                    set(x, y, value(x, y));
                }
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
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
        if (!isIn(x, y)) return -1; // Out of bounds
        String key = x + "," + y;
        if (!visited.add(key)) return -1; // Circular reference detected

        Cell cell = get(x, y);
        if (cell instanceof SCell) {
            SCell sCell = (SCell) cell;
            String content = sCell.getContent();

            if (sCell.isNumber(content) || sCell.isText(content)) {
                return 0; // Numbers and text have no dependencies
            } else if (sCell.isForm(content)) {
                String formula = content.substring(1); // Remove '='
                formula = formula.toUpperCase();
                int maxDepth = 0;

                // Split the formula into parts and evaluate dependencies
                for (String part : formula.split("[+\\-*/()]")) {
                    part = part.trim();
                    if (isCellReference(part)) {
                        int depX = xCell(part);
                        int depY = yCell(part);
                        int depDepth = computeDepth(depX, depY, visited);
                        if (depDepth == -1) return -1; // Circular dependency
                        maxDepth = Math.max(maxDepth, depDepth);
                    }
                }
                return maxDepth + 1; // Add one for this formula's depth
            }
        }
        return -1; // Invalid content
    }


    @Override
    public void load(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        int rows = lines.size();
        int cols = lines.get(0).split("\t").length;

        // Resize the table to match the loaded data
        table = new SCell[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] cells = lines.get(i).split("\t");
            for (int j = 0; j < cells.length; j++) {
                table[i][j] = new SCell(cells[j]);
            }
        }
        eval(); // Re-evaluate all formulas after loading
    }


    @Override
    public void save(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                sb.append(get(i, j).toString());
                if (j < height() - 1) {
                    sb.append("\t");
                }
            }
            sb.append(System.lineSeparator());
        }

        Files.write(Paths.get(fileName), sb.toString().getBytes());
    }

    @Override
    public Double computeForm(String form) {
        if (form == null || !form.startsWith("=")) {
            return null; // Invalid formula
        }

        form = form.toUpperCase();

        try {
            // Remove the '=' prefix
            String expression = form.substring(1).trim();

            // Replace cell references with their evaluated values
            for (String part : expression.split("[+\\-*/()]")) {
                part = part.trim();
                if (isCellReference(part)) {
                    Double cellValue = evaluateCellReference(part);
                    if (cellValue == null) {
                        return null; // Invalid cell reference
                    }
                    // Replace the reference in the expression
                    expression = expression.replace(part, cellValue.toString());
                }
            }

            // Evaluate the resulting expression
            return evaluateExpression(expression);
        } catch (Exception e) {
            return null; // Handle invalid expressions gracefully
        }
    }



    public static Double evaluateExpression(String expression) {
        try {
            return evaluate(expression);
        } catch (Exception e) {
            return null;
        }
    }

    private static double evaluate(String expression) {
        char[] tokens = expression.toCharArray();

        // Stack for numbers
        Stack<Double> values = new Stack<>();

        // Stack for operators
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            // Skip whitespace
            if (tokens[i] == ' ') continue;

            // If it's a number, parse the entire number
            if (Character.isDigit(tokens[i])) {
                StringBuilder sb = new StringBuilder();
                while (i < tokens.length && (Character.isDigit(tokens[i]) || tokens[i] == '.')) {
                    sb.append(tokens[i++]);
                }
                values.push(Double.parseDouble(sb.toString()));
                i--; // Step back because the loop will increment
            } else if (tokens[i] == '(') {
                ops.push(tokens[i]);
            } else if (tokens[i] == ')') {
                while (ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.pop(); // Remove '('
            } else if (isOperator(tokens[i])) {
                // While top of 'ops' has same or greater precedence, apply operator
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(tokens[i])) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(tokens[i]);
            }
        }

        // Apply remaining operators
        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private static boolean isOperator(char op) {
        return op == '+' || op == '-' || op == '*' || op == '/';
    }

    private static int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    private static double applyOp(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            default:
                throw new UnsupportedOperationException("Unsupported operator");
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
