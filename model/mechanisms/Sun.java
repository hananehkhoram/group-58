package model.mechanisms;

public class Sun {
    private int x;
    private int y;
    private SunType type;
    private int ticksUntilHitGround;
    private boolean isOnGround;
    private static final int FALL_TICKS = 50;

    public Sun(int x, int y, SunType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.ticksUntilHitGround = FALL_TICKS;
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

    public void update(){
        if (isOnGround) return;
        ticksUntilHitGround--;
        if (ticksUntilHitGround <= 0) {
            isOnGround = true;
            if (type == SunType.RADIOACTIVE) {
                type = SunType.NORMAL;
            }
            view.ConsoleView.showMessage("Sun reached the ground at position (" + x + ", " + y + ")");
        }
    }
}

