package model;

import java.util.Objects;

public class GridCell {
    private final int row;
    private final int col;

    public GridCell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridCell)) return false;
        GridCell other = (GridCell) o;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
