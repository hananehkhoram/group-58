package com.workshop.model.plants.plantAbilities;

import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.TargetingMode;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.projectile.BulletType;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.zombie.Effects;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Modifier implements BaseAbility {

    public void modify(ModifierType modifierType, Plant plant, GameEngine engine) {
        int pRow = plant.getRow();
        int pCol = plant.getCol();
        GameContext ctx = engine.getCtx();

        switch (modifierType) {
            case BULLET_BUFF:
                for (int i = 0; i < ctx.getProjectiles().size(); i++) {
                    Projectile proj = ctx.getProjectiles().get(i);
                    if (proj.getRow() == pRow && Math.abs(proj.getX() - pCol) <= 0.5) {
                        if (proj.getBulletType() == BulletType.NORMAL || proj.getBulletType() == BulletType.ICE) {
                            int multiplier = "BLUE_FLAME".equals(plant.getDamage()) ? 3 : 2;
                            Projectile fireProj = new Projectile(
                                    proj.getDamage() * multiplier,
                                    proj.getX(), proj.getY(), proj.getRow(), proj.getSpeed(),
                                    BulletType.FIRE, proj.getTrajectory(), proj.isFromZombie(), proj.getOwnerPlant()
                            );
                            ctx.getProjectiles().set(i, fireProj);
                        }
                    }
                }
                break;

            case HYPNOTIZE:
                List<Zombie> targets = engine.findTargets(pRow, pCol, TargetingMode.NONE);
                if (targets != null && !targets.isEmpty()) {
                    Zombie eater = targets.get(0);
                    eater.getEffect().add(Effects.HYPNOTIZED);
                    if ("GARGANTUAR_MODE".equals(plant.getDamage())) {
                        eater.setName("Gargantuar");
                        eater.setId("ZombieGargantuar");
                    }
                    engine.removePlant(pRow, pCol);
                }
                break;

            case FAMILY_BUFF:
                if (plant.getAbilityParams() != null && plant.getAbilityParams().containsKey("targetFamily")) {
                    String familyTag = plant.getAbilityParams().get("targetFamily");
                    for (Plant p : ctx.getAlivePlants()) {
                        if (p.getTags() != null && p.getTags().contains(familyTag)) {
                            p.activatePlantFood(ctx);
                        }
                    }
                }
                engine.removePlant(pRow, pCol);
                break;

            case WATER_PLATFORM:
                break;
            case COPY_PLANT:
                List<Plant> candidates = new ArrayList<>(ctx.getActivePlants());
                candidates.removeIf(c -> c.getName().equalsIgnoreCase(plant.getName()));
                if (!candidates.isEmpty()) {
                    Plant target = candidates.get(new Random().nextInt(candidates.size()));
                    Plant newPlant = ctx.getPlantFactory().create(target.getName());
                    if (newPlant != null) {
                        newPlant.setRow(pRow);
                        newPlant.setCol(pCol);
                        Tile tile = engine.getTiles(pCol, pRow);
                        if (tile != null) tile.setPlant(newPlant);
                        ctx.getPlantGrid()[pRow][pCol] = newPlant;
                        ctx.getAlivePlants().add(newPlant);
                        ctx.recordPlantPlaced(newPlant, pRow, pCol);
                        Console.showMessage("Imitater successfully transformed into " + newPlant.getName() + " at (" + pCol + ", " + pRow + ")");
                    }
                }
                break;

        }
    }

    @Override
    public void activatePlantFood(Plant self, GameContext ctx, PlantFoodMode mode) {
        ModifierType type = ModifierType.valueOf(self.getAbilityParams().get("modifierType"));
        int pRow = self.getRow();
        int pCol = self.getCol();

        switch (type) {
            case BULLET_BUFF:
                self.setDamage("BLUE_FLAME");
                break;

            case HYPNOTIZE:
                self.setDamage("GARGANTUAR_MODE");
                break;

            case WATER_PLATFORM:
                int maxCols = ctx.getLevel().getColumns();
                for (int c = 0; c < maxCols; c++) {
                    if (c != pCol && ctx.getSeason().isWaterCell(pRow, c, ctx) && ctx.getPlantGrid()[pRow][c] == null){
                        Plant copy = ctx.getPlantFactory().create(self.getName());
                        if (copy != null) {
                            copy.setRow(pRow);
                            copy.setCol(c);
                            ctx.getPlantGrid()[pRow][c] = copy;
                            ctx.getAlivePlants().add(copy);
                        }
                    }
                }
                break;

            case COPY_PLANT:
            case FAMILY_BUFF:
                break;
        }
    }
}
