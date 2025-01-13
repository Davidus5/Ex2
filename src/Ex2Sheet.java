import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0; i<x; i=i+1) {
            for(int j=0; j<y; j=j+1) {
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
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        Cell c = get(x,y);
        if(c != null) {
            if(c.getType() == Ex2Utils.FORM) {
                ans = eval(x, y);
            } else {
                ans = c.toString();
            }
        }
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        if (cords != null && cords.length() >= 2) {
            int x = xCell(cords);
            int y = yCell(cords);
            if (x >= 0 && y >= 0 && isIn(x, y)) {
                ans = get(x, y);
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
        ((SCell) table[x][y]).setContent(value(x,y));
        eval();
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell instanceof SCell) {
            SCell sCell = (SCell) cell;
            String content = sCell.getData();

            if (sCell.isForm(content)) {
                // Check for self-reference first
                if (hasSelfReference(x, y, content)) {
                    sCell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    return Ex2Utils.ERR_CYCLE;
                }

                // Validate formula format
                if (!isValidFormulaFormat(content)) {
                    sCell.setType(Ex2Utils.ERR_FORM_FORMAT);
                    return Ex2Utils.ERR_FORM;
                }

                try {
                    Double result = computeForm(content);
                    if (result == null) {
                        sCell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        return Ex2Utils.ERR_FORM;
                    }
                    sCell.setType(Ex2Utils.FORM);
                    return result.toString();
                } catch (Exception e) {
                    sCell.setType(Ex2Utils.ERR_FORM_FORMAT);
                    return Ex2Utils.ERR_FORM;
                }
            } else if (sCell.isNumber(content)) {
                return content;
            } else if (sCell.isText(content)) {
                return content;
            }
        }
        return Ex2Utils.EMPTY_CELL;
    }

    @Override
    public void eval() {
        int[][] depths = depth();
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (depths[x][y] == -1) {
                    table[x][y].setType(Ex2Utils.ERR_CYCLE_FORM);
                } else {
                    ((SCell) table[x][y]).setContent(value(x,y));
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
        if (!isIn(x, y)) return -1;
        String key = x + "," + y;
        if (!visited.add(key)) return -1;

        Cell cell = get(x, y);
        if (cell instanceof SCell) {
            SCell sCell = (SCell) cell;
            String content = sCell.getContent();

            if (sCell.isNumber(content) || sCell.isText(content)) {
                return 0;
            } else if (sCell.isForm(content)) {
                String formula = content.substring(1);
                formula = formula.toUpperCase();
                int maxDepth = 0;

                for (String part : formula.split("[+\\-*/()]")) {
                    part = part.trim();
                    if (isCellReference(part)) {
                        int depX = xCell(part);
                        int depY = yCell(part);
                        int depDepth = computeDepth(depX, depY, visited);
                        if (depDepth == -1) return -1;
                        maxDepth = Math.max(maxDepth, depDepth);
                    }
                }
                return maxDepth + 1;
            }
        }
        return -1;
    }

    @Override
    public Double computeForm(String form) {
        if (!isValidFormulaFormat(form)) {
            return null;
        }

        form = form.toUpperCase();
        try {
            String expression = form.substring(1).trim();

            for (String part : expression.split("[+\\-*/()]")) {
                part = part.trim();
                if (!part.isEmpty() && isCellReference(part)) {
                    Cell referencedCell = get(part);
                    if (referencedCell == null) {
                        return null;
                    }
                    String value = eval(xCell(part), yCell(part));
                    if (value.equals(Ex2Utils.ERR_FORM) || value.equals(Ex2Utils.EMPTY_CELL)) {
                        return null;
                    }
                    expression = expression.replace(part, value);
                }
            }

            return evaluateExpression(expression);
        } catch (Exception e) {
            return null;
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