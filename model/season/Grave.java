package model.season;

import model.GameContext;

public class Grave {
    public enum GraveType { NORMAL, HAS_SUN, HAS_PLANT_FOOD }

    private int hp = 700;
    private GraveType type;
    private int row, col;

    public Grave(GraveType type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }

    public void onDestroyed(GameContext ctx) {
        if (type.equals(GraveType.HAS_SUN))
            ctx.addSun(50);
        else if (type.equals(GraveType.HAS_PLANT_FOOD))
            ctx.addPlantFood(1);

        ctx.removeGrave(this.row,this.col);
    }

    public void onWaveStart(GameContext ctx) {
    }

    public void takeDamage(int amount,GameContext ctx){
        this.hp -= amount;
        if (this.hp <= 0) {
            onDestroyed(ctx);
        }
    }
    public String displayGraveType() {
        return switch (this.type) {
            case HAS_SUN -> "Sun";       // Grave with Sun
            case HAS_PLANT_FOOD -> "Food"; // Grave with Food
            default -> "Normal";            // Normal Grave
        };
    }
}
