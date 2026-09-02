package com.workshop.model;

import com.workshop.controller.NewsManager;
import com.workshop.controller.QuestManager;
import com.workshop.controller.ScoringManager;
import com.workshop.controller.repository.DataManager;
import com.workshop.model.level.Level;
import com.workshop.model.level.LevelType;
import com.workshop.model.season.Season;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

import java.util.List;

/**
 * Win/loss progression side-effects previously inlined in {@link GameContext}.
 */
public final class LevelEndProgression {

    private LevelEndProgression() {}

    public static void onPlayerWin(GameContext ctx) {
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getDifficultyLevel() == 5) {
                currentUser.setWinStreakAtMaxDifficulty(currentUser.getWinStreakAtMaxDifficulty() + 1);
            } else {
                currentUser.setWinStreakAtMaxDifficulty(0);
            }
            currentUser.setNumberOfPassedLevels(currentUser.getNumberOfPassedLevels() + 1);

            Level level = ctx.getLevel();
            Season season = ctx.getSeason();

            if (isMiniGameLevel(level)) {
                List<Level> miniLevels = season.getLevels();
                int index = miniLevels.indexOf(level);
                if (index + 1 < miniLevels.size()) {
                    currentUser.unlockLevel(
                        miniLevels
                            .get(index + 1)
                            .getName()
                    );
                }
            }

            if (isMiniGameLevel(level)) {
                currentUser.incrementMinigamesCompleted();
            }

            List<Level> levelsInSeason = season.getLevels();
            if (levelsInSeason == null) {
                levelsInSeason = new java.util.ArrayList<>();
            }
            int levelIndex = levelsInSeason.indexOf(level);

            int chapterNumber = DataManager.getInstance().seasons.getChapterNumber(season);

            if (chapterNumber > 0) {
                currentUser.setLastLevel(levelIndex + 1);
                currentUser.setLastSeason(DataManager.getInstance().seasons.getChapterNumber(season));
            }

            if (levelIndex + 1 < levelsInSeason.size()) {
                currentUser.unlockLevel(levelsInSeason.get(levelIndex + 1).getName());
                NewsManager.addNews("New Level In Season", "You unlocked new level: "
                    + levelsInSeason.get(levelIndex + 1).getName()
                    + " in seasson: " + currentUser.getLastSeason());
            } else {
                Season nextSeason = DataManager.getInstance().seasons.getNextSeason(season);
                if (nextSeason != null && !nextSeason.getLevels().isEmpty()) {
                    currentUser.unlockLevel(nextSeason.getLevels().get(0).getName());
                    NewsManager.addNews("New Season", "You unlocked season: " + nextSeason.getName());
                }
                String minigameName = DataManager.getInstance().getRelatedMinigame(season.getName());
                if (minigameName != null) {
                    Season minigame = DataManager.getInstance().seasons.get(minigameName);
                    if (minigame != null && !minigame.getLevels().isEmpty()) {
                        currentUser.unlockLevel(minigame.getLevels().get(0).getName());
                        NewsManager.addNews("New Minigame", "You unlocked new minigame: " + minigameName);
                    }
                }
                if ("Beghouled".equalsIgnoreCase(season.getName())) {
                    Season zombotany =
                        DataManager.getInstance().seasons.get("Zombotany");

                    if (zombotany != null && !zombotany.getLevels().isEmpty()) {
                        currentUser.unlockLevel(
                            zombotany.getLevels().get(0).getName()
                        );

                        NewsManager.addNews(
                            "New Minigame",
                            "You unlocked new minigame: Zombotany"
                        );
                    }
                }
            }
            QuestManager.evaluateLevelEndQuests(ctx, currentUser);
            if (level.getLevelType() == LevelType.BONUS) {
                ScoringManager.evaluateLevelEndScoring(ctx, currentUser);
            }
        }
        DataManager.getInstance().saveUser();
        Console.showMessage(
            "Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz."
        );
    }

    public static void onPlayerLoss() {
        User currentUser = UserManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setWinStreakAtMaxDifficulty(0);
        }
        DataManager.getInstance().saveUser();
        Console.showMessage("The zombie ate your brain; LOSER!!!");
    }

    private static boolean isMiniGameLevel(Level level) {
        LevelType type = level.getLevelType();
        return type == LevelType.Wallnuts_MG
            || type == LevelType.Vase_MG
            || type == LevelType.Izambie_MG
            || type == LevelType.Beghouled_MG
            || type == LevelType.Zombotany_MG;
    }
}
