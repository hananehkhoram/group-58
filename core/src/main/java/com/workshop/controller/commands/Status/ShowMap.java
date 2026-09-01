package com.workshop.controller.commands.Status;

import com.workshop.controller.MenuManager;
import com.workshop.controller.commandHandler.Command;
import com.workshop.model.GameContext;
import com.workshop.model.MiniGame.Izambi.IZombieManager;
import com.workshop.model.MiniGame.VaseGame.Vase;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.LawnMower;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;
import com.workshop.model.projectile.Projectile;
import com.workshop.model.user.UserManager;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

public class ShowMap implements Command {
    private final MenuManager menuManager;

    public ShowMap(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    @Override
    public void execute(String[] args) {
        GameContext ctx = menuManager.getCtx();
        GameEngine engine =
            menuManager.getGameEngine();

        if (ctx == null || engine == null) {
            Console.showMessage("No active battle.");
            return;
        }

        int rows = ctx.getLevel().getRows();
        int columns = ctx.getLevel().getColumns();

        IZombieManager iZombieManager =
            ctx.getLevelManager()
                instanceof IZombieManager manager
                ? manager
                : null;

        StringBuilder output = new StringBuilder();

        if (iZombieManager != null) {
            output
                .append("I-Zombie | Sun: ")
                .append(ctx.getSunAmount())
                .append(" | Brains eaten: ")
                .append(
                    iZombieManager.getEatenBrainCount()
                )
                .append("/")
                .append(rows)
                .append(" | Red line column: ")
                .append(
                    IZombieManager.RED_LINE_COLUMN
                )
                .append("\n");
        } else {
            output
                .append("Wave: ")
                .append(ctx.getCurrentWaveIndex())
                .append("/")
                .append(
                    ctx.getLevel().getWaves().length
                )
                .append(" | Sun: ")
                .append(ctx.getSunAmount())
                .append(" | Plant Food: ")
                .append(
                    UserManager
                        .getInstance()
                        .getCurrentUser()
                        .getPlantFoodCount()
                )
                .append("\n");
        }

        if (ctx.getBeghouldManager() != null) {
            output
                .append("Beghouled Matches: ")
                .append(
                    ctx.getBeghouldManager()
                        .getCurrentMatches()
                )
                .append("/")
                .append(
                    ctx.getBeghouldManager()
                        .getTargetMatches()
                )
                .append("\n");
        }

        LawnMower[] mowers =
            engine.getLawnMowers();

        for (int row = 0; row < rows; row++) {
            output.append("Row ").append(row);

            if (iZombieManager != null) {
                output
                    .append(" [Brain: ")
                    .append(
                        iZombieManager.isBrainEaten(row)
                            ? "EATEN"
                            : "READY"
                    )
                    .append("] ");
            } else {
                output
                    .append(" [Mower: ")
                    .append(
                        mowers[row].isAvailable()
                            ? "OK"
                            : "USED"
                    )
                    .append("] ");
            }

            for (
                int column = 0;
                column < columns;
                column++
            ) {
                /*
                 * || قبل از ستون ۶ نمایش داده می‌شود؛
                 * یعنی بین ستون‌های ۵ و ۶ خط قرمز قرار دارد.
                 */
                if (
                    iZombieManager != null
                        && column
                        == IZombieManager.RED_LINE_COLUMN
                ) {
                    output.append("||");
                }

                Tile tile =
                    engine.getTiles(column, row);

                String terrainSymbol =
                    terrainSymbol(tile);

                String vaseSymbol = "";

                if (
                    tile != null
                        && tile.getVase() != null
                        && !tile.getVase().isBroken()
                ) {
                    Vase vase = tile.getVase();

                    Object content = vase.getContent();

                    String contentName =
                        content != null
                            ? content
                            .toString()
                            .trim()
                            .toUpperCase()
                            : "";

                    String hiddenEntity =
                        vase.getHiddenEntityName();

                    String hiddenLower =
                        hiddenEntity != null
                            ? hiddenEntity.toLowerCase()
                            : "";

                    if (contentName.equals("PLANT")) {
                        vaseSymbol = "VP";
                    } else if (
                        !hiddenLower.isEmpty()
                            && hiddenLower.contains(
                            "gargantuar"
                        )
                    ) {
                        vaseSymbol = "VG";
                    } else {
                        vaseSymbol = "V.";
                    }
                }

                Plant plant =
                    tile != null
                        ? tile.getPlant()
                        : null;

                String plantSymbol = "..";

                if (plant != null) {
                    String plantName =
                        plant.getName();

                    if (
                        plantName != null
                            && !plantName.isEmpty()
                    ) {
                        plantSymbol =
                            plantName.substring(
                                0,
                                Math.min(
                                    2,
                                    plantName.length()
                                )
                            );
                    }
                }

                StringBuilder projectileSymbol =
                    new StringBuilder();

                for (
                    Projectile projectile
                    : ctx.getProjectiles()
                ) {
                    boolean sameRow =
                        (int) Math.round(
                            projectile.getY()
                        ) == row;

                    boolean sameColumn =
                        (int) Math.floor(
                            projectile.getX()
                        ) == column;

                    if (sameRow && sameColumn) {
                        if (
                            projectile.isFromZombie()
                        ) {
                            projectileSymbol.append("-");
                        } else {
                            projectileSymbol.append("+");
                        }
                    }
                }

                StringBuilder zombieSymbol =
                    new StringBuilder();

                for (
                    Zombie zombie
                    : ctx.getAliveZombies()
                ) {
                    boolean sameRow =
                        (int) Math.round(
                            zombie.getY()
                        ) == row;

                    boolean sameColumn =
                        (int) Math.floor(
                            zombie.getX()
                        ) == column;

                    if (sameRow && sameColumn) {
                        if (
                            zombie.getArmor() != null
                                && !zombie
                                .getArmor()
                                .isDestroyed()
                        ) {
                            zombieSymbol.append("Z");
                        } else {
                            zombieSymbol.append("z");
                        }
                    }
                }

                String contentSymbol =
                    !vaseSymbol.isEmpty()
                        ? vaseSymbol
                        : plantSymbol;

                output
                    .append("[")
                    .append(terrainSymbol)
                    .append(contentSymbol)
                    .append(
                        zombieSymbol.isEmpty()
                            ? " "
                            : zombieSymbol
                    )
                    .append("]")
                    .append(
                        projectileSymbol.isEmpty()
                            ? " "
                            : projectileSymbol
                    );
            }

            output.append("\n");
        }

        Console.showMessage(output.toString());
    }

    private String terrainSymbol(Tile tile) {
        if (tile == null) {
            return "?";
        }

        return switch (tile.getTerrainType()) {
            case WATER -> "W";
            case LOW_TIDE -> "w";
            case GRAVE -> "G";
            case FROZEN -> "F";
            case SLIPPERY_UP -> "^";
            case SLIPPERY_DOWN -> "v";
            case NECROMANCY -> "N";
            case CRATER -> "C";
            case BURNED -> "B";
            default -> ".";
        };
    }

}
