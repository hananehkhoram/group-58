package model.MiniGame.Izambi;

import controller.MenuManager;
import controller.repository.DataManager;
import controller.repository.factory.LevelFactory;
import controller.repository.factory.PlantFactory;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.plants.Plant;
import model.season.Season;
import model.season.miniGameSeason.IzombieSeason;
import model.mechanisms.Tile;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.List;

public class Izambi {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;

    private static Izambi activeInstance;

    public Izambi() {
        activeInstance = this;
    }

    public static Izambi getActiveInstance() {
        return activeInstance;
    }

    public void startMiniGame(MenuManager menuManager) {
        // استفاده از متد اصلاح‌شده که موج خودکار ندارد
        List<Level> izombieLevels = LevelFactory.buildIzombieLevels();
        if (izombieLevels == null || izombieLevels.isEmpty()) {
            ConsoleView.showMessage("Error: No levels found for Izombie mini-game.");
            return;
        }
        this.currentLevel = izombieLevels.get(0);

        Season izombieSeason = new IzombieSeason(izombieLevels);

        this.ctx = new GameContext(this.currentLevel, izombieSeason);

        ctx.setZombieFactory(new ZombieFactory(DataManager.getInstance()));
        this.gameEngine = new GameEngine(this.ctx, menuManager);
        this.ctx.setGameEngine(this.gameEngine);

        // غیرفعال کردن کامل نوار نقاله
        this.ctx.setLevelManager(null);

        // مقدار اولیه خورشید برای کاشت دستی زامبی
        ctx.setSunAmount(150);

        // چیدن گیاهان اولیه در ستون‌های بازی
        initSunProducerZombies();

        // شروع نبرد و اجازه آپدیت به موتور بازی
        ctx.setBattleStarted(true);

        System.out.print("start\n");
    }

    public boolean placeZombie(String zombieType, int row, int col) {
        if (zombieType == null) return false;

        int cost = getZombieCost(zombieType);

        if (this.ctx.getSunAmount() >= cost) {
            this.ctx.setSunAmount(this.ctx.getSunAmount() - cost);

            ZombieFactory factory = new ZombieFactory(DataManager.getInstance());
            Zombie newZombie = factory.create(zombieType);
            if (newZombie != null) {
                newZombie.setY(row);
                newZombie.setX(col);

                // اضافه کردن به لیست زامبی‌های زنده برای حرکت توسط موتور بازی
                ctx.getAliveZombies().add(newZombie);

                ConsoleView.showMessage("Zombie " + zombieType + " placed at row " + row + ", col " + col);
                return true;
            } else {
                this.ctx.setSunAmount(this.ctx.getSunAmount() + cost);
            }
        } else {
            ConsoleView.showMessage("Not enough sun!");
        }
        return false;
    }

    private int getZombieCost(String type) {
        if (type == null) return 50;
        return switch (type.toLowerCase().trim()) {
            case "ra", "default" -> 50;
            case "cone head" -> 100;
            case "bucket head" -> 125;
            default -> 50;
        };
    }

    private void initSunProducerZombies() {
        if (currentLevel == null || ctx == null || ctx.getGameEngine() == null) return;
        int rows = currentLevel.getRows();

        PlantFactory plantFactory = new PlantFactory(DataManager.getInstance());
        String[] plantTypes = {"Sunflower", "Peashooter", "Wall-nut", "Sunflower", "Peashooter", "Sunflower"};

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 6; c++) {
                String pType = plantTypes[c % plantTypes.length];
                Plant plant = plantFactory.create(pType);
                if (plant != null) {
                    Tile tile = ctx.getGameEngine().getTiles(c, r);
                    if (tile != null) {
                        tile.setPlant(plant);
                    }
                }
            }
        }
    }

    public GameContext getCtx(){
        return this.ctx;
    }

    public GameEngine getGameEngine(){
        return this.gameEngine;
    }

    public void advancedTimeCommand(double sec){
        if (this.gameEngine != null) {
            this.gameEngine.update(sec);
        } else {
            System.out.println("Game engine is null\n");
        }
    }
}