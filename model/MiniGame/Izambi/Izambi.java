package model.MiniGame.Izambi;

import controller.MenuManager;
import controller.repository.DataManager;
import controller.repository.factory.LevelFactory;
import controller.repository.factory.PlantFactory;
import controller.repository.factory.ZombieFactory;
import model.GameContext;
import model.level.Level;
import model.mechanisms.GameEngine;
import model.mechanisms.Tile;
import model.plants.Plant;
import model.season.Season;
import model.season.miniGameSeason.IzombieSeason;
import model.zombie.Zombie;
import view.ConsoleView;

import java.util.List;

public class Izambi {

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;
    private int sunAmount;
    private MenuManager menuManager;

    public Izambi(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    public void startMiniGame() {
        List<Level> izombieLevels = LevelFactory.buildIzombieLevels();
        if (izombieLevels == null || izombieLevels.isEmpty()) {
            ConsoleView.showMessage("Error: No levels found for Izombie mini-game.");
            return;
        }
        this.currentLevel = izombieLevels.get(0);

        Season izombieSeason = new IzombieSeason(izombieLevels);

        this.ctx = new GameContext(this.currentLevel, izombieSeason);
        this.gameEngine = new GameEngine(this.ctx, this.menuManager);
        this.ctx.setGameEngine(this.gameEngine);

        if (this.menuManager != null) {
            this.menuManager.setCtx(this.ctx);
            this.menuManager.setGameEngine(this.gameEngine);
        }

        // بازیکن در ابتدا 150 خورشید دارد
        this.sunAmount = 150;
        ctx.setSunAmount(this.sunAmount);

        // قرار دادن زامبی‌های تولیدکننده خورشید در ستون اول هر ردیف
        initSunProducerZombies();
        // قرار دادن مغزها در انتهای ردیف‌ها
        initBrains();

        System.out.print("start\n");
    }

    public boolean placeZombie(String zombieType, int row, int col) {
        if (zombieType == null) {
            ConsoleView.showMessage("Invalid zombie type!");
            return false;
        }

        int cost = getZombieCost(zombieType);

        if (this.ctx.getSunAmount() >= cost) {
            this.ctx.setSunAmount(this.ctx.getSunAmount() - cost);

            ZombieFactory factory = new ZombieFactory(DataManager.getInstance());
            Zombie newZombie = factory.create(zombieType);
            if (newZombie != null) {
                newZombie.setY(row);
                newZombie.setX(col); // بازیکن زامبی را در سمت راست می‌گذارد
                ctx.getAliveZombies().add(newZombie);
                return true;
            } else {
                ConsoleView.showMessage("Failed to create zombie of type: " + zombieType);
                this.ctx.setSunAmount(this.ctx.getSunAmount() + cost);
            }
        } else {
            ConsoleView.showMessage("Not enough sun to place this zombie!");
        }
        return false;
    }

    private int getZombieCost(String type) {
        if (type == null) {
            return 50;
        }
        return switch (type.toLowerCase().trim()) {
            case "ra" -> 50;
            case "cone head" -> 100;
            case "bucket head" -> 125;
            case "imp dragon" -> 175;
            case "gargantuar" -> 350;
            default -> 50;
        };
    }

    private void initSunProducerZombies() {
        if (currentLevel == null || ctx == null || ctx.getGameEngine() == null) return;
        int rows = currentLevel.getRows();

        PlantFactory plantFactory = new PlantFactory(DataManager.getInstance());
        for (int r = 0; r < rows; r++) {
            Plant sunPlant = plantFactory.create("Sunflower"); // یا نام گیاه تولیدکننده خورشید در بازی شما
            if (sunPlant != null) {
                // گرفتن کاشی در ستون صفر و سطر r
                Tile tile = ctx.getGameEngine().getTiles(0, r);
                if (tile != null) {
                    tile.setPlant(sunPlant);
                }
            }
        }
    }
    private void initBrains() {
        // پیاده‌سازی مغزها در انتهای ردیف‌ها مطابق با قوانین پروژه
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
            checkWinLossConditions();
        } else {
            ConsoleView.showMessage("Game engine is null");
        }
    }

    private void checkWinLossConditions() {
        // بررسی شرایط برد (خورده شدن تمام مغزها) و باخت (تمام شدن زامبی‌ها و نداشتن خورشید کافی)
    }
}