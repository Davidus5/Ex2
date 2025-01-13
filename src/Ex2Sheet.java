import java.util.*; // Import utility classes such as Stack, Set, and HashSet
import java.io.IOException; // Import IOException for handling file input/output errors
import java.nio.file.Files; // Import Files class for reading/writing files
import java.nio.file.Paths; // Import Paths class for working with file paths

public class Ex2Sheet implements Sheet { // Class declaration, implementing the Sheet interface
    private Cell[][] table; // A 2D array representing the spreadsheet grid

    public Ex2Sheet(int x, int y) { // Constructor to initialize a spreadsheet with x rows and y columns
        table = new SCell[x][y]; // Create a grid of SCell objects
        for (int i = 0; i < x; i++) { // Loop through each row
            for (int j = 0; j < y; j++) { // Loop through each column
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL); // Initialize each cell with an empty value
            }
        }
        eval(); // Evaluate the entire spreadsheet after initialization
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    public int xCell(String c) { // Convert a column letter (e.g., "A") to a numerical index
        if (c.length() < 2) return -1; // Return -1 if the string is too short to be a valid cell reference
        char colChar = c.charAt(0); // Extract the first character (column letter)
        return Character.isLetter(colChar) ? colChar - 'A' : -1; // Calculate the column index or return -1 if invalid
    }

    public int yCell(String c) { // Extract the row number from a cell reference
        try {
            return Integer.parseInt(c.substring(1)); // Parse the numeric part of the string
        } catch (NumberFormatException e) { // Handle invalid formats
            return -1; // Return -1 if parsing fails
        }
    }

    private boolean isValidFormulaFormat(String formula) {
        // Must start with '=' for formulas
        if (formula == null || !formula.startsWith("=")) {
            return false;
        }

        String expression = formula.substring(1).trim();

        // Check for empty expression or just parentheses
        if (expression.isEmpty() || expression.equals("()")) {
            return false;
        }

        // Check for invalid characters (@) or patterns (**)
        if (expression.contains("@") ||
                expression.contains("**") ||
                expression.matches(".*[+\\-*/]{2,}.*")) {
            return false;
        }

        // Check for expressions ending with operators or starting with binary operators
        if (expression.matches(".*[+\\-*/]$") ||
                expression.matches("^[*/].*")) {
            return false;
        }

        // Check for expressions with unbalanced operators like "2+)"
        int openParens = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') openParens++;
            if (c == ')') {
                openParens--;
                if (openParens < 0) return false;  // More closing than opening parentheses
            }
        }
        if (openParens != 0) return false;  // Unmatched parentheses

        // Check for valid operators and placement
        char[] chars = expression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (isOperator(chars[i])) {
                // Check if operator is at start or end
                if (i == 0 || i == chars.length - 1) return false;
                // Check if operator is next to another operator
                if (i > 0 && isOperator(chars[i-1])) return false;
                if (i < chars.length-1 && isOperator(chars[i+1])) return false;
            }
        }

        return true;
    }

    private boolean hasSelfReference(int x, int y, String formula) {
        if (!formula.startsWith("=")) return false;

        String cellRef = String.format("%c%d", (char)('A' + x), y);
        String expression = formula.substring(1).toUpperCase();

        // Split by operators and check each part
        for (String part : expression.split("[+\\-*/()]")) {
            part = part.trim();
            if (part.equals(cellRef)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String value(int x, int y) { // Get the value of a cell at (x, y)
        String ans = Ex2Utils.EMPTY_CELL; // Default to an empty cell
        Cell c = get(x, y); // Retrieve the cell object
        if (c != null) { // Check if the cell exists
            if (c.getType() == Ex2Utils.FORM) { // If the cell contains a formula
                ans = eval(x, y); // Evaluate the formula
            } else {
                ans = c.toString(); // Otherwise, get the cell's string value
            }
        }
        return ans; // Return the cell's value
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) { // Get a Cell object using string coordinates (e.g., "A1")
        Cell ans = null;
        if (cords != null && cords.length() >= 2) { // Validate the input
            int x = xCell(cords); // Get the column index
            int y = yCell(cords); // Get the row index
            if (x >= 0 && y >= 0 && isIn(x, y)) { // Check if the indices are valid
                ans = get(x, y); // Retrieve the cell
            }
        }
        return ans; // Return the cell or null if invalid
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
        ((SCell) table[x][y]).setContent(value(x,y));
        eval();
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y); // Retrieves the cell at position (x, y).
        if (cell instanceof SCell) { // Checks if the cell is an instance of SCell.
            SCell sCell = (SCell) cell; // Casts the cell to an SCell object.
            String content = sCell.getData(); // Gets the raw data of the cell.

            if (sCell.isForm(content)) { // If the content is identified as a formula:
                if (hasSelfReference(x, y, content)) { // Checks for self-referencing in the formula.
                    sCell.setType(Ex2Utils.ERR_CYCLE_FORM); // Marks the cell as having a cyclic dependency.
                    return Ex2Utils.ERR_CYCLE; // Returns a cycle error message.
                }

                if (!isValidFormulaFormat(content)) { // Validates the format of the formula.
                    sCell.setType(Ex2Utils.ERR_FORM_FORMAT); // Marks the cell as having a format error.
                    return Ex2Utils.ERR_FORM; // Returns a format error message.
                }

                try {
                    Double result = computeForm(content); // Evaluates the formula.
                    if (result == null) { // If the formula evaluation fails:
                        sCell.setType(Ex2Utils.ERR_FORM_FORMAT); // Mark as a format error.
                        return Ex2Utils.ERR_FORM; // Return a format error message.
                    }
                    sCell.setType(Ex2Utils.FORM); // Marks the cell as containing a valid formula.
                    return result.toString(); // Returns the computed result as a string.
                } catch (Exception e) { // Handles any exceptions during formula computation.
                    sCell.setType(Ex2Utils.ERR_FORM_FORMAT); // Marks as a format error.
                    return Ex2Utils.ERR_FORM; // Returns a format error message.
                }
            } else if (sCell.isNumber(content)) { // If the content is a number:
                return content; // Return the number as a string.
            } else if (sCell.isText(content)) { // If the content is text:
                return content; // Return the text as is.
            }
        }
        return Ex2Utils.EMPTY_CELL; // Return an empty cell value if none of the above conditions are met.
    }

    @Override
    public void eval() {
        int[][] depths = depth(); // Computes the depth (dependency levels) of each cell.
        for (int x = 0; x < width(); x++) { // Iterate through all rows.
            for (int y = 0; y < height(); y++) { // Iterate through all columns.
                if (depths[x][y] == -1) { // If a cell has a cyclic dependency:
                    table[x][y].setType(Ex2Utils.ERR_CYCLE_FORM); // Mark the cell as having a cycle error.
                } else {
                    ((SCell) table[x][y]).setContent(value(x, y)); // Update the content with the evaluated value.
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
        // Recursively computes the depth of a cell, considering dependencies.

        if (!isIn(x, y)) return -1; // Return -1 if (x, y) is out of bounds.
        String key = x + "," + y; // Creates a unique key for the cell.
        if (!visited.add(key)) return -1; // If the cell is already visited, return -1 (cycle detected).

        Cell cell = get(x, y); // Retrieves the cell at (x, y).
        if (cell instanceof SCell) { // Checks if the cell is of type SCell.
            SCell sCell = (SCell) cell;
            String content = sCell.getContent(); // Gets the content of the cell.

            if (sCell.isNumber(content) || sCell.isText(content)) {
                // If the cell contains a number or plain text, its depth is 0.
                return 0;
            } else if (sCell.isForm(content)) {
                // If the cell contains a formula, calculate its depth.
                String formula = content.substring(1).toUpperCase(); // Remove '=' and convert to uppercase.
                int maxDepth = 0;

                // Splits the formula into parts based on operators.
                for (String part : formula.split("[+\\-*/()]")) {
                    part = part.trim(); // Remove surrounding whitespace.
                    if (isCellReference(part)) {
                        // If the part is a cell reference, compute its depth recursively.
                        int depX = xCell(part); // Get x-coordinate from the cell reference.
                        int depY = yCell(part); // Get y-coordinate from the cell reference.
                        int depDepth = computeDepth(depX, depY, visited); // Recursively compute depth.

                        if (depDepth == -1) return -1; // If a cycle is detected, return -1.
                        maxDepth = Math.max(maxDepth, depDepth); // Update the maximum depth.
                    }
                }
                return maxDepth + 1; // The depth is one more than the maximum depth of dependencies.
            }
        }
        return -1; // Return -1 if the cell type is unhandled.
    }

    @Override
    public Double computeForm(String form) {
        // Computes the result of a formula in a cell.

        if (!isValidFormulaFormat(form)) {
            // If the formula format is invalid, return null.
            return null;
        }

        form = form.toUpperCase(); // Convert the formula to uppercase.
        try {
            String expression = form.substring(1).trim(); // Remove '=' and trim whitespace.

            // Replace cell references with their values in the expression.
            for (String part : expression.split("[+\\-*/()]")) {
                part = part.trim(); // Remove surrounding whitespace.
                if (!part.isEmpty() && isCellReference(part)) {
                    Cell referencedCell = get(part); // Retrieve the referenced cell.
                    if (referencedCell == null) {
                        return null; // Return null if the referenced cell is null.
                    }
                    String value = eval(xCell(part), yCell(part)); // Evaluate the referenced cell.

                    if (value.equals(Ex2Utils.ERR_FORM) || value.equals(Ex2Utils.EMPTY_CELL)) {
                        // Return null if the referenced cell contains an error or is empty.
                        return null;
                    }
                    expression = expression.replace(part, value); // Replace reference with its value.
                }
            }

            return evaluateExpression(expression); // Evaluate the final expression.
        } catch (Exception e) {
            return null; // Return null if any exception occurs.
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
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ') continue;

            if (Character.isDigit(tokens[i])) {
                StringBuilder sb = new StringBuilder();
                while (i < tokens.length && (Character.isDigit(tokens[i]) || tokens[i] == '.')) {
                    sb.append(tokens[i++]);
                }
                values.push(Double.parseDouble(sb.toString()));
                i--;
            } else if (tokens[i] == '(') {
                ops.push(tokens[i]);
            } else if (tokens[i] == ')') {
                while (ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.pop();
            } else if (isOperator(tokens[i])) {
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(tokens[i])) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(tokens[i]);
            }
        }

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
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            default:
                throw new UnsupportedOperationException("Unsupported operator");
        }
    }

    @Override
    public void load(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        int rows = lines.size();
        int cols = lines.get(0).split("\t").length;

        table = new SCell[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] cells = lines.get(i).split("\t");
            for (int j = 0; j < cells.length; j++) {
                table[i][j] = new SCell(cells[j]);
            }
        }
        eval();
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

    private boolean isCellReference(String s) {
        if (s.length() < 2) return false;
        char col = s.charAt(0);
        String row = s.substring(1);
        return Character.isLetter(col) && Character.isDigit(row.charAt(0));
    }

    @Override
    public String[][] evalAll() {
        int rows = width();
        int cols = height();
        String[][] result = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = eval(i, j);
            }
        }
        return result;
    }
}
/**
 *
 *     @Override
 *     public String eval(int x, int y) {
 *         Cell cell = get(x, y);
 *         if (cell instanceof SCell) {
 *             SCell sCell = (SCell) cell;
 *             String content = sCell.getData();
 *
 *             if (sCell.isNumber(content)) {
 *                 return content; // Return the number as a string
 *             } else if (sCell.isForm(content)) {
 *                 Double result = computeForm(content);
 *                 if (result == null) {
 *                     // Mark the cell as containing an invalid formula
 *                     sCell.setContent(Ex2Utils.ERR_FORM);
 *                     return Ex2Utils.ERR_FORM; // Return the error message
 *                 }
 *                 return result.toString();
 *             } else if (sCell.isText(content)) {
 *                 return content; // Return the text as is
 *             }
 *         }
 *         return Ex2Utils.EMPTY_CELL; // Default for empty or invalid cells
 *     }
 */