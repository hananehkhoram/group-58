package com.workshop.model.mechanisms;

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

    public float getFallProgress() {
        if (isOnGround) {
            return 1f;
        }

        return 1f - (float) ticksUntilHitGround / FALL_TICKS;
    }

    public void update(){
        if (isOnGround) return;
        ticksUntilHitGround--;
        if (ticksUntilHitGround <= 0) {
            isOnGround = true;
            if (type == SunType.RADIOACTIVE) {
                type = SunType.NORMAL;
            }
            com.workshop.view.Console.showMessage("Sun reached the ground at position (" + x + ", " + y + ")");
        }
    }
}

