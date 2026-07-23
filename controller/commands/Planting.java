package controller.commands;

import controller.MenuManager;
import controller.SpecialLevelManager.LevelManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.mechanisms.GameEngine;
import model.mechanisms.Tile;
import model.plants.Plant;
import model.user.User;
import model.user.UserManager;
import model.projectile.BowlingWallnut;
import view.ConsoleView;

public class Planting implements Command {
    private MenuManager menuManager;

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
            ConsoleView.showMessage("No active battle.");
            return;
        }

        LevelManager levelManager = ctx.getLevelManager();
        boolean isConveyorLevel = levelManager instanceof controller.SpecialLevelManager.ConveyorBeltManager;

        Plant template = null;

        if (isConveyorLevel) {
            controller.SpecialLevelManager.ConveyorBeltManager conveyorManager =
                    (controller.SpecialLevelManager.ConveyorBeltManager) levelManager;
            for (Plant p : conveyorManager.getConveyorBelt()) {
                if (p.getName().equalsIgnoreCase(type)) { template = p; break; }
            }
            if (template == null) {
                ConsoleView.showMessage("This plant is not on the conveyor belt.");
                return;
            }
        } else {
            for (Plant p : ctx.getActivePlants()) {
                if (p.getName().equalsIgnoreCase(type)) { template = p; break; }
            }
            if (template == null) {
                ConsoleView.showMessage("You haven't selected this plant for this level.");
                return;
            }
        }

        if (levelManager != null && !levelManager.canPlant(type, ctx)) {
            ConsoleView.showMessage("You can't plant this here.");
            return;
        }

        Tile tile = engine.getTiles(x, y);
        if (tile == null || !tile.isPlantable() || tile.getPlant() != null) {
            ConsoleView.showMessage("You can't plant here.");
            return;
        }

        if (ctx.isOnCooldown(type) && !isConveyorLevel) {
            ConsoleView.showMessage("This plant is still recharging.");
            return;
        }

        boolean needsSun = !(levelManager instanceof controller.SpecialLevelManager.ConveyorBeltManager);
        if (needsSun && ctx.getSunAmount() < template.getSunCost()) {
            ConsoleView.showMessage("Not enough sun.");
            return;
        }

        boolean isBowlingLevel = false;
        if (ctx.getLevel() != null && ctx.getLevel().getName() != null) {
            isBowlingLevel = ctx.getLevel().getName().toLowerCase().contains("wallnuts");
        }

        if (isBowlingLevel && (type.equalsIgnoreCase("Wall-nut") || type.equalsIgnoreCase("Explode-o-nut") || type.equalsIgnoreCase("Giant Wall-nut"))) {

            BowlingWallnut rollingNut = new BowlingWallnut(500, x, y, x, 2.0, null);
            ctx.getProjectiles().add(rollingNut);

            if (levelManager != null) {
                levelManager.onPlantSuccess(template, ctx);
            }

            ConsoleView.showMessage("BOWL! " + type + " is rolling!");
            return;
        }

        if (!isConveyorLevel) ctx.setCooldown(type, template.getRechargeTime());

        Plant newPlant = ctx.getPlantFactory().create(String.valueOf(template.getName()));
        tile.setPlant(newPlant);
        ctx.getPlantGrid()[y][x] = newPlant;
        ctx.getAlivePlants().add(newPlant);

        User currentUser = UserManager.getInstance().getCurrentUser();
        if (template.isPlantFoodActive() || currentUser.hasStoredBoost(type)) {
            newPlant.activatePlantFood(ctx);
            if (currentUser.hasStoredBoost(type)) {
                currentUser.consumeStoredBoost(type);
            }
            ConsoleView.showMessage("Boosted plant food effect activated on planting!");
        }

        if (needsSun) {
            ctx.setSunAmount(ctx.getSunAmount() - template.getSunCost());
        }

        if (levelManager != null) {
            levelManager.onPlantSuccess(newPlant, ctx);
        }

        ctx.setCooldown(type, template.getRechargeTime());
        ConsoleView.showMessage("Planted %s at (%d, %d).", type, x, y);
        ctx.recordPlantPlaced(newPlant, y, x);
    }
}//plant plant -t <type> -l (<x>, <y>)