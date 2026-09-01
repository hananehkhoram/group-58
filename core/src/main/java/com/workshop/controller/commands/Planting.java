package com.workshop.controller.commands;

import com.workshop.controller.MenuManager;
import com.workshop.controller.SpecialLevelManager.ConveyorBeltManager;
import com.workshop.controller.SpecialLevelManager.LevelManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.Tag;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;
import com.workshop.model.level.LevelType;

public class Planting implements Command {
    private MenuManager menuManager;
    private static final int BOWLING_RED_LINE_COLUMN = 3;

    public Planting(MenuManager menuManager) {
        this.menuManager = menuManager;
    }


    @Override
    public void execute(String[] args) {
        String type = args[0];
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);

        GameContext ctx = menuManager.getCtx();
        GameEngine engine = menuManager.getGameEngine();

        if (ctx == null || engine == null) {
            Console.showMessage("No active battle.");
            return;
        }

        LevelManager levelManager = ctx.getLevelManager();
        boolean isConveyorLevel = levelManager instanceof ConveyorBeltManager;
        String heldSeed = ctx.getHeldSeed();
        boolean isHeldSeed = (heldSeed != null && heldSeed.equalsIgnoreCase(type));

        Plant template = resolvePlantTemplate(type, ctx, levelManager, isConveyorLevel, isHeldSeed);
        if (template == null) {
            Console.simplePrint("You haven't selected this plant for this level.\n");
            return;
        }

        if (ctx.getLevel().getLevelType() == LevelType.PLANT_WHAT_YOU_GET && template.getName().equalsIgnoreCase("sunflower")){
            Console.simplePrint("You can't plant sunflowers in this level.");
            return;
        }

        if (!isValidPlacement(template, type, x, y, ctx, engine, levelManager, isConveyorLevel, isHeldSeed)) {
            return;
        }

        Plant plantToRemoveFromBelt = isConveyorLevel ? findPlantOnBelt(type, (ConveyorBeltManager) levelManager) : null;

        if (isBowlingLevel(ctx)) {
            if (handleBowlingMinigame(template, type, x, y, ctx, levelManager, plantToRemoveFromBelt)) {
                return;
            }
        }

        performNormalPlanting(template, type, x, y, ctx, engine, levelManager, plantToRemoveFromBelt, isConveyorLevel, isHeldSeed);
    }

    private Plant resolvePlantTemplate(String type, GameContext ctx, LevelManager levelManager, boolean isConveyorLevel, boolean isHeldSeed) {
        if (isConveyorLevel) {
            return findPlantOnBelt(type, (ConveyorBeltManager) levelManager);
        }
        if (isHeldSeed) {
            try { return ctx.getPlantFactory().create(ctx.getHeldSeed()); } catch (Exception e) { return null; }
        }
        try { return ctx.getPlantFactory().create(type); } catch (Exception e) { return null; }
    }

    private Plant findPlantOnBelt(String type, ConveyorBeltManager cbm) {
        for (Plant p : cbm.getConveyorBelt()) {
            if (p.getName().equalsIgnoreCase(type)) {
                return p;
            }
        }
        return null;
    }

    public boolean isValidPlacement(Plant template, String type, int x, int y, GameContext ctx, GameEngine engine, LevelManager levelManager, boolean isConveyorLevel, boolean isHeldSeed) {
        if (ctx.getLevel().getLevelType() == LevelType.Wallnuts_MG
            && x >= BOWLING_RED_LINE_COLUMN) {
            Console.showMessage("You can only place bowling nuts before the red line.");
            return false;
        }

        if (levelManager != null && !isHeldSeed && !levelManager.canPlant(type, ctx)) {
            Console.showMessage("You can't plant this here.");
            return false;
        }

        Tile tile = engine.getTiles(x, y);
        if (tile == null) {
            Console.showMessage("You can't plant here.");
            return false;
        }

        boolean water = ctx.getSeason().isWaterCell(y, x, ctx);
        Plant occupant = tile.getPlant();
        boolean onLilyPad = occupant != null && occupant.isLilyPad();
        boolean coverTarget = template.isStackableCover() && occupant != null && occupant.getCoverPlant() == null;

        if (template.isStackableCover()) {
            if (occupant == null) {
                Console.showMessage("This can only be planted on top of another plant.");
                return false;
            }
            if (occupant.getCoverPlant() != null) {
                Console.showMessage("That plant is already covered.");
                return false;
            }
        }

        if (template.isLilyPad()) {
            if (!water) {
                Console.showMessage("Lily Pad can only be planted on water.");
                return false;
            }
            if (occupant != null) {
                Console.showMessage("You can't plant here.");
                return false;
            }
        } else if (water
            && !onLilyPad
            && !template.hasTheTag(Tag.WATER)
            && !template.isHasLilyPadUnderneath()) {
            Console.showMessage("You can't plant this on a water cell!");
            return false;
        }

        if (isGraveDestroyer(template)) {
            if (occupant != null) {
                Console.showMessage("You can't plant here.");
                return false;
            }
            if (ctx.getGraveGrid()[y][x] == null) {
                Console.showMessage("Grave Buster can only be planted on a grave.");
                return false;
            }
        } else if (!tile.isPlantable() || (occupant != null && !onLilyPad && !coverTarget)) {
            Console.showMessage("You can't plant here.");
            return false;
        }

        if (ctx.isOnCooldown(type) && !isConveyorLevel && !isHeldSeed) {
            Console.showMessage("This plant is still recharging.");
            return false;
        }

        boolean needsSun = !isConveyorLevel && !isVaseLevel(ctx) && !isHeldSeed;
        if (needsSun && ctx.getSunAmount() < template.getSunCost()) {
            Console.showMessage("Not enough sun.");
            return false;
        }

        return true;
    }

    private boolean isBowlingLevel(GameContext ctx) {
        return ctx.getLevel() != null
            && ctx.getLevel().getLevelType() == LevelType.Wallnuts_MG;
    }

    private boolean handleBowlingMinigame(Plant template, String type, int x, int y, GameContext ctx, LevelManager levelManager, Plant plantToRemoveFromBelt) {
        boolean isValidNut = type.equalsIgnoreCase("Wall-nut") || type.equalsIgnoreCase("Explode-o-nut") ||
            type.equalsIgnoreCase("Giant Wall-nut") || type.equalsIgnoreCase("Tall-nut") ||
            type.equalsIgnoreCase("Cherry Bomb");

        if (!isValidNut) return false;

        if (type.equalsIgnoreCase("Explode-o-nut")) {
            ctx.getProjectiles().add(
                new com.workshop.model.projectile.ExplodeONut(
                    1800, x, y, y, 2.0, template, ctx
                )
            );
        } else if (type.equalsIgnoreCase("Giant Wall-nut") || type.equalsIgnoreCase("Tall-nut")) {
            ctx.getProjectiles().add(new com.workshop.model.projectile.GiantWallnut(500, x, y, y, 2.0, template));
        } else {
            ctx.getProjectiles().add(new com.workshop.model.projectile.BowlingWallnut(190, x, y, y, 2.0, template));
        }

        if (plantToRemoveFromBelt != null && levelManager instanceof ConveyorBeltManager) {
            ((ConveyorBeltManager) levelManager)
                .getConveyorBelt()
                .remove(plantToRemoveFromBelt);
        }

        Console.showMessage("BOWL! " + type + " is rolling!");
        return true;
    }

    private void performNormalPlanting(Plant template, String type, int x, int y, GameContext ctx, GameEngine engine, LevelManager levelManager, Plant plantToRemoveFromBelt, boolean isConveyorLevel, boolean isHeldSeed) {
        Plant newPlant = ctx.getPlantFactory().create(template.getName());
        Plant occupant = ctx.getPlantGrid()[y][x];
        boolean onLilyPad = occupant != null && occupant.isLilyPad();

        if (newPlant.isStackableCover() && occupant != null) {
            // Pumpkin sits over the existing plant instead of replacing it:
            // Tile.setPlant() links the two together and deliberately
            // leaves the covered plant in ctx.getPlantGrid() so it keeps
            // acting normally (attacking, producing sun, etc).
            engine.getTiles(x, y).setPlant(newPlant);
        } else {
            if (onLilyPad) {
                newPlant.setHasLilyPadUnderneath(true);
                ctx.getAlivePlants().remove(occupant);
                ctx.getPlantGrid()[y][x] = null;
            }
            engine.getTiles(x, y).setPlant(newPlant);
            ctx.getPlantGrid()[y][x] = newPlant;
        }
        ctx.getAlivePlants().add(newPlant);

        applyPlantFoodBoost(template, newPlant, type, ctx);

        boolean needsSun = !isConveyorLevel && !isVaseLevel(ctx) && !isHeldSeed;
        if (needsSun) {
            ctx.setSunAmount(ctx.getSunAmount() - template.getSunCost());
        }

        if (levelManager != null) {
            levelManager.onPlantSuccess(newPlant, ctx);
        }

        if (plantToRemoveFromBelt != null) {
            ((ConveyorBeltManager) levelManager).getConveyorBelt().remove(plantToRemoveFromBelt);
        }

        if (isHeldSeed) {
            ctx.setHeldSeed(null); // DebugF
        }

        if (!isConveyorLevel && !isHeldSeed && ctx.getLevel().getLevelType() != LevelType.PLANT_WHAT_YOU_GET) {
            ctx.setCooldown(type, template.getRechargeTime());
        }

        Console.showMessage("Planted %s at (%d, %d).", type, x, y);
        ctx.recordPlantPlaced(newPlant, y, x);
    }

    private void applyPlantFoodBoost(Plant template, Plant newPlant, String type, GameContext ctx) {
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (template.isPlantFoodActive() || currentUser.hasStoredBoost(type)) {
            newPlant.activatePlantFood(ctx);
            if (currentUser.hasStoredBoost(type)) {
                currentUser.consumeStoredBoost(type);
            }
            Console.showMessage("Boosted plant food effect activated on planting!");
        }
    }

    private static boolean isGraveDestroyer(Plant plant) {
        return plant != null
            && plant.getAbilityParams() != null
            && "GRAVE_DESTROY".equals(plant.getAbilityParams().get("explosiveType"));
    }

    private boolean isVaseLevel(GameContext ctx) {
        return ctx.getLevel() != null
            && ctx.getLevel().getLevelType() == LevelType.Vase_MG;
    }
}
