package model.mechanisms;

public class SunDrop{
    private int x;
    private int y;
    private SunType type;
    private int ticksUntilHitGround;
    private boolean isOnGround;

    public SunDrop(int x, int y, SunType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public SunType getType() {
        return type;
    }

    public boolean isOnGround() {
        return isOnGround;
    }
    public void update(){}
}

