package controller;

import controller.repository.DataManager;
import model.GameContext;
import model.Quest;
import model.plants.Plant;
import model.plants.PlantFamily;
import model.plants.Tag;
import model.season.Season;
import model.user.User;
import view.ConsoleView;

public class QuestManager {

    public static void evaluateLevelEndQuests(GameContext ctx, User user) {

        if (ctx.getTotalLostPlants() <= 5) {
            complete(user, "economic-herbivore-" + ctx.getTotalLostPlants());
        }

        if (ctx.getSunAmount() == 0) {
            complete(user, "defense-master");
        }

        if (isGardenSymmetric(ctx, false)) {
            complete(user, "symmetry");
        }
        if (!isGardenSymmetric(ctx, true)) {
            complete(user, "ocd");
        }

        if (ctx.getExplosivePlantsPlacedThisLevel() >= 3) {
            complete(user, "pro-destroyer");
        }

        if (ctx.getSunProducerPlantsPlacedThisLevel() == 3 && ctx.getTotalPlantsPlacedThisLevel() == 3) {
            complete(user, "cloudy-day");
        }

        for (int col = 0; col < ctx.getLevel().getColumns(); col++) {
            String questId = "one-less-column-" + col;
            if (!ctx.getPlantedColumns().contains(col) && !user.isQuestCompleted(questId)) {
                complete(user, questId);
                break;
            }
        }

        for (int row = 0; row < ctx.getLevel().getRows(); row++) {
            String questId = "undefended-row-" + row;
            if (!ctx.getPlantedRows().contains(row) && !user.isQuestCompleted(questId)) {
                complete(user, questId);
                break;
            }
        }

        boolean hasEmptyRow = ctx.getPlantedRows().size() < ctx.getLevel().getRows();
        boolean hasEmptyColumn = ctx.getPlantedColumns().size() < ctx.getLevel().getColumns();
        if (hasEmptyRow && hasEmptyColumn) {
            complete(user, "undefended-cross");
        }

        if (ctx.getTotalKillsThisLevel() >= 10 && ctx.getPlantNamesThatKilledThisLevel().size() == 1) {
            String onlyKiller = ctx.getPlantNamesThatKilledThisLevel().iterator().next();
            if (onlyKiller.equalsIgnoreCase("Cactus")) {
                complete(user, "only-cactus");
            }
            complete(user, "plant-pro-killer");
        }

        if (ctx.getTotalKillsThisLevel() > 0 && ctx.getPlantFamiliesUsedToKillThisLevel().size() == 1) {
            PlantFamily onlyFamily = ctx.getPlantFamiliesUsedToKillThisLevel().iterator().next();
            complete(user, "family-kill-" + onlyFamily);
        }

        for (PlantFamily family : PlantFamily.values()) {
            String questId = "family-avoid-" + family;
            if (!ctx.getPlantFamiliesPlantedThisLevel().contains(family) && !user.isQuestCompleted(questId)) {
                complete(user, questId);
                break;
            }
        }
        for (int n : new int[]{10, 20, 30, 40, 50}) {
            if (ctx.getLawnMowerKillsThisLevel() >= n) {
                complete(user, "lawnmower-time-" + n);
            }
        }

        if (ctx.getFirstWaveStartTick() != -1) {
            long deadline = ctx.getFirstWaveStartTick() + 300;
            long earlyKills = ctx.getEarlyKillTicks().stream().filter(t -> t <= deadline).count();
            if (earlyKills >= 10) {
                complete(user, "quick-action");
            }
        }

        boolean isNightLevel = ctx.getLevelManager() != null && ctx.getLevelManager().disableSkySun();
        if (isNightLevel && onlyShroomPlantsUsed(ctx)) {
            complete(user, "night-or-morning");
        }

        if (ctx.getAlmostLostKillsThisLevel() >= 10) {
            complete(user, "almost-lost");
        }

        if (user.getWinStreakAtMaxDifficulty() >= 5) {
            complete(user, "win-streak-5");
        }

    }
    private static boolean onlyShroomPlantsUsed(GameContext ctx) {
        for (Plant p : ctx.getAlivePlants()) {
            if (p.getTags() == null || !p.getTags().contains(Tag.SHROOM)) {
                return false;
            }
        }
        return !ctx.getAlivePlants().isEmpty();
    }

    public static void progress(User user, String questId, int amount) {
        Quest quest = DataManager.getInstance().quests.get(questId);
        if (quest == null || user.isQuestCompleted(questId)) return;

        int current = user.getQuestProgress(questId) + amount;
        user.setQuestProgress(questId, current);

        if (current >= quest.getTargetProgress()) {
            complete(user, questId);
        }
    }

    private static void complete(User user, String questId) {
        Quest q = DataManager.getInstance().quests.get(questId);
        if (q == null || user.isQuestCompleted(questId)) return;

        grantReward(user, q);
        user.completeQuest(questId, q.getCategory() == Quest.QuestCategory.DAILY);
        DataManager.getInstance().saveUser();
    }

    private static void grantReward(User user, Quest quest) {
        switch (quest.getRewardType()) {
            case COIN -> user.setCoins(user.getCoins() + quest.getRewardAmount());
            case GEM -> user.setGems(user.getGems() + quest.getRewardAmount());
            case SEED_PACKET -> {
                Plant target = user.getRandomUnlockedPlant();
                if (target != null) {
                    user.addSeedsToInventory(target.getName(), quest.getRewardAmount());
                }
            }
            case RANDOM_PLANT -> {
                Plant newPlant = getRandomLockedPlant(user);
                if (newPlant != null) {
                    user.getUnlockedPlantTypes().add(newPlant);
                }
            }
        }
        ConsoleView.showMessage("Quest completed: " + quest.getName());
    }

    private static Plant getRandomLockedPlant(User user) {
        var allPlants = DataManager.getInstance().plants.getPlantDataMap().values();
        var locked = allPlants.stream()
                .filter(p -> user.getUnlockedPlantTypes().stream()
                        .noneMatch(up -> up.getName().equalsIgnoreCase(p.getName())))
                .toList();
        if (locked.isEmpty()) return null;
        return locked.get((int) (Math.random() * locked.size()));
    }

    private static boolean isGardenSymmetric(GameContext ctx, boolean exceptMiddleRow) {
        Plant[][] grid = ctx.getPlantGrid();
        int rows = grid.length;
        int cols = grid[0].length;
        int middleRow = rows / 2;

        for (int r = 0; r < rows; r++) {
            if (exceptMiddleRow && r == middleRow) continue;
            for (int c = 0; c < cols / 2; c++) {
                Plant left = grid[r][c];
                Plant right = grid[r][cols - 1 - c];
                boolean leftEmpty = (left == null);
                boolean rightEmpty = (right == null);
                if (leftEmpty != rightEmpty) return false;
                if (!leftEmpty && !left.getName().equalsIgnoreCase(right.getName())) return false;
            }
        }
        return true;
    }
}