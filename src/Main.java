
import java.util.*;



public class Spreadsheet {
    private Cell[][] grid;

    public Spreadsheet(int rows, int cols) {
        grid = new Cell[rows][cols];
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
