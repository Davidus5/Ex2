// Add your documentation below:

public class CellEntry implements Index2D {
    private int x; // Column index
    private int y; // Row index

    // Constructor
    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Checks if the indices are valid (non-negative)
    @Override
    public boolean isValid() {
        // x must be in the range of 'A' to 'Z' or 'a' to 'z'
        if (x < 0 || x > 25) return false;

        // y must be in the range [0, 99]
        if (y < 0 || y > 99) return false;

        return true;
    }


    // Get the column index
    @Override
    public int getX() {
        return x;
    }

    // Get the row index
    @Override
    public int getY() {
        return y;
    }

    // Equals method to compare CellEntry objects
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CellEntry other = (CellEntry) obj;
        return x == other.x && y == other.y;
    }

    // Hash code for use in hash-based collections
    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    // String representation for debugging purposes
    @Override
    public String toString() {
        // Convert x to a column letter (A-Z)
        char column = (char) ('A' + x);

        // Combine column and row
        return column + Integer.toString(y);
    }

}
