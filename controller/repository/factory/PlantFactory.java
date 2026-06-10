package controller.repository.factory;

import controller.repository.DataManager;
import model.plants.plantsKinds.Plant;

public class PlantFactory extends BaseFactory<Plant> {

    public PlantFactory(DataManager dm) {
        super(dm);
    }


    @Override
    public Plant create(String id) {
        //creates new plant based on csv file
        return null;
    }
}
