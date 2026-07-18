package controller.commands.Status;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.mechanisms.GameEngine;
import model.mechanisms.Tile;
import model.plants.Plant;
import model.zombie.Zombie;
import view.ConsoleView;

public class ShowTileStatus implements Command {
    private final MenuManager menuManager;

    public ShowTileStatus(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

        GameContext ctx = menuManager.getCtx();
        GameEngine engine = menuManager.getGameEngine();
        if (ctx == null || engine == null) {
            ConsoleView.showMessage("No active battle.");
            return;
        }

        Tile tile = engine.getTiles(x, y);
        if (tile == null) {
            ConsoleView.showMessage("Invalid tile coordinates.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Tile (").append(x).append(", ").append(y).append(") ===\n");
        sb.append("Terrain: ").append(tile.getTerrainType()).append("\n");

        Plant plant = tile.getPlant();
        if (plant != null) {
            sb.append("Plant: ").append(plant.getName())
                    .append(" | HP: ").append(plant.getHp())
                    .append(" | Family: ").append(plant.getFamily())
                    .append(" | Level: ").append(plant.getLevel())
                    .append(" | Tags: ").append(plant.getTags())
                    .append("\n");
        } else {
            sb.append("Plant: none\n");
        }

        boolean anyZombie = false;
        for (Zombie z : ctx.getAliveZombies()) {
            if ((int) Math.round(z.getY()) == x && (int) Math.floor(z.getX()) == y) {
                anyZombie = true;
                sb.append("Zombie: ").append(z.getName())
                        .append(" | HP: ").append(z.getHp())
                        .append(" | Speed: ").append(z.getSpeed())
                        .append(" | Position: (").append(z.getX()).append(", ").append(z.getY()).append(")")
                        .append("\n");
            }
        }
        if (!anyZombie) {
            sb.append("Zombie: none\n");
        }

        ConsoleView.showMessage(sb.toString());
    }
}