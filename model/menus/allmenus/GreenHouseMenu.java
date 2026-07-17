package model.menus.allmenus;

import controller.repository.DataManager;
import controller.repository.factory.PlantFactory;
import model.GameContext;
import model.GreenHouseData.GreenHouse;
import model.GreenHouseData.Pot;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.plants.Plant;
import model.user.User;
import model.user.UserManager;
import view.ConsoleView;

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
    public String showGreenHouse(){
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Greenhouse ===\n");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                Pot pot = greenHouse.getPot(i, j);
                sb.append("---------\n").append(" (").append(i).append(", ").append(j).append(") ").append(pot.isLocked() ? "Locked " : "Open ");
                if (pot.isEmpty()) {
                    sb.append("Pot is empty.\n");
                } else {
                    String plantName = pot.isMarigold() ? "Marigold" : pot.getPlantType().getName();
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
            pot.setRemainingPlantedTime(2);
            pot.plant(null);
        }
        else {
            pot.setRemainingPlantedTime(8);
            pot.setMarigold(false);
            pot.plant(plant);
        }
//        um.saveToFile();
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



        if (pot.isMarigold()){
            currentUser.setCoins(currentUser.getCoins() + 500);
            ConsoleView.showMessage("Successfully collected marigold");
            pot.collectPlant();
        }
        else {
            String plantName = pot.getPlantType().getName();

            if (currentUser.hasStoredBoost(plantName)) {
                result =  ("Harvested " + plantName + ". You already have a stored boost for this plant, so no extra boost was added.");
            } else {
                currentUser.addStoredBoost(plantName);
                result = ("Harvested " + plantName + "! A stored boost has been activated for your next match.");
            }
        }
//        um.saveToFile();
        DataManager.getInstance().saveUser();
        pot.setEmpty(true);
        pot.setPlantType(null);
        pot.setMarigold(false);


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

        pot.setPlantReady(true);
//        um.saveToFile();
        DataManager.getInstance().saveUser();

        return "Successfully accelerated growth! Gained a fully grown " + pot.getPlantType().getName() + " for " +
                gemsNeeded + " gems.";
    }
    public Plant determineRandomPlantToPlant() {
        if (random.nextBoolean()) {
            return null;
        }

        List<Plant> unlockedPlants = currentUser.getUnlockedPlantTypes();

        List<Plant> validPlants = new ArrayList<>();
        for (Plant p : unlockedPlants) {
            if (p.isPlantFood()) {
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
