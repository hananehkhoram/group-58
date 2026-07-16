package controller.commands;

import controller.MenuManager;
import controller.SpecialLevelManager.LevelManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.TimeManager;
import model.mechanisms.GameEngine;
import model.mechanisms.Tile;
import model.plants.Plant;
import view.ConsoleView;

public class Planting implements Command {
    private MenuManager menuManager;

    public Planting(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {//اگه لول Conveyor belt بود نیازی به افتاب نداریم. از نوار انتخاب کن
        String type = args[0];
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);

        GameContext ctx = menuManager.getCtx();
        GameEngine engine = menuManager.getGameEngine();
        TimeManager timeManager = ctx.getTimeManager();

        if (ctx == null || engine == null) {
            ConsoleView.showMessage("No active battle.");
            return;
        }

        Plant template = null;
        for (Plant p : ctx.getActivePlants()) {
            if (p.getName().equalsIgnoreCase(type)) { template = p; break; }
        }
        if (template == null) {
            ConsoleView.showMessage("You haven't selected this plant for this level.");
            return;
        }

        LevelManager levelManager = ctx.getLevelManager();
        if (levelManager != null && !levelManager.canPlant(type, ctx)) {
            ConsoleView.showMessage("You can't plant this here.");
            return;
        }

        Tile tile = engine.getTiles(x, y);
        if (tile == null || !tile.isPlantable() || tile.getPlant() != null) {
            ConsoleView.showMessage("You can't plant here.");
            return;
        }

        if (ctx.isOnCooldown(type)) {
            ConsoleView.showMessage("This plant is still recharging.");
            return;
        }

        boolean needsSun = !(levelManager instanceof controller.SpecialLevelManager.ConveyorBeltManager);
        if (needsSun && ctx.getSunAmount() < template.getSunCost()) {
            ConsoleView.showMessage("Not enough sun.");
            return;
        }

        ctx.setCooldown(type, template.getRechargeTime());
        Plant newPlant = ctx.getPlantFactory().create(String.valueOf(template.getName()));
        tile.setPlant(newPlant);
        ctx.getPlantGrid()[x][y] = newPlant;
        ctx.getAlivePlants().add(newPlant);

        if (needsSun) {
            ctx.setSunAmount(ctx.getSunAmount() - template.getSunCost());
        }

        if (levelManager != null) {
            levelManager.onPlantSuccess(newPlant, ctx);
        }

        ctx.setCooldown(type, template.getRechargeTime());
        ConsoleView.showMessage("Planted " + type + " at (" + x + ", " + y + ").");
        ctx.recordPlantPlaced(newPlant, x, y);
    }

    //Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.

    //plant plant -t <type> -l (<x>, <y>)
}
