package model;

import model.plants.plantsKinds.Plant;
import model.user.User;
import model.zombie.Zombie;

import java.util.HashMap;
import java.util.Map;

//saving everything needed during the game
public class GameContext {
    private Map<String, Zombie> activeZombies = new HashMap<>();
    private Map<String, Plant> activePlants = new HashMap<>();
    private User loggedInUser;
    private int coins;
    private int gems;
    private int currentDifficulty;

    public void addCoins(int amount) {
    }

    public void addZombie(String name, Zombie newZombie) {
    }

    public void addPlant(String name, Plant plant) {
    }

    public boolean canSpendCoins(int amount) {
        return false;
    }


    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }
}
