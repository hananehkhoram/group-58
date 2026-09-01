package com.workshop.model.MiniGame.Izambi.multiplayer;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.level.Level;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.projectile.TrajectoryType;
import com.workshop.model.season.Season;
import com.workshop.model.season.miniGameSeason.IzombieSeason;
import com.workshop.model.zombie.Zombie;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GuestLawnMirror {

    private final GameContext ctx;
    private final Map<Integer, Plant> plantsById = new HashMap<>();
    private final Map<Integer, Zombie> zombiesById = new HashMap<>();
    private final Map<Integer, Projectile> projectilesById = new HashMap<>();
    private final PlantFactory plantFactory = new PlantFactory(DataManager.getInstance());

    public GuestLawnMirror(int levelNumber) {
        List<Level> levels = LevelFactory.buildIzombieLevels();
        int levelIndex = Math.max(0, Math.min(levelNumber - 1, levels.size() - 1));
        Level level = levels.get(levelIndex);
        Season season = new IzombieSeason(levels);

        ctx = new GameContext(level, season);
        ctx.setZombieFactory(new ZombieFactory(DataManager.getInstance()));

        IZombieManager manager = new IZombieManager(levelIndex, level.getRows());
        ctx.setLevelManager(manager);
        manager.onLevelStart(ctx);

        ctx.setBattleStarted(true);
    }

    public GameContext getCtx() {
        return ctx;
    }

    public void applySnapshot(IzambiSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        ctx.setSunAmount(snapshot.zombieSun);
        syncPlants(snapshot.plants);
        syncZombies(snapshot.zombies);
        syncProjectiles(snapshot.projectiles);
        syncBrains(snapshot.brainsEaten);
    }

    private void syncPlants(List<IzambiSnapshot.EntityView> views) {
        Set<Integer> seen = new HashSet<>();

        for (IzambiSnapshot.EntityView view : views) {
            seen.add(view.id);

            Plant plant = plantsById.get(view.id);
            if (plant == null) {
                try {
                    plant = plantFactory.create(view.name);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                plantsById.put(view.id, plant);
            }

            int oldRow = plant.getRow();
            int oldCol = plant.getCol();
            if (ctx.getPlantGrid()[oldRow][oldCol] == plant) {
                ctx.getPlantGrid()[oldRow][oldCol] = null;
            }

            plant.setRow(view.row);
            plant.setCol((int) Math.round(view.x));
            applyPlantHpPercent(plant, view.hpPercent);

            ctx.getPlantGrid()[plant.getRow()][plant.getCol()] = plant;
            if (!ctx.getAlivePlants().contains(plant)) {
                ctx.getAlivePlants().add(plant);
            }
        }

        Iterator<Map.Entry<Integer, Plant>> it = plantsById.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Plant> entry = it.next();
            if (seen.contains(entry.getKey())) {
                continue;
            }
            Plant plant = entry.getValue();
            ctx.getAlivePlants().remove(plant);
            if (ctx.getPlantGrid()[plant.getRow()][plant.getCol()] == plant) {
                ctx.getPlantGrid()[plant.getRow()][plant.getCol()] = null;
            }
            it.remove();
        }
    }

    private void applyPlantHpPercent(Plant plant, int hpPercent) {
        int target = Math.round(plant.getBaseHp() * hpPercent / 100f);
        int diff = plant.getHp() - target;
        if (diff > 0) {
            plant.takeDamage(diff);
        } else if (diff < 0) {
            plant.heal(-diff);
        }
    }

    private void syncZombies(List<IzambiSnapshot.ZombieView> views) {
        Set<Integer> seen = new HashSet<>();

        for (IzambiSnapshot.ZombieView view : views) {
            seen.add(view.id);

            Zombie zombie = zombiesById.get(view.id);
            if (zombie == null) {
                try {
                    zombie = ctx.getZombieFactory().create(view.name);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                zombiesById.put(view.id, zombie);
                ctx.getAliveZombies().add(zombie);
            }

            zombie.setRow(view.row);
            zombie.setX(view.x);
            zombie.setEating(view.eating);
            zombie.setMirroredDeathState(view.ashed, view.ashFinished, view.deathAnimFinished);
            zombie.setMirroredIceState(view.iced, view.initialFrozenBlock, view.iceHp);

            if (view.armorHp >= 0 && zombie.getArmor() != null) {
                zombie.getArmor().setArmorHp(view.armorHp);
            }

            int targetHp = zombie.getMaxHp() <= 0
                ? 0
                : Math.round(zombie.getMaxHp() * view.hpPercent / 100f);
            zombie.setHp(targetHp);
        }

        Iterator<Map.Entry<Integer, Zombie>> it = zombiesById.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Zombie> entry = it.next();
            if (seen.contains(entry.getKey())) {
                continue;
            }
            ctx.getAliveZombies().remove(entry.getValue());
            it.remove();
        }
    }

    private void syncProjectiles(List<IzambiSnapshot.ProjectileView> views) {
        Set<Integer> seen = new HashSet<>();

        for (IzambiSnapshot.ProjectileView view : views) {
            seen.add(view.id);

            Projectile projectile = projectilesById.get(view.id);
            if (projectile == null) {
                BulletType bulletType;
                TrajectoryType trajectory;
                try {
                    bulletType = BulletType.valueOf(view.bulletType);
                    trajectory = TrajectoryType.valueOf(view.trajectory);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }

                Plant ownerPlant = view.ownerPlantId >= 0 ? plantsById.get(view.ownerPlantId) : null;

                projectile = new Projectile(
                    0,
                    view.x,
                    view.y,
                    view.row,
                    0,
                    bulletType,
                    trajectory,
                    view.isFromZombie,
                    ownerPlant
                );

                projectilesById.put(view.id, projectile);
                ctx.getProjectiles().add(projectile);
            }

            projectile.setMirroredPosition(view.x, view.y, view.row);
        }

        Iterator<Map.Entry<Integer, Projectile>> it = projectilesById.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Projectile> entry = it.next();
            if (seen.contains(entry.getKey())) {
                continue;
            }
            Projectile projectile = entry.getValue();
            projectile.deactivate();
            ctx.getProjectiles().remove(projectile);
            it.remove();
        }
    }

    private void syncBrains(boolean[] brainsEaten) {
        if (brainsEaten == null || !(ctx.getLevelManager() instanceof IZombieManager manager)) {
            return;
        }
        for (int row = 0; row < brainsEaten.length; row++) {
            if (brainsEaten[row]) {
                manager.eatBrain(row);
            }
        }
    }
}
