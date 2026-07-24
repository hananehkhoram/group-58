package controller.commands.Status;

import controller.MenuManager;
import controller.commandHandler.Command;
import model.GameContext;
import model.mechanisms.GameEngine;
import model.mechanisms.LawnMower;
import model.mechanisms.Tile;
import model.plants.Plant;
import model.projectile.Projectile;
import model.user.UserManager;
import model.zombie.Zombie;
import model.MiniGame.VaseGame.Vase;
import view.ConsoleView;

public class ShowMap implements Command {
    private MenuManager menuManager;

    public ShowMap(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        GameContext ctx = menuManager.getCtx();
        GameEngine engine = menuManager.getGameEngine();
        if (ctx == null || engine == null) {
            ConsoleView.showMessage("No active battle.");
            return;
        }

        int rows = ctx.getLevel().getRows();
        int cols = ctx.getLevel().getColumns();

        StringBuilder sb = new StringBuilder();
        sb.append("Wave: ").append(ctx.getCurrentWaveIndex())
                .append("/").append(ctx.getLevel().getWaves().length)
                .append(" | Sun: ").append(ctx.getSunAmount())
                .append(" | Plant Food: ").append(UserManager.getInstance().getCurrentUser().getPlantFoodCount())
                .append("\n");

        LawnMower[] mowers = engine.getLawnMowers();

        for (int r = 0; r < rows; r++) {
            sb.append("Row ").append(r)
                    .append(" [Mower: ").append(mowers[r].isAvailable() ? "OK" : "USED").append("] ");

            for (int c = 0; c < cols; c++) {

                Tile tile = engine.getTiles(c, r);
                String terrainSymbol = terrainSymbol(tile);

                // --- بخش جدید: تشخیص نوع کوزه ---
                String vaseSymbol = "";
                if (tile != null && tile.getVase() != null && !tile.getVase().isBroken()) {
                    Vase vase = tile.getVase();
                    String contentName = vase.getContent().toString(); // کلمه PLANT یا ZOMBIE
                    String hiddenEntity = vase.getHiddenEntityName();

                    if (contentName.equals("PLANT")) {
                        vaseSymbol = "VP"; // کوزه گیاه
                    } else if (hiddenEntity != null && hiddenEntity.toLowerCase().contains("gargantuar")) {
                        vaseSymbol = "VG"; // کوزه غول (Gargantuar)
                    } else {
                        vaseSymbol = "V.";  // کوزه معمولی
                    }
                }
                // ---------------------------------

                Plant plant = (tile != null) ? tile.getPlant() : null;

                String plantSymbol = (plant != null)
                        ? plant.getName().substring(0, Math.min(2, plant.getName().length()))
                        : "..";

                StringBuilder projectileSymbol = new StringBuilder();
                for (Projectile p : ctx.getProjectiles()) {
                    if ((int) Math.round(p.getY()) == r && (int) Math.floor(p.getX()) == c){
                        if (!p.isFromZombie()) projectileSymbol.append("+");
                        if (p.isFromZombie()) projectileSymbol.append("-");
                    }
                }

                StringBuilder zombieSymbol = new StringBuilder();
                for (Zombie z : ctx.getAliveZombies()) {
                    if ((int) Math.round(z.getY()) == r && (int) Math.floor(z.getX()) == c) {
                        if (z.getArmor() != null && !z.getArmor().isDestroyed()) {
                            zombieSymbol.append("Z");
                        } else {
                            zombieSymbol.append("z");
                        }

                    }
                }

                String contentSymbol = !vaseSymbol.isEmpty() ? vaseSymbol : plantSymbol;

                sb.append("[")
                        .append(terrainSymbol)
                        .append(contentSymbol)
                        .append(zombieSymbol.length() > 0 ? zombieSymbol : " ")
                        .append(projectileSymbol.length() > 0 ? projectileSymbol : " ")
                        .append("]");
            }
            sb.append("\n");}

        ConsoleView.showMessage(sb.toString());
    }

    private String terrainSymbol(Tile tile) {
        if (tile == null) return "?";
        return switch (tile.getTerrainType()) {
            case WATER -> "W";
            case LOW_TIDE -> "w";
            case GRAVE -> "G";
            case FROZEN -> "F";
            case SLIPPERY_UP -> "^";
            case SLIPPERY_DOWN -> "v";
            case NECROMANCY -> "N";
            default -> ".";
        };
    }
}