package com.workshop.model.MiniGame.Izambi;

import com.workshop.controller.MenuManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.LevelFactory;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.controller.repository.factory.ZombieFactory;
import com.workshop.model.GameContext;
import com.workshop.model.level.Level;
import com.workshop.model.mechanisms.GameEngine;
import com.workshop.model.mechanisms.Tile;
import com.workshop.model.plants.Plant;
import com.workshop.model.season.Season;
import com.workshop.model.season.miniGameSeason.IzombieSeason;
import com.workshop.model.zombie.Zombie;
import com.workshop.view.Console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

public class Izambi {
    private static final int INITIAL_SUN = 150;


    private static final int SUN_PRODUCER_COLUMN = 8;


    private static final String[] ATTACKING_PLANT_POOL = {
        "Peashooter",
        "Repeater",
        "Snow Pea",
        "Cabbage-pult"
    };


    private static final String[] RANDOM_PLANT_POOL = {
        "Peashooter",
        "Repeater",
        "Snow Pea",
        "Cabbage-pult",
        "Wall-nut"
    };

    private static Izambi activeInstance;

    private final Random random = new Random();

    private Level currentLevel;
    private GameEngine gameEngine;
    private GameContext ctx;
    private IZombieManager iZombieManager;

    public Izambi() {
        activeInstance = this;
    }

    public static Izambi getActiveInstance() {
        return activeInstance;
    }


    public void startMiniGame(MenuManager menuManager) {
        startMiniGame(menuManager, 1);
    }

    public void startMiniGame(
        MenuManager menuManager,
        int levelNumber
    ) {
        List<Level> levels =
            LevelFactory.buildIzombieLevels();

        if (levels == null || levels.isEmpty()) {
            Console.showMessage(
                "Error: No levels found for I-Zombie mini-game."
            );
            return;
        }

        if (levelNumber < 1 || levelNumber > levels.size()) {
            Console.showMessage(
                "I-Zombie level must be between 1 and "
                    + levels.size()
                    + "."
            );
            return;
        }

        int levelIndex = levelNumber - 1;

        User currentUser =
            UserManager.getInstance().getCurrentUser();

        if (currentUser == null
            || !currentUser.isLevelUnlocked(
            levels.get(levelNumber - 1).getName()
        )) {
            Console.showMessage(
                "This I-Zombie level is locked."
            );
            return;
        }

        currentLevel = levels.get(levelIndex);

        Season season = new IzombieSeason(levels);

        ctx = new GameContext(currentLevel, season);

        ctx.setZombieFactory(
            new ZombieFactory(DataManager.getInstance())
        );

        iZombieManager = new IZombieManager(
            levelIndex,
            currentLevel.getRows()
        );


        ctx.setLevelManager(iZombieManager);
        iZombieManager.onLevelStart(ctx);

        gameEngine = new GameEngine(ctx, menuManager);
        ctx.setGameEngine(gameEngine);

        ctx.setSunAmount(INITIAL_SUN);

        initRandomPlants();
        initSunProducerZombies();

        ctx.setBattleStarted(true);

        Console.showMessage(
            "I-Zombie level " + levelNumber + " started."
        );

        showAvailableZombies();
    }

    public boolean placeZombie(
        String requestedType,
        int row,
        int column
    ) {
        if (ctx == null
            || iZombieManager == null
            || ctx.isGameEnded()) {

            Console.showMessage(
                "I-Zombie mini-game is not active."
            );
            return false;
        }

        String zombieType =
            iZombieManager.findCanonicalZombieName(
                requestedType
            );

        if (zombieType == null) {
            Console.showMessage(
                "This zombie is not available "
                    + "in the current I-Zombie level."
            );

            showAvailableZombies();
            return false;
        }

        if (!iZombieManager.isValidPlacement(
            row,
            column,
            ctx
        )) {
            Console.showMessage(
                "Zombies must be placed on the right side "
                    + "of the red line: columns "
                    + IZombieManager.RED_LINE_COLUMN
                    + " to "
                    + (currentLevel.getColumns() - 1)
                    + "."
            );
            return false;
        }


        if (iZombieManager.isBrainEaten(row)) {
            Console.showMessage(
                "The brain in row "
                    + row
                    + " has already been eaten."
            );
            return false;
        }

        int cost =
            iZombieManager.getZombieCost(zombieType);

        if (ctx.getSunAmount() < cost) {
            Console.showMessage(
                "Not enough sun. Required: " + cost + "."
            );
            return false;
        }

        try {
            Zombie zombie =
                ctx.getZombieFactory().create(zombieType);

            zombie.setRow(row);
            zombie.setX(column);

            zombie.setSpawnTick(
                ctx.getTimeManager().getTotalTicks()
            );


            ctx.getAliveZombies().add(zombie);

            ctx.setSunAmount(
                ctx.getSunAmount() - cost
            );

            Console.showMessage(
                "Zombie "
                    + zombieType
                    + " placed at ("
                    + column
                    + ", "
                    + row
                    + ") for "
                    + cost
                    + " sun."
            );

            return true;
        } catch (IllegalArgumentException exception) {
            Console.showMessage(exception.getMessage());
            return false;
        }
    }

    public void showAvailableZombies() {
        if (iZombieManager == null) {
            return;
        }

        StringBuilder output =
            new StringBuilder("Available zombies: ");

        boolean first = true;

        for (Map.Entry<String, Integer> entry
            : iZombieManager
            .getAvailableZombieCosts()
            .entrySet()) {

            if (!first) {
                output.append(", ");
            }

            output
                .append(entry.getKey())
                .append("=")
                .append(entry.getValue());

            first = false;
        }

        Console.showMessage(output.toString());
    }


    private void initRandomPlants() {
        PlantFactory plantFactory =
            new PlantFactory(DataManager.getInstance());

        int rows = currentLevel.getRows();

        for (int row = 0; row < rows; row++) {
            List<Integer> columns = new ArrayList<>();


            for (
                int column = 0;
                column < IZombieManager.RED_LINE_COLUMN;
                column++
            ) {
                columns.add(column);
            }

            Collections.shuffle(columns, random);

            int plantCount = 4 + random.nextInt(3);


            String guaranteedAttacker =
                ATTACKING_PLANT_POOL[
                    random.nextInt(
                        ATTACKING_PLANT_POOL.length
                    )
                    ];

            placeInitialPlant(
                plantFactory,
                guaranteedAttacker,
                row,
                columns.get(0)
            );


            for (
                int index = 1;
                index < plantCount;
                index++
            ) {
                String plantName =
                    RANDOM_PLANT_POOL[
                        random.nextInt(
                            RANDOM_PLANT_POOL.length
                        )
                        ];

                placeInitialPlant(
                    plantFactory,
                    plantName,
                    row,
                    columns.get(index)
                );
            }
        }
    }

    private void placeInitialPlant(
        PlantFactory plantFactory,
        String plantName,
        int row,
        int column
    ) {
        Plant plant;

        try {
            plant = plantFactory.create(plantName);
        } catch (IllegalArgumentException exception) {
            Console.showMessage(exception.getMessage());
            return;
        }

        Tile tile = gameEngine.getTiles(column, row);


        if (tile == null || !tile.setPlant(plant)) {
            return;
        }

        plant.setRow(row);
        plant.setCol(column);


        ctx.getAlivePlants().add(plant);
    }

    private void initSunProducerZombies() {
        for (
            int row = 0;
            row < currentLevel.getRows();
            row++
        ) {

            Zombie producer =
                ctx.getZombieFactory().create("bucket head");

            producer.setName("Sun Producer Zombie");
            producer.setSpeed(0);

            producer.setRow(row);
            producer.setX(SUN_PRODUCER_COLUMN);

            producer.setSpawnTick(
                ctx.getTimeManager().getTotalTicks()
            );

            ctx.getAliveZombies().add(producer);

            iZombieManager.registerSunProducer(
                producer,
                ctx
            );
        }
    }

    public GameContext getCtx() {
        return ctx;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }


    public void advancedTimeCommand(double seconds) {
        if (gameEngine == null || ctx == null) {
            Console.showMessage("Game engine is null");
            return;
        }

        int ticks = Math.max(
            0,
            (int) Math.round(seconds * 10)
        );

        for (
            int i = 0;
            i < ticks && !ctx.isGameEnded();
            i++
        ) {
            ctx.getTimeManager().advanceTime(1);
            gameEngine.update(0.1);
        }
    }
}
