package model.menus.allmenus;

import model.GameContext;
import model.GreenHouseData.GreenHouse;
import model.GreenHouseData.Pot;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.plants.Plant;
import model.user.User;
import model.user.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GreenHouseMenu extends BaseMenu {
    private UserManager um;
    protected User currentUser;
    private GreenHouse greenHouse;

    private Random random = new Random();

    public GreenHouseMenu(GameContext ctx, MenuType menuType) {
        super(ctx, MenuType.GREENHOUSE);
        this.um = UserManager.getInstance();
        this.currentUser = um.getCurrentUser();
        this.greenHouse = currentUser.getGreenHouse();
    }
    public String showGreenHouse(){
        StringBuilder sb = new StringBuilder();
        sb.append("=== Welcome to the Greenhouse ===\n");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                Pot pot = greenHouse.getPot(i,j);
                sb.append("---------\n").append(pot.isLocked() ? "Locked ":"Open ");
                if (pot.isEmpty()) sb.append("Pot is empty.\n");
                else {
                    sb.append(pot.getPlantType().getName()).append("Remaining time: ");
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

        if (plant == null){//if its marigold
            pot.setRemainingPlantedTime(2);
        }
        else pot.setRemainingPlantedTime(8);
        pot.plant(plant);
        return "Pot successfully planted.";
    }
    public String collectPlant(int x,int y){
        Pot pot = greenHouse.getPot(x,y);
        if (pot == null) return "Invalid pot index.";
        if (pot.isLocked()) return "Pot is locked!";
        if (pot.isEmpty()) return "Pot is empty";
        if (!pot.isPlantReady()) return "Plant is not ready.";

        if (pot.getPlantType() == null){//if its marigold
            currentUser.setCoins(currentUser.getCoins() + 500);
            pot.collectPlant();
        }
        else currentUser.setCoins(currentUser.getCoins() + 500);
    }

    public String growPlant(int x,int y){
        Pot pot = greenHouse.getPot(x,y);
        if (pot == null) return "Invalid pot index.";
        if (pot.isLocked()) return "Pot is locked!";
        if (pot.isEmpty()) return "Pot is empty";
        if (pot.isPlantReady()) return "Plant is ready.";

        //int gemsNeeded = Math.ceil(pot.getRemainingPlantedTime());

        return null;
    }
    public Plant determineRandomPlantToPlant() {
        if (random.nextBoolean()) {
            return null; // return a marigold
        }

        List<Plant> unlockedPlants = currentUser.getUnlockedPlantTypes();

        List<Plant> validPlants = new ArrayList<>();
        for (Plant p : unlockedPlants) {
            if (p.isPlantFood()) {
                validPlants.add(p);
            }
        }

        if (validPlants.isEmpty()) {
            return null; // return a marigold
        }

        int randomIndex = random.nextInt(validPlants.size());
        return validPlants.get(randomIndex);
    }
}
