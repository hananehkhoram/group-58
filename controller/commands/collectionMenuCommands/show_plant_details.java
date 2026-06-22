package controller.commands.collectionMenuCommands;

import controller.MenuManager;
import controller.commandHandler.Command;
import controller.repository.PlantRepository;
import model.menus.Menu;
import model.plants.Plant;

public class show_plant_details implements Command {

    private PlantRepository plantRepository;

    public show_plant_details(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }

    @Override
    public void execute(String[] args) {
        String plantName = args[0];

        Plant plant = plantRepository.get();

        if (currentMenu instanceof Plant){
            ((Plant) currentMenu).show_plant_details(plantName);
        }

    }

    //menu collection show-plant -p <plant_name>
}
