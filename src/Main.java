
import java.util.*;
public class Main {
    public static void main(String[] args) {

        class Cell {
            private String content;

            public Cell(String content) {
                this.content = content;
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
                        form = form.substring(1); // Remove '='
                    }
                    // Basic expression evaluation
                    return evaluateExpression(form);
                } catch (Exception e) {
                    return null; // Invalid formula
                }
            }

            private Double evaluateExpression(String expression) {
                // TODO: Implement a proper formula evaluator (considering precedence and parentheses)
                // Placeholder for simple evaluation
                return 0.0;
            }

            public String getContent() {
                return content;
            }
        }

        class Spreadsheet {
            private Cell[][] grid;

            public Spreadsheet(int rows, int cols) {
                grid = new Cell[rows][cols];
            }

            public void set(int x, int y, Cell cell) {
                grid[x][y] = cell;
            }

            public Cell get(int x, int y) {
                return grid[x][y];
            }

            public int width() {
                return grid.length;
            }

            public int height() {
                return grid[0].length;
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

            public String eval(int x, int y) {
                Cell cell = get(x, y);
                if (cell == null) return "";
                String content = cell.getContent();
                if (cell.isNumber(content)) {
                    return content;
                } else if (cell.isText(content)) {
                    return content;
                } else if (cell.isForm(content)) {
                    Double result = cell.computeForm(content);
                    return (result == null) ? "ERR_FORM" : result.toString();
                }
                return "ERR_FORM";
            }

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
                Cell cell = get(x, y);
                if (cell == null) return 0;
                String content = cell.getContent();
                if (cell.isNumber(content) || cell.isText(content)) {
                    return 0;
                } else if (cell.isForm(content)) {
                    if (!visited.add(x + "," + y)) return -1; // Cycle detected
                    // TODO: Parse and compute dependencies
                    return 1; // Placeholder
                }
                return -1;
            }
        }

    }
}