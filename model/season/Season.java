package model.season;

import model.GameContext;
import model.GridCell;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public abstract class Season {
    protected String name;
    protected List<Level> levels; // normal, special, special, boss

    public boolean sunFallsFromSky() { return true; }

    public boolean iceEffectiveOnZombies() { return true; }

    public void onWaveStart(GameContext ctx, int waveNumber) {}

    public void onTick(GameContext ctx) {}

    public Set<GridCell> getWaterCells(GameContext ctx) { return Set.of(); }

    public List<Grave> getInitialGraves(Level level) { return List.of(); }
}
