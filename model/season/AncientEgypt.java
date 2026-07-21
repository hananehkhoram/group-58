package model.season;

import model.GameContext;
import model.GridCell;
import model.level.Level;
import view.ConsoleView;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class AncientEgypt extends Season{
    private final Random random = new Random();
    public AncientEgypt(List<Level> levels) {
        super("Ancient Egypt", levels,1);
    }


    public void onWaveStart(GameContext ctx, int waveNumber, boolean isLastWave) {
        if (!isLastWave) return;
        ConsoleView.simplePrint("A sandstorm is approaching!\n");
    }
    public void onLevelStart(GameContext ctx) {
        int gravesToSpawn = 5;

        int spawned = 0;
        while (spawned < gravesToSpawn) {
            int row = random.nextInt(ctx.getLevel().getRows());
            int col = 3 + random.nextInt(ctx.getLevel().getColumns() - 3);

            if (ctx.getGraveGrid()[row][col] == null && ctx.getPlantGrid()[row][col] == null) {
                Grave grave = new Grave(Grave.GraveType.NORMAL, row, col);
                ctx.placeGrave(grave, row, col);
                spawned++;
            }
        }
        ConsoleView.simplePrint("Ancient Egypt: Initial graves have been spawned on the battlefield!\n");
    }

    public void onTick(GameContext ctx, double deltaTime) {
    }

    public Set<GridCell> getWaterCells(GameContext ctx) {
        return Set.of();
    }
    public boolean isWaterCell(int row, int col, GameContext ctx) { return false; }

    public List<Grave> getInitialGraves(Level level) {
        return List.of();
    }

    public boolean isNecromancyCell(int row, int col) { return false; }


}