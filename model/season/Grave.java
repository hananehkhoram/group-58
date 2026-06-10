package model.season;

import model.GameContext;

public class Grave {
    public enum GraveType { NORMAL, HAS_SUN, HAS_PLANT_FOOD }

    private int hp = 700;
    private GraveType type;
    private int row, col;

    public void onDestroyed(GameContext ctx) {
    }

    public void onWaveStart(GameContext ctx) {
    }
}
