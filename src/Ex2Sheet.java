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
        // Add your code here

        /////////////////////
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
        table[x][y] = c;
        // Add your code here

        /////////////////////
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
        SCell cell = new SCell(get(x, y));

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
}
