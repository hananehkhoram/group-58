package model.SaveOueSeeds;

import controller.repository.PlantRepository;

public class SaveOurSeeds extends PlantRepository {

    boolean plantAlive = true;

    private int[][] protectedPlace;

    private void gameOn(){
        while (plantAlive){}
    }

}
