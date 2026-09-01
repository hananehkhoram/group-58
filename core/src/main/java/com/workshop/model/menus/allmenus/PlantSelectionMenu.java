package com.workshop.model.menus.allmenus;

import com.workshop.controller.repository.DataManager;
import com.workshop.controller.repository.PlantRepository;
import com.workshop.controller.repository.factory.PlantFactory;
import com.workshop.controller.SpecialLevelManager.PlantWhatYouGetManager;
import com.workshop.model.GameContext;
import com.workshop.model.level.LevelType;
import com.workshop.model.menus.BaseMenu;
import com.workshop.model.menus.MenuType;
import com.workshop.model.plants.Plant;
import com.workshop.model.season.Grave;
import com.workshop.model.user.User;
import com.workshop.model.user.UserManager;

import java.util.ArrayList;
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
        List<Plant> plants = new ArrayList<>(plantRepository.getPlantDataMap().values());
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
        List<Plant> plants = new ArrayList<>(currentUser.getUnlockedPlantTypes());
        Plant inUserPlant = null;
        for (Plant p : plants) if (p.getName().equalsIgnoreCase(plantType)) inUserPlant = p;

        if (inUserPlant == null) return "Plant is not unlocked.";
        if (isSunflowerBanned(inUserPlant.getName())) {
            return "You cannot select Sun Producer in this level.";
        }
        for (Plant p : ctx.getActivePlants()) if (p.getName().equalsIgnoreCase(plantType)) return "Plant is already chosen.";

        Plant newPlant = plantFactory.create(String.valueOf(inUserPlant.getName()));
        newPlant.setPlantFoodActive(inUserPlant.isPlantFoodActive());
        ctx.getActivePlants().add(newPlant);
        return "Successfully added " + newPlant.getName() + " to your plants.";
    }    public String removePlant(String plantType) {
        List<Plant> allPlants = new ArrayList<>(plantRepository.getPlantDataMap().values());
        List<Plant> plants = new ArrayList<>(currentUser.getUnlockedPlantTypes());
        List<Plant> plantsInCtx = new ArrayList<>(ctx.getActivePlants());
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
        List<Plant> plants = new ArrayList<>(currentUser.getUnlockedPlantTypes());
        Plant userPlant = null;
        for (Plant p : plants) if (p.getName().equalsIgnoreCase(plantType)) userPlant = p;
        if (userPlant == null) return "Plant is not unlocked.";
        if (currentUser.getGems() < 2) return "You don't have enough gems.";

        currentUser.setGems(currentUser.getGems() - 2);
        userPlant.setPlantFoodActive(true);
        for (Plant p : ctx.getActivePlants()) if (p.getName().equalsIgnoreCase(plantType)) p.setPlantFoodActive(true);
        return "Successfully boosted " + userPlant.getName() + "!";
    }

    private boolean isSunflowerBanned(String plantName) {
        return ctx.getLevel() != null
            && ctx.getLevel().getLevelType() == LevelType.PLANT_WHAT_YOU_GET
            && PlantWhatYouGetManager.isSunflower(plantName);
    }

    public String startGame() {
        if (ctx.getActivePlants().isEmpty()) {
            return "You must choose at least one plant before starting.";
        }

        ctx.getSeason().onLevelStart(ctx);

        for (Grave g : ctx.getSeason().getInitialGraves(ctx.getLevel())) {
            ctx.placeGrave(g, g.getRow(), g.getCol());
        }

        ctx.setBattleStarted(true);
        return "Let's begin this level: " + ctx.getLevel().getName();
    }
}
