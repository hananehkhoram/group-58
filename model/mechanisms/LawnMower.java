package model.mechanisms;

public class LawnMower{
    private int row;
    private boolean isAvailable;

    public LawnMower(int row) {
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
    public void trigger(){}
}
