package controller.repository;

import model.user.User;
import model.user.UserManager;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    public PlantRepository plants = new PlantRepository(); //
    public ZombieRepository zombies = new ZombieRepository();
    public UserRepository game = new UserRepository();
    public SeasonRepository seasons = new SeasonRepository();
    public UserRepository users = new UserRepository();

    private String userPath = "Files/users.dat";

    private DataManager(){
        instance = this;
        initialize();
    }
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    public void loadUser(){

        users.load(userPath);
        List<User> loadedUsers = new ArrayList<>(users.getUserMap().values());
        UserManager.getInstance().updateUsers(loadedUsers);
    }
    public void saveUser(){
        users.getUserMap().clear();
        for (User u : UserManager.getInstance().users) {
            users.getUserMap().put(u.getUsername(), u);
        }

        users.save();    }

    public void initialize(){
        plants.load("Files/plants.csv");
        zombies.load("Files/zombies.csv");
        users.load("Files/users.dat");
        seasons.load(null);
    }
}
