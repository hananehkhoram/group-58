package model.season;

import model.GameContext;
import model.GridCell;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public abstract class Season {
    protected String name;
    protected List<Level> levels; // normal, special, special, boss

    public Season(String name, List<Level> levels) {
        this.name = name;
        this.levels = levels;
    }

    public boolean sunFallsFromSky() { return true; }

    public boolean iceEffectiveOnZombies() { return true; }

    public void onWaveStart(GameContext ctx, int waveNumber, boolean isLastWave) {}

    public void onLevelStart(GameContext ctx) {}

    public boolean isWaterCell(int row, int col, GameContext ctx) { return false; }

    public void onTick(GameContext ctx, double deltaTime) {}

    public Set<GridCell> getWaterCells(GameContext ctx) { return Set.of(); }

    public List<Grave> getInitialGraves(Level level) { return List.of(); }

    public int getSliderNextRow(int row, int col) { return row; }

    public boolean isNecromancyCell(int row, int col) { return false; }

    public String getName() { return name; }
    public List<Level> getLevels() { return levels; }
}
