package model.menus.allmenus;

import controller.repository.DataManager;
import controller.repository.factory.PlantFactory;
import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.plants.Plant;
import model.user.User;
import model.user.UserManager;
import model.zombie.Zombie;

import java.util.List;

public class CollectionMenu extends BaseMenu {
    private UserManager um;
    private DataManager dm;//temporary!!!
    private PlantFactory plantFactory;
    protected User currentUser;
    private List<Plant> plants;
    private List<Plant> unlockedPlants;
    private List<Zombie> zombies;
    public CollectionMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.COLLECTION);
        this.um = UserManager.getInstance();
        currentUser = um.getCurrentUser();
        this.dm = new DataManager();//temporary!!!
        this.plants = ctx.getActivePlants();
        this.zombies = ctx.getActiveZombies();
        this.unlockedPlants = currentUser.getUnlockedPlantTypes();
        this.plantFactory = new PlantFactory(dm);
    }

    public String showAllPlants() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Collection Menu ===\n");
        sb.append("All Plants ->\n");

        for (Plant p : plants){
            sb.append(p.getName()).append("\n");
        }
        return sb.toString();
    }
    public String showAllZombies() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Collection Menu ===\n");
        sb.append("All Zombies ->\n");

        for (Zombie z : zombies){
            sb.append(z.getName()).append("\n");
        }
        return sb.toString();
    }
    public String showPlantDetails(String plantName) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Collection Menu ===\n");
        Plant plant = null;
        for (Plant p : plants){
            if (p.getName().equalsIgnoreCase(plantName)) plant = p;
        }
        if (plant == null) return "Invalid plant name.";
        sb.append(plantName).append("'s details ->\n");
        sb.append("Id: ").append(plant.getId());
        sb.append(" Base ability: ").append(plant.getBaseAbility().toString());
        sb.append(" Base hp: ").append(plant.getBaseHp());
        sb.append(" Family: ").append(plant.getFamily().name()).append("\n----------\n");

        return sb.toString();
    }
    public String showZombieDetails(String zombieName) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Collection Menu ===\n");
        Zombie zombie = null;
        for (Zombie z : zombies){
            if (z.getName().equalsIgnoreCase(zombieName)) zombie = z;
        }
        if (zombie == null) return "Invalid zombie name.";
        sb.append(zombieName).append("'s details ->\n");
        sb.append("Id: ").append(zombie.getId());
        sb.append(" Speed: ").append(zombie.getSpeed());
        sb.append(" Info: ").append(zombie.zombieInfo()).append("\n----------\n");

        return sb.toString();
    }
    public String upgradePlant(String plantName) {
        return null;// level up the plant here
    }
    public String purchasePlant(String plantName) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Collection Menu ===\n");
        Plant plant = null;
        for (Plant p : plants){
            if (p.getName().equalsIgnoreCase(plantName)) plant = p;
        }
        if (plant == null) return "Invalid plant name.";
        for (Plant p : unlockedPlants) {
            if (p.getName().equalsIgnoreCase(plantName)) {
                return "You have already unlocked " + p.getName() + "!";
            }
        }
        int cost = 2000;

        if (currentUser.getCoins() < cost) {
            return "Not enough coins! You need " + cost + " coins to buy a new plant, but you only have "
                    + currentUser.getCoins() + ".";
        }


        currentUser.setCoins(currentUser.getCoins() - cost);

        Plant newPlant = plantFactory.create(String.valueOf(plant.getId()));

        unlockedPlants.add(newPlant);


        um.saveToFile();

        return "Successfully purchased " + newPlant.getName() + "! It is now added to your collection.";
    }
}
