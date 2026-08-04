package com.workshop.controller.repository;

import com.workshop.controller.SpecialLevelManager.*;
import com.workshop.model.level.Level;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    public PlantRepository plants = new PlantRepository();
    public ZombieRepository zombies = new ZombieRepository();
    public SeasonRepository seasons = new SeasonRepository();
    public QuestRepository quests = new QuestRepository();
    public UserRepository users = new UserRepository();

    private final String userPath = "Files/users.dat";

    public String getRelatedMinigame(String seasonName) {
        return switch (seasonName) {
            case "Ancient Egypt" -> "Vasebreaker";
            case "Frozen Caves" -> "Wallnut Bowling";
            case "Big Wave Beach" -> "I, Zombie";
            case "Dark Ages" -> "Beghouled";
            default -> null;
        };
    }

    public LevelManager createManagerForLevel(Level level) {
        switch (level.getLevelType()) {
            case CONVEYOR_BELT:
                return new ConveyorBeltManager();
            case SAVE_QUR_SEEDS:
                return new SaveOurSeedsManager();
            case TIMED_WAR:
                return new TimedWarManager();
            case NIGHT_OPS:
                return new NightOpsManager();
            case DEADLINE:
                return new DeadLineManager();
            case PLANT_WHAT_YOU_GET:
                return new PlantWhatYouGetManager();
            case LOCKED_PLANTS:
                return new LockedPlantsManager(level.getBannedPlants(), level.getForcedPlants());
            case Wallnuts_MG:
                return new ConveyorBeltManager();
            case NORMAL:
                return null;
            case BONUS:
                return null;
            default:
                return null;
        }
    }

    private DataManager() {
        instance = this;
        initialize();
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void loadUser() {
        users.load(userPath);
        List<User> loadedUsers = new ArrayList<>(users.getUserMap().values());
        UserManager.getInstance().updateUsers(loadedUsers);
    }

    public void saveUser() {
        users.getUserMap().clear();
        for (User u : UserManager.getInstance().users) {
            users.getUserMap().put(u.getUsername(), u);
        }
        users.save();
    }

    public void initialize() {
        plants.load("Files/plants.csv");
        zombies.load("Files/zombies.csv");
        seasons.load(null);
        quests.load(null);
    }
}
