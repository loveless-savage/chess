package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row, col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    /**
     * @return is this position on the board?
     */
    public boolean isInBounds() {
        return (row>=1 && row<=8 && col>=1 && col<=8);
    }

    /**
     * overrides for builtin functions
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }
    @Override
    public int hashCode() {
        return 8*row + col;
    }
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
