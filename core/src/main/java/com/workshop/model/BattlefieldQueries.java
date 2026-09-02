package com.workshop.model;

import com.workshop.model.plants.Plant;
import com.workshop.model.season.Grave;
import com.workshop.model.zombie.Zombie;

import java.util.List;

/**
 * Grid / lane queries and burn-cell helpers previously inlined in {@link GameContext}.
 */
final class BattlefieldQueries {

    private BattlefieldQueries() {}

    static Plant findNearestPlantInRow(Plant[][] plantGrid, Zombie zombie) {
        int row = zombie.getRow();
        Plant nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Plant p : plantGrid[row]) {
            if (p == null || p.isDead()) {
                continue;
            }
            double dist = Math.abs(zombie.getX() - p.getCol());
            if (dist < minDist) {
                minDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }

    static Zombie findAliveBoss(List<Zombie> aliveZombies) {
        for (Zombie zombie : aliveZombies) {
            if (zombie != null && zombie.isBoss() && !zombie.isDead()) {
                return zombie;
            }
        }
        return null;
    }

    static boolean hasHostileAhead(List<Zombie> aliveZombies, Grave[][] graveGrid, int row, double fromX) {
        for (Zombie zombie : aliveZombies) {
            if (zombie == null || zombie.isDead()) {
                continue;
            }
            if (zombie.occupiesRow(row) && zombie.getX() >= fromX) {
                return true;
            }
        }
        if (graveGrid == null || row < 0 || row >= graveGrid.length) {
            return false;
        }
        Grave[] lane = graveGrid[row];
        if (lane == null) {
            return false;
        }
        int startCol = (int) Math.floor(fromX) + 1;
        startCol = Math.max(0, startCol);
        for (int c = startCol; c < lane.length; c++) {
            if (lane[c] != null) {
                return true;
            }
        }
        return false;
    }

    static void igniteCell(
        Plant[][] plantGrid,
        long[][] burnUntilTick,
        TimeManager timeManager,
        int row,
        int col,
        double seconds
    ) {
        if (row < 0 || col < 0
            || row >= burnUntilTick.length
            || col >= burnUntilTick[row].length) {
            return;
        }
        long now = timeManager != null ? timeManager.getTotalTicks() : 0L;
        long until = now + Math.max(1L, Math.round(seconds * 10.0));
        burnUntilTick[row][col] = Math.max(burnUntilTick[row][col], until);

        Plant plant = plantGrid[row][col];
        if (plant != null && !plant.isDead()) {
            plant.takeDamage(10_000);
        }
    }

    static boolean isBurnedCell(long[][] burnUntilTick, TimeManager timeManager, int row, int col) {
        if (row < 0 || col < 0
            || row >= burnUntilTick.length
            || col >= burnUntilTick[row].length
            || timeManager == null) {
            return false;
        }
        return timeManager.getTotalTicks() < burnUntilTick[row][col];
    }
}
