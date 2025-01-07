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
        return x >= 0 && y >= 0;
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
        return "(" + x + ", " + y + ")";
    }
}
