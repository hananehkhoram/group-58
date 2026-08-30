package com.workshop.model.menus.allmenus;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.model.GameContext;
import com.workshop.model.GreenHouseData.GreenHouse;
import com.workshop.model.GreenHouseData.Pot;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.plants.Plant;
import com.workshop.model.plants.plantFoodEffect.PlantFoodMode;
import com.workshop.model.shopData.ItemType;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;
import com.workshop.view.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GreenHouseMenu extends BaseMenu {
    private UserManager um;
    protected User currentUser;
    private GreenHouse greenHouse;
    private PlantFactory plantFactory;
    private DataManager dm;

    private Random random = new Random();

    public GreenHouseMenu(GameContext ctx) {
        super(ctx, MenuType.GREENHOUSE);
        this.um = UserManager.getInstance();
        this.dm = DataManager.getInstance();
        this.currentUser = um.getCurrentUser();
        this.greenHouse = currentUser.getGreenHouse();
        this.plantFactory = new PlantFactory(dm);
        this.name = "Greenhouse menu";
    }

    public String buyPot(int x, int y) {
        Pot pot = greenHouse.getPot(x, y);
        if (pot == null) return "Invalid pot index.";
        if (!pot.isLocked()) return "Pot is already unlocked.";

        ItemType potItem = ItemType.POT_UNLOCK;
        if (currentUser.getGems() < potItem.getPrice()) {
            return "Not enough gems!";
        }

        currentUser.setGems(currentUser.getGems() - potItem.getPrice());
        currentUser.setOwnedPotsCount(currentUser.getOwnedPotsCount() + 1);
        greenHouse.unlockPot(x, y);
        DataManager.getInstance().saveUser();
        return "Pot unlocked!";
    }

    public String showGreenHouse(){
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Greenhouse ===\n");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                Pot pot = greenHouse.getPot(i, j);
                sb.append("---------\n").append(" (").append(i).append(", ").append(j).append(") ").
                    append(pot.isLocked() ? "Locked " : "Open ");
                if (pot.isEmpty()) {
                    sb.append("Pot is empty.\n");
                } else {
                    String plantName = (pot.isMarigold() || pot.getPlantType() == null)
                        ? "Marigold" : pot.getPlantType().getName();
                    sb.append(plantName).append(" Remaining time: ");
                    sb.append(pot.getRemainingPlantedTime()).append("\n");
                    if (pot.isPlantReady()) sb.append("Plant is ready!\n");
                }
            }
        }
        return sb.toString();
    }

    public String plantPot(int x,int y){
        Pot pot = greenHouse.getPot(x,y);
        if (pot == null) return "Invalid pot index.";
        if (pot.isLocked()) return "Pot is locked!";
        if (!pot.isEmpty()) return "Pot is not empty";

        Plant plant = determineRandomPlantToPlant();

        if (plant == null){
            pot.setMarigold(true);
            pot.setPlantedHours(2);
            pot.plant(null);
        }
        else {
            pot.setPlantedHours(8);
            pot.setMarigold(false);
            pot.plant(plant);
        }
        DataManager.getInstance().saveUser();
        return "Pot successfully planted.";
    }

    public String collectPlant(int x,int y){
        Pot pot = greenHouse.getPot(x,y);
        String result = null;
        if (pot == null) return "Invalid pot index.";
        if (pot.isLocked()) return "Pot is locked!";
        if (pot.isEmpty()) return "Pot is empty";
        if (!pot.isPlantReady()) return "Plant is not ready.";

        if (pot.isMarigold() || pot.getPlantType() == null){
            currentUser.setCoins(currentUser.getCoins() + 500);
            Console.showMessage("Successfully collected marigold");
            pot.collectPlant();
            result = "Successfully collected 500 coins from Marigold!";
        }
        else {
            String plantName = pot.getPlantType().getName();

            if (currentUser.hasStoredBoost(plantName)) {
                result = ("Harvested " + plantName + ". You already have a stored boost for this plant, so no extra boost was added.");
            } else {
                currentUser.addStoredBoost(plantName);
                result = ("Harvested " + plantName + "! A stored boost has been activated for your next match.");
            }
            pot.collectPlant();
        }

        DataManager.getInstance().saveUser();
        return result;
    }

    public String growPlant(int x,int y){
        Pot pot = greenHouse.getPot(x,y);
        if (pot == null) return "Invalid pot index.";
        if (pot.isLocked()) return "Pot is locked!";
        if (pot.isEmpty()) return "Pot is empty";
        if (pot.isPlantReady()) return "Plant is ready.";

        double remainingHours = pot.getRemainingPlantedTime();
        int gemsNeeded = (int) Math.ceil(remainingHours);

        if (currentUser.getGems() < gemsNeeded) {
            return "Not enough gems!";
        }

        currentUser.setGems(currentUser.getGems() - gemsNeeded);
        pot.setPlantReady(true);
        DataManager.getInstance().saveUser();

        String name = (pot.isMarigold() || pot.getPlantType() == null) ? "Marigold" : pot.getPlantType().getName();

        return "Successfully accelerated growth! Gained a fully grown " + name + " for " + gemsNeeded + " gems.";
    }

    public Plant determineRandomPlantToPlant() {
        if (random.nextBoolean()) {
            return null;
        }

        List<Plant> unlockedPlants = currentUser.getUnlockedPlantTypes();

        List<Plant> validPlants = new ArrayList<>();
        for (Plant p : unlockedPlants) {
            if (!p.getPlantFoodMode().equals(PlantFoodMode.NONE)) {
                validPlants.add(p);
            }
        }

        if (validPlants.isEmpty()) {
            return null;
        }

        int randomIndex = random.nextInt(validPlants.size());
        String name = String.valueOf(validPlants.get(randomIndex).getName());
        return plantFactory.create(name);
    }
}
