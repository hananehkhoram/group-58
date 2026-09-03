package com.workshop.model.season;

import com.workshop.model.GameContext;
import com.workshop.model.user.UserManager;

public class Grave {
    public enum GraveType { NORMAL, HAS_SUN, HAS_PLANT_FOOD }

    private static final int MAX_HP = 700;
    private int hp = MAX_HP;
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
            ctx.addPlantFoodDrop(new com.workshop.model.mechanisms.PlantFoodDrop(this.col, this.row));

        ctx.removeGrave(this.row,this.col);
    }

    public void takeDamage(int amount,GameContext ctx){
        this.hp -= amount;
        if (this.hp <= 0) {
            onDestroyed(ctx);
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public GraveType getType() {
        return type;
    }

    public int getHp() {
        return hp;
    }
    public int getMaxHp() {
        return MAX_HP;
    }


}
