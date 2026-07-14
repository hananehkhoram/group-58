package model.menus.allmenus;

import controller.repository.DataManager;
import controller.repository.PlantRepository;
import controller.repository.factory.PlantFactory;
import model.GameContext;
import model.menus.BaseMenu;
import model.menus.MenuType;
import model.plants.Plant;
import model.season.Grave;
import model.user.User;
import model.user.UserManager;

import java.util.List;

public class PlantSelectionMenu extends BaseMenu {
    private UserManager um;
    private DataManager dm;
    private PlantFactory plantFactory;
    protected User currentUser;
    private PlantRepository plantRepository;

    public PlantSelectionMenu(GameContext ctx) {
        super(ctx, MenuType.SELECT_PLANTS);
        this.um = UserManager.getInstance();
        this.currentUser = um.getCurrentUser();
        this.dm = DataManager.getInstance();
        this.plantFactory = new PlantFactory(dm);
        this.plantRepository = dm.plants;
        this.name = "Plant Selection menu";
    }

    public String showAllPlants() {
        List<Plant> plants = (List<Plant>) plantRepository.getPlantDataMap().values();
        StringBuilder sb = new StringBuilder();
        sb.append("All plants ->\n");
        for (Plant plant : plants){
            sb.append(plant.getName()).append(" - ");
        }
        sb.append("\n-----\n");
        return sb.toString();
    }
    public String showAvailablePlants() {
        List<Plant> plants = currentUser.getUnlockedPlantTypes();
        StringBuilder sb = new StringBuilder();
        sb.append("Available plants ->\n");
        for (Plant plant : plants){
            sb.append(plant.getName()).append(" - ");
        }
        sb.append("\n-----\n");
        return sb.toString();
    }
    public String addPlant(String plantType) {
        List<Plant> allPlants = (List<Plant>) plantRepository.getPlantDataMap().values();
        List<Plant> plants = currentUser.getUnlockedPlantTypes();
        List<Plant> plantsInCtx = ctx.getActivePlants();
        Plant inAllPlant = null;
        Plant inUserPlant = null;
        boolean plantInCtx = false;
        for (Plant p : allPlants){
            if (p.getName().equalsIgnoreCase(plantType)) inAllPlant = p;
        }
        for (Plant p : plants){
            if (p.getName().equalsIgnoreCase(plantType)) inUserPlant = p;
        }
        for (Plant p : plantsInCtx){
            if (p.getName().equalsIgnoreCase(plantType)) plantInCtx = true;
        }
        if (inAllPlant == null) return "Invalid plant type.";
        if (inUserPlant == null) return "Plant is not unlocked.";
        if (plantInCtx) return "Plant is already chosen.";

        Plant newPlant = plantFactory.create(String.valueOf(inUserPlant.getId()));
        ctx.getActivePlants().add(newPlant);
        return "Successfully added "+newPlant.getName()+"to your plants.";
    }
    public String removePlant(String plantType) {
        List<Plant> allPlants = (List<Plant>) plantRepository.getPlantDataMap().values();
        List<Plant> plants = currentUser.getUnlockedPlantTypes();
        List<Plant> plantsInCtx = ctx.getActivePlants();
        Plant inAllPlant = null;
        Plant inUserPlant = null;
        Plant plantInCtx = null;
        for (Plant p : allPlants){
            if (p.getName().equalsIgnoreCase(plantType)) inAllPlant = p;
        }
        for (Plant p : plants){
            if (p.getName().equalsIgnoreCase(plantType)) inUserPlant = p;
        }
        for (Plant p : plantsInCtx){
            if (p.getName().equalsIgnoreCase(plantType)) plantInCtx = p;
        }
        if (inAllPlant == null) return "Invalid plant type.";
        if (inUserPlant == null) return "Plant is not unlocked.";
        if (plantInCtx ==null) return "Plant is not chosen.";

        ctx.getActivePlants().remove(plantInCtx);
        return "Successfully removed "+plantInCtx.getName()+"from your plants.";
    }
    public String boostPlant(String plantType) {
        List<Plant> allPlants = (List<Plant>) plantRepository.getPlantDataMap().values();
        List<Plant> plants = currentUser.getUnlockedPlantTypes();
        List<Plant> plantsInCtx = ctx.getActivePlants();
        Plant inAllPlant = null;
        Plant inUserPlant = null;
        Plant plantInCtx = null;
        for (Plant p : allPlants){
            if (p.getName().equalsIgnoreCase(plantType)) inAllPlant = p;
        }
        for (Plant p : plants){
            if (p.getName().equalsIgnoreCase(plantType)) inUserPlant = p;
        }
        for (Plant p : plantsInCtx){
            if (p.getName().equalsIgnoreCase(plantType)) plantInCtx = p;
        }
        if (inAllPlant == null) return "Invalid plant type.";
        if (inUserPlant == null) return "Plant is not unlocked.";
        if (plantInCtx ==null) return "Plant is not chosen.";

        if (currentUser.getGems() < 2) return "You don't have enough gems.";
        currentUser.setGems(currentUser.getGems() - 2);
        plantInCtx.setPlantFood(true);

        return "Successfully boosted "+plantInCtx.getName()+"from your plants.";

    }
    public String startGame() {
        if (ctx.getActivePlants().isEmpty()) {
            return "You must choose at least one plant before starting.";
        }

        ctx.getSeason().onLevelStart(ctx);

        for (Grave g : ctx.getSeason().getInitialGraves(ctx.getLevel())) {
            ctx.placeGrave(g, g.getRow(), g.getCol());
        }

        return "Let's begin this level: " + ctx.getLevel().getName();
    }
}
